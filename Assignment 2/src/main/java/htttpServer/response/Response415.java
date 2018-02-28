package htttpServer.response;

import java.net.Socket;

public class Response415 extends Response {

	public Response415(Socket client) {
		super(client, 415, "Unsupported Media Type");
	}

}
