import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPEchoClient extends NetworkingLayer {
	private byte[] buf;
	private DatagramPacket sendPacket;
	private DatagramPacket receivePacket;
	private DatagramSocket socket;
	
    public UDPEchoClient(String[] args, int id) {
		super(args, id);
	}
    
    // Set up datagram socket, check if message is correct
    // length for a UDP packet.
    public void setUpClient() {
    	try {
			socket = new DatagramSocket(null);
			socket.bind(localBindPoint);
		} catch (SocketException e) {
			System.err.println(prefix + "Socket failed to be created. Exiting...");
			System.exit(0);
		}
    	// Checking if it's possible to connect to the server. Client should
    	// close if not.
    	try (Socket testSocket = new Socket(IP, port)) {
    		
    	} catch (UnknownHostException e) {
			System.err.println("Cannot find host. Exiting...");
			System.exit(0);
		} catch (IOException e) {
			System.err.println("No connection to the server. Exiting...");
			System.exit(0);
		}
    	System.out.println(prefix + "Connected!");
    }
    
    // After message sending is done, disconnect
    public void finishConnection() {
    	socket.close();
    }
    
    // Send message method
    public void sendMessage() {
		try {
			// Set up buffer for received message
			buf = new byte[bufSize];
			// Set up packets for receiving and for sending data
			sendPacket = new DatagramPacket(message.getBytes(), message.length(), remoteBindPoint);
			receivePacket = new DatagramPacket(buf, buf.length);
			// Send message
			socket.send(sendPacket);
			// Receive message
			socket.receive(receivePacket);
		} catch (SocketException e) {
		} catch (IOException e) {
		}
		// Message received was the same, print out information
		if(isSameMessage(receivePacket)) {
			System.out.println(prefix + "Message sent: '" + message + "', message received: '" 
			+ new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength()) 
			+ "'. Messages are same!");
		}
		// Messaged received differs, let user know
		else {
			System.err.println(prefix + "Message received is not the same as the one sent!");
		}
    }
    
    // Check if received message is the same as the sent one
    private boolean isSameMessage(DatagramPacket packet) {
		String receivedString = new String(packet.getData(), packet.getOffset(),
			       packet.getLength());
		return receivedString.compareTo(message) == 0;
    }
}