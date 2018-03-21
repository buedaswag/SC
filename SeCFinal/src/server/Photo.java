package server;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Photo {
	private String name;
	private List<Comment> comments;
	private int likes;
	//do we need to keep this?
	private Date uploadDate;
	private long size;
	
	public Photo(String name) {
		this.name = name;
		this.uploadDate = new Date();
		this.comments = new LinkedList<Comment>();
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
	 * 
	 * @return the size of this photo
	 */
	public long getSize() {
		return this.size;
	}

	public int getTotalLikes() {
		return this.likes;
	}

	/**
	 * 
	 * @return a list of this photo's comments
	 */
	public List<Comment> getComments() {
		return comments;
	}

	public void addComments(List<Comment> comments) {
		this.comments.addAll(comments);
	}

	/**
	 * Adds a comment made by user in this user's photo
	 * 
	 * @param comment - the comment to be made
	 * @param userid - the userid of the user
	 */
	public void addComment(String comment, String userid) {
		comments.add(new Comment(userid, comment));
	}

	/**
	 * Adds a like made by user in this user's photo
	 * 
	 * @param userid - the userid of the user
	 * @param likedUserid - the userid of the likedUser 
	 */
	//TODO incomplete, doesnt use Like object, istead uses an int
	public void addLike(String userid) {
		likes++;
	}
	
	/**
	 * Adds a like made by user in this user's photo
	 * 
	 * @param userid - the userid of the user
	 * @param likedUserid - the userid of the likedUser 
	 */
	//TODO incomplete, doesnt use Like object, istead uses an int
	public void addLikes(int likes) {
		this.likes+=likes;
	}
}