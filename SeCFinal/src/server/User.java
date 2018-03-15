package server;

import java.util.ArrayList;
import java.util.List;

public class User
{
	private String id;
	private String pass;
	private ArrayList<String> followers;
	
	public User(String id, String pass)
	{
		this.id = id;
		this.pass = pass;
		this.followers = new ArrayList<String>();
	}
	
	public void addFollowers(List<String> followers)
	{
		for(String u : followers)
		{
			followers.add(u);
		}
	}
	
	public void removeFollowers(List<User> followers)
	{
		for(User u : followers)
		{
			followers.remove(u);
		}
	}
	
	public String getName()
	{
		return this.id;
	}
	
	public String getPassword()
	{
		return this.pass;
	}
	
	public ArrayList<String> getFollowers()
	{
		return this.followers;
	}
}
