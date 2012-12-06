package tugb.mosnuic;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import javax.imageio.ImageIO;

public class ProcessImage {

	/*Method to Split a given image*/
	/**
     * @param filePath							file path of the image to
     * 											be split.
     * @param targetImageFileFormat				file format of the image.
     * @param height							dimensions according to 
     * 											which the target image should
     * 											be split by height.
     * @param width								dimensions according to
     * 											which the target image should
     * 											be split by width.
     * @param tempFolderOfTargetSplitLocation	file path where the temporary
     * 											folder should be created.
     * @throws IOException						if the image cannot be read.
     */
	public void split(String filePath,String targetImageFileFormat, int height,
			int width, String tempFolderOfTargetSplitLocation)
					throws IOException {
		/*variables used in the loops*/
		int x = 0 ; 
		int y = 0 ;
		int i = 0 ;

		/*load target image into memory*/
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		BufferedImage image = ImageIO.read(fis);

		/*calculate target image dimensions*/
		int targetImageHeight = image.getHeight();
		int targetImageWidth = image.getWidth();

		/*calculate total number of cells, and calculate dimensions of
		 *  each cell*/
		int rows = (targetImageHeight/height); 
		int cols = (targetImageWidth/width);
		int totalCells = rows * cols;

		int cellWidth = image.getWidth() / cols; 
		int cellHeight = image.getHeight() / rows;

		/*BufferedImage array to hold Cells*/
		int count = 0;
		BufferedImage bi[] = new BufferedImage[totalCells];

		/*fill the BufferedImage array*/
		for (x = 0; x < rows; x++) {
			for (y = 0; y < cols; y++) {
				//Initialize the image array with image totalCells
				if(image.getType() == 0) {
					/* Some image types like Tiff return 0 with getTpye().
					 * Hence, if the getType() returns 0, we set it to a
					 *  non-zero integer.*/
					bi[count] = new BufferedImage(cellWidth, cellHeight, 5);
				} else {
					bi[count] = new BufferedImage(cellWidth, cellHeight,
							image.getType());
				}
				//Given a BufferedImage array, draw the cells on a canvas 
				Graphics2D gr = bi[count++].createGraphics();
				gr.drawImage(image, 0, 0, cellWidth, cellHeight, cellWidth * y,
						cellHeight * x, cellWidth * y + cellWidth,
						cellHeight * x + cellHeight, null);
				gr.dispose();
			}
		}

		/*Since we want to preserve the order in which we write the cells to
		 * the Temp directory, we number them starting from 0 to number of
		 * tiles available.*/
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumIntegerDigits(4);

		for (i = 0; i < bi.length; i++) {
			//split target image, and store it in a temp folder
			ImageIO.write(bi[i], targetImageFileFormat,
					new File(tempFolderOfTargetSplitLocation + 
							form.format(i) + "." + targetImageFileFormat));
		}		
	} 

	/*Method to join multiple images to form a single image*/
	/**
	 * @param filePath				Target image file path.		
	 * @param tileHeight			Height of a tile to calculate output
	 * 								image dimensions.
	 * @param tileWidth				Width of a tile to calculate output
	 * 								image dimensions.
	 * @param outputArray			Array containing best matching tiles. 								
	 * @param outputFileLocation	Location to write the output image.
	 * @param outputFileFormat		File format of the output image
	 * @throws IOException			if the image cannot be written.
	 */
	protected void renderOutputImage(String filePath, int tileHeight,
			int tileWidth, String[] outputArray, String outputFileLocation,
			String outputFileFormat) throws IOException {

		int num = 0;  
		int i = 0; 
		int j = 0;

		/*Load target image into memory*/
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		BufferedImage targetImage = ImageIO.read(fis);

		/*target image dimensions*/
		int targetImageHeight = targetImage.getHeight();
		int targetImageWidth = targetImage.getWidth();

		/*calculate total number of cells, and calculate dimensions of
		 * each cell*/
		int rows = (targetImageHeight/tileHeight); 
		int cols = (targetImageWidth/tileWidth);
		int totalCells = rows * cols;

		int cellWidth = targetImage.getWidth() / cols; 
		int cellHeight = targetImage.getHeight() / rows;

		//File array to read images from outputArray.
		File[] imgFiles = new File[totalCells];  
		for (i = 0; i < totalCells; i++) {  
			imgFiles[i] = new File(outputArray[i]);  
		} 

		//BufferedImage array to read images
		BufferedImage[] buffImages = new BufferedImage[totalCells];  
		for (i = 0; i < totalCells; i++) {  
			try {
				buffImages[i] = ImageIO.read(imgFiles[i]);
			} catch (IOException e) {
				System.out.println("Cannot create output image.");
			}  
		} 

		int type = buffImages[0].getType(); 
		BufferedImage finalImg = new BufferedImage(cellWidth*cols,
				cellHeight*rows, type);

		//renders the output image.
		for (i = 0; i < rows; i++) {  
			for (j = 0; j < cols; j++) {  
				finalImg.createGraphics().drawImage(buffImages[num],
						cellWidth * j, cellHeight * i, null);  
				num++;  
			}  
		}  

		try{
			ImageIO.write(finalImg,outputFileFormat,
					new File(outputFileLocation));
			System.out.println("Final Image created.");  
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("Cannot write to the specified location");
		} 
	}   
}