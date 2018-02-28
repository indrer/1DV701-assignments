package htttpServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import htttpServer.config.Config;

public class WebServer {
    public static final int MYPORT = 8888;
    private static String resourceFolder = "";
    private static String[] forbidden;
    private static Map<String, String> redir;
    private static Config cfg = new Config("./src/main/resources/");

    public static void main(String[] args) throws IOException {

    if (args.length != 1) {
    	if (cfg.getResourceFolder().isEmpty()) {
	    	System.err.println("Make sure you provide resource directory in the parameters or the config file!");
	    	System.exit(0);
    	}
		System.out.println("[SERVER] Getting resource folder from config.");
    	resourceFolder = cfg.getResourceFolder();
    } else {
    	resourceFolder = args[0];
    }
    if (cfg.getForbidden() != null) {
		System.out.println("[SERVER] Getting forbidden folders from config.");
    	forbidden = cfg.getForbidden();
    }
    
    if (cfg.getRedir() != null) {
    	redir = cfg.getRedir();
    	System.out.println("[SERVER] Getting redirected locations.");
    }
    
	// Create server socket
	@SuppressWarnings("resource")
	ServerSocket serverSocket = new ServerSocket(MYPORT);


	while (true) {
		// New Client connected
		Socket clientSocket = serverSocket.accept();
		// Create new thread for the new client
		clientSocket.setKeepAlive(true);
		Connection client = new Connection (clientSocket, resourceFolder, forbidden, redir);
		// Start the thread
		client.start();
		}
    } 
}
