package htttpServer.response;

import java.net.Socket;

public class Response409 extends Response{

	public Response409(Socket client) {
		super(client, 409, "Conflict");
	}

}
