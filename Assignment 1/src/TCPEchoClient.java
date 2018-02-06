import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPEchoClient extends NetworkingLayer {
	
	private byte[] buf;
	private DataInputStream input;
	private DataOutputStream output;
	private Socket socket;

	public TCPEchoClient(String[] args, int id) {
		super(args, id);
	}
	
	// Set up a socket for the client
	public void setUpClient() {
		try {
			socket = new Socket(IP, port);
		} catch (IOException e) {
			System.err.println("No route or host found. Exiting..");
			System.exit(0);
		}
		System.out.println(prefix + "Connected!");
	}
	
	// Close the socket
	public void finishConnection() {
		try {
			socket.close();
		} catch (IOException e) {
			System.out.println(prefix + "Could not close connection.");
		}
		System.out.println(prefix + "Disconnected!");
	}
	
	// Send a single message
	public void sendMessage() {
		// Variable to store the message
		String receivedMessage = "";
		try {
			// Set up buffer
			buf = new byte[bufSize];
			// Set up input and output streams
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
			
			// Send a message
			output.write(message.getBytes());
			while (receivedMessage.length() < message.length()) {
				// Get a message, store the size to be able to create a string
				int size = input.read(buf);
				// Create a string of received bytes
				receivedMessage = receivedMessage + new String(buf, 0, size);
			}
		} catch (IOException e) {
			System.err.println(prefix + "Something went wrong when sending or receiving the message");
			e.printStackTrace();
		}
		
		// Check if received message is the same as sent one
		if (checkReceivedMessage(receivedMessage)) {
			System.out.println(prefix + "Message sent: '" + message + "', message received: '" 
			+ receivedMessage + "' Messages are the same!");
		}
		// Received message was different than the one that was sent
		else {
			System.err.println(prefix + "Message received is not the same as the one sent!");
		}
	}
	
	// A method to check if received message is the same as sent one
	private boolean checkReceivedMessage(String receivedMessage) {
		System.out.println(prefix + "Comparing original: " + message.length() + " with new: " + receivedMessage.length());
		return message.equals(receivedMessage);
    }

}
