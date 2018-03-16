package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import trabalho1.Persistent;

public class User {//implements Persistent{
	
	private static final String path = new String("database");
	
	private String userid;
	private String password;
	private List<String> followers;
	private List<Photo> photos;

	/**
	 * constructs a new User object
	 * @param userid of this User
	 * @param password of this User
	 */
	public User(String userid, String password) {
		this.userid = userid;
		this.password = password;
		this.followers = new ArrayList<>();
		this.photos = new ArrayList<>();
	}

	/**
	 * @requires followUsers is a list of registered users
	 * @param followUserIds list of userids of the users to add as 
	 * followers of this user
	 * @return true if success, false if failure
	 */
	public boolean addFollowers(List<User> followUsers) {
		//auxiliary collection of users
		List<String> aux = new ArrayList<>();
		//check if any of the followUserids is already a follower of this user
		for(String userid : followers)
			for(User user : followUsers)
				if(user.getUserid().equals(userid))
					return false;
				else
					aux.add(user.getUserid());
		followers.addAll(aux);
		return true;
	}
	
	/**
	 * checks if the user has a photo with the given name
	 * @param photo
	 * @return
	 */
	public boolean hasPhoto(String name) {
		for(Photo p : photos)
			if(p.getName().equals(name))
				return true;
		return false;
	}
	
	/**
	 * checks if the user has any photo with any of the given names
	 * @param names
	 * @return
	 */
	public boolean hasPhotos(String[] names) {
		for(String name : names)
			if(hasPhoto(name))
				return true;
		return false;
	}

	/**
	 * adds this user's photo s
	 * @param photo
	 */
	public void addPhotos(String[] names, File photosPath) {
		//adds the photos to the file system and to memory
		for (String name : names) {
			photos.add(new Photo(name, photosPath));
		}
		
		Photo photo = new Photo()
		photos.add(photo);
	}

	/**
	 * 
	 * @return this user's userid
	 */
	public String getUserid() {
		return userid;
	}

	/**
	 * 
	 * @return this user's password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @throws IOException 
	 * @requires this is an existing user in the file system
	 * @requires existes a followers.txt file in this user's directory
	 */
	public void load() throws IOException {
		//load followers
		File followersFile = new File(path + "\\" + getUserid() + "\\" + "followers.txt");

		BufferedReader br = new BufferedReader(new FileReader(followersFile));

		String userid;
		while ((userid = br.readLine()) != null)
			followers.add(userid);
				
		
		//load photos
		File photosFile = new File(path + "\\" + getUserid() + "\\" + "Photos");

		File[] subDirs = photosFile.listFiles(new FileFilter() {
		    public boolean accept(File pathname) {
		        return pathname.isDirectory();
		    }
		});
		  
		for (File subDir : subDirs) {
		    System.out.println(subDir.getPath());
		}

		String userid;
		while ((userid = br.readLine()) != null)
			followers.add(userid);
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}
}