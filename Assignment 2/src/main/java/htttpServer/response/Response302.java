package htttpServer.response;

import java.io.File;
import java.net.Socket;

public class Response302 extends Response{
	
	private File location;
	private String contentType;
	private String newLocation;

	public Response302(Socket client, String newLocation, File location, String contentType) {
		super(client, 302, "Found");
		this.location = location;
		this.contentType = contentType;
		this.newLocation = newLocation;
	}
	
	@Override
	protected void generateContent() {
		
	}
	
	@Override
	public void sendResponse() {
		generator = new ResponseGenerator(this.client, this, location, contentType, newLocation);
		generator.sendResponse();
	}

}
