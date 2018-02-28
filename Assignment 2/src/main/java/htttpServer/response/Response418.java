package htttpServer.response;

import java.net.Socket;

public class Response418 extends Response{

	public Response418(Socket client) {
		super(client, 418, "I'm a teapot");
	}

}
