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
			/*Initializing variables and objects.*/
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
			
			/*For each tile, create ImageInfo object to calculate RGB and get
			 * its location.*/
			File filepathOfTileLibrary = new File(tileLibrary);			
			for (final File fileEntry : filepathOfTileLibrary.listFiles()) {
				/*This check ensures that only files are read from the tile 
				  directory.*/
				if(fileEntry.exists() && fileEntry.isFile()){
					ImageInfo temp = new ImageInfo();
					temp.setFilePath(fileEntry.getPath());
					/*calculateRGB() returns true only when an image is
					 * read from the tile directory. Hence, only when
					 * an image is read, add it to tiles list.*/
					if(temp.calculateRGB())
						tiles.add(temp);
				}
			}

			System.out.println("Tile Library processed.");
			
			/*Create a temporary directory to store cells. 
			 * This condition here is to handle exception cases, where the
			 * user would break the process via keyboard interrupt or
			 * when the program terminates due to an interrupt.*/
			boolean isDirectoryCreated = tempFolderOfTargetSplit.mkdir();
			if (!isDirectoryCreated) {			
				deleteDirectory(tempFolderOfTargetSplit);
				tempFolderOfTargetSplit.mkdir();
			}
			
			/*Load target image and calculate its dimensions to determine if 
			 * there are enough tiles to create the output image.*/
			ti = ImageIO.read(new File(targetImage));
			
			int targetImageSize  = ti.getHeight() * ti.getWidth();
			int tileSize = tiles.get(0).getImageSize();
			int totalCells = targetImageSize/tileSize;
			int checkTileCount = tiles.size() * repetition; 
			
			if(checkTileCount >= totalCells){
				/*(When target image is split in the tile dimensions,
				 * each piece is called cell.)*/
				
				/*Process target image to create cells.*/
				try{
				ProcessImage st = new ProcessImage();
				st.split(targetImage,targetImageFileFormat,
						tiles.get(0).getImageHeight(),
						tiles.get(0).getImageWidth(),
						tempFolderOfTargetSplitLocation);
				}catch(Exception e){
					System.out.println("Error in reading images from" +
							" tile library");
				}
				
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
				
				/*Initializing the outputArray to be equal to the size of the
				 	number of cells.*/
				outputArray = new String[cells.size()];
				
				/*Randomize selection of cells in the list to avoid patterns.*/
				ArrayList<Integer> numList = new ArrayList<Integer>();
				for (int k=0 ; k<cells.size(); k++){
					numList.add(k);				
				}
				//This inbuilt method shuffles the cells to be selected.
				Collections.shuffle(numList);
				
				for(int m=0; m<numList.size(); m++){
					calculateBestMatchElement(numList.get(m), cells, tiles,
							repetition);
				}
				
				//Create a ProcessImage object to render the output image.
				ProcessImage oi = new ProcessImage();
				oi.renderOutputImage(targetImage,
						tiles.get(0).getImageHeight(),
						tiles.get(0).getImageWidth(),
						outputArray, outputFileLocation, outputFileFormat);
				
				/*Delete the Temp Directory after a successful run.*/
				deleteDirectory(tempFolderOfTargetSplit);
			}
			else {
				System.out.println("Not enough tiles in the library to" +
						" create an output image.");
				System.exit(0);
			}
		}else {
			System.out.println("Invalid inputs provided.");
			System.exit(0);
		}
	}
	
	/*Method to calculate best match tiles for the random position.*/
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
		
		/*HashMap to store the metric and the file path of the corresponding
		tile as a <key,value> pair.*/
		Map <Double,String> rgbDiff = new HashMap<Double,String>();
		double diff=0.0;
		
		for(int j=0;j<tiles.size(); j++) {
			diff = compareImages(cells.get(pos),tiles.get(j));
			rgbDiff.put(diff, tiles.get(j).filePath);
		}
		
		//calculate the minimum distance. Thus the best match.
		double min = Collections.min(rgbDiff.keySet());
		
		if(pos==0){ //Add to outputArray for very first iteration.
			outputArray[pos]=rgbDiff.get(min);
		}
		//Add to outputArray if repetitions are valid.
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
		}	
	}

	/*Euclidean distance metric calculation*/
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
     * 						false if the directory is not deleted.
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
