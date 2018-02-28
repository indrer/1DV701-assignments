package htttpServer.request;

import java.io.File;
import java.net.Socket;

import htttpServer.response.Response;
import htttpServer.response.Response200;
import htttpServer.response.Response302;
import htttpServer.response.Response403;
import htttpServer.response.Response404;
import htttpServer.response.Response500;
import htttpServer.util.HTMLPageGenerator;

public class GETRequest extends Request {
	private Socket clientSocket = null;
	private boolean dash = false;
	private boolean redirected = false;
	
	public GETRequest(String file, String resourceFolder, Socket connection, boolean redirected) {
		this.requestedFile = file;
		this.resourceFolder = resourceFolder;
		this.clientSocket = connection;
		if (file.endsWith("/")) {
			this.dash = true;
		}
		this.redirected = redirected;
	}

	public Response generateResponse() {
		File dir = new File (resourceFolder + requestedFile);
		// Check if attempted to access root dir
		if (isRootFolderAccessed(requestedFile)) {
			return new Response403(clientSocket);
		}
		// Check if accessing a directory
		if (dir.exists() && isDirectory(requestedFile) ) {
			// Directory contained index.html or index.htm file
			if (directoryContainsIndex(dir)) {
				// Checking if redirected directory and contains index.htm(l)
				if (redirected) {
					return new Response302(clientSocket, requestedFile, new File(dir + getExistingIndex(dir)), getContentType("html"));
				}
				return new Response200(clientSocket, new File(dir + getExistingIndex(dir)), getContentType("html"));
			}
			// Directory didn't contain an index file, printing out existing files
			else {
				// Redirected didn't contain any index files, present with list of files
				if (redirected) {
					HTMLPageGenerator html = new HTMLPageGenerator(dir, dash);
					return new Response302(clientSocket, requestedFile, html.generateTempPage(), getContentType("html"));
				}
				HTMLPageGenerator html = new HTMLPageGenerator(dir, dash);
				return new Response200(clientSocket, html.generateTempPage(), getContentType("html"));
			}
		}
		
		// If request contains "/" only
		if (requestedFile.equals("/")) {
			File resourceDir = new File(resourceFolder);
			if (directoryContainsIndex(resourceDir) ) {
				return new Response200(clientSocket, 
						new File(resourceFolder + getExistingIndex(resourceDir)), 
						getContentType("html"));
			}
			else {
				HTMLPageGenerator html = new HTMLPageGenerator(resourceDir, dash);
				return new Response200(clientSocket, html.generateTempPage(), getContentType("html"));
			}
		}
		// A request file exists in correct directory
		if (dir.exists() && !isDirectory(requestedFile)) {
			try {
				if (redirected) {
					return new Response302(clientSocket, requestedFile, dir, getContentType(getFileType(requestedFile)));
				}
				return new Response200(clientSocket, dir, getContentType(getFileType(requestedFile)));
			} catch (Exception e) {
				System.out.println("GET REQUEST EXCEPTION");
				return new Response500(clientSocket);
			}
		}
		return new Response404(clientSocket);	
	}
	/**
	 * Checks if directory contains index.html or 
	 * index.htm file.
	 * @param dir directory that is checked.
	 * @return
	 */
	private boolean directoryContainsIndex(File dir) {
		for (String f : dir.list()) {
			if (f.equals("index.html") || f.equals("index.htm")) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns full file name of an index file in a directory.
	 * Makes it possible for .html and htm pages to be seen.
	 * @param dir directory where the index file is located.
	 * @return
	 */
	private String getExistingIndex(File dir) {
		String index = "";
		for (String f : dir.list()) {
			if (f.equals("index.html") || f.equals("index.htm")) {
				index = "/" + f;
				break;
			}
		}
		return index;
	}
}
