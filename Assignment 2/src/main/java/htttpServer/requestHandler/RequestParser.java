package htttpServer.requestHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import htttpServer.exceptions.ForbiddenException;

public class RequestParser {
	private String requestMessage = "";
	private ArrayList<Byte> requestBytes;
	private String[] forbidden;
	private Map<String, String> redir;
	
	public RequestParser(String message, ArrayList<Byte> bytes, String[] forbidden, Map<String, String> redir) {
		this.requestMessage = message;
		this.requestBytes = bytes;
		this.forbidden = forbidden;
		this.redir = redir;
	}
	/**
	 * @return HTTP request as a String
	 */
	public String getMessage() {
		return requestMessage;
	}
	
	/**
	 * @return type of HTTP method requested
	 */
	public String getType() {
		String[] requestArray = requestMessage.split("\r\n");
		String[] requestType = requestArray[0].split(" ");
		return requestType[0];
	}
	
	/**
	 * Finds whether requested URI is forbidden to be accessed. If not,
	 * returns URI from request message
	 * @return URI
	 * @throws Exception
	 */
	public String getFolder() throws ForbiddenException {
		String[] requestArray = requestMessage.split("\r\n");
		String[] firstLine = requestArray[0].split(" ");
		if (isForbidden(firstLine[1])) {
				throw new ForbiddenException();
		}
		
		return firstLine[1];
	}
	
	/**
	 * Private method to make sure that requested URI is not forbidden.
	 * @param folder
	 * @return
	 */
	private boolean isForbidden(String folder) {
		if (forbidden != null) {
			String forbiddenMod = "";
			String[] folderArray = folder.split("/");
			// If folder provided in URI contains more than one directory
			// we need to iterate over these directory names then.
			if (folderArray.length > 1) {
				for (int i = 0; i < folderArray.length; i++) {
					for (int j = 0; j < forbidden.length; j++) {
						// If forbidden folder and requested folder are same, 
						// it means forbidden folder is attempted to be accessed.
						if (forbidden[j].toLowerCase().replace("/", "").equals(folderArray[i].toLowerCase())) {
							return true;
						}
					}
				}
			}
			else if(folder.equals("/")) {
				return false;
			}
			else {
				String folderMod = folder.replaceAll("/", "");
				for (int i = 1; i < forbidden.length; i++) {
					forbiddenMod = forbidden[i].replaceAll("/", "");
					// If forbidden folder and requested folder are same, 
					// it means forbidden folder is attempted to be accessed.
					if (forbiddenMod.toLowerCase().equals(folderMod.toLowerCase())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public String checkRedirect(String folder) {
		if (redir != null || redir.size() > 0) {
			String redirFold = "";
			String[] userFoldArray = folder.split("/");
			// If folder provided in URI contains more than one directory
			// we need to iterate over these directory names then.
			if (userFoldArray.length > 1) {
				for (int i = 0; i	< userFoldArray.length; i++) {
					for (Iterator<String> it = redir.keySet().iterator(); it.hasNext();) {
				        String key = it.next();
				        redirFold = key.replaceAll("/", "").toLowerCase();
				        if (userFoldArray[i].toLowerCase().equals(redirFold)) {
				        	return redir.get(key);
				        }
				    }
				}
			}
			else {
				String userFold = folder.replaceAll("/", "").toLowerCase();
			    for (Iterator<String> it = redir.keySet().iterator(); it.hasNext();) {
			        String key = it.next();
			        redirFold = key.replaceAll("/", "").toLowerCase();
			        if (userFold.equals(redirFold)) {
			        	return redir.get(key);
			        }
			    }
			}
			
		}
		return "";
	}
	
	/**
	 * Takes URI, checks if only directory is provided, no file
	 * @param folder URI
	 * @return true if directory
	 */
	public boolean isDirectory(String folder) {
		String[] folderParts = folder.split("/");
		if (folderParts.length <= 1) {
			return false;
		}
		return !(folderParts[folderParts.length - 1].contains("."));
	}
	
	/**
	 * Returns content type from request header.
	 * @return content type from request header
	 */
	public String getContentType() {
		String[] requestArray = requestMessage.split("\r\n");
		String requestType = "";
		for (int i = 0; i < requestArray.length; i++) {
			if (requestArray[i].contains("Content-Type:")) {
				String[] temp = requestArray[i].split(" ");
				requestType = temp[1];
				break;
			}
		}
		return requestType;
	}
	
	/**
	 * Returns content from header as byte array
	 * @return content from header
	 * @throws Exception 
	 */
	public byte[] getContentBytes() throws Exception {
		int index = 0;	
		byte[] temp = new byte[requestBytes.size()];
		// Move bytes from ArrayList to array.
		for (int i =0; i< temp.length; i++) {
			temp[i] = requestBytes.get(i).byteValue();
		}
		// Where content begins
		String regex = "(\r\n\r\n)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(new String(temp));
		if(matcher.find()){
			// Since content begins at that specific regex, we only care what starts 
			// there.
			index = matcher.end();
			// Content starts at the same space where header is suppose to end, therefore
			// there is no content
			if(index >= temp.length-1){
				// No content
				return null;
			}
			// Otherwise content found, return it.
			else {
				return Arrays.copyOfRange(temp, index, temp.length);
			}
		}
		// Regex was not found, must be bad request
		else {
			throw new Exception ("Bad request!");
		}
	}
 
}
