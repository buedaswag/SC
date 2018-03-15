package interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Handles the interaction of an object with the persistent storage
 * @author migdi
 *
 */
public interface Persistent {
	
	/**
	 * loads the object from persistent memory
	 * @throws IOException 
	 */
	public void load() throws IOException;
	
	/**
	 * saves the object to persistent memory
	 */
	public void save();

}
