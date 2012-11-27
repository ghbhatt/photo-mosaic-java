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
	//outputList is the list to store matching tiles.
	static String[] outputList = null;
	static BufferedImage ti = null;

	public static void main(String[] args) throws IOException {

		ParseInputArguments parser = new ParseInputArguments();
		boolean check = parser.ParsePath(args);
		/*parser.getMaxNumberOfTiles();
		parser.getOutputImageFileFormat();
		parser.getOutputImageFilePath();
		parser.getTargetImageFileFormat();
		parser.getTargetImageFilePath();
		parser.getTileDirectory();
		System.exit(0);*/

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

			File filepathOfTileLibrary = new File(tileLibrary);
			int i = 0;
			int j = 0;

			/* For each tile, create ImageInfo object to calculate RGB and get its location */
			for (final File fileEntry : filepathOfTileLibrary.listFiles()) {
				ImageInfo temp = new ImageInfo();
				temp.setFilePath(fileEntry.getPath());
				temp.calculateRGB();
				tiles.add(temp);
			}

			System.out.println("Tile Library processed.");

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
			
			/* Calculate the target image dimension to determine if there are
			 * enough tiles to create the output image*/
			ti = ImageIO.read(new File(targetImage));
			
			int targetImageSize  = ti.getHeight() * ti.getWidth();
			int tileSize = tiles.get(0).getImageSize();
			int totalCells = targetImageSize/tileSize;
			/*System.out.println(targetImageSize);
			System.out.println(tileSize);
			System.out.println("total no of cells:"+totalCells);*/
			
			//Check if there are enough tiles to create the output image.
			if((tiles.size()* repetition) >= totalCells){
				
				// Process target image 
				SplitTarget st = new SplitTarget();
				st.split(targetImage,targetImageFileFormat,tiles.get(0).getTileHeight(), tiles.get(0).getTileWidth(),
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

				

				/* For every cell in the cells ArrayList, calculate Euclidean distance with each tile.
				 * Store this distance and the tile location in a Map rgbDiff. Select the minimum distance
				 * and store the path of the corresponding tile in outputList. 
				for(i=0;i<cells.size(); i++) {
					Map <Double,String> rgbDiff = new HashMap<Double,String>();
					for(j=0;j<tiles.size(); j++) {
						diff = compareImages(cells.get(i),tiles.get(j));
						rgbDiff.put(diff, tiles.get(j).filePath);
					}
					double min = Collections.min(rgbDiff.keySet());
					//Add the tile with minimum distance to the outputList in the very first iteration for a cell.
					if(i==0){
						outputList.add(rgbDiff.get(min));
					}
					//Add until tiles are allowed to repeat.
					else if(i!=0 && countOccurences(rgbDiff.get(min)) < repetition){
						outputList.add(rgbDiff.get(min));
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
						//Add the new minimum to the outputList
						outputList.add(rgbDiff.get(min_updated));
					}
				}*/
				outputList = new String[cells.size()];
				createOutputList(cells, tiles, repetition);
				System.out.println("O/P LIST=================");
				for(i=0;i<cells.size();i++)
					System.out.println(i+"\t"+outputList[i]);
				//WE ARE HERE
				SplitTarget oi = new SplitTarget();
				oi.renderOutputImage(targetImage, tiles.get(0).getTileHeight(), tiles.get(0).getTileWidth(),
						outputList, outputFileLocation, outputFileFormat);
				deleteDirectory(tempFolderOfTargetSplit);
				long endTime   = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				System.out.println(totalTime);
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

	private static void createOutputList(ArrayList<ImageInfo> cells,
			ArrayList<ImageInfo> tiles, int repetition) {
		int mid;
		int mrow, mcol, cols, lpos, rpos;
		
		int targetImageHeight = ti.getHeight();
		int targetImageWidth = ti.getWidth();

		/*calculate total number of cells, and calculate dimensions of each cell*/
		cols = (targetImageWidth/tiles.get(0).getTileWidth());
		mrow = (targetImageHeight/tiles.get(0).getTileHeight())/2;
		mcol = cols/2; 
		
		mid=(mrow*cols)+mcol;
		//mid=cells.size()/2;
		System.out.println("Cells.size = "+cells.size()+mrow+"\t"+mcol+"\t"+cols);

		
		//COMMENTED FROM HERE
				/*calculateBestMatch(mid, cells, tiles, repetition, mid);
				
				for(i=mid-1, j=mid+1;i>=0||j<cells.size();i--,j++){
					if(i>=0)
						calculateBestMatch(i, cells, tiles, repetition, mid);
					if(j<cells.size())
						calculateBestMatch(j, cells, tiles, repetition, mid);			
				}

			}

			private static void calculateBestMatch(int pos, ArrayList<ImageInfo> cells,
					ArrayList<ImageInfo> tiles, int repetition, int mid) {
				// TODO Auto-generated method stub
				Map <Double,String> rgbDiff = new HashMap<Double,String>();
				double diff=0.0;
				
				for(int j=0;j<tiles.size(); j++) {
					diff = compareImages(cells.get(pos),tiles.get(j));
					rgbDiff.put(diff, tiles.get(j).filePath);
				}
				
				double min = Collections.min(rgbDiff.keySet());
				
				//Add the tile with minimum distance to the outputList in the very first iteration for a cell.
				if(pos==mid){
					outputList[pos]=rgbDiff.get(min);
					//System.out.println("pos\t"+outputList);
				}
				else if(pos!=mid && countOccurences(rgbDiff.get(min)) < repetition){
					outputList[pos]=rgbDiff.get(min);
					//System.out.println("pos\t"+outputList);
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
					//Add the new minimum to the outputList
					outputList[pos]=rgbDiff.get(min_updated);
					//System.out.println("pos\t"+outputList);
				}*/
		
		for(int k=0;k<cells.size();k++){
			if(k%cols==0)
				System.out.print("\n");	
			System.out.print(k+"\t");
				
		}
		lpos = mid - cols;
		rpos = mid + cols;
		System.out.println("lpos: "+lpos+"\trpos: "+rpos);
		calculateBestMatchRow(mid, cols, cells, tiles, repetition);
		//outputList[2142]=new String("/home/pait1988/prototype3/Tiles24x18/2005corn.jpg");
		
		
		while(lpos>0 || (rpos<cells.size())){
			if(lpos>0){
				System.out.println("lpos: "+lpos+"\trpos: "+rpos);
				calculateBestMatchRow(lpos, cols, cells, tiles, repetition);
				lpos=lpos-cols;
			}
			if(rpos<cells.size()){
				System.out.println("lpos: "+lpos+"\trpos: "+rpos);
				calculateBestMatchRow(rpos, cols, cells, tiles, repetition);
				rpos=rpos+cols;
			}
			
		}
	}

	private static void calculateBestMatchRow(int mid, int cols, ArrayList<ImageInfo> cells,
			ArrayList<ImageInfo> tiles, int repetition) {
		// TODO Auto-generated method stub
		int i, j;
		calculateBestMatchElement(mid, cols, cells, tiles, repetition, mid);
		for(i=mid-1, j=mid+1; (((i%cols)<(mid%cols))||((j%cols)>(mid%cols))) && 
				(i>=0 && j<=cells.size());){
			
			//System.out.println("In loop\t"+i+"\t"+j);
			if((i%cols)<(mid%cols) && i>=0){
				System.out.println("i=\t"+i);
				calculateBestMatchElement(i, cols, cells, tiles, repetition, mid);
				if(i==2143)
					System.out.println("=============================Decrementing i\t"+i);
				i--;
				System.out.println("=============================Decremented i\t"+i);
			}

			if((j%cols)>(mid%cols) && j<cells.size()){
				System.out.println("j=\t"+j);
				calculateBestMatchElement(j, cols, cells, tiles, repetition, mid);
				j++;
			}
			
			if(i<0 && j>=cells.size())
				break;
		}	
	}

	private static void calculateBestMatchElement(int pos, int cols, ArrayList<ImageInfo> cells,
			ArrayList<ImageInfo> tiles, int repetition, int mid) {
		// TODO Auto-generated method stub
		Map <Double,String> rgbDiff = new HashMap<Double,String>();
		double diff=0.0;
		
		for(int j=0;j<tiles.size(); j++) {
			diff = compareImages(cells.get(pos),tiles.get(j));
			rgbDiff.put(diff, tiles.get(j).filePath);
		}
		
		double min = Collections.min(rgbDiff.keySet());
		
		//Add the tile with minimum distance to the outputList in the very first iteration for a cell.
		if(pos==mid){
			outputList[pos]=rgbDiff.get(min);
			System.out.println(pos+"\t"+outputList[pos]);
		}
		else if(pos!=mid && countOccurences(rgbDiff.get(min)) < repetition){
			outputList[pos]=rgbDiff.get(min);
			System.out.println(pos+"\t"+outputList[pos]);
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
			//Add the new minimum to the outputList
			outputList[pos]=rgbDiff.get(min_updated);
			System.out.println(pos+"\t"+outputList[pos]);
		}	
	}

	/*Euclidean distance calculation */
	public static double compareImages(ImageInfo cell,ImageInfo tile) {
		double diff = 5000;
		
		/*double deltaRed = (cell.red - tile.red);
		double deltaBlue = (cell.blue - tile.blue);
		double deltaGreen = (cell.green - tile.green);
		diff = ((deltaRed*deltaRed)+(deltaBlue*deltaBlue)+(deltaGreen*deltaGreen));*/
		double deltaCieL = (cell.cieL - tile.cieL);
		double deltaCieA = (cell.cieA - tile.cieA);
		double deltaCieB = (cell.cieB - tile.cieB);
		
		/*double deltaYuvY = (cell.yuvY - tile.yuvY);
		double deltaYuvU = (cell.yuvU - tile.yuvU);
		double deltaYuvV = (cell.yuvV - tile.yuvV);*/
		
		diff = ((deltaCieL*deltaCieL)+(deltaCieA*deltaCieA)+(deltaCieB*deltaCieB));
		
		//diff = Math.sqrt((deltaYuvY*deltaYuvY) + (deltaYuvU*deltaYuvU) + (deltaYuvV*deltaYuvV));
	
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
