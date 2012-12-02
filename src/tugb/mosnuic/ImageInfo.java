package tugb.mosnuic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageInfo {

	protected String filePath;
	protected double red, green , blue;
	int imageWidth, imageHeight,imageSize; 
	protected double cieL, cieA, cieB;
	protected double yuvY, yuvU, yuvV;

	public void calculateRGB() throws IOException {
		/*Calculate RGB for an image */
		try{
			BufferedImage inputImage = ImageIO.read(new File(filePath));
			imageWidth = inputImage.getWidth();
			imageHeight = inputImage.getHeight();
			imageSize = imageHeight * imageWidth;

			//Find RGB for each pixel of the image and return the average RGB for the image
			for (int i = 0; i<imageWidth; i++){
				for(int j = 0 ; j<imageHeight; j++){
					Color c= new Color(inputImage.getRGB(i, j));
					red += c.getRed();
					green += c.getGreen();
					blue += c.getBlue();
				}
			}
			rgbToLab(red, green, blue);
			//rgbtoYUV(red, green, blue);
			
		}catch(Exception e) {
			System.out.println("Error in reading input image file");
		}
	}
	

	/*private void rgbtoYUV(int r, int g, int b) {
		// TODO Auto-generated method stub
		yuvY = (int)(0.299 * r + 0.587 * g + 0.114 * b);
		yuvU = (int)((b - yuvY) * 0.492f); 
		yuvV = (int)((r - yuvY) * 0.877f);		
	}*/

	private void rgbToLab(double R, double G, double B) {
		double r;
		double g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
		double Ls, as, bs;
		double eps = 216.f/24389.f;
		double k = 24389.f/27.f;
		   
		double Xr = 0.964221f;  
		double Yr = 1.0f;
		double Zr = 0.825211f;
		
		
		r = R/255.f; 
		g = G/255.f; 
		b = B/255.f; 
		
		
		if (r <= 0.04045)
			r = r/12;
		else
			r = (double) Math.pow((r+0.055)/1.055,2.4);
		
		if (g <= 0.04045)
			g = g/12;
		else
			g = (double) Math.pow((g+0.055)/1.055,2.4);
		
		if (b <= 0.04045)
			b = b/12;
		else
			b = (double) Math.pow((b+0.055)/1.055,2.4);
		
		
		X =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
		Y =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
		Z =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;
		
		// XYZ to Lab
		xr = X/Xr;
		yr = Y/Yr;
		zr = Z/Zr;
				
		if ( xr > eps )
			fx =  (double) Math.pow(xr, 1/3.);
		else
			fx = (double) ((k * xr + 16.) / 116.);
		 
		if ( yr > eps )
			fy =  (double) Math.pow(yr, 1/3.);
		else
		fy = (double) ((k * yr + 16.) / 116.);
		
		if ( zr > eps )
			fz =  (double) Math.pow(zr, 1/3.);
		else
			fz = (double) ((k * zr + 16.) / 116);
		
		Ls = ( 116 * fy ) - 16;
		as = 500*(fx-fy);
		bs = 200*(fy-fz);
		
		cieL = (double) (2.55*Ls + .5);
		cieA = (double) (as + .5); 
		cieB = (double) (bs + .5);     
	
	}


	/* Getters and setters for the ImageInfo objects */
	public void setFilePath(String filePathOfImage){
		filePath = filePathOfImage;
	}
	
	public String getFilePath() {		
		return filePath;
	}

	public double getTotalRed() {
		return red/imageSize;
	}

	public double getTotalGreen() {
		return green/imageSize;
	}

	public double getTotalBlue() {
		return blue/imageSize;
	}

	public int getImageHeight(){
		return imageHeight;
	}

	public int getImageWidth(){
		return imageWidth;
	}
	
	public int getImageSize(){
		return imageSize;
		
	}
}
