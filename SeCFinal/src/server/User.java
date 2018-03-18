package server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class User {

	private static final String path = new String("database");

	private String userid;
	private String password;
	private List<String> followers;
	private List<Photo> photos;

	/**
	 * constructs a new User object
	 * 
	 * @param userid
	 *            of this User
	 * @param password
	 *            of this User
	 */
	public User(String userid, String password) {
		this.userid = userid;
		this.password = password;
		this.followers = new ArrayList<>();
		this.photos = new ArrayList<>();
	}

	/**
	 * adds followers to this User´s list of followers
	 * 
	 * @param followers - the followers to be added 
	 */
	public void addFollowers(List<String> followers) {
		this.followers.addAll(followers);
	}

	/**
	 * removes followers from this User´s list of followers
	 * 
	 * @param followers - the followers to be removed 
	 */
	public void removeFollowers(List<String> followers) {
		this.followers.removeAll(followers);
	}

	/**
	 *  adds a single follower to this User´s followers list
	 *  
	 * @param follower the follower to be added
	 */
	public void addFollower(String follower) {
		this.followers.add(follower);
	}

	/**
	 *  removes a single followers to this User´s followers list
	 *  
	 * @param follower the follower to be removed
	 */
	public void removeFollower(String follower) {
		this.followers.remove(follower);
	}
	
	/**
	 * checks if the user has a photo with the given name
	 * @param name - the name to check
	 * @return true if the User does has a photo with the given name 
	 */
	public boolean hasPhoto(String name) {
		if (photos.contains(new Photo(name))) 
			return true;
		
		return false;
	}
	
	/**
	 * checks if the user has any photo with any of the given names
	 * @param names of the photos given
	 * @return true if the User does have a photo with the given names
	 */
	public boolean hasPhotos(String[] names) {
		for(String name : names)
			if(hasPhoto(name))
				return true;
		return false;
	}

	/**
	 * 
	 * @param photo
	 */
	public void addPhoto(Photo photo) {
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
	 * load photos from database to the user
	 */
	public void loadPhotos() {
		// load photos
		String photosFile = path + "\\" + this.getUserid() + "\\" + "Photos";
		// todas as pastas de fotos
		File[] directories = new File(photosFile).listFiles(File::isDirectory);

		// buscar as fotos presentes em cada directoria
		for (File photo : directories) {
			Photo p = new Photo(photo.getName());
			// colocar a foto na lista de fotos do utilizador
			this.addPhoto(p);
		}

	}
	
	/**
	 * Verifica se o utilizador local segue "user"
	 * 
	 * @param user
	 *            - O utilizador
	 * @return - Se o utilizador local segue "user"
	 * @throws IOException
	 */
	public boolean follows(String user) throws IOException {
		return followers.contains(user);
	}

}
