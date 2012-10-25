package tugb.mosnuic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageInfo {

	protected String filePath;
	protected int red, green , blue;
	int tileWidth, tileHeight,tileSize;

	public void calculateRGB() throws IOException {
		/*Calculate RGB for an image */
		try{
			BufferedImage tile = ImageIO.read(new File(filePath));
			tileWidth = tile.getWidth();
			tileHeight = tile.getHeight();
			tileSize = tileHeight * tileWidth;

			//Find RGB for each pixel of the image and return the average RGB for the image
			for (int i = 0; i<tileWidth; i++){
				for(int j = 0 ; j<tileHeight; j++){
					Color c= new Color(tile.getRGB(i, j));
					red += c.getRed();
					green += c.getGreen();
					blue += c.getBlue();
				}
			}
		}catch(Exception e) {
			System.out.println("Error in reading input image file" + e);
		}
	}

	/* Getters and setters for the ImageInfo objects */
	public void setFilePath(String filePathOfImage){
		filePath = filePathOfImage;
	}

	public String getFilePath() {		
		return filePath;
	}

	public int getTotalRed() {
		return red/tileSize;
	}

	public int getTotalGreen() {
		return green/tileSize;
	}

	public int getTotalBlue() {
		return blue/tileSize;
	}

	public int getTileHeight(){
		return tileHeight;
	}

	public int getTileWidth(){
		return tileWidth;
	}
}
