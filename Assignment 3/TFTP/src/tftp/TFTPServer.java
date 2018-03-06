package tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TFTPServer 
{
	public static final int TFTPPORT = 4970;
	public static final int BUFSIZE = 516;
	public static final int MAX_READ_BYTES = BUFSIZE - 4;
	public static final String READDIR = "/read/"; //custom address at your PC
	public static final String WRITEDIR = "/write/"; //custom address at your PC
	// OP codes
	public static final int OP_RRQ = 1;
	public static final int OP_WRQ = 2;
	public static final int OP_DAT = 3;
	public static final int OP_ACK = 4;
	public static final int OP_ERR = 5;

	public static void main(String[] args) {
		if (args.length > 0) 
		{
			System.err.printf("usage: java %s\n", TFTPServer.class.getCanonicalName());
			System.exit(1);
		}
		
		//Starting the server
		try 
		{
			TFTPServer server= new TFTPServer();
			server.start();
		}
		catch (SocketException e) 
			{e.printStackTrace();}
	}
	
	private void start() throws SocketException 
	{
		byte[] buf= new byte[BUFSIZE];
		
		// Create socket
		DatagramSocket socket= new DatagramSocket(null);
		
		// Create local bind point 
		SocketAddress localBindPoint= new InetSocketAddress(TFTPPORT);
		socket.bind(localBindPoint);

		System.out.printf("Listening at port %d for new requests\n", TFTPPORT);

		// Loop to handle client requests 
		while (true) 
		{        
			
			final InetSocketAddress clientAddress = receiveFrom(socket, buf);
			
			// If clientAddress is null, an error occurred in receiveFrom()
			if (clientAddress == null) 
				continue;

			final StringBuffer requestedFile= new StringBuffer();
			final int reqtype = ParseRQ(buf, requestedFile);

			new Thread() 
			{
				public void run() 
				{
					try 
					{
						DatagramSocket sendSocket= new DatagramSocket(0);

						// Connect to client
						sendSocket.connect(clientAddress);						
						
						System.out.printf("%s request from %s using port %d\n",
								(reqtype == OP_RRQ)?"Read":"Write",
								clientAddress.getHostName(), clientAddress.getPort());  
								
						// Read request
						if (reqtype == OP_RRQ) 
						{      
							requestedFile.insert(0, READDIR);
							HandleRQ(sendSocket, requestedFile.toString(), OP_RRQ);
						}
						// Write request
						else 
						{                       
							requestedFile.insert(0, WRITEDIR);
							HandleRQ(sendSocket,requestedFile.toString(),OP_WRQ);  
						}
						// IETF document is suggesting to delay before closing
						Thread.sleep(300);
						sendSocket.close();
					} 
					catch (SocketException e){
						e.printStackTrace();
					} 
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
	
	/**
	 * Reads the first block of data, i.e., the request for an action (read or write).
	 * @param socket (socket to read from)
	 * @param buf (where to store the read data)
	 * @return socketAddress (the socket address of the client)
	 */
	private InetSocketAddress receiveFrom(DatagramSocket socket, byte[] buf) 
	{
		// Create datagram packet
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

        // Receive packet
		try {
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Get client address and port from the packet
		InetSocketAddress socketAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
		
		return socketAddress;
	}

	/**
	 * Parses the request in buf to retrieve the type of request and requestedFile
	 * 
	 * @param buf (received request)
	 * @param requestedFile (name of file to read/write)
	 * @return opcode (request type: RRQ or WRQ)
	 */
	private int ParseRQ(byte[] buf, StringBuffer requestedFile) 
	{
		ByteBuffer wrap= ByteBuffer.wrap(buf);
		//Get code
		short opcode = wrap.getShort();
		//Add content to the StringBuffer
		requestedFile.append(new String(buf, 2, buf.length-2)); 
		
		return opcode;
	}

	/**
	 * Handles RRQ and WRQ requests 
	 * 
	 * @param sendSocket (socket used to send/receive packets)
	 * @param requestedFile (name of file to read/write)
	 * @param opcode (RRQ or WRQ)
	 */
	private void HandleRQ(DatagramSocket sendSocket, String requestedFile, int opcode) 
	{		
		String[] request = requestedFile.split("\0");
		String fileName = request[0];
		String mode = "";
		
		// Linux client seems to not include the mode if it is octet. In fact, Linux
		// client's request is extremely short, containing only the file name and
		// string end character.
		if (request.length > 1) {
			mode = request[1];
		}
		else {
			mode = "octet";
		}
		
		// Only support for octet mode
		if (mode.equals("octet")) {
			// Client sends Read ReQuest
			if(opcode == OP_RRQ)
			{
				send_DATA_receive_ACK(sendSocket, fileName);
			}
			else if (opcode == OP_WRQ) 
			{
				receive_DATA_send_ACK(sendSocket, fileName);
			}
			else 
			{
				System.err.println("Invalid request or client is dead. Sending an error packet.");
				send_ERR(sendSocket, (short) 0, "Invalid request");
				return;
			}
		} else {
			System.err.println("Unsupported mode. Sending error packet.");
			send_ERR(sendSocket, (short) 0, "Unsupported mode");
			return;
		}
	}

	/**
	 * Handles RRQ by sending data to client and receiving ACK
	 * @param socket socket to send and receive messages over
	 * @param requestedFile file that was requested by a client
	 */
	private void send_DATA_receive_ACK(DatagramSocket socket, String requestedFile){
		
		// Check if file exists before we start anything
		File file = new File (requestedFile);
		if (!file.exists()) {
			send_ERR(socket, (short) 1, "File not found");
			System.err.println(file.getAbsolutePath() + " not found");
			return;
		}
		// Check file permissions
		if (!file.canRead()) {
			send_ERR(socket, (short) 2, "Access violation");
			System.err.println(file.getAbsolutePath() + " is forbidden");
			return;
		}
		// Check if file is a directory
		if (file.isDirectory()) {
			send_ERR(socket, (short) 0, "Please select file, not directory");
			System.err.println("Only files allowed");
			return;
		}
		
		// Sending file to the client
		try {
			FileInputStream read = new FileInputStream(file);
			short block = 1;
			int readByte = 512;
			int errorCount = 0;
			boolean finishedOnError = false;
			// If last readByte was less than 512, it means it's the end of the message, so 
			// loop has to stop. Also boolean finishedOnError is set to true when
			// receiving ACK was unsuccessful in some way (timeout or bad block constantly).
			while (!(readByte < 512) && !finishedOnError) {
				// Temporary buffer to read data from requested file.
				byte[] buffer = new byte[MAX_READ_BYTES];
				readByte = read.read(buffer);
				// Remove empty bytes
				buffer = removeWhiteSpace(buffer);
				// Send data to the client
				sendData(socket, buffer, block);
				// Attempt to receive ACK
				if (receiveACK(socket, block)) {
					// ACK received, increase block count
					block++;
				}
				//Returned false, so either wrong block or timeout, retransmit
				else {
					// Loop that exists when either enough times attempted to 
					// retransmit or ACK received successfully
					while (true) {
						sendData(socket, buffer, block);
						if (receiveACK(socket, block)) {
							block++;
							break;
						}
						errorCount ++;
						// After 5 attempts, stop trying, give up
						if (errorCount == 5) {
							finishedOnError = true;
							break;
						}
					}
				}
			}
			read.close();
			// Let server know that sending data and receiving ACK was not successful
			if (finishedOnError) {
				System.err.println("Connection with client timed out or something went wrong");
			}
			
		} catch (FileNotFoundException e) {
			send_ERR(socket, (short) 1, "File not found");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends file to client.
	 * @param socket socket to send the file over
	 * @param buf array that has bytes of the file
	 * @param block which part of the file (block) is being sent
	 */
	private void sendData (DatagramSocket socket, byte[] buf, short block) {
		// Data to be sent
		ByteBuffer byteBuffer = ByteBuffer.allocate(buf.length+4);
		// Operation number 
		byteBuffer.putShort((short)OP_DAT);
		// Block number
		byteBuffer.putShort(block);
		byteBuffer.put(buf);
		
		DatagramPacket packet = new DatagramPacket(byteBuffer.array(), buf.length+4);
		try {
			socket.send(packet);
		} catch (IOException e) {
			System.err.println("Sending data failed");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Method to wait for ACK from client
	 * @param socket socket to receive ACK from
	 * @param block used to check if client sends same block number
	 * @return true if reiceved ACK successfully
	 */
	private boolean receiveACK(DatagramSocket socket, short block) {
		// ACK contains of 2 bytes of OP number and 2 bytes of block
		// number
		
		// Temporary buffer to receive ACK. Making it larger than 
		// expected
		byte[] buf = new byte[4];
		DatagramPacket ack = new DatagramPacket(buf, buf.length);
		try {
			// If it doesn't receive ACK for 3 seconds, throws exception
			// And exception results in whole method returning false.
			socket.setSoTimeout(3000);
			socket.receive(ack);
		}catch (SocketTimeoutException e) {
			System.err.println("Took too long to receive acknowledge, retransmitting.");
			return false;
		}catch (IOException e) {
			System.out.println("Something went wrong while receiving acknowledgement");
			return false;
		}
		ByteBuffer ackBuffer = ByteBuffer.wrap(buf);
		// getShort() reads next two bytes, which is perfect to get OP number and 
		// block
		short op = ackBuffer.getShort();
		// Does ACK received from client contains ACK code?
		if (op == (short)OP_ERR) {
			//Received error, client ded
			send_ERR(socket, (short) 0, "Error message received");
		}
		if (op ==(short)OP_ACK) {
			short receivedBlock = ackBuffer.getShort();
			if (receivedBlock == block) {
				return true;
			}
			System.err.println("Wrong block received, retransmit.");
			return false;
		}
		// Code was not ACK, retransmit
		System.err.println("Not ACK for whatever reason, retrasmit.");
		return false;
	}
	
	/**
	 * This method handles WRQ.
	 * @param socket socket to receive data from and send ACK to
	 * @param requestedFile file name to be received from client
	 */
	private void receive_DATA_send_ACK(DatagramSocket socket, String requestedFile){
		// Now client wants to upload a file. We care if file exists or not
		File file = new File(requestedFile);
		if (file.exists()) {
			send_ERR(socket, (short) 6, "File already exists");
			return;
		}
		// If it's a directory, that exists, let client know
		if (file.isDirectory()) {
			send_ERR(socket, (short) 0, "Select file, not directory");
			return;
		}
		
		//Receiving data from client
		try {
			FileOutputStream write = new FileOutputStream(requestedFile);
			short block = 0;
			short op = 0;
			// First let client know that we received their first message
			sendAck(socket, block);
			int receivedData = 512;
			// Again, if received data is less than block size
			// it means that message is over
			while (receivedData > 511) {
				byte[] data = new byte[MAX_READ_BYTES];
				DatagramPacket receive = new DatagramPacket(data, MAX_READ_BYTES);
				socket.setSoTimeout(1000);
				socket.receive(receive);
				// Store how much data was received to check when there is less than 512
				// Remove 0 bytes from the end of the received data
				data = removeWhiteSpace(data);
				ByteBuffer dataBuffer = ByteBuffer.wrap(data);
				// Get operation code and block number
				op = dataBuffer.getShort();
				block = dataBuffer.getShort();
				if (op == (short) OP_ERR) {
					// Again, client ded
					send_ERR(socket, (short) 0, "Error received");
					break;
				}
				// Check if operation code is for sending data
				if (op == (short) OP_DAT) {
					// Get data, send ACK, increase block count (for correct ACK)
					write.write(dataBuffer.array(), 4, dataBuffer.array().length - 4);
					receivedData = dataBuffer.array().length;
					sendAck(socket, block);
					block++;
				}
			}
			System.out.println("Finished receiving data from client.");
			write.flush();
			write.close();
		}catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Simple method to send ACK back
	 * @param socket socket to send ACK to
	 * @param block which block of the message it is currently at
	 */
	private void sendAck(DatagramSocket socket, short block) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		byteBuffer.putShort((short)OP_ACK);
		byteBuffer.putShort(block);
		DatagramPacket ackPacket = new DatagramPacket(byteBuffer.array(), byteBuffer.array().length);
		try {
			socket.send(ackPacket);
		} catch (IOException e) {
			System.err.println("Sending ACK failed");
		}
		
	}
	
	/**
	 * Removes 0 bytes from the end of byte array
	 * @param bytes
	 * @return modified array without 0 bytes at the end
	 */
	private byte[] removeWhiteSpace(byte[] bytes){
	    int i = bytes.length - 1;
	    while (i >= 0 && bytes[i] == 0) {
	        --i;
	    }
	    return Arrays.copyOf(bytes, i + 1);
	}
	
	/**
	 * Sends error message to the client
	 * @param socket socket to send error message to
	 * @param code error code from 0 to 7
	 * @param message error message
	 */
	private void send_ERR(DatagramSocket socket, short code, String message){
		// 2 - OP code, 2 - Error Code, 1 - byte 0
		int bufSize = 5 + message.getBytes().length;
		ByteBuffer buffer = ByteBuffer.allocate(bufSize);
		// Check if code is correct, don't trust people
		if (code > 7 || code < 0) {
			System.err.println("Wrong code!");
			return;
		}
		// Construct error message
		buffer.putShort((short)OP_ERR);
		buffer.putShort(code);
		buffer.put(message.getBytes());
		buffer.put((byte) 0);

		DatagramPacket error = new DatagramPacket(buffer.array(), buffer.array().length);
		// Send error message
		try {
			socket.send(error);
			socket.close();
		} catch (IOException e) {
			System.err.println("Sending error failed");
		}
	}
	
}



