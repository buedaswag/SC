package server;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Photo {
	private String name;
	private Date uploadDate;
	private long size;
	private List<Comment> comments;
	//private List<Like> likes;
	private int like;

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

	/**
	 * 
	 * @return a list of this photo's comments
	 */
	public List<Comment> getComments() {
		return comments;
	}
}