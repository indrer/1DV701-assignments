package htttpServer.response;

import java.io.File;
import java.net.Socket;

import htttpServer.util.HTMLPageGenerator;

public abstract class Response {
	protected int responseCode = 0;
	protected String responseMessage = "";
	protected ResponseGenerator generator;
	protected File resourceToReturn = null;
	protected String contentType;
	protected Socket client;
	
	public Response (Socket client, int responseCode, String responseMessage) {
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.client = client;
		
		generateContent();
	}
	
	public int getCode() {
		return responseCode;
	}
	
	public String getMessage() {
		return responseMessage;
	}
	
	/**
	 * Generates content to return. New HTML page is created displaying response code and 
	 * response message. Valid for all responses except 200, as it returns a page.
	 */
	protected void generateContent() {
		HTMLPageGenerator html = new HTMLPageGenerator(responseCode + " " + responseMessage, true);
		resourceToReturn = html.generateTempPage();
		contentType = "text/html";
	}
	
	/**
	 * Response is sent to a client.
	 */
	public void sendResponse() {
		generator = new ResponseGenerator(this.client, this, resourceToReturn, contentType, "");
		generator.sendResponse();
	}
}
