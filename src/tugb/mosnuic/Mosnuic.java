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

	//Keeps track of tile repetitions.
	static HashMap <String,Integer> imageCount = new HashMap<String,Integer>();
	//outputArray is an array to store matching tiles.
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
			String tempFolderOfTargetSplitLocation = 
					userPath+"/DemoDirectory/";
			File tempFolderOfTargetSplit = 
					new File(tempFolderOfTargetSplitLocation);
			
			long sTile = System.currentTimeMillis();;
			File filepathOfTileLibrary = new File(tileLibrary);
			
			/* For each tile, create ImageInfo object to calculate RGB and get
			 * its location */
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
			if (!isDirectoryCreated) {			
				deleteDirectory(tempFolderOfTargetSplit);
				tempFolderOfTargetSplit.mkdir();
			}
			long timeForDD2 = System.currentTimeMillis();
			System.out.println("Time for Demo Dir: " + (timeForDD2-timeForDD));
			
			
			long sTI = System.currentTimeMillis();
			/* Load target image and calculate its dimensions to determine if 
			 * there are enough tiles to create the output image*/
			ti = ImageIO.read(new File(targetImage));
			
			int targetImageSize  = ti.getHeight() * ti.getWidth();
			int tileSize = (int) tiles.get(0).getImageSize();
			int totalCells = targetImageSize/tileSize;
			
			if((tiles.size()* repetition) >= totalCells){
				/* (When target image is split in the tile dimensions,
				 * each piece is called cell.) */
				
				// Process target image to create cells.
				ProcessImage st = new ProcessImage();
				st.split(targetImage,targetImageFileFormat,
						tiles.get(0).getImageHeight(),
						tiles.get(0).getImageWidth(),
						tempFolderOfTargetSplitLocation );
				
				//Creates ImageInfo object for each cell to calculate RGB 
				ArrayList<ImageInfo> cells = new ArrayList<ImageInfo>();
				
				for (final File fileEntry : 
					tempFolderOfTargetSplit.listFiles()) {
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
				
				/*Randomize selection of cells in the list to avoid patterns.*/
				ArrayList<Integer> numList = new ArrayList<Integer>();
				for (int k=0 ; k<cells.size(); k++){
					numList.add(k);				
				}
				long startToShuffle = System.currentTimeMillis();
				//This inbuilt method shuffles the cells to be selected.
				Collections.shuffle(numList);
				long endToShuffle = System.currentTimeMillis();
				long timeToShuffle = endToShuffle - startToShuffle;
				System.out.println("Time to shuffle "+timeToShuffle);
				for(int m=0; m<numList.size(); m++){
					calculateBestMatchElement(numList.get(m), cells, tiles,
							repetition);
				}
				
				//Create an object to create an outout image.
				ProcessImage oi = new ProcessImage();
				oi.renderOutputImage(targetImage,
						tiles.get(0).getImageHeight(),
						tiles.get(0).getImageWidth(),
						outputArray, outputFileLocation, outputFileFormat);
				
				//delete the Temp Directory after a successful run.
				deleteDirectory(tempFolderOfTargetSplit);
				long eOut = System.currentTimeMillis();;
				System.out.println("Time for output: "+ (eOut - sOut));
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				System.out.println("Total Time: " +totalTime);
			}
			else {
				System.out.println("Not enough tiles in the library to" +
						"create an output image.");
				System.exit(0);
			}
		}else {
			System.out.println("Invalid inputs provided.");
			System.exit(0);
		}
	}
	
	/*method calculates best match tiles for the random position */
	/**
     * @param pos			position of the cell to be read from the 
     *                      list of cells.
     * 
     * @param cells			list of ImageInfo objects for the broken
     * 						down target image.
     * @param tiles			list of ImageInfo objects for tile library images.
     * @param repetition	number of times a tile can be repeated.
     */
	private static void calculateBestMatchElement(int pos,
			ArrayList<ImageInfo> cells, ArrayList<ImageInfo> tiles,
			int repetition) {
		
		Map <Double,String> rgbDiff = new HashMap<Double,String>();
		double diff=0.0;
		
		for(int j=0;j<tiles.size(); j++) {
			diff = compareImages(cells.get(pos),tiles.get(j));
			rgbDiff.put(diff, tiles.get(j).filePath);
		}
		
		double min = Collections.min(rgbDiff.keySet());
		
		/*Add the tile with minimum distance to the outputArray in the very
		 *first iteration for a cell.*/
		if(pos==0){
			outputArray[pos]=rgbDiff.get(min);
		}
		else if(pos!=0 && countOccurences(rgbDiff.get(min)) < repetition){
			outputArray[pos]=rgbDiff.get(min);
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
	/**
     * @param cell			part of the target image object that is being
     * 						compared.
     * @param tile			the tile object that is being compared.
     * @return				the difference between the the CIELAB values of
     * 						both images.
     */
	public static double compareImages(ImageInfo cell,ImageInfo tile) {
		double diff = 5000;
		
		double deltaCieL = (cell.cieL - tile.cieL);
		double deltaCieA = (cell.cieA - tile.cieA);
		double deltaCieB = (cell.cieB - tile.cieB);
	
		diff = ((deltaCieL*deltaCieL)+
				(deltaCieA*deltaCieA)+
				(deltaCieB*deltaCieB));	
		return diff;
	}

	/*Temp Directory deletion */
	/**
     * @param directory		File object of the temporary directory.
     * @return				a boolean value. TRUE if the directory is deleted.
     * 						FALSE if the directory is not deleted.
     */
	public static boolean deleteDirectory(File directory) {
		if(directory.isDirectory()){
			String[] files = directory.list();
			for(int i = 0; i< files.length ; i++) {
				boolean deleted = deleteDirectory
						(new File(directory,files[i]));
				if(!deleted) {
					return false;
				}
			}
		}
		return directory.delete();
	}

	/* Helper to count the tile occurrences.*/
	/**
     * @param filepath		file path of the tile image.
     * @return				number of times a tile image is used.
     */
	public static int countOccurences(String filepath){
		if(!imageCount.containsKey(filepath)){
			imageCount.put(filepath, 0);
		}
		if(imageCount.containsKey(filepath)){
			imageCount.put(filepath, imageCount.get(filepath)+1);
		}
		return imageCount.get(filepath);
	}
}
