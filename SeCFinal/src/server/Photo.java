package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 */
public class Photo {

	/**********************************************************************************************
	 * findAll and load variables and methods
	 **********************************************************************************************
	 */
	private static String databaseRootDirName = "database";
	private static File databaseRootDir = new File(databaseRootDirName);
	private static String fileSeparator = System.getProperty("file.separator");
	
	public static Collection<Photo> findAll(String userId) {
		//find all the photos from this user in the file system
		//call find on each photo
		
		//the Collection to be returned
		Collection<Photo> photos = new LinkedList<>();
		//the directory name for this user's photos
		String photosDirName = databaseRootDirName + fileSeparator + userId;
		//if photosDir is empty, there are no photos. Return the empty Collection
		if (databaseRootDir.list().length > 0) {
			try {
				//create the usersTxt file if it doesn't exist yet
				usersTxt.createNewFile();
				//create the buffers for reading from files, and create the Map
				fileReader = new FileReader(usersTxt.getAbsoluteFile());
				buffReader = new BufferedReader(fileReader);
				users = new Hashtable<>();
				/*
				 * get all the info to load each user to memory 
				 * (userId, password, followers and photos
				 * Reads the current users from the usersTxt file
				 */
				String line;
				while ((line = buffReader.readLine()) != null) {
					// splits the line in the form 'userid:password'
					String[] userCredentials = line.split(":");
					//adds the user to the map
					users.put(userCredentials[0], User.find(userCredentials[0], userCredentials[1]));
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					fileReader.close();
					buffReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return users;
	}

	/**********************************************************************************************
	 * User variables and methods
	 **********************************************************************************************
	 */

	private String name;
	private Queue<Comment> comments;
	private int likes;
	private int dislikes;
	private Date uploadDate;
	private SimpleDateFormat df;

	public Photo(String name, long date) {
		this.name = name;
		this.uploadDate = new Date(date);
		this.comments = new LinkedBlockingDeque<Comment>();
		this.df = new SimpleDateFormat("dd/MM/yyyy HH'h'mm");
	}

	/**
	 * 
	 * @return the name of this photo
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @return the publish date of this photo
	 */
	public Date getDate() {
		return this.uploadDate;
	}

	/**
	 * Returns this photo's number of likes
	 * 
	 * @return
	 */
	public int getTotalLikes() {
		return this.likes;
	}

	/**
	 * Returns this photo's number of dislikes
	 * 
	 * @return
	 */
	public int getTotalDislikes() {
		return this.dislikes;
	}

	/**
	 * 
	 * @return a list of this photo's comments
	 */
	public Collection<Comment> getComments() {
		return comments;
	}

	public void addComments(Collection<Comment> comments) {
		this.comments.addAll(comments);
	}

	/**
	 * Adds a comment made by user in this user's photo
	 * 
	 * @param comment
	 *            - the comment to be made
	 * @param userid
	 *            - the userid of the user
	 */
	public void addComment(String comment, String userid) {
		comments.add(new Comment(userid, comment));
	}

	/**
	 * Adds a like made by user in this user's photo
	 * 
	 * @param userid
	 *            - the userid of the user
	 * @param likedUserid
	 *            - the userid of the likedUser
	 */
	// TODO incomplete, doesnt use Like object, instead uses an int
	public void addLike(String userid) {
		likes++;
	}

	/**
	 * Adds a dislike made by user in this user's photo
	 * 
	 * @param userid
	 *            - the userid of the user
	 * @param dislikedUserid
	 *            - the userid of the dislikedUser
	 */
	// TODO incomplete, doesnt use Like object, istead uses an int
	public void addDisLike(String userid) {
		dislikes++;

	}

	/**
	 * @return A String representation of this object
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(name);
		sb.append(" ");
		sb.append(likes);
		sb.append(" ");
		sb.append(dislikes);
		sb.append(" ");
		sb.append(uploadDate);
		return sb.toString();
	}
}