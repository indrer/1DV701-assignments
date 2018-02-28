package htttpServer.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Config {
	private String filePath;
	
	public Config(String filePath) {
		this.filePath = filePath;
	}
	
	/**
	 * If resource folder is provided in a config file, this method
	 * will get the folder.
	 * @return absolute path to resource folder
	 * @throws FileNotFoundException 
	 * @throws JSONException 
	 */
	public String getResourceFolder() throws JSONException, FileNotFoundException {
		String resourceFolder = "";
		JSONObject json = new JSONObject(new JSONTokener(new FileReader(filePath + "config.json")));
		resourceFolder = (String) json.get("resource");
		
		return resourceFolder;
	}

	/**
	 * If any forbidden folders or content are specified in a config file,
	 * this method will get it and return a String array containing paths
	 * of those folder or files, navigating to them from source folder (so 
	 * not absolute path on the machine).
	 * @return array of paths to forbidden files and folders
	 * @throws FileNotFoundException 
	 * @throws JSONException 
	 */
	public String[] getForbidden() throws JSONException, FileNotFoundException {
		String[] forbidden = null;
		JSONObject json = new JSONObject(new JSONTokener(new FileReader(filePath + "config.json")));
		JSONArray array = (JSONArray) json.get("forbidden");
		Iterator<Object> it = array.iterator();
		forbidden = new String[array.length()];
		if (forbidden.length > 0) {
			int count = 0;
			while (it.hasNext() && (forbidden[count] = (String) it.next()) != "") {
				count++;
			}
		}
		else {
			forbidden = null;
		}
		
		return forbidden;
	}
	
	/**
	 * Returns a Map object containing redirected locations. Key is old location, value is new.
	 * @return Map of redirected locations
	 * @throws JSONException
	 * @throws FileNotFoundException
	 */
	public Map<String, String> getRedir() throws JSONException, FileNotFoundException {
		Map<String, String> result = new HashMap<String, String>();
		JSONObject json = new JSONObject(new JSONTokener(new FileReader(filePath + "config.json")));
		JSONObject array = json.getJSONObject("redir");
		if (array.length() > 0) {
		    for (Iterator<String> it = array.keys(); it.hasNext();) {
		        String key = it.next();
		        result.put(key, (String) array.get(key));
		    }
		}
		else {
			result = null;
		}
		
		return result;
	}

}
