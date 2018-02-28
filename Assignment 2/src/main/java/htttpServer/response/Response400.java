package htttpServer.response;

import java.net.Socket;

public class Response400 extends Response{

	public Response400(Socket client) {
		super(client, 400, "Bad Request");
	}

}
