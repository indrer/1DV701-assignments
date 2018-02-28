package htttpServer.response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ResponseGenerator {
	
	private Socket clientSocket = null;
	private File contentToReturn = null;
	private Response response = null;
	private String contentType = "";
	private String location = "";
	
	
	public ResponseGenerator (Socket clientSocket, Response response, File content, String contentType, String location) {
		this.clientSocket = clientSocket;
		this.contentToReturn = content;
		this.response = response;
		this.contentType = contentType;
		this.location = location;
		
	}
	
	public void sendResponse() {
		sendHeader();
		sendContent();
	}
	
	/**
	 * Creates a header to send, then sends it. It is assumed that server is ran
	 * in GMT+1 time zone.
	 */
	private void sendHeader() {
		String headerMessage;
		if (location == null || location.isEmpty()) {
		headerMessage = "HTTP/1.1 " + response.getCode() + " " + response.getMessage() + "\r\n"
					+ "Date: " + getCurrentLocalDateTimeStamp() + " GMT+1 \r\n" 
					+ "Content-Length: " + contentToReturn.length() + "\r\n"
					+ "Content-Type: " + contentType + "\r\n"
					+ "\r\n";
		}
		else {
			headerMessage = "HTTP/1.1 " + response.getCode() + " " + response.getMessage() + "\r\n"
					+ "Location: " + location + "\r\n"
					+ "Date: " + getCurrentLocalDateTimeStamp() + " GMT+1 \r\n" 
					//+ "Content-Length: " + contentToReturn.length() + "\r\n"
					+ "Content-Type: " + contentType + "\r\n"
					+ "\r\n";
		}
		
		try {
			clientSocket.getOutputStream().write(headerMessage.getBytes());
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	/**
	 * Gets content byte array, sends it to the client.
	 */
	private void sendContent() {
		byte[] fileArray = new byte[(int) contentToReturn.length()];
		try {
			FileInputStream in = new FileInputStream(contentToReturn);
			int readInput = 0;
			while ((readInput = in.read(fileArray)) != -1) {
				clientSocket.getOutputStream().write(fileArray, 0, readInput);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper method to get current date and time.
	 * @return current date and time
	 */
	private String getCurrentLocalDateTimeStamp() {
	    return LocalDateTime.now()
	       .format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy, HH:mm:ss"));
	}

}
