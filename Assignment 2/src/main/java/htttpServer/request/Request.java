package htttpServer.request;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import htttpServer.response.Response;

public abstract class Request {
	protected Response response = null;
	protected String resourceFolder = null;
	protected String requestedFile = null;
	
	/**
	 * Responsible for generating response. This method should contain all the 
	 * error checks for specific HTTP method, example, this method for GET HTTP 
	 * method should check if requested directory or file exists and return 
	 * Response404 if it doesn't.
	 * @return
	 */
	public Response generateResponse() {
		return null;
	}
	
	/**
	 * Checks if user is attempting to access root folder
	 * @param requestedFile a string from the request sent from the client, containing address to a file/folder
	 * @param response response object, created in response classes
	 * @return true if user is attempting to access root folder
	 */
	protected boolean isRootFolderAccessed(String requestedFile) {
		if (requestedFile.contains("..")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Takes the requested file (or dir) path, split it by "/" characters
	 * and puts it in array, so (example: /image/space/s.png):
	 * ["image", "space", "s.png"]
	 * Then takes the last element of the array and iterates through the
	 * name of it from the end, towards the start of the name. It stops
	 * when . is reached, returning a file type as a result.
	 * @return
	 * @throws Exception 
	 */
	protected String getFileType(String folder) throws Exception {
		// Assuming whoever is using this method doesn't check if request URI
		// is a directory, attempt to check and throw exception if it is
		if(!isDirectory(folder)) {
			// Create an array of request URI removing "/" characters,
			// then save last element of created array as variable "file".
			String[] requestedFileArray = folder.split("/");
			String file = requestedFileArray[requestedFileArray.length -1];
			String fileTypeString = "";
			// Simply iterate through file name until dot (".") is reached to get 
			// file type.
			for (int i = file.length() -1; i > 0; i--) {
				if (file.charAt(i) != '.'){
					fileTypeString = file.charAt(i) + fileTypeString;
				}
				else {
					break;
				}
			}
			return fileTypeString;
		}
		else {
			throw new Exception("This is a folder!");
		}
	}
	
	/**
	 * Writes a file from provided bytes of content, and to provided location.
	 * @param requestPath path of a folder inside a resource folder
	 * @param content byte array
	 * @param contentType type of content (example, image/png)
	 * @throws FileNotFoundException When provided file is a directory rather than a file
	 * @throws Exception When server doesn't support that kind of content
	 */
	protected void saveFile(String requestPath, byte[] content, String contentType) throws FileNotFoundException, Exception {
		FileOutputStream output;
		output = new FileOutputStream(resourceFolder 
				+ getDirectory(requestPath) 
				+ getFileName(requestPath) + "."
				+ getContent(contentType));
		output.write(content);
		output.close();
	}
	
	/**
	 * Return a directory extracted from a string containing path (inside a resource folder)
	 * and requested file name.
	 * @param requestPath URI of the request
	 * @return directory of the request
	 */
	protected String getDirectory (String requestPath) {
		if (!requestPath.contains(".")) {
			return requestPath;
		}
		else {
			int index = 0;
			for (int i = requestPath.length() - 1; i > 0; i--) {
				if (requestPath.charAt(i) == '/') {
					index = i;
					break;
				}
			}
			return requestPath.substring(0, index+1);
		}
	}
	
	/**
	 * Gets file name from URI.
	 * @param requestPath request URI
	 * @return file name without type (example, images/test.png -> test)
	 */
	protected String getFileName(String requestPath) {
		//URI didn't contain a dot ("."), therefore it doesn't contain file name.
		if (!requestPath.contains(".")) {
			return "file";
		}
		else {
			String name = "";
			for (int i = requestPath.indexOf(".")-1; i >0; i--) {
				//Iterate over URI from the dot (".") until character "/" is reached
				if (requestPath.charAt(i) == '/') {
					break;
				}
				name = requestPath.charAt(i) + name;
			}
			return name;
		}
	}
	/**
	 * Checks if URI is a directory
	 * @param folder URI from the request
	 * @return true if it is a directory
	 */
	protected  boolean isDirectory(String folder) {
		String[] folderParts = folder.split("/");
		if (folderParts.length == 0) {
			return true;
		}
		return !(folderParts[folderParts.length - 1].contains("."));
	}
	
	/**
	 * Checks if provided file type is in content types 
	 * that the server is capable of accepting and sending.
	 * @param fileType file type to be checked
	 * @return
	 */
	protected String getContentType(String fileType) {
		for (ContentType s : ContentType.values()) {
			if (s.toString().equals(fileType)) {
				return s.getType();
			}
		}
		return "";
	}
	
	/**
	 * Returns a content (example, png) if content type (example images/png) is provided
	 * @param contentType
	 * @return 
	 * @throws Exception when server doesn't support certain content type
	 */
	protected String getContent (String contentType) throws Exception {
		for (ContentType s : ContentType.values()){
			if (s.getType().equals(contentType)) {
				return s.toString();
			}
		}
		throw new Exception("Didn't match any content type");
	}
	
	/**
	 * Enum to list which files the server
	 * can accept and return.
	 */
	protected enum ContentType{
		mp4("video/mp4"),
		xml("text/xml"),
		txt("text/plain"),
		html("text/html"),
		htm("text/html"),
		gif("image/gif"),
		bmp("image/bmp"),
		png("image/png"),
		jpeg("image/jpeg"),
		jpg("image/jpeg");
		
		private String type = "";
		
		ContentType (String type) {
			this.type =  type;
		}
		
		public String getType() {
			return this.type;
		}
	}
	
}
