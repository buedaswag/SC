package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 *         Represents a comment made by the user in the photo
 */
public class Comment {
	
	/**********************************************************************************************
	 * findAll and load variables and methods
	 **********************************************************************************************
	 */
	private static String fileSeparator = System.getProperty("file.separator");
	private static final String commentsTxtName = "comments.txt";
	private static FileReader fileReader;
	private static BufferedReader buffReader;
	
	/**
	 * Finds all the comments in this photo's directory and loads them into memory.
	 * @param photoDirectorie
	 * @return comments
	 */
	public static Queue<Comment> findAll(File photoDirectorie) {
		//create the buffers for reading from the file and the Queue
		Queue<Comment> comments = new LinkedList<>();
		File commentsTxt = new File(photoDirectorie + fileSeparator + commentsTxtName);
		fileReader = new FileReader(commentsTxt);
		buffReader = new BufferedReader(fileReader);
		String line;
		while ((line = buffReader.readLine()) != null) {
			comments.add(Comment.find(line));
		}
		filer.close();
		buffr.close();
		return comments;
	}
	
	/**
	 * Find all the information necessary to load the comment to memory
	 * (commenterUserId and the comment).
	 * @param line - a line in the commentsTxt file in the form <commenterUserId>:<comment>
	 * @return comment - the Comment object corresponding to a line in the commentsTxt file
	 */
	private static Comment find(String line) {
		String[] lineComponents = line.split(":");
		String commenterUserId = lineComponents[0];
		String comment = lineComponents[1];
		return Comment.load(commenterUserId, comment);
	}
	
	/**
	 * Constructs a Photo object with the given parameters.
	 * @param commenterUserId
	 * @param comment
	 * @return
	 */
	private static Comment load(String commenterUserId, String comment) {
		// TODO Auto-generated method stub
		return null;
	}
	/**********************************************************************************************
	 * Comment variables and methods
	 **********************************************************************************************
	 */
	private String userid;
	private Date date;
	private String comment;

	/**
	 * Constructor: constructs a new comment made by the user with the given userid
	 * in the Photo to which this comment belongs to.
	 * 
	 * @requires user is a follower of the user who uploaded the photo
	 * @param comment
	 * @param date
	 *            the date of this comment
	 * @param user
	 */
	public Comment(String userid, Date date, String comment) {
		this.userid = userid;
		this.date = date;
		this.comment = comment;
	}

	/**
	 * Constructor: constructs a new comment made by the user with the given userid
	 * in the Photo to which this comment belongs to. Sets the date to the current
	 * date
	 * 
	 * @requires user is a follower of the user who uploaded the photo
	 * @param comment
	 * @param user
	 */
	public Comment(String userid, String comment) {
		this.userid = userid;
		this.date = new Date();
		this.comment = comment;
	}

	// Information methods
	/**
	 * 
	 * @return the user id of this user
	 */
	public String getUserid() {
		return this.userid;
	}

	/**
	 * 
	 * @return the String comment
	 */
	public String getComment() {
		return this.comment;
	}

	/**
	 * @return A String representation of this object
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(userid);
		sb.append(" ");
		sb.append(date);
		sb.append(" ");
		sb.append(comment);
		return sb.toString();
	}
}