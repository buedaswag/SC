import interfaces.Persistent;

/**
 * Represents a like made by user in a Photo
 * @author migdi, max, antonio
 *
 */
public class Like implements Persistent{
	//the user that made this comment
	private User user;
	
	/**
	 * adds a like made by user in a Photo
	 * @param user
	 */
	public Like(User user) {
		this.user = user;
	}

	/**
	 * 
	 * @return the user that made this comment
	 */
	public User getUser() {
		return user;
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}
}
