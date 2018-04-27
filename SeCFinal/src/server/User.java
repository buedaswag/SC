package server;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import crypto.Crypto;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 */
public class User {

	/**********************************************************************************************
	 * findAll and load variables and methods
	 **********************************************************************************************
	 */
	private static String databaseRootDirName = "database";
	private static String usersTxtName = "users.txt";
	private static String followersTxtName = "followers.txt";
	private static String fileSeparator = System.getProperty("file.separator");
	private static File usersTxt = new File(databaseRootDirName + fileSeparator + usersTxtName);
	private static File databaseRootDir = new File(databaseRootDirName);

	/**
	 * Finds all the users in the file system and loads them into memory.
	 * @requires databaseRootDir.exists()
	 * @requires usersTxt.exists()
	 * @requires when a user is registered, a folder must be created for him
	 * @return users - a Map<String, User> containing all the users in the file system, or an empty
	 * Map if there are no users yet
	 * @throws Exception 
	 */
	protected static Map<String, User> findAll () throws Exception {
		//the Map to be returned
		Map<String, User> users = new Hashtable<>();
		//if databaseRootDir does not exist, create it and the usersTxt file and return the empty map
		if (!databaseRootDir.exists()) {
			databaseRootDir.mkdir();
			usersTxt.createNewFile();
			return users;
		}
		//if databaseRootDir only has the usersTxt file, there are no users. Return the empty Map
		if (databaseRootDir.list().length > 1) {
			FileReader fileReader;
			BufferedReader buffReader = null;

			//create the buffers for reading from files, and create the Map
			fileReader = new FileReader(usersTxt);
			buffReader = new BufferedReader(fileReader);
			/*
			 * get all the info to load each user to memory 
			 * (userId, password, followers and photos
			 * Reads the current users from the usersTxt file
			 */
			String line;
			while ((line = buffReader.readLine()) != null) {
				// splits the line in the form 'userId:password'
				String[] userCredentials = line.split(":");
				//adds the user to the map
				users.put(userCredentials[0], User.find(userCredentials[0], 
						userCredentials[1]));
			}
			buffReader.close();
		}
		return users;
	}

	/**
	 * Find all the information necessary to load the user with the given credentials to memory
	 * (followers and photos).
	 * @param userId - userId and password of the user to be found
	 * @return user - The constructed user with all its information loaded to memory.
	 * @throws Exception 
	 */
	private static User find(String userId, String password) throws Exception {
		//the User to be returned
		User user = null;
		//get the followers
		Collection<String> followers = User.findFollowers(userId);
		//get the photos
		Collection<Photo> photos = Photo.findAll(userId);
		//load the user
		user = User.load(userId, password, followers, photos);
		return user;
	}

	/**
	 * Finds the followUserIds of the given user's followers
	 * @param userId - the user whose followers are to be found
	 * @return followers - a Collection<String> of the followUserIds. 
	 * The Collection will be empty if there are no followers
	 * @throws IOException 
	 * @throws NoSuchProviderException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnrecoverableKeyException 
	 * @throws InvalidKeyException 
	 */
	private static Collection<String> findFollowers(String userId) throws IOException, InvalidKeyException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, KeyStoreException, CertificateException, NoSuchProviderException {
		//get the followersTxt file
		File followersTxt = new File(databaseRootDirName + fileSeparator + userId + fileSeparator 
				+ followersTxtName);
		SecretKey sk = Crypto.getInstance().getSecretKey();
		Crypto.getInstance().decipherFile(followersTxt, sk);
		//the Collection to store the followUserIds
		Collection<String> followers = new LinkedList<>();
		FileReader fileReader;
		BufferedReader buffReader = null;
		fileReader = new FileReader(followersTxt);
		buffReader = new BufferedReader(fileReader);
		String followerUserId;
		while ((followerUserId = buffReader.readLine()) != null) {
			followers.add(followerUserId);
		}
		buffReader.close();
		Crypto.getInstance().cipherFile(followersTxt, sk);
		return followers;
	}

	/**
	 * Constructs a User object with the given parameters
	 * @param userId
	 * @param password
	 * @param followers
	 * @param photos
	 */
	private static User load(String userId, String password, Collection<String> followers,
			Collection<Photo> photos) {
		return new User(userId, password, followers, photos);
	}

	/**********************************************************************************************
	 * insert and update variables and methods
	 **********************************************************************************************
	 */
	
	/**
	 * Creates and inserts a new user with the given credentials
	 * @param localUserId
	 * @param password
	 * @return
	 * @throws IOException 
	 */
	protected static User insert(String localUserId, String password) throws IOException {
		FileWriter fileWriter = new FileWriter(usersTxt, true);
		BufferedWriter buffWriter = new BufferedWriter(fileWriter);
		//add the line in the usersTxt file corresponding to the user
		String line = localUserId + ":" + password;
		buffWriter.write(line);
		buffWriter.newLine();
		buffWriter.close();
		//create localUser's directory
		String localUserDirName = databaseRootDirName + fileSeparator + localUserId;
		File localUserDir = new File(localUserDirName);
		localUserDir.mkdir();
		//create the followersTxt file
		new File(localUserDirName + fileSeparator + followersTxtName).createNewFile();
		//create the new user
		return new User(localUserId, password);
	}

