package server;

import java.util.Date;

/**
 * Represents a comment made by user in a photo
 * 
 * @author migdi asd
 */
public class Comment {
	private String userid;
	private Date date;
	private String comment;

	/**
	 * Constructor: constructs a new comment made by the user with the 
	 * given userid in the Photo to which this comment belongs to.
	 * 
	 * @requires user is a follower of the user who uploaded the photo
	 * @param comment
	 * @param date the date of this comment
	 * @param user
	 */
	public Comment(String userid, Date date, String comment) {
		this.userid = userid;
		this.date = date;
		this.comment = comment;
	}
	
	/**
	 * Constructor: constructs a new comment made by the user with the 
	 * given userid in the Photo to which this comment belongs to. 
	 * Sets the date to the current date
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

	// Metodos de informacao
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
}