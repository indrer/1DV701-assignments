package htttpServer.response;

import java.net.Socket;

public class Response501 extends Response {

	public Response501(Socket client) {
		super(client, 501, "Not Implemented");
	}

}