	/**
	 * Adds the followUsers as followers of localUser, in the persistent storage.
	 * @param localUserId
	 * @param followUserIds
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws NoSuchProviderException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws UnrecoverableKeyException 
	 * @throws SignatureException 
	 */
	protected static void insertFollowers(String localUserId, String[] followUserIds) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, SignatureException {
		// Opens resources and necessary streams
		File followersTxt = new File(databaseRootDirName + fileSeparator + localUserId + 
				fileSeparator + followersTxtName);
		FileWriter fileWriter = new FileWriter(followersTxt, true);
		BufferedWriter buffWriter = new BufferedWriter(fileWriter);
		for (String followUserId : followUserIds) {
			buffWriter.write(followUserId);
			buffWriter.newLine();
		}
		buffWriter.close();
		//Crypto.getInstance().signFile(followersTxt);
		SecretKey sk = Crypto.getInstance().getSecretKey();
		Crypto.cipherFile(followersTxt, sk);
	}

	/**
	 * Updates the followersTxt file, repacing its content with the given followers. 
	 * @param localUserId
	 * @param followers
	 * @throws IOException 
	 */
	private static void updateFollowers(String localUserId, Collection<String> followers) throws IOException {
		// Opens resources and necessary streams
		File followersTxt = new File(databaseRootDirName + fileSeparator + localUserId + 
				fileSeparator + followersTxtName);
		//delete old file
		followersTxt.delete();
		//create new file
		followersTxt.createNewFile();
		FileWriter fileWriter = new FileWriter(followersTxt, true);
		BufferedWriter buffWriter = new BufferedWriter(fileWriter);
		for (String followUserId : followers) {
			buffWriter.write(followUserId);
			buffWriter.newLine();
		}
		buffWriter.close();
	}

	/**********************************************************************************************
	 * User variables , methods and constructors
	 **********************************************************************************************
	 */
	private String userId;
	private String password;
	private Collection<String> followers;
	private Collection<Photo> photos;

	/**
	 * Constructs a new User object from it's userId and it's password. 
	 * 
	 * @param userId 
	 * @param password
	 */
	protected User(String userId, String password) {
		this.userId = userId;
		this.password = password;
		this.followers = new LinkedList<>();
		this.photos = new LinkedList<>();
	}

	/**
	 * Constructs a new User object from the given parameters. 
	 * @param userId
	 * @param password
	 * @param followers
	 * @param photos
	 */
	private User(String userId, String password, Collection<String> followers,
			Collection<Photo> photos) {
		this.userId = userId;
		this.password = password;
		this.followers = followers;
		this.photos = photos;
	}

	/**
	 * Checks if this user has followUser as a follower
	 * 
	 * @param followUserId - the userId of the followUser
	 */
	protected boolean isFollowed(String followUserId) {
		return followers.contains(followUserId);
	}

	/**
	 * Checks if any of the followUsers is already a follower
	 * 
	 * @param followuserIds - the userIds of the followUsers
	 */
	protected boolean isFollowed(String[] followuserIds) {
		// check one by one
		for (String f : followers)
			for (String u : followuserIds)
				if (f.equals(u))
					return true;
		return false;
	}

	/**
	 * Checks if any of the followUsers is not a follower of this user
	 * 
	 * @param followuserIds- the userIds of the followUsers
	 * @return true - if any of the followUsers is not a follower of this user, 
	 * or false if all of the followUsers are followers of this user
	 */
	protected boolean isNotFollowed(String[] followuserIds) {
		// check one by one
		for (String f : followuserIds)
			if (!followers.contains(f))
				return true;
		return false;
	}

	/**
	 * Inserts the followUsers as followers of this user.
	 * @param localUserId - the localUserId of the user
	 * @param followUserIds - the userIds of the followUsers
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws NoSuchProviderException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws UnrecoverableKeyException 
	 * @throws SignatureException 
	 */
	protected void addFollowers(String localUserId, String[] followUserIds) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, SignatureException {
		User.insertFollowers(localUserId, followUserIds);
		Collections.addAll(this.followers, followUserIds);
	}

	/**
	 * Removes the followUsers as followers from this user.
	 * @param localUserId - the localUserId of the user
	 * @param followUserIds - the userIds of the followUsers
	 * @throws IOException 
	 */
	protected void removeFollowers(String localUserId, String[] followUserIds) throws IOException {
		//from memory
		for (String followUserId : followUserIds) {
			followers.remove(followUserId);
		}
		//from disk
		User.updateFollowers(localUserId, followers);
	}

	/**
	 * adds a single follower to this User´s followers list
	 * 
	 * @requires the follower has been added to this user's persistent storage
	 * @param follower - the follower to be added
	 */
	protected void addFollower(String follower) {
		this.followers.add(follower);
	}

	/**
	 * removes a single followers to this User´s followers list
	 * 
	 * @param follower - the follower to be removed
	 */
	protected void removeFollower(String follower) {
		this.followers.remove(follower);
	}

