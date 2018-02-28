package htttpServer.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HTMLPageGenerator {
	
	private final String START = "<html><body><p>";
	private final String END = "</p></body></html>";
	private File directory = null;
	private String responseMessage = null;
	private boolean containsDash = false;
	
	public HTMLPageGenerator (File dir, boolean dash) {
		this.directory = dir;
		this.containsDash = dash;
	}
	
	public HTMLPageGenerator (String responseMessage, boolean dash) {
		this.responseMessage = responseMessage;
		this.containsDash = dash;
	}
	
	/**
	 * Creates a temporary file that acts as simple HTML page, used to either return 
	 * error message or list of items in directory
	 * @return temporary html page
	 */
	public File generateTempPage() {
		File tempFile = null;
		try {
			tempFile = File.createTempFile("tempPage", "html");
		} catch (IOException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		sb.append(START);
		// Directory was provided, therefore user is accessing directory without
		// index.htm(l) file. Create a list of files in that directory.
		if (directory != null) {
			sb.append("<h4>Index.html or index.htm was not found, showing contents of the directory:</h4><br>");
			sb.append("<div style = \"margin: 10px;\">");
			for (File f : directory.listFiles()) {
				if(!containsDash) {
					sb.append("<a href=\"./" + directory.getName() + "/" + f.getName() +"\">" + f.getName() + "</a><br>");
				}
				else {
					sb.append("<a href=\"./" + f.getName() +"\">" + f.getName() + "</a><br>");
				}
			}
			sb.append("</div>");
		}
		// Response message was provided, therefore a simple HTML page displaying
		// response message is needed. Create it.
		else if (responseMessage != null) {
			sb.append("<h1>" + responseMessage + "</h1><br>");
		}
		else {
			System.err.println("Something went wrong generating temporary HTML page");
		}
		sb.append(END);
		// Write contents of HTML page to file object
		if (tempFile != null) {
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter(tempFile));
				bw.write(sb.toString());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// On exit temporary file is deleted.
		tempFile.deleteOnExit();
		return tempFile;
	}
}
