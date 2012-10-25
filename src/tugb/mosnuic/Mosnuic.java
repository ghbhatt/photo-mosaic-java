package tugb.mosnuic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Mosnuic {

	static HashMap <String,Integer> imageCount = new HashMap<String,Integer>();

	public static void main(String[] args) throws IOException {

		ParseInputArguments parser = new ParseInputArguments();
		boolean check = parser.ParsePath(args);

		if(check){
			/* initializing variables and objects.*/
			//long startTime = System.currentTimeMillis();
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

			// Process target image 
			SplitTarget st = new SplitTarget();
			st.split(targetImage,targetImageFileFormat,tiles.get(0).getTileHeight(), tiles.get(0).getTileWidth(),
					tempFolderOfTargetSplitLocation );
			ArrayList<ImageInfo> cells = new ArrayList<ImageInfo>();

			/* Creates ImageInfo object for each cell to calculate RGB */ 
			/* (When target image is split in the tile dimensions, each piece is called cell.) */
			for (final File fileEntry : tempFolderOfTargetSplit.listFiles()) {
				ImageInfo temp = new ImageInfo();
				temp.setFilePath(fileEntry.getPath());
				temp.calculateRGB();
				cells.add(temp);
			}
			System.out.println("Target image processed.");

			//Check if there are enough tiles to create the output image.
			if((tiles.size()* repetition) >= cells.size()){

				//outputList is the list to store matching tiles.
				ArrayList<String> outputList = new ArrayList<String>();
				double diff = 0.0;

				/* For every cell in the cells ArrayList, calculate Euclidean distance with each tile.
				 * Store this distance and the tile location in a Map rgbDiff. Select the minimum distance
				 * and store the path of the corresponding tile in outputList. */
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
				}
				SplitTarget oi = new SplitTarget();
				oi.renderOutputImage(targetImage, tiles.get(0).getTileHeight(), tiles.get(0).getTileWidth(),
						outputList, outputFileLocation, outputFileFormat);
				//deleteDirectory(tempFolderOfTargetSplit);
				//long endTime   = System.currentTimeMillis();
				//long totalTime = endTime - startTime;
				//System.out.println(totalTime);
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

	/*Euclidean distance calculation */
	public static double compareImages(ImageInfo cell,ImageInfo tile) {
		double diff = 5000;
		double deltaRed = (cell.red - tile.red);
		double deltaBlue = (cell.blue - tile.blue);
		double deltaGreen = (cell.green - tile.green);
		diff = ((deltaRed*deltaRed)+(deltaBlue*deltaBlue)+(deltaGreen*deltaGreen));
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
