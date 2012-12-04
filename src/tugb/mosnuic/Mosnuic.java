package tugb.mosnuic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class Mosnuic {

	static HashMap <String,Integer> imageCount = new HashMap<String,Integer>();
	//outputArray is the list to store matching tiles.
	static String[] outputArray = null;
	static BufferedImage ti = null;

	public static void main(String[] args) throws IOException {

		ParseInputArguments parser = new ParseInputArguments();
		boolean check = parser.ParsePath(args);
		
		if(check){
			/* initializing variables and objects.*/
			long startTime = System.currentTimeMillis();
			ArrayList<ImageInfo> tiles = new ArrayList<ImageInfo>();
			String targetImage  = parser.getTargetImageFilePath();
			String targetImageFileFormat = parser.getTargetImageFileFormat();
			String tileLibrary = parser.getTileDirectory(); 

			String outputFileLocation = parser.getOutputImageFilePath();
			String outputFileFormat = parser.getOutputImageFileFormat();
			//Gets current user path to create a Temp Directory
			String userPath = System.getProperty("user.dir"); 

			int repetition = parser.getMaxNumberOfTiles();
			String tempFolderOfTargetSplitLocation = userPath+"/DemoDirectory/";
			File tempFolderOfTargetSplit = new File(tempFolderOfTargetSplitLocation);
			
			long sTile = System.currentTimeMillis();;
			File filepathOfTileLibrary = new File(tileLibrary);
			
			/* For each tile, create ImageInfo object to calculate RGB and get its location */
			for (final File fileEntry : filepathOfTileLibrary.listFiles()) {
				ImageInfo temp = new ImageInfo();
				temp.setFilePath(fileEntry.getPath());
				temp.calculateRGB();
				tiles.add(temp);
			}

			System.out.println("Tile Library processed.");
			long eTile = System.currentTimeMillis();
			System.out.println("Time for tile lib: " + (eTile - sTile));

			long timeForDD = System.currentTimeMillis();
			/* Create a temporary directory to store cells. */
			boolean isDirectoryCreated = tempFolderOfTargetSplit.mkdir();
			if (isDirectoryCreated) {
				/* Print statements used for debugging. */
				//System.out.println("Target image split and stored in a Temp folder.");
			} else {
				deleteDirectory(tempFolderOfTargetSplit);
				tempFolderOfTargetSplit.mkdir();
				/* Print statements used for debugging. */
				//System.out.println("folder deleted and created again.");
			}
			long timeForDD2 = System.currentTimeMillis();
			System.out.println("Time for Demo Dir: " + (timeForDD2-timeForDD));
			
			
			long sTI = System.currentTimeMillis();
			/* Calculate the target image dimension to determine if there are
			 * enough tiles to create the output image*/
			ti = ImageIO.read(new File(targetImage));
			
			int targetImageSize  = ti.getHeight() * ti.getWidth();
			int tileSize = (int) tiles.get(0).getImageSize();
			int totalCells = targetImageSize/tileSize;
			
			//Check if there are enough tiles to create the output image.
			if((tiles.size()* repetition) >= totalCells){
				
				// Process target image 
				ProcessImage st = new ProcessImage();
				st.split(targetImage,targetImageFileFormat,tiles.get(0).getImageHeight(), tiles.get(0).getImageWidth(),
						tempFolderOfTargetSplitLocation );
				
				/* Creates ImageInfo object for each cell to calculate RGB */ 
				/* (When target image is split in the tile dimensions, each piece is called cell.) */
				ArrayList<ImageInfo> cells = new ArrayList<ImageInfo>();
				
				for (final File fileEntry : tempFolderOfTargetSplit.listFiles()) {
					ImageInfo temp = new ImageInfo();
					temp.setFilePath(fileEntry.getPath());
					temp.calculateRGB();
					cells.add(temp);
				}
				System.out.println("Target image processed.");
				long eTI = System.currentTimeMillis();
				System.out.println("Time for TI:" + (eTI - sTI));
				
				long sOut = System.currentTimeMillis();
				outputArray = new String[cells.size()];
				//Start
				ArrayList<Integer> numList = new ArrayList<Integer>();
				for (int k=0 ; k<cells.size(); k++){
					numList.add(k);
				
				}
				long startToShuffle = System.currentTimeMillis();
				Collections.shuffle(numList);
				long endToShuffle = System.currentTimeMillis();
				long timeToShuffle = endToShuffle - startToShuffle;
				System.out.println("Time to shuffle "+timeToShuffle);
				for(int m=0; m<numList.size(); m++){
					//System.out.println(numList.get(m));
					calculateBestMatchElement(numList.get(m), cells, tiles, repetition);
				}
 				//createoutputArray(cells, tiles, repetition);			
				
				ProcessImage oi = new ProcessImage();
				oi.renderOutputImage(targetImage, tiles.get(0).getImageHeight(), tiles.get(0).getImageWidth(),
						outputArray, outputFileLocation, outputFileFormat);
				deleteDirectory(tempFolderOfTargetSplit);
				long eOut = System.currentTimeMillis();;
				System.out.println("Time for output: "+ (eOut - sOut));
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				System.out.println("Total Time: " +totalTime);
			}
			else {
				System.out.println("Not enough tiles in the library to create an output image.");
				System.exit(0);
			}
		}else {
			System.out.println("Invalid inputs provided.");
			System.exit(0);
		}
	}

	/*private static void createoutputArray(ArrayList<ImageInfo> cells,
			ArrayList<ImageInfo> tiles, int repetition) {
		int mid;
		int mrow, mcol, cols;
		int i, j, k, div;
		int targetImageHeight = ti.getHeight();
		int targetImageWidth = ti.getWidth();

		calculate total number of cells, and calculate dimensions of each cell
		cols = (int) (targetImageWidth/tiles.get(0).getImageWidth());
		mrow = (int) ((targetImageHeight/tiles.get(0).getImageHeight())/2);
		mcol = cols/2; 
		mid=(mrow*cols)+mcol;
		
		if(cols%5==0)
			div=(cols-1)/5;
		else
			div=cols/5;
		
		for(i=0;i<div;i++){
			for(j=(mid-1)-i, k=mid+i;;j-=div, k+=div){
				if(j>=0)
					calculateBestMatchElement(j, cols, cells, tiles, repetition);
				if(k<cells.size())
					calculateBestMatchElement(k, cols, cells, tiles, repetition);
				if(j<0 && k>=cells.size())
					break;
			}
		}
	}*/

	private static void calculateBestMatchElement(int pos, ArrayList<ImageInfo> cells,
			ArrayList<ImageInfo> tiles, int repetition) {
		
		Map <Double,String> rgbDiff = new HashMap<Double,String>();
		double diff=0.0;
		
		for(int j=0;j<tiles.size(); j++) {
			diff = compareImages(cells.get(pos),tiles.get(j));
			rgbDiff.put(diff, tiles.get(j).filePath);
		}
		
		double min = Collections.min(rgbDiff.keySet());
		
		//Add the tile with minimum distance to the outputArray in the very first iteration for a cell.
		if(pos==0){
			outputArray[pos]=rgbDiff.get(min);
			//System.out.println(pos+"\t"+outputArray[pos]);
		}
		else if(pos!=0 && countOccurences(rgbDiff.get(min)) < repetition){
			outputArray[pos]=rgbDiff.get(min);
			//System.out.println(pos+"\t"+outputArray[pos]);
		}
		//If tiles repetition exceeds the maximum number of times allowed:
		else{
			//remove the tile which exceeded the limit from the tile list 
			for (int k=0;k<tiles.size();k++) {
				if(tiles.get(k).getFilePath()==rgbDiff.get(min)){
					tiles.remove(k);		
				}
			}
			//remove the tile which exceeded the limit from rgbDiff
			rgbDiff.remove(min); 
			//Find the next tile with minimum value from rgbDiff
			double min_updated = Collections.min(rgbDiff.keySet());
			//Add the new minimum to the outputArray
			outputArray[pos]=rgbDiff.get(min_updated);
			//System.out.println(pos+"\t"+outputArray[pos]);
		}	
	}

	/*Euclidean distance calculation */
	public static double compareImages(ImageInfo cell,ImageInfo tile) {
		double diff = 5000;
		
		/*RGB Color Model
		double deltaRed = (cell.red - tile.red);
		double deltaBlue = (cell.blue - tile.blue);
		double deltaGreen = (cell.green - tile.green);
		diff = ((deltaRed*deltaRed)+(deltaBlue*deltaBlue)+(deltaGreen*deltaGreen));*/
		
		double deltaCieL = (cell.cieL - tile.cieL);
		double deltaCieA = (cell.cieA - tile.cieA);
		double deltaCieB = (cell.cieB - tile.cieB);
		
		/*YUV Color Model
		double deltaYuvY = (cell.yuvY - tile.yuvY);
		double deltaYuvU = (cell.yuvU - tile.yuvU);
		double deltaYuvV = (cell.yuvV - tile.yuvV);*/
		
		diff = ((deltaCieL*deltaCieL)+(deltaCieA*deltaCieA)+(deltaCieB*deltaCieB));	
		return diff;
	}

	/*Temp Directory deletion */
	public static boolean deleteDirectory(File directory) {
		if(directory.isDirectory()){
			String[] files = directory.list();
			for(int i = 0; i< files.length ; i++) {
				boolean success = deleteDirectory(new File(directory,files[i]));
				if(!success) {
					return false;
				}
			}
		}
		return directory.delete();
	}

	/* Helper to count the tile occurrences.*/
	public static Integer countOccurences(String filepath){
		if(!imageCount.containsKey(filepath)){
			imageCount.put(filepath, 0);
		}
		if(imageCount.containsKey(filepath)){
			imageCount.put(filepath, imageCount.get(filepath)+1);
		}
		return imageCount.get(filepath);
	}
}
