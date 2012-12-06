package tugb.mosnuic;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;


public class ParseInputArguments {
	private static String targetImageFilePath;
	private static String targetImageFileName;
	private static String targetImageFileFormat;
	private static String targetImageFileExtension;
	private static String tileDirectory;
	private static String outputImageFilePath;
	private static String outputImageFileName;
	private static String outputImageFileFormat;
	private static String outputImageFileExtension;
	private static int maxNumberOfTiles = 10000;
	/*private static String targetImageFileParent;*/

	static boolean TargetImagePathFound = false;
	static int OutputImagePathFound = 0;
	static boolean TileDirectoryPathFound = false;
	static boolean OutputImagePathToBeConstructed = false;

	static String ERROR = "The application accepts the input in the following format\nfile1 dir2 [-o file3] [-r n4]";
	static String UNSUPPORTED_FORMAT = "The application only supports the JPEG, JPG, BMP, GIF and TIFF image formats";
	static String OUTPUT_SUFFIX = "_out";

	public enum INPUT_TYPE {
		FLAG,
		TARGET_IMAGE_FILE,
		TILE_LIBRARY_DIR,
		ERROR		
	}

	public boolean ParsePath(String args[]){

		/*Check if argument count is less than 2 or a multiple of 2*/
		checkNumberOfArguments(args);

		/*Check for any repeated occurrences of flags*/
		checkRepeatedFlags(args);

		/* Main method to parse the Input arguments and check them */
		boolean finalCheck = checkArgs(args);
		
		/* Method to check if mandatory inputs are present */
		checkMandatoryInputs();
		
		/* Method to find the relevant information of the output image */
		createOutputImageInformation();

		return finalCheck;

	}

	public static boolean checkArgs(String[] args){

		/*Initialize variables*/
		int i;
		boolean result = true;

		/*Determines input type which is either a FLAG, FILE, DIR. If neither then throw error and terminate*/
		for(i=0;i<args.length;i++){
			String inputType = checkInputType(args[i]);
			INPUT_TYPE ip = INPUT_TYPE.valueOf(inputType);

			switch(ip){
			case TARGET_IMAGE_FILE:
				if(TargetImagePathFound == false){
					TargetImagePathFound = true;	
					try {
						File temp = new File(args[i]);
						targetImageFilePath = new String(temp.getCanonicalPath());
						String tempName = temp.getName();

						if(tempName.lastIndexOf(".")==-1){
							targetImageFileName = tempName;
							targetImageFileExtension = "";
						}
						else{
							targetImageFileName = tempName.substring(0, tempName.lastIndexOf("."));
							targetImageFileExtension = tempName.substring(tempName.lastIndexOf("."), tempName.length());
						}
						result = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("There was an IO Exception while handling the target image file.");
						System.exit(0);
					}
				}
				else
				{
					//Throw error
					result = false;
					System.out.println("There seems to be a redundant file in input");
					System.out.println(ERROR);
					System.exit(0);
				}
				break;
			case TILE_LIBRARY_DIR:
				if(TileDirectoryPathFound == false){
					TileDirectoryPathFound = true;
					try {
						File temp = new File(args[i]);
						tileDirectory = temp.getCanonicalPath();
					} catch (IOException e) {
						System.out.println("There was an IO Exception while handling the tile library directory.");
						System.exit(0);
					}
				}
				else
				{
					//Throw error
					System.out.println(ERROR);
					System.out.println("Redundant dir in input");
					System.exit(0);
					result = false;
				}
				break;
			case FLAG:
				try {
					if(!checkFlagArgs(args[i], args[++i])){
						System.out.println(ERROR);
						System.out.println("Please check flag arguments");
						System.exit(0);
						result = false;
					}
				} catch (Exception e) {					
					System.out.println("There was an error while handling the files. Please try again");
					System.exit(0);
					result = false;
				}
				break;
			case ERROR:
				System.out.println("There was an error while handling the files. Please try again");
				System.exit(0);
				result = false;
				break;
			default:
				break;
			}
		}
		return result;
	}

