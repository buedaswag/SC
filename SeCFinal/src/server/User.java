package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
	 * @requires databaseRootDir.exists().
	 * @requires usersTxt.exists().
	 * @requires when a user is registered, a folder must be created for him.
	 * @return users - a Map<String, User> containing all the users in the file system, or an empty
	 * Map if there are no users yet
	 * @throws IOException 
	 */
	//TODO decipher password
	protected static Map<String, User> findAll () throws IOException {
		//the Map to be returned
		Map<String, User> users = new Hashtable<>();
		//if databaseRootDir does not exist, raise an exeption
		if (!databaseRootDir.exists()) {
			throw new java.lang.UnsupportedOperationException(
					"There are no users registered! you need to run ManUsers first!");
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
				// splits the line in the form userId:salt:salted_password_hash
				String[] userCredentials = getCipheredCredentials(line);
				//adds the user to the map
				users.put(userCredentials[0], User.find(userCredentials[0], 
						userCredentials[1], userCredentials[2]));
			}
			buffReader.close();
		}
		return users;
	}

	/**
	 * Find all the information necessary to load the user with the given credentials to memory
	 * (followers and photos).
	 * @param userId - userId of the user to be found.
	 * @param salt - the salt used to cipher the password of the user to be found.
	 * @param password - the ciphered password of the user to be found.
	 * @return user - The constructed user with all its information loaded to memory.
	 * @throws IOException 
	 */
	private static User find(String userId, String salt, String password) throws IOException {
		//the User to be returned
		User user = null;
		//get the followers
		Collection<String> followers = User.findFollowers(userId);
		//get the photos
		Collection<Photo> photos = Photo.findAll(userId);
		//load the user
		user = User.load(userId, salt, password, followers, photos);
		return user;
	}

	/**
	 * Finds the followUserIds of the given user's followers
	 * @param userId - the user whose followers are to be found
	 * @return followers - a Collection<String> of the followUserIds. 
	 * The Collection will be empty if there are no followers
	 * @throws IOException 
	 */
	//TODO introduce cryptography
	private static Collection<String> findFollowers(String userId) throws IOException {
		//get the followersTxt file
		File followersTxt = new File(databaseRootDirName + fileSeparator + userId + fileSeparator 
				+ followersTxtName);
		//the Collection to store the followUserIds
		Collection<String> followers = cipheredFileToStringCollection(followersTxt);
		return followers;
	}

	/**
	 * Constructs a User object with the given parameters
	 * @param userId
	 * @param salt - the salt used to cipher the password of the user to be found.
	 * @param password - the ciphered password of the user to be found.
	 * @param followers
	 * @param photos
	 */
	private static User load(String userId, String salt, String password, Collection<String> followers,
			Collection<Photo> photos) {
		return new User(userId, salt, password, followers, photos);
	}

	/**********************************************************************************************
	 * insert, update and remove variables and methods
	 **********************************************************************************************
	 */
	
	/**
	 * Creates and inserts a new user with the given credentials. Ciphers the password.
	 * This method is only used by ManUsers. Protects the file with a MAC.
	 * @param localUserId
	 * @param password
	 * @return
	 * @throws IOException 
	 */
	//TODO MAC PROTECT THE FILE
	public static void insert(String localUserId, String password) throws IOException {
		FileWriter fileWriter = new FileWriter(usersTxt, true);
		BufferedWriter buffWriter = new BufferedWriter(fileWriter);
		//add the line in the usersTxt file corresponding to the user
		String line = localUserId + ":" + cipherPassword(password);
		buffWriter.write(line);
		buffWriter.newLine();
		buffWriter.close();
		//TODO MAC PROTECT THE FILE
		//create localUser's directory
		String localUserDirName = databaseRootDirName + fileSeparator + localUserId;
		File localUserDir = new File(localUserDirName);
		//in case the directory doesn't exist yet, create it.
		if (!localUserDir.exists()) {
			localUserDir.mkdir();
			//create the followersTxt file
			new File(localUserDirName + fileSeparator + followersTxtName).createNewFile();
		}
	}

	/**
	 * Adds the followUsers as followers of localUser, in the persistent storage.
	 * @param localUserId
	 * @param followUserIds
	 * @throws IOException 
	 */
	protected static void insertFollowers(String localUserId, String[] followUserIds) throws IOException {
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
	}

	/**
	 * Updates the followersTxt file of the given user, 
	 * replacing its content with the given followers. 
	 * @param localUserId
	 * @param followers
	 * @throws IOException 
	 */
	//TODO introduce cryptography
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
		//TODO CIPHER THE FILE
		buffWriter.close();
	}
	
	/**
	 * Removes the given user from the usersTxt file, removes the user's folder and removes 
	 * the user as follower of any user that currently has him as a follower.
	 * This method is only used by ManUsers.
	 * @requires localUser was already removed from usersTxtContent. 
	 * @param localUserId
	 * @param usersTxtContent - the content of the usersTxt file before the removal.
	 * @throws IOException 
	 */
	//TODO MAC PROTECT THE FILE
	protected static void remove(String localUserId, Collection<String> usersTxtContent) 
			throws IOException {
		//delete old file
		usersTxt.delete();
		//create new file without the given user
		usersTxt.createNewFile();
		FileWriter fileWriter = new FileWriter(usersTxt, true);
		BufferedWriter buffWriter = new BufferedWriter(fileWriter);
		for (String line : usersTxtContent) {
			buffWriter.write(line);
			buffWriter.newLine();
		}
		buffWriter.close();
		//TODO MAC PROTECT THE FILE
		removeAllFollower(localUserId, usersTxtContent);
		//delete the user folder and all its files and sub-directories
		String userDirName = databaseRootDirName + fileSeparator + localUserId;
		Path rootPath = Paths.get(userDirName);     
		final List<Path> pathsToDelete = Files.walk(rootPath).sorted(Comparator.reverseOrder()).
				collect(Collectors.toList());
		for(Path path : pathsToDelete) {
		    Files.deleteIfExists(path);
		}
	}
	
	/**
	 * Updates the given user's password in the usersTxt file.
	 * This method is only used by ManUsers. Deletes the old usersTxt and creates a new one with 
	 * the content of the old one with the given user's password updated.
	 * @requires localUser was already removed from usersTxtContent. 
	 * @param localUserId
	 * @param newPassword
	 * @param usersTxtContent - the content of the usersTxt file before the removal.
	 * @throws IOException 
	 */
	protected static void updatePassword(String localUserId, String newPassword, Collection<String> usersTxtContent) 
			throws IOException {
		//delete old file
		usersTxt.delete();
		//create new file without the given user
		usersTxt.createNewFile();
		FileWriter fileWriter = new FileWriter(usersTxt, true);
		BufferedWriter buffWriter = new BufferedWriter(fileWriter);
		for (String line : usersTxtContent) {
			buffWriter.write(line);
			buffWriter.newLine();
		}
		buffWriter.close();
		User.insert(localUserId, newPassword);
	}
	
	/**
	 * Removes the given user from the followersTxt file of every user in the system.
	 * This method is only used by ManUsers.
	 * Deciphers to read and ciphers again 
	 * @requires localUser was already removed from usersTxt file. 
	 * @param localUserId - the user to be removed.
	 * @param usersTxtContent - the content of the usersTxt file before the removal.
	 * @throws IOException 
	 */
	private static void removeAllFollower(String localUserId, Collection<String> usersTxtContent) 
			throws IOException {
		String userId = null;
		//remove the localUser from the followersTxt file of every user.
		for (String line : usersTxtContent) {
			//the user to remove from.
			userId = line.split(":")[0];
			Collection<String> followers = findFollowers(userId);
			followers.remove(localUserId);
			updateFollowers(userId, followers);
		}
	}

	/**********************************************************************************************
	 * cipher variables , methods and constructors
	 **********************************************************************************************
	 */
	
	/**
	 * Ciphers the password and returnes a ciphered password in the format:
	 * salt:salted_password_hash
	 * @param password
	 * @return cipheredPassword
	 */
	//TODO
	private static String cipherPassword(String password) {
		String cipheredPassword = "salt:" + password;
		return cipheredPassword;
	}
	
	/**
	 * Deciphers the given file and returns its content as a String[]
	 * @param file
	 * @return fileContent - the String[] representation of the given file
	 * @throws IOException 
	 */
	//TODO DECIPHER THE FILE
	private static Collection<String> cipheredFileToStringCollection(File file) 
			throws IOException {
		//get the Collection<String>
		Collection<String> lines = new LinkedList<>();
		FileReader fileReader;
		BufferedReader buffReader = null;
		/*
		 * TODO
		 * DECIPHER THE FILE
		 * GET THE STRING COLLECTION
		 * DELETE THE DECYPHERED FILE AND KEEP THE CYPHERED ONE
		 */
		fileReader = new FileReader(file);
		buffReader = new BufferedReader(fileReader);
		String line;
		while ((line = buffReader.readLine()) != null) {
			lines.add(line);
		}
		buffReader.close();
		return lines;
	}
	
	/**
	 * Deciphers the user's credentials present in the given line according to the default 
	 * strategy.
	 * @param line
	 * @return
	 */
	//TODO
	private static String[] getCipheredCredentials(String line) {
		//TODO DECIPHER LINE
		String[] userCredentials = line.split(":");
		return userCredentials;
	}
	
	/**********************************************************************************************
	 * User variables , methods and constructors
	 **********************************************************************************************
	 */
	private String userId;
	private String salt;
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
	 * @param salt
	 * @param password
	 * @param followers
	 * @param photos
	 */
	private User(String userId, String salt, String password, Collection<String> followers,
			Collection<Photo> photos) {
		this.userId = userId;
		this.salt = salt;
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
	 */
	protected void addFollowers(String localUserId, String[] followUserIds) throws IOException {
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
	 */
	protected void addPhotos(String localUserId, File photosPath) throws IOException {
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
	 * @return this user's salt, used to cipher the password.
	 */
	protected String getSalt() {
		return salt;
	}

	/**
	 * 
	 * @return this user's salted_password_hash
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
	 * @throws IOException 
	 */
	protected void addComment(String commentedUserId, String commenterUserId, String comment, 
			String photoName) throws IOException {
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
	 * @throws IOException 
	 */
	protected void addLike(String likedUserId, String likerUserId, String photoName) throws IOException {
		getPhoto(photoName).addLike(likedUserId, likerUserId, photoName);
	}

	/**
	 * Adds a dislike made by likerUser in this user's photo
	 * @param dislikedUserId - the userId of the dislikedUser
	 * @param dislikerUserId - the userId of the dislikerUser 
	 * @param photoName - the name of this user's photo
	 * @throws IOException 
	 */
	protected void addDislike(String dislikedUserId, String dislikerUserId, String photoName) throws IOException {
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
