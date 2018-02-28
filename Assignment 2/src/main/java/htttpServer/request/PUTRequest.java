package htttpServer.request;

import java.io.File;
import java.net.Socket;

import htttpServer.response.Response;
import htttpServer.response.Response200;
import htttpServer.response.Response400;
import htttpServer.response.Response403;
import htttpServer.response.Response404;
import htttpServer.response.Response415;
import htttpServer.response.Response418;
import htttpServer.util.HTMLPageGenerator;

public class PUTRequest extends Request {
	private String requestPath;
	private byte[] content;
	private Socket client;
	private String contentType;
	
	public PUTRequest(String requestPath, String resourceFolder, byte[] content, Socket client, String contentType) {
		this.requestPath = requestPath;
		this.content = content;
		this.client = client;
		this.resourceFolder = resourceFolder;
		this.contentType = contentType;
	}

	public Response generateResponse() {
		// First of all, if no content provided, let client know
		// by responding with 418. 4xx in general indicates client issues.
		// Not providing a content is a client issue. As a joke I included 418, which 
		// has response message "I'm a tea pot". Obviously if this was serious
		// web server, it would return 400 Bad Request. It sound never get to this point, however.
		if (content == null) {
			return new Response418(client);
		}
		String directory = getDirectory(requestPath);
		// Again, not providing content in PUT request is possible,
		// therefore URI would contain on only "/", so it needs to be checked.
		if (!requestPath.equals("/")) {
			try {
				contentType = getContentType(getFileType(requestPath));
			} catch (Exception e) {
				return new Response400(client);
			}
		}
		// Create a File object that will be used to check whether 
		// this file already exists in provided directory. Catches exception when 
		// provided content type is not supported by the server.
		File file;
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
		// Check if attempted to access root folder
		if (isRootFolderAccessed(requestPath)) {
			return new Response403(client);
		}
		// Check if directory doesn't exist
		if (!dir.exists()) {
			return new Response404(client);
		}
		// Check if there is no file to update
		if (!file.exists()) {
			return new Response404(client);
		}
		// Delete old file and create new file.
		file.delete();
		try {
			saveFile(requestPath, content, contentType);
		} catch (Exception e) {
			return new Response415(client);
		}
		// Successful creation, return response code 200, let server administrator
		// know that file was updated
		System.out.println("[SERVER] " 
				+ client.getInetAddress().getHostAddress() + " updated file \"" 
				+ getFileName(requestPath) + "\" in directory " + getDirectory(requestPath));
		HTMLPageGenerator html = new HTMLPageGenerator("File has been updated.", true);
		return new Response200(client, html.generateTempPage(), getContentType("html"));
	}

}
