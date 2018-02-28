package htttpServer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import htttpServer.requestHandler.RequestHandler;
import htttpServer.requestHandler.RequestParser;

class Connection extends Thread {
	private final int bufSize = 9999;
	private DataInputStream input;
	private Socket clientConnection;
	private RequestHandler requestHandler;
	private String resourceFolder = "";
	private RequestParser parser;
	private String[] forbidden;
	private Map<String, String> redir;
	
	public Connection (Socket socket, String resourceFolder, String[] forbidden, Map<String, String> redir)  {
		this.clientConnection = socket;
		this.resourceFolder = resourceFolder;
		this.forbidden = forbidden;
		this.redir = redir;
	}
	
	@Override
	public void run() {
		String receivedMessage = "";
		// This stores the entire request received from the client.
		// Using ArrayList because unknown size.
		ArrayList<Byte> storage = new ArrayList<Byte>();
		boolean inputReceived = false;
		while(true) { 
			try {
				byte[] buf = new byte[bufSize];
				input = new DataInputStream(clientConnection.getInputStream());
				// Reads input while there is any available.
				while (input.available() > 0)
				{
					int read = input.read(buf);
					// Stores received request in a string as well as in 
					// Byte ArrayList
					receivedMessage = receivedMessage + new String(buf, 0, read);
					for (int i = 0; i < read; i++) {
						storage.add(buf[i]);
					}
					inputReceived = true;
				}
				if (inputReceived) {
					// Parser divides request into header (and it's parts, such as method, URI and so on)
					// and content. RequestHandler decides which method was called and calls
					// response classes to create a response.
					parser = new RequestParser(receivedMessage, storage, forbidden, redir);
					requestHandler = new RequestHandler(resourceFolder, clientConnection, parser);
					requestHandler.processRequest().sendResponse();
					// Since we are keeping connection open, we need to reset some variables after 
					// sending response is done.
					inputReceived = false;
					receivedMessage = "";
					storage.clear();
				}
				
			} catch (IOException e1) {
				try {
					input.close();
				} catch (IOException e) {
				}
				break;
			}
		}
	}
}
