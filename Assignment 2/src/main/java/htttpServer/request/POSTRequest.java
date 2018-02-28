package htttpServer.request;

import java.io.File;
import java.net.Socket;

import htttpServer.response.Response;
import htttpServer.response.Response201;
import htttpServer.response.Response400;
import htttpServer.response.Response403;
import htttpServer.response.Response404;
import htttpServer.response.Response409;
import htttpServer.response.Response415;

public class POSTRequest extends Request {
	private String requestPath;
	private byte[] content;
	private Socket client;
	private String contentType;
	
	public POSTRequest(String requestPath, String resourceFolder, byte[] content, Socket client, String contentType) {
		this.requestPath = requestPath;
		this.content = content;
		this.client = client;
		this.resourceFolder = resourceFolder;
		this.contentType = contentType;
	}
	
	public Response generateResponse() {
		// After testing with insomnia tool, I noticed that if POST or PUT
		// methods can be called not containing file name or type in URI,
		// so I decided that my web server will not allow such requests, and 
		// send 400 (Bad Request) response.
		if (!requestPath.equals("/")) {
			// Set content type if URI in request was correct
			try {
				contentType = getContentType(getFileType(requestPath));
			} catch (Exception e) {
				return new Response400(client);
			}
		}
		String directory = getDirectory(requestPath);
		File file;
		// Create a File object that will be used to check whether 
		// this file already exists in provided directory. Catches exception when 
		// provided content type is not supported by the server.
		try {
			file = new File(resourceFolder 
					+ directory 
					+ getFileName(requestPath) + "."
					+ getContent(contentType));
		} catch (Exception e1) {
			return new Response415(client);
		}
		// File object to check if provided directory exists
		File dir = new File(resourceFolder + directory);
		// Check if any content was provided
		if (content.length == 0 || content == null) {
			return new Response400(client);
		}
		// Check if attempted to access root folder
		if (isRootFolderAccessed(requestPath)) {
			return new Response403(client);
		}
		// Check if directory exists where file is suppose to be posted
		if (!dir.exists()) {
			return new Response404(client);
		}
		// Check if file already exists. If it does, do not allow POST.
		// PUT should be used in this case to update the file.
		if (file.exists()) {
			return new Response409(client);
		}
		// Attempt to save a file. Again, possible unsupported media
		// response in this case.
		try {
			saveFile(requestPath, content, contentType);
		} catch (Exception e) {
			return new Response415(client);
		}
		// Saving file was successful. Server console informs administrator
		// that a new file was posted and client is presented with 201 response.
		System.out.println("[SERVER] " 
				+ client.getInetAddress().getHostAddress() + " posted a file \"" 
				+ getFileName(requestPath) + "\" in directory " + getDirectory(requestPath));
		return new Response201(client);
		
	}

}
