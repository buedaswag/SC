package server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Photo {
	private String name;
	private Date uploadDate;
	private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	private long size;
	private List<Comment> comments;
	private List<Like> likes;

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
		return this.likes.size();
	}

	/**
	 * 
	 * @return a list of this photo's comments
	 */
	public List<Comment> getComments() {
		return comments;
	}
	
	public void addComments(List<Comment> comments) {

		for (Comment comment : comments) 
			this.comments.add(comment);
		
	}
}