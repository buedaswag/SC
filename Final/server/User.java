package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class User {

	private static final String path = new String("database");

	private String userid;
	private String password;
	private List<String> followers;
	private List<Photo> photos;

	/**
	 * constructs a new User object
	 * 
	 * @param userid
	 *            of this User
	 * @param password
	 *            of this User
	 */
	public User(String userid, String password) {
		this.userid = userid;
		this.password = password;
		this.followers = new ArrayList<>();
		this.photos = new ArrayList<>();
	}

	public void addFollowers(List<String> followers) {
		for (String u : followers) {
			this.followers.add(u);
		}
	}

	public void removeFollowers(List<String> followers) {
		for (String u : followers) {
			this.followers.remove(u);
		}
	}
	
	public void addFollower(String follower) {
			this.followers.add(follower);
	}
	
	public void removeFollower(String follower) {
		this.followers.remove(follower);
}


	/**
	 * adds this user's photo
	 * 
	 * @param photo
	 */
	public void addPhoto(Photo photo) {
		photos.add(photo);
	}

	/**
	 * 
	 * @return this user's userid
	 */
	public String getUserid() {
		return userid;
	}

	/**
	 * 
	 * @return this user's password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @throws IOException
	 * @requires this is an existing user in the file system
	 * @requires exists a followers.txt file in this user's directory
	 */
	

}