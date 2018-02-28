package htttpServer.requestHandler;

import java.net.Socket;

import htttpServer.exceptions.ForbiddenException;
import htttpServer.request.GETRequest;
import htttpServer.request.POSTRequest;
import htttpServer.request.PUTRequest;
import htttpServer.request.Request;
import htttpServer.response.Response;
import htttpServer.response.Response400;
import htttpServer.response.Response403;
import htttpServer.response.Response500;
import htttpServer.response.Response501;

public class RequestHandler {
	private Request request = null;
	private String resourceFolder = "";
	private Socket connection = null;
	private RequestParser parser;
	
	public RequestHandler(String resourceFolder, Socket connection, RequestParser parser) {
		this.resourceFolder = resourceFolder;
		this.connection = connection;
		this.parser = parser;
	}
	
	/**
	 * Method that decides which HTTP method is requested. Returns whatever
	 * response is generated.
	 * @return
	 */
	public Response processRequest() {
		boolean redirected = false;
		if (parser.getType().equals("GET")) {
			try {
				String URI = parser.getFolder();
				// Check if any folder needs to be redirected
				if (parser.checkRedirect(parser.getFolder()).length() != 0) {
					redirected = true;
					URI = parser.checkRedirect(parser.getFolder());
				}
				request = new GETRequest(URI, resourceFolder, connection, redirected);
			} catch (ForbiddenException e) {
				return new Response403(connection);
			}
			return request.generateResponse();
		}
		else if(parser.getType().equals("POST")) {
			try {
				request = new POSTRequest(parser.getFolder(), resourceFolder, parser.getContentBytes(), connection, parser.getContentType());
			}catch (ForbiddenException e) {
				return new Response403(connection);
			}catch (Exception e) {
				return new Response400(connection);
			} 
			return request.generateResponse();
		}
		else if (parser.getType().equals("PUT")) {
			try {
				request = new PUTRequest(parser.getFolder(), resourceFolder, parser.getContentBytes(), connection, parser.getContentType());
			}catch (ForbiddenException e) {
				return new Response403(connection);
			}catch (Exception e) {
				return new Response400(connection);
			} 
			return request.generateResponse();
		}
		else{

			switch (parser.getType()) {
				// These methods are not implemented, therefore return 501
				case "DELETE": case "HEAD": case "TRACE": case "OPTIONS":
				case "CONNECT" : case "PATCH":
					return new Response501(connection);
				// No method provided, wrong request
				case "":
					return new Response400(connection);
				// Requested method is not recognized, return 500
				default:
					return new Response500(connection);	
			}	
		}
	}
}
