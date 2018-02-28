package htttpServer.response;

import java.net.Socket;

public class Response403 extends Response {

	public Response403(Socket client) {
		super(client, 403, "Forbidden");
	}

}
