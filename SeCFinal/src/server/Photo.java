package server;

import java.util.Date;
import java.util.LinkedList;

public class Photo
{
	private String name;
	private String path;
	private Date uploadDate;
	private long size;
	private LinkedList<Comment> comments;
	private int likes;
	private int dislikes;
	
	public Photo(String name)
	{
		this.name = name;
		this.uploadDate = new Date();
		this.comments = new LinkedList<Comment>();
	}
	
	// Metodos de informacao
	public String getName()
	{
		return this.name;
	}
	
	public Date getDate()
	{
		return this.uploadDate;
	}
	
	public long getSize()
	{
		return this.size;
	}
	
	public LinkedList<Comment> getComments()
	{
		return this.comments;
	}
}
