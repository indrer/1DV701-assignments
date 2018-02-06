import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
	
	public static final int CLIENTS = 1;

	public static void main(String[] args) {

		// To test with multiple clients. CLIENTS variable changes how many
		// clients should be sending messages to the server
		ExecutorService exec = Executors.newFixedThreadPool(CLIENTS); 	
		for(int i = 0; i < CLIENTS; i++){ 
			exec.execute(new ClientThread(args, i+1));
		}
		exec.shutdown();
	}

}

class ClientThread implements Runnable {
	private int id = 0;
	private String[] args = null;
	public ClientThread(String[] args, int id){
		this.id = id;
		this.args = args;
	}
	
	@Override
	public void run(){
		NetworkingLayer clientThread = new UDPEchoClient(args, id); 
		clientThread.startClient();
	}
}
