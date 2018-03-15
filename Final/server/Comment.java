package server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Comment
{
	private User user;
	private Date date;
	private String comment;
	private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	
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
	
	public Date getDate() {
		return this.date;
	}
}
