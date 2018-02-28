package htttpServer.response;

import java.io.File;
import java.net.Socket;

public class Response200 extends Response {

	public Response200(Socket client, File file, String contentType) {
		super(client, 200, "OK");
		this.resourceToReturn = file;
		this.contentType = contentType;

	}
	
	@Override
	protected void generateContent() {
		
	}
	
	

}
