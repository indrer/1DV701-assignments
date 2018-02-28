package htttpServer.response;

import java.net.Socket;

public class Response404 extends Response{

	public Response404(Socket client) {
		super(client, 404, "Not Found");
	}

}
