/*
  UDPEchoServer.java
  A simple echo server with no error handling
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class UDPEchoServer {
    public static final int BUFSIZE= 1024;
    public static final int MYPORT= 4950;

    public static void main(String[] args) throws IOException {
	byte[] buf= new byte[BUFSIZE];

	/* Create socket */
	@SuppressWarnings("resource")
	DatagramSocket socket= new DatagramSocket(null);

	/* Create local bind point */
	SocketAddress localBindPoint = new InetSocketAddress(MYPORT);
	socket.bind(localBindPoint);
	
	// It is not specified that UDP server has to support multiple
	// Connections like TCP does, therefore it doesn't.
	while (true) {
	    /* Create datagram packet for receiving message */
	    DatagramPacket receivePacket= new DatagramPacket(buf, buf.length);

	    /* Receiving message */
	    socket.receive(receivePacket);
		System.out.println("Received UDP request of " + new String(receivePacket.getData()).trim().length() + " bytes long from " 
		+ receivePacket.getAddress().getHostAddress() 
		+ ". Echoing..");

	    /* Create datagram packet for sending message */
	    DatagramPacket sendPacket = new DatagramPacket(receivePacket.getData(),
				   receivePacket.getLength(),
				   receivePacket.getAddress(),
				   receivePacket.getPort());

	    /* Send message*/
	    socket.send(sendPacket);
		System.out.println("Echoed " + new String(sendPacket.getData()).trim().length() + " bytes.");
		}
    } 
}