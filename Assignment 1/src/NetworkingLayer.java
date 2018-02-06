import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

public abstract class NetworkingLayer implements Runnable {
	
	protected int bufSize = 0;
	protected String IP = "";
	protected int port = 0;
	protected String message = "Hello world!"; 
	protected int rate = 0;
	protected int messagesSent = 0;
	// The time varies, for whatever reason. At times, it is more than 1 second. Tried to 
	// lower milliseconds to as low as possible to get the thread run for one second, but 
	// it seems to be random.
	protected final int ONE_SECOND = 994;
	// How many times to run a second long process
	protected final int SECONDS_TO_RUN = 1;
	protected boolean finishedSending = false;
	protected int remainingMessages = 0;
	protected long timeSpent = 0;
	protected SocketAddress localBindPoint;
	protected SocketAddress remoteBindPoint;
	protected String prefix = "";
	
	public NetworkingLayer(String[] args, int id) {
		// Check if input is valid before passing it
		// to variables
		if(isInputValid(args)) {
			IP = args[0];
			port = Integer.parseInt(args[1]);
			rate = Integer.parseInt(args[2]);
			if (rate == 0) {
				rate = 1;
			}
			bufSize = Integer.parseInt(args[3]);
			// ID doesn't need checking, it is merely for displaying purposes
			prefix = "[Client " + id + "] ";
			// Mostly for UDP
			localBindPoint = new InetSocketAddress(0);
			remoteBindPoint = new InetSocketAddress(IP, port);
		}
		//Input not valid, close client
		else {
			System.exit(0);
		}
	}
	
	protected void setUpClient() {}
	protected void sendMessage() {}
	protected void finishConnection() {}
	
	// A run() method that sends messages until specified message
	// rate is reached or until thread is interrupted (1 second runs
	// out)
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			// Message rate reached?
			if (!finishedSending) {
				// Send a single message
				sendMessage();
				// Increase message count
				messagesSent++;
				// Check if message rate is reached
				if (messagesSent == rate) {
					finishedSending = true;
				}
			}
		}
	}
	
	// Main method to set up client, then make sure it 
	// sends messages for 1 second
	public void startClient() {
		setUpClient();
		// For calculating how long it takes to finish the process.
		// Theoretically should take 1 second exactly, but results
		// vary. Can be more or less than a second.
		timeSpent = System.currentTimeMillis();
		// The loop runs for as many seconds as specified
		for (int i = 0; i < SECONDS_TO_RUN; i++) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
	        Future<?> future = executor.submit(new Thread(this));
	        try {
	        	// Let's the thread run for a second, then times out
	            future.get(ONE_SECOND, TimeUnit.MILLISECONDS);
	        } catch (TimeoutException e) {
	            future.cancel(true);
	        } catch (InterruptedException e) {
	        	System.err.println(prefix + "Interrupted");
			} catch (ExecutionException e) {
				System.err.println(prefix + "Execution exception");
				e.printStackTrace();
			}
	        executor.shutdownNow();
	        
	        //Logging finishing time
	        timeSpent = System.currentTimeMillis() - timeSpent;
	        
	        
	        // Noticed that if there are a lot of messages, it tries to send after shutdownNow()
	        // Therefore adding some break.
	        try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				System.out.println(prefix + "Message break interrupted");
			}
	        
	        // Some information about how sending went
	        System.out.println(prefix + "Time spent sending messages: " + timeSpent + "ms");
	        System.out.println(prefix + "Messages sent: " + messagesSent);
	        finishedSending = false;
	        // If not all messages managed to be delivered, let user know
	        if (messagesSent != rate) {
	        	System.out.println(prefix + "Not all messages went through, " + (rate - messagesSent) + " messages remaining");
	        	remainingMessages = remainingMessages + (rate - messagesSent);
	        }
	        messagesSent = 0;
		}
		
		// Finally, close the socket
		finishConnection();
	}
	// Check if input is 4 strings
	// And if 4 strings correspond to required input
	protected boolean isInputValid(String[] input) {
    	if (input.length != 4) {
    		System.err.println(prefix + "Please provide a valid input => [IP] [PORT] [transfer rate] [message buffer]");
    		return false;
    	}
    	return (isIpValid(input[0]) && isPortValid(input[1]) && isRateValid(input[2]) && isBufferValid(input[3]) && isMessageValid(message));
    }
    
	// Check IP validity 
    private boolean isIpValid(String ip) {
    	String[] splitIp = ip.split( "\\.");
    	// IP consists of 4 decimals, separated by dots
    	if (splitIp.length != 4) {
    		System.err.println(prefix + "IP format is invalid!");
    		return false;
    	}
    	// Input is integers, not string or any other characters
    	// Also if integers are 0 - 255
    	for (String s : splitIp) {
    		try {
    			int integer = Integer.parseInt(s);
    			if (integer < 0 || integer > 255) {
    				return false;
    			}
    		} catch (NumberFormatException e) {
        		System.err.println(prefix + "IP expected, but not found");
        		return false;
    		}
    	}
    	if (ip.endsWith(".") || ip.startsWith(".")) {
    		System.err.println(prefix + "IP cannot start or end with a dot!");
    		return false;
    	}
    	return true;
    }
    
    // Check port validity
    private boolean isPortValid(String port) {
    	try {
    	//Check if integer and if valid port
    		int integer = Integer.parseInt(port);
    		// https://stackoverflow.com/questions/113224/what-is-the-largest-tcp-ip-network-port-number-allowable-for-ipv4
    		if (integer < 1 || integer > 65535) {
    			return false;
    		}
		} catch (NumberFormatException e) {
    		System.err.println(prefix + "PORT expected, but not found");
    		return false;
		}
    	return true;
    }
    
    // Check port validity
    private boolean isBufferValid (String buffer) {
    	// Check if integer and if more than 1
    	try {
    		int integer = Integer.parseInt(buffer);
    		if (integer < 1) {
    			System.err.println(prefix + "Buffer should be more or equal to 1!");
    			return false;
    		}
    		// Unlikely, but after testing noticed that Integer.MAX_VALUE and 
    		// same value divided by 2 throws OutOfMemoryError exception
    		try {
    			@SuppressWarnings("unused")
				byte [] outOfMemory = new byte[integer];
    		} catch (OutOfMemoryError e) {
    			System.err.println(prefix + "Boy, you chose a big buffer");
    			return false;
    		}
		} catch (NumberFormatException e) {
    		System.err.println(prefix + "Buffer should be a number!");
    		return false;
		}
    	return true;
    }
    
    // Check rate validity
    private boolean isRateValid(String rate) {
    	try {
    		int integer = Integer.parseInt(rate);
    		if (integer < 0) {
        		System.err.println(prefix + "Rate should be more or equal to 0!");
        		return false;
    		}
    	} catch (NumberFormatException e) {
    		System.err.println(prefix + "Rate should be a number!");
    		return false;
		}
    	return true;
    }
    
    // Check if message is correct 
    private boolean isMessageValid(String message) {
    	if (message.length() == 0) {
    		return false;
    	}
    	if (message.length() >= 65507) {
    		return false;
    	}
    	
    	return true;
    }
}
