import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPEchoServer {
    public static final int BUFSIZE= 1024;
    public static final int MYPORT= 4950;
    private static int count = 1;

    public static void main(String[] args) throws IOException {

	// Create server socket
	@SuppressWarnings("resource")
	ServerSocket serverSocket = new ServerSocket(MYPORT);


	while (true) {
		// New Client connected
		Socket clientSocket = serverSocket.accept();
		// Create new thread for the new client
		Connection client = new Connection (clientSocket, count, BUFSIZE);
		count++;
		// Start the thread
		client.start();
		}
    } 
}

/*
 * This class is only specific for TCP server, no 
 * other classes need to know about it
 */
class Connection extends Thread {
	private DataInputStream input;
	private DataOutputStream output;
	private int id = 0;
	private Socket clientConnection;
	private int bufSize = 0;
	private String prefix = "";
	
	public Connection (Socket socket, int id, int bufSize) {
		this.clientConnection = socket;
		this.id = id;
		try {
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
		}
		this.bufSize = bufSize;
		prefix = "[Client " + this.id + "] ";
		System.out.println(prefix + " Connected!");
	}
	
	@Override
	public void run() {
		String receivedMessage = "";
		try {
			while (true) {
				byte[] buf = new byte[bufSize];
				int size = input.read(buf);
				if(size != -1) {
					receivedMessage = new String(buf, 0, size);
					System.out.println(prefix + "Received TCP request of " + size + " bytes long from " 
					+ clientConnection.getInetAddress().getHostAddress() 
					+ ". Echoing..");
					output.write(receivedMessage.getBytes());
					System.out.println(prefix + "Echoed " + receivedMessage.getBytes().length + " bytes.");
				}
				else {
					break;
				}
			}
			clientConnection.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(prefix + "Echo done. Disconnected!");
	}
}