	/**
	 * checks if the user has a photo with the given name
	 * 
	 * @param name - the name to check
	 * @return true if the User does has a photo with the given name
	 */
	protected boolean hasPhoto(String name) {
		for (Photo p : photos)
			if (p.getName().equals(name))
				return true;
		return false;
	}

	/**
	 * checks if the user has any photo with any of the given names
	 * 
	 * @param names of the photos given
	 * @return true if the User does have a photo with the given names
	 */
	protected boolean hasPhotos(String[] names) {
		for (String name : names)
			if (hasPhoto(name))
				return true;
		return false;
	}

	/**
	 * Inserts the photos with the given names to this user.
	 * @param localUserId
	 * @param photoNames
	 * @param photosPath
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws NoSuchProviderException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws UnrecoverableKeyException 
	 */
	protected void addPhotos(String localUserId, File photosPath) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException {
		photos.addAll(Photo.insertAll(localUserId, photosPath));
	}

	/**
	 * Inserts the photos with the given names to this user.
	 * @param localUserId
	 * @param copiedUserId - the userId of the user where photos the photos are copied from.
	 * @param copiedUser - the user where photos the photos are copied from.
	 * @throws IOException 
	 * 
	 */
	protected void copyPhotos(String localUserId, String copiedUserId, User copiedUser) throws IOException {
		File localUserDir = new File(databaseRootDirName + fileSeparator + localUserId);
		File copiedUserDir = new File(databaseRootDirName + fileSeparator + copiedUserId);
		photos.addAll(Photo.insertAll(copiedUser.getPhotos(), localUserDir, copiedUserDir));
	}

	/**
	 * 
	 * @return this user's userId
	 */
	protected String getUserId() {
		return userId;
	}

	/**
	 * 
	 * @return this user's password
	 */
	protected String getPassword() {
		return password;
	}

	/**
	 * Adds a comment made by commenterUser in this user's photo
	 * @param commentedUserId - the userId of the commentedUser
	 * @param commenterUserId - the userId of the commenterUser
	 * @param comment - the comment to be made
	 * @param photoName - the name of the commentedUser's photo
	 * @throws Exception 
	 */
	protected void addComment(String commentedUserId, String commenterUserId, String comment, 
			String photoName) throws Exception {
		this.getPhoto(photoName).addComment(commentedUserId, commenterUserId, comment, photoName);
	}

	/**
	 * returns the photo with the given name
	 * 
	 * @param name
	 * @return
	 */
	protected Photo getPhoto(String name) {
		for (Photo p : photos)
			if (p.getName().equals(name))
				return p;
		return null;
	}

	/**
	 * 
	 * @return photos - this user's collection of photos
	 */
	private Collection<Photo> getPhotos() {
		return this.photos;
	}

	/**
	 * Adds a like made by likerUser in this user's photo
	 * @param likedUserId - the userId of the likedUser
	 * @param likerUserId - the userId of the likerUser 
	 * @param photoName - the name of this user's photo
	 * @throws Exception 
	 */
	protected void addLike(String likedUserId, String likerUserId, String photoName) throws Exception {
		getPhoto(photoName).addLike(likedUserId, likerUserId, photoName);
	}

	/**
	 * Adds a dislike made by likerUser in this user's photo
	 * @param dislikedUserId - the userId of the dislikedUser
	 * @param dislikerUserId - the userId of the dislikerUser 
	 * @param photoName - the name of this user's photo
	 * @throws Exception 
	 */
	protected void addDislike(String dislikedUserId, String dislikerUserId, String photoName) throws Exception {
		getPhoto(photoName).addDislike(dislikedUserId, dislikerUserId, photoName);
	}

	/**
	 * List the photos from this user, with the format: "photoName - uploadDate\n"
	 * @return listedPhotos - a String containing all of this user's photos listed according to the
	 * format
	 */
	public String listPhotos() {
		// Builds the string to be sent to the client
		StringBuilder sb = new StringBuilder();
		for (Photo photo : this.getPhotos()) {
			sb.append(photo.getName() + " - " + photo.getDate() + "\n");
		}
		// Conversion to string and return
		return sb.toString();
	}

	/**
	 * Get the info for the specified photo from this user, with the format: 
	 * "Likes: <number-of-likes>\n Dislikes: <number-of-dislikes>\n
	 * Comments:\n<comment-1>\n...<comment-n>\n"
	 * @return listedPhotos - a String containing all of this user's photos listed according to the
	 * format
	 */
	public String getInfoPhoto(String photoName) {
		return getPhoto(photoName).getInfoPhoto();
	}

	/**
	 * @return A String representation of this object
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(userId);
		sb.append(" ");
		sb.append(password);
		return sb.toString();
	}

	/**
	 * Get the names of all the photos of this user
	 * @return
	 */
	protected String[] getPhotoNames() {
		String[] photoNames = new String[photos.size()];
		int i = 0;
		for (Photo photo : photos) {
			photoNames[i] = photo.getName();
			i++;
		}
		return photoNames;
	}
}
