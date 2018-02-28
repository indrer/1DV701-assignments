package htttpServer.response;

import java.net.Socket;

public class Response201 extends Response {

	public Response201(Socket client) {
		super(client, 201, "Created");
	}

}
