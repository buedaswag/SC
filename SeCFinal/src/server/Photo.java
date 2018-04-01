package server;

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