package tugb.mosnuic;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;


import javax.imageio.ImageIO;

public class SplitTarget {


	public void split(String filePath,String targetImageFileFormat, int height, int width, String tempFolderOfTargetSplitLocation) throws IOException {
		
		int x = 0 ; // used in the loops
		int y = 0 ;
		int i = 0 ;
		
		/*Load target image into memory*/
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		BufferedImage image = ImageIO.read(fis);

		/* calculate target image dimensions*/
		int targetImageHeight = image.getHeight();
		int targetImageWidth = image.getWidth();


		/*calculate total number of cells, and calculate dimensions of each cell*/
		int rows = (targetImageHeight/height); 
		int cols = (targetImageWidth/width);
		int totalCells = rows * cols;

		int cellWidth = image.getWidth() / cols; 
		int cellHeight = image.getHeight() / rows;

		/*Define an Image array to hold totalCells in the image*/
		int count = 0;
		BufferedImage bi[] = new BufferedImage[totalCells];

		/*fill image array with the split image*/
		for (x = 0; x < rows; x++) {
			for (y = 0; y < cols; y++) {
				//Initialize the image array with image totalCells
				bi[count] = new BufferedImage(cellWidth, cellHeight, image.getType());

				// draw each cell of the image
				Graphics2D gr = bi[count++].createGraphics();
				gr.drawImage(image, 0, 0, cellWidth, cellHeight, cellWidth * y, cellHeight * x, cellWidth * y + cellWidth, cellHeight * x + cellHeight, null);
				gr.dispose();
			}
		}

		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumIntegerDigits(4);

		for (i = 0; i < bi.length; i++) {
			//split target image, and store it in a temp folder
			ImageIO.write(bi[i], targetImageFileFormat, new File(tempFolderOfTargetSplitLocation + form.format(i) + "." + targetImageFileFormat));
			
		}

	}

	protected void  renderOutputImage(String filePath, int tileHeight, int tileWidth, ArrayList<String> outputList, String outputFileLocation, String outputFileFormat) throws IOException {
		
		int num = 0;  
		int i = 0; 
		int j = 0;
		/*Load target image into memory*/
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		BufferedImage targetImage = ImageIO.read(fis);

		// target image dimensions
		int targetImageHeight = targetImage.getHeight();
		int targetImageWidth = targetImage.getWidth();


		/*calculate total number of cells, and calculate dimensions of each cell*/
		int rows = (targetImageHeight/tileHeight); 
		int cols = (targetImageWidth/tileWidth);
		int totalCells = rows * cols;

		int cellWidth = targetImage.getWidth() / cols; 
		int cellHeight = targetImage.getHeight() / rows;

		// file array to read images from outputlist.
		File[] imgFiles = new File[totalCells];  
		for (i = 0; i < totalCells; i++) {  
			imgFiles[i] = new File(outputList.get(i));  
		} 
		// array to read images
		BufferedImage[] buffImages = new BufferedImage[totalCells];  
		for (i = 0; i < totalCells; i++) {  
			try {
				buffImages[i] = ImageIO.read(imgFiles[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}  
		}  
		int type = buffImages[0].getType();

		BufferedImage finalImg = new BufferedImage(cellWidth*cols, cellHeight*rows, type);  

		//renders the output image.
		for (i = 0; i < rows; i++) {  
			for (j = 0; j < cols; j++) {  
				finalImg.createGraphics().drawImage(buffImages[num], cellWidth * j, cellHeight * i, null);  
				num++;  
			}  
		}  
		System.out.println("Final Image created.");  
		try {
			ImageIO.write(finalImg,outputFileFormat, new File(outputFileLocation));
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}   


}