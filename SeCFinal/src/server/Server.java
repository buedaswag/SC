package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 */
public class Server {
	private FileManager fileManager;
	// Lista de utilizadores (permite manipulacao facil em runtime)
	private static List<User> users;
	private String needsToBeFollower;
	private String allGood;
	private String alreadyFollower;
	private String notFollower;

	/**
	 * Constructor for the Server class initiates the server which represents the
	 * system ... TO COMPLETE Inicia o servidor, criando directorios e ficheiros de
	 * registo se necessario. Nao cria interfaces de rede; tudo o que diz respeito a
	 * portos, TCP e outras coisas giras fica ao encargo do handler.
	 * 
	 * @throws IOException
	 */
	public Server() throws IOException {
		fileManager = new FileManager();
		users = fileManager.loadUsers();
		needsToBeFollower = "You must be a follower of the given user to do that";
		allGood = "ok";
		alreadyFollower = "At least one of the users given is already a follower";
		notFollower = "At least one of the users given is not a follower";
	}

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// set up the server, and get the port
		Server server = new Server();
		int port = new Integer(args[0]);

		/*
		 * listen to the TCP port and set up a thread for each request
		 */
		ServerSocket sSoc = null;
		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		while (true) {
			try {
				Socket inSoc = sSoc.accept();
				// set up a thread
				ServerThread newServerThread = new ServerThread(inSoc, server);
				newServerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	// ================== OPERATIONS =================== //
	// These methods communicate with the fileManage and //
	// updates the physical memory and the programs memory //

	/**
	 * gets the user with the given credentials from the List users
	 * 
	 * @requires the user is authenticated
	 * @param localUserId
	 * @param password
	 * @return the user, or null if its not on the List
	 */
	private User getUser(String localUserId) {
		for (User u : users)
			if (u.getUserid().equals(localUserId))
				return u;
		return null;
	}

	/**
	 * Authenticates the user if he exists, otherwise creates him and adds him to
	 * the database
	 * 
	 * @param localUserId
	 * @param password
	 * @throws IOException
	 * @throws IOException
	 */
	public boolean authenticate(String localUserId, String password) throws IOException {
		// Case 1 : the user exists
		for (User u : users) {
			// Is the password correct?
			if (u.getUserid().equals(localUserId)) {
				if (u.getPassword().equals(password)) {
					return true;
				} else {
					return false;
				}
			}
		}
		// Case 2: client doesnt exist, adds him to the database and program memory
		fileManager.FMaddUser(localUserId, password);
		users.add(new User(localUserId, password));
		return true;
	}

	/**
	 * @requires all the photos have been loaded from the file system
	 * @requires the user is authenticated
	 * 
	 *           checks the photos of the user with the given localUserId if he
	 *           already has a photo with any of the names given
	 * @param localUserId
	 * @param password
	 * @return true - if he already has a photo with any of the names given, false -
	 *         otherwise
	 */
	public boolean checkDuplicatePhotos(String localUserId, String[] names) {
		User user = getUser(localUserId);
		return user.hasPhotos(names);
	}

	/**
	 * adds the photos with the given names to the user with the given localUserId,
	 * by adding them to the persistent storange and to the program memory, and
	 * after its done, deletes the photos from the given directory
	 * 
	 * @requires a check was performed for duplicate photos
	 * @param localUserId
	 * @param password
	 * @param names
	 * @param photosPath
	 *            the path to the photos in the user's temp folder
	 * @throws IOException
	 */
	public String addPhotos(String localUserId, String password, String[] names, File photosPath) throws IOException {
		// get the corresponding user
		User user = getUser(localUserId);

		// adds the photos to the persistent storage
		fileManager.FMmovePhotos(localUserId, names, photosPath);
		// adds the photos to the program memory
		user.addPhotos(names);
		return allGood;
	}

	/**
	 * Adds a comment made by user in the commentedUser's photo
	 * 
	 * @requires the user is authenticated
	 * @param comment
	 *            - the comment to be made
	 * @param localUserId
	 *            - the localUserId of the user
	 * @param commentedUserid
	 *            - the userid of the commentedUser
	 * @param name
	 *            - the name of the commentedUser's photo
	 * @return allGood if it all went well, needsToBeFollower otherwise
	 * @throws IOException
	 */
	public String addComment(String comment, String localUserId, String commentedUserid, String name)
			throws IOException {
		// get the user with the given credentials
		User localUser = getUser(localUserId);
		// get the commented user with the given credentials
		User commentedUser = getUser(commentedUserid);

		// check if the localUser is not a follower
		if (!commentedUser.follows(localUser))
			return needsToBeFollower;

		// adds comment to the file system
		fileManager.FMaddComment(comment, localUserId, commentedUserid, name);
		// adds comment from localUser to commentedUser's photo
		commentedUser.addComment(comment, localUserId, name);

		return allGood;
	}

	/**
	 * Adds a like made by user in the likedUser's photo
	 * 
	 * @requires the user is authenticated
	 * @param localUserId
	 *            - the localUserId of the user
	 * @param likedUserid
	 *            - the userid of the likedUser
	 * @param name
	 *            - the name of the likedUser's photo
	 * @return allGood if it all went well, needsToBeFollower otherwise
	 * @throws IOException
	 */
	public String addLike(String localUserId, String likedUserid, String name) throws IOException {
		// get the user with the given credentials
		User user = getUser(localUserId);
		// get the liked user with the given credentials
		User likedUser = getUser(likedUserid);

		// check if the localUser is not a follower
		if (!likedUser.follows(user))
			return needsToBeFollower;

		// adds like to the file system
		fileManager.FMaddLike(localUserId, likedUserid, name);
		// adds like from user to likedUser's photo
		likedUser.addLike(localUserId, name);
		return allGood;
	}

	/**
	 * Adds a dislike made by user in the disLikedUser's photo
	 * 
	 * @requires the user is authenticated
	 * @param localUserId
	 *            - the localUserId of the user
	 * @param disLikedUserid
	 *            - the userid of the dislikedUser
	 * @param name
	 *            - the name of the likedUser's photo
	 * @return "success" if it all went well, needsToBeFollower otherwise
	 * @throws IOException
	 */
	public String addDislike(String localUserId, String dislikedUserid, String name) throws IOException {
		// get the user with the given credentials
		User localUser = getUser(localUserId);
		// get the liked user with the given credentials
		User dislikedUser = getUser(dislikedUserid);

		// check if the localUser is not a follower
		if (!dislikedUser.follows(localUser))
			return needsToBeFollower;
		// adds like to the file system
		fileManager.FMaddDislike(localUserId, dislikedUserid, name);
		// adds like from user to likedUser's photo
		dislikedUser.addDislike(localUserId, name);
		return allGood;
	}

	/**
	 * Adds the followUsers as followers of the user user with the given localUserId
	 * 
	 * @param localUserId
	 *            - the localUserId of the user
	 * @param followUserIds
	 *            - the userids of the followUsers
	 * @return allGood if it all went well, alreadyFollower otherwise
	 * @throws IOException
	 */
	public String addFollowers(String localUserId, String[] followUserIds) throws IOException {
		// get the user with the given credentials
		User user = getUser(localUserId);

		// check if any of the followUsers is already a follower
		if (user.follows(followUserIds))
			return alreadyFollower;

		// adds like to the file system
		fileManager.FMaddFollowers(followUserIds, localUserId);

		// add the followers to the user
		user.addFollowers(Arrays.asList(followUserIds));

		return allGood;
	}

	/**
	 * Removes the followUsers from the user user with the given localUserId
	 * 
	 * @param localUserId
	 *            - the localUserId of the user
	 * @param followUserIds
	 *            - the userids of the followUsers
	 * @return allGood if it all went well, notFollower otherwise
	 * @throws IOException
	 */
	public String removeFollowers(String localUserId, String[] followUserIds) throws IOException {
		// get the user with the given credentials
		User localUser = getUser(localUserId);

		// check if any of the followUsers is not a follower of the localUser
		if (localUser.isNotFollower(followUserIds))
			return notFollower;

		fileManager.FMremoveFollowers(followUserIds, localUserId);

		// removes the followers from the user
		localUser.removeFollowers(Arrays.asList(followUserIds));

		return allGood;
	}

	/**
	 * Lists all the photos and upload dates of the listedUser
	 * 
	 * @param localUserId
	 *            - the localUserId of the user
	 * @param listedUserid
	 *            - the userid of the listedUser
	 * @return allGood if it all went well, notFollower otherwise
	 */
	public String listPhotos(String localUserId, String listedUserid) {
		// get the user with the given credentials
		User localUser = getUser(localUserId);
		User listedUser = getUser(listedUserid);

		// check if the localUser is not a follower
		if (!listedUser.follows(localUser))
			return needsToBeFollower;

		// Builds the string to be sent to the client
		StringBuilder sbuilder = new StringBuilder();
		List<Photo> listedUserPhotos = listedUser.getPhotos();

		// Iterates over the user's photos and appends the upload date
		for (Photo p : listedUserPhotos) {
			sbuilder.append(p.getName() + " - " + p.getDate() + "\n");

		}

		// Conversion to string and return
		return sbuilder.toString();
	}

	/**
	 * Lists all the comments, likes and dislikes of the photo from the user
	 * 
	 * @param localUserId
	 *            - the localUserId of the user
	 * @param listedUserid
	 *            - the userid of the listedUser
	 * @return allGood if it all went well, notFollower otherwise
	 */
	public String getInfoPhoto(String localUserId, String listedUserid, String photo) {
		// get the user with the given credentials
		User localUser = getUser(localUserId);
		User listedUser = getUser(listedUserid);

		// check if the localUser is not a follower
		if (!listedUser.follows(localUser))
			return needsToBeFollower;

		// Creates auxiliary variables
		StringBuilder sb = new StringBuilder();
		Photo p = listedUser.getPhoto(photo);

		// Info to be fetched from the photo
		int likes = p.getTotalLikes();
		int dislikes = p.getTotalDislikes();
		Collection<Comment> commentList = p.getComments();

		// Generate answer
		sb.append("Likes: " + likes + "\n");
		sb.append("Dislikes: " + dislikes + "\n");
		sb.append("Comentarios: " + "\n");
		for (Comment c : commentList) {
			sb.append(c.getComment() + "\n");
		}

		String info = sb.toString();
		return info;
	}

	/**
	 * Copies all the photos from copiedUser to localUser
	 * 
	 * @param localUserId
	 *            - the localUserId of the user
	 * @param copiedUserId
	 *            - the userid of the listedUser
	 * @return allGood if it all went well, notFollower otherwise
	 * @throws IOException
	 */
	public String savePhotos(String localUserId, String copiedUserId) throws IOException {
		// Gets the given users
		User localUser = getUser(localUserId);
		User copiedUser = getUser(copiedUserId);

		// check if the localUser is not a follower
		if (!copiedUser.follows(localUser))
			return needsToBeFollower;

		// add to disk
		fileManager.FMsavePhotos(localUserId, copiedUserId);

		// add to memory
		for (Photo p : copiedUser.getPhotos())
			localUser.addPhoto(p);

		return allGood;
	}
}
