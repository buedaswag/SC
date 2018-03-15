import java.util.Date;

/**
 * Represents a comment made by user in a photo
 * 
 * @author migdi
 * asd
 */
public class Comment
{
	private User user;
	private String comment;

	/**
	 * Creates a comment made by user in a photo
	 * 
	 * @requires user is a follower of the user who uploaded the photo
	 * @param comment
	 * @param user
	 */
	public Comment(User user, String comment)
	{
		this.user = user;
		this.comment = comment;
	}

	// Metodos de informacao
	/**
	 * 
	 * @return the user id of this user
	 */
	public User getUserID()
	{
		return this.user;
	}

	/**
	 * 
	 * @return the String comment
	 */
	public String getComment()
	{
		return this.comment;
	}
}