	/*Determines input type which is either a FLAG, FILE, DIR. If neither then default is ERROR*/
	private static String checkInputType(String arg) {
		String inputType = new String("ERROR");

		if(checkTargetImagePath(arg))
			inputType = "TARGET_IMAGE_FILE";
		if(checkTileDirectory(arg))
			inputType = "TILE_LIBRARY_DIR";
		if(arg.contains("-") && arg.length() == 2)
			inputType = "FLAG";

		return inputType;
	}
	
	private static void createOutputImageInformation(){
		switch(OutputImagePathFound){
		case 0:
			outputImageFileName = targetImageFileName+OUTPUT_SUFFIX;
			outputImageFileExtension = targetImageFileExtension;
			outputImageFileFormat = targetImageFileFormat;
			outputImageFilePath = targetImageFilePath.substring(0, targetImageFilePath.lastIndexOf(File.separatorChar))
					+File.separator+outputImageFileName+outputImageFileExtension;
			break;
		case 1:
			outputImageFilePath = targetImageFilePath.substring(0, targetImageFilePath.lastIndexOf(File.separatorChar))
					+File.separator+outputImageFileName+outputImageFileExtension;
			break;
		case 2:
			break;
		case 3:
			outputImageFileFormat = targetImageFileFormat;
			outputImageFilePath = targetImageFilePath.substring(0, targetImageFilePath.lastIndexOf(File.separatorChar))
					+File.separator+outputImageFileName+outputImageFileExtension;
			break;
		case 4:
			outputImageFileFormat = targetImageFileFormat;
			break;
		case 5:
			outputImageFilePath = targetImageFilePath.substring(0, targetImageFilePath.lastIndexOf(File.separatorChar))
					+File.separator+outputImageFileName+outputImageFileExtension;
			outputImageFileFormat = targetImageFileFormat;
			break;
		default:
		System.out.println("There was an error while handling the files. Please try again");
		System.exit(0);
			
		}
	}
	
	private static boolean checkTargetImagePath(String path) {
		try {
			File cliTargetImage = new File(path);
			boolean chkExists = cliTargetImage.exists();
			boolean chkFile = cliTargetImage.isFile();
			boolean chkFileRead = cliTargetImage.canRead();

			boolean chkImageFileFormat = false;

			if(chkExists && chkFile && chkFileRead){
				ImageInputStream iis = ImageIO.createImageInputStream(cliTargetImage);
				java.util.Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
				if (!((java.util.Iterator<ImageReader>) iter).hasNext()) {
					chkImageFileFormat = false;
				}
				ImageReader reader = (ImageReader) iter.next();
				iis.close();

				String format = reader.getFormatName();
				chkImageFileFormat = checkFileTypes(format);

				if(chkImageFileFormat){
					targetImageFileFormat = format;
				}
				else{
					return false;
				}
			}
			return (chkExists && chkFile && chkFileRead);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("There was an IO Exception while handling the target image file.");
			System.exit(0);

		}
		return false;
	}

	private static boolean checkTileDirectory(String path) {
		try {
			File cliLibImages = new File(path);
			boolean chkExists = cliLibImages.exists();
			boolean chkDir = cliLibImages.isDirectory();
			boolean chkDirRead = cliLibImages.canRead();
			boolean chkDirEmpty = false;
			
			if(chkDir && chkExists){
				chkDirEmpty = (cliLibImages.listFiles().length > 0);
			}

			return (chkExists && chkDir && chkDirRead && chkDirEmpty);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("There was an IO Exception while handling the tile library directory.");
			e.printStackTrace();
			System.exit(0);

		}
		return false;
	}

