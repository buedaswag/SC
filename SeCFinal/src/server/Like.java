package server;

import java.io.File;
import java.util.Queue;

/**
 * Represents a like made by user in a Photo
 * @author migdi, max, antonio
 *
 */
public class Like {
	//the user that made this comment
	private User user;
	
	/**
	 * adds a like made by user in a Photo
	 * @param user
	 */
	public Like(User user) {
		this.user = user;
	}

	/**
	 * 
	 * @return the user that made this comment
	 */
	public User getUser() {
		return user;
	}

	public static Queue<Like> findAll(File photoDirectorie) {
		// TODO Auto-generated method stub
		return null;
	}
}