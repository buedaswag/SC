package server;

import java.util.Date;

public class Comment
{
	private User user;
	private Date date;
	private String comment;
	
	public Comment(User user, Date date, String comment)
	{
		this.user = user;
		this.date = date;
		this.comment = comment;
	}
	
	// Metodos de informacao
	public User getUserID()
	{
		return this.user;
	}
	
	public String getComment()
	{
		return this.comment;
	}
}
