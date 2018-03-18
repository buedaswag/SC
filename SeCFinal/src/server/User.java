package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
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
	 * Verifica se o utilizador actual tem user como seguidor
	 * 
	 * @return - idem
	 */
	//TODO
	public boolean isFollower(User user) {
		return followers.contains(user.getUserid());
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
	 * TODO
	 * @param names
	 * @param photosPath
	 */
	public void addPhotos(String[] names, File photosPath) 
	{
		// Abre directorio temporario, lista ficheiros e cria lista final
		File dir = new File(photosPath.getAbsolutePath());
		ArrayList<File> files = new ArrayList<File>(Arrays.asList(dir.listFiles()));
		ArrayList<File> filesFinal = new ArrayList<File>();
		
		// Constroi lista final a partir dos nomes dos ficheiros a mover
		for(File f: files) {
			for(String s: names) {
				if(s.equals(f.getName()))
					filesFinal.add(f);
			}
		}
		
		// Iterar sobre lista de ficheiros a copiar
		for(File f: filesFinal) {
			// Colocar na directoria nova
			FileManager.FMaddPhoto(this.userid, f);
			// Apaga a foto da directoria antiga
			f.delete();
			// Cria objecto abstracto e coloca em memoria temporaria
			Photo photo = new Photo(f.getName());
			photos.add(photo);
		}
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

}