	private static boolean checkFlagArgs(String flag, String value) {
		boolean returnType = false;		
		if(flag.equals("-o"))
			returnType = checkFlag_O(value);
		if(flag.equals("-r"))
			returnType = checkFlag_R(value);		
		return returnType;		
	}
	
	
	private static boolean checkFlag_R(String value) {

		try {
			int repTiles = Integer.parseInt(value);
			if(repTiles<=0){
				System.out.println(ERROR);
				System.out.println("Please supply an unsigned integer for the n4 value");
				System.exit(0);
			}
			else{
				maxNumberOfTiles = repTiles;
			}
			return (repTiles > 0);
		} catch (NumberFormatException e) {
			System.out.println(ERROR);
			System.out.println("Integer n4 in [-r n4] not parseable");
			System.exit(0);
			return false;
		}
	}
	
	public static boolean checkFlag_O(String args3){
		String targetPath = new String(args3);
		int lastIndexSlash = targetPath.lastIndexOf(File.separatorChar);
		
		int posDot = args3.lastIndexOf(".");
		
		if(checkFileTypesForOP(args3.substring(posDot+1))){
			if(lastIndexSlash==-1){
				OutputImagePathFound = 1;
				outputImageFileName = args3.substring(0, posDot);
				outputImageFileExtension = args3.substring(posDot, args3.length());
				outputImageFileFormat = args3.substring(posDot+1, args3.length());
				return true;
			}
			else{
				try {
					File outputParent = new File(args3.substring(0, lastIndexSlash+1)); 
					boolean chkExists = outputParent.exists();
					boolean chkDir = outputParent.isDirectory();
					boolean chkDirRead = outputParent.canRead();
					boolean chkDirWrite = outputParent.canWrite();
					
					if(chkExists && chkDir && chkDirRead && chkDirWrite){
						OutputImagePathFound = 2;
						outputImageFileName = args3.substring(lastIndexSlash+1, posDot);
						outputImageFileExtension = args3.substring(posDot, args3.length());
						outputImageFileFormat = args3.substring(posDot+1, args3.length());
						outputImageFilePath = outputParent.getCanonicalPath()+File.separator+outputImageFileName+outputImageFileExtension;
						return true;
					}
					else{
						System.out.println("Please check if the output directory has read and write permissions.");
						System.exit(0);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("Please check if the output directory has read and write permissions.");
					System.exit(0);
				}
			}
		}
		else if(args3.lastIndexOf('.')!=-1 && lastIndexSlash==-1){
			OutputImagePathFound = 5;
			outputImageFileName = args3.substring(0, posDot);
			outputImageFileExtension = args3.substring(posDot, args3.length());
			return true;
		}
		else if(args3.lastIndexOf('.')==-1 && lastIndexSlash==-1){
			OutputImagePathFound = 3;
			outputImageFileName = args3;
			outputImageFileExtension = "";
			return true;
		}
		else if(lastIndexSlash!=-1){
			if(lastIndexSlash==args3.length()-1){
				System.out.println(ERROR);
				System.out.println("The argument file3 should be a file path and not a directory.");
				System.exit(0);
				return false;
			}
			else{
				try {
					String parent = args3.substring(0, lastIndexSlash);
					File outputParent = new File(parent);
					boolean chkExists = outputParent.exists();
					boolean chkDir = outputParent.isDirectory();
					boolean chkDirRead = outputParent.canRead();
					boolean chkDirWrite = outputParent.canWrite();
					
					if(chkExists && chkDir && chkDirRead && chkDirWrite){
						OutputImagePathFound = 4;
						outputImageFileName = args3.substring(lastIndexSlash+1, args3.length());
						outputImageFileExtension = "";
						outputImageFilePath = outputParent.getCanonicalPath()+File.separator+outputImageFileName+outputImageFileExtension;
						return true;
					}
					else{
						System.out.println(ERROR);
						System.out.println("Please confirm that output directory exists and is writable");
						System.exit(0);
						return false;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println(ERROR);
					System.out.println("Please confirm that output directory exists and is writable");
					System.exit(0);
					return false;
				}
			}
			
		}
		return false;
		 
	}
	
	
	private static boolean checkFileTypes(String format) {
		boolean chkBMP = format.equalsIgnoreCase("bmp");
		boolean chkGIF = format.equalsIgnoreCase("gif");
		boolean chkJPEG = format.equalsIgnoreCase("jpeg");
		boolean chkJPG = format.equalsIgnoreCase("jpg");
		boolean chkPNG = format.equalsIgnoreCase("png");
		boolean chkTIF = format.equalsIgnoreCase("tif");		
		return (chkBMP || chkGIF || chkJPEG || chkJPG || chkPNG || chkTIF);
	}
	
	private static boolean checkFileTypesForOP(String format) {
		boolean chkBMP = format.equalsIgnoreCase("bmp");
		boolean chkGIF = format.equalsIgnoreCase("gif");
		boolean chkJPEG = format.equalsIgnoreCase("jpeg");
		boolean chkJPG = format.equalsIgnoreCase("jpg");
		boolean chkPNG = format.equalsIgnoreCase("png");
		boolean chkTIFF = format.equalsIgnoreCase("tiff");		
		return (chkBMP || chkGIF || chkJPEG || chkJPG || chkPNG || chkTIFF);
	}

	/*Function to check if any flags are repeated or not*/
	private void checkRepeatedFlags(String[] args) {
		int count_o = 0;
		int count_r = 0;

		/*Calculate count of each flags available*/
		for(int i=0;i<args.length;i++){
			if(args[i].equals("-o"))
				count_o++;
			if(args[i].equals("-r"))
				count_r++;
		}

		/*If count > 1 throw error and terminate*/
		if(count_o > 1 || count_r > 1){
			System.out.println("ERROR");
			System.out.println("Flags repeated");
			System.exit(0);		
		}
	}

	/*Function to check number of arguments. It should be atleast 2 and a multiple of 2*/
	private void checkNumberOfArguments(String[] args) {
		int arg_count = args.length;

		if(arg_count < 2 || (arg_count%2!=0)){
			System.out.println(ERROR);
			System.out.println("Invalid number of arguments");
			System.exit(0);		
		}
	}

	/*Method simply checks if the TargetImagePath and TileLibraryDirectory have been specified in the inputs*/
	private void checkMandatoryInputs() {
		if(TargetImagePathFound == false || TileDirectoryPathFound == false){
			System.out.println(ERROR);
			System.out.println("Mandatory inputs not found");
			System.exit(0);		
		}
				
	}
	
	public String getTargetImageFilePath()
	{
		System.out.println("TargetImageFilePath\t "+targetImageFilePath);
		return targetImageFilePath;
	}

	public String getTargetImageFileName()
	{
		System.out.println("TargetImageFileName\t "+targetImageFileName);
		return targetImageFileName;
	}
	public String getTargetImageFileFormat()
	{
		System.out.println("TargetImageFileFormat\t "+targetImageFileFormat);
		return targetImageFileFormat;
	}
	public String getTargetImageFileExtenstion()
	{
		System.out.println("TargetImageFileExtenstion\t "+targetImageFileExtension);
		return targetImageFileExtension;
	}
	public String getTileDirectory()
	{
		System.out.println("TileDirectory\t"+tileDirectory);
		return tileDirectory;
	}
	public int getMaxNumberOfTiles()
	{
		System.out.println("MaxNumberOfTilesRepeated\t"+maxNumberOfTiles);
		return maxNumberOfTiles;
	}
	
	public String getOutputImageFileExtension()
	{
		System.out.println("OutputImageFileExtension\t"+outputImageFileExtension);
		return outputImageFileExtension;
	}
	
	public String getOutputImageFileFormat()
	{
		System.out.println("outputImageFileFormat\t"+outputImageFileFormat);
		return outputImageFileFormat;
	}
	public String getOutputImageFileName()
	{
		System.out.println("getOutputImageFileName\t"+outputImageFileName);
		return outputImageFileName;
	}
	public String getOutputImageFilePath()
	{
		System.out.println("getOutputImageFilePath\t"+outputImageFilePath);
		return outputImageFilePath;
	}
	
}

