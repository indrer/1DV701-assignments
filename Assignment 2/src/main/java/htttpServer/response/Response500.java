package htttpServer.response;

import java.net.Socket;

public class Response500 extends Response {

	public Response500(Socket client) {
		super(client, 500, "Internal Server Error");
	}

}
