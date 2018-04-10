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
	/**
	 * TO LOCK METHODS USE THIS: (for save and update methods)
	 * https://docs.oracle.com/javase/tutorial/essential/concurrency/locksync.html
protected class MsLunch {
    private long c1 = 0;
    private long c2 = 0;
    private Object lock1 = new Object();
    private Object lock2 = new Object();

    protected void inc1() {
        synchronized(lock1) {
            c1++;
        }
    }

    protected void inc2() {
        synchronized(lock2) {
            c2++;
        }
    }
}
	 */
	private static Server server = new Server();
	private static Map<String, User> users;
	private String needsToBeFollower;
	private String allGood;
	private String alreadyFollower;
	private String notFollower;

	/**
	 * Constructor for the Server class initiates the server which represents the
	 * system ... TO COMPLETE Inicia o servIdor, criando directorios e ficheiros de
	 * registo se necessario. Nao cria interfaces de rede; tudo o que diz respeito a
	 * portos, TCP e outras coisas giras fica ao encargo do handler.
	 */
	private Server() {
		users = User.findAll();
		needsToBeFollower = "You must be a follower of the given user to do that";
		allGood = "ok";
		alreadyFollower = "At least one of the users given is already a follower";
		notFollower = "At least one of the users given is not a follower";
	}

	/**
	 * 
	 * @return server - The single instance of Class Server
	 */
	protected static Server getInstance() {
		return server;
	}

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	protected static void main(String[] args) throws IOException {
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
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * Authenticates the user if he exists, otherwise creates him and inserts him.
	 * @param localUserId
	 * @param password
	 * @return true if the user was successfully authenticated or false if the password is wrong.
	 */
	protected boolean authenticate(String localUserId, String password){
		// Case 1 : the user exists
		if (users.containsKey(localUserId)) {
			if (users.get(localUserId).getPassword().equals(password)) {
				return true;
			} else {
				return false;
			}
		}
		// Case 2: user doesn't exist, adds him to the database and program memory
		users.put(localUserId, User.insert(localUserId, password));
		return true;
	}

	/**
	 * Checks the photos of the user with the given localUserId if he
	 * already has a photo with any of the names given.
	 * @requires the user is authenticated
	 * @param localUserId
	 * @param password
	 * @return true - if he already has a photo with any of the names given, false - otherwise
	 */
	protected boolean checkDuplicatePhotos(String localUserId, String[] photoNames) {
		User localUser = users.get(localUserId);
		return localUser.hasPhotos(photoNames);
	}

	/**
	 * Inserts the photos with the given names to the user with the given localUserId, and
	 * after its done, deletes the photos from the given directory. 
	 * @requires a check was performed for duplicate photos
	 * @param localUserId
	 * @param password
	 * @param photoNames
	 * @param photosPath the path to the photos in the user's temp folder
	 */
	protected String addPhotos(String localUserId, String[] photoNames, File photosPath) throws IOException {
		// get the corresponding user
		User localUser = users.get(localUserId);
		// adds the photos to the persistent storage and the program memory
		localUser.addPhotos(localUserId, photoNames, photosPath);
		return allGood;
	}

	/**
	 * Inserts a comment made by user in the commentedUser's photo
	 * @requires the user is authenticated
	 * @param comment - the comment to be made
	 * @param localUserId - the localUserId of the user
	 * @param commentedUserId - the userId of the commentedUser
	 * @param photoName - the name of the commentedUser's photo
	 * @return allGood if it all went well, needsToBeFollower otherwise
	 */
	protected String addComment(String comment, String localUserId, String commentedUserId, 
			String photoName) {
		// get the commented user with the given credentials
		User commentedUser = users.get(commentedUserId);
		// check if the localUser is not a follower
		if (!commentedUser.isFollowed(localUserId)) {
			return needsToBeFollower;
		}
		String commenterUserId = localUserId;
		// adds comment to the file system and to memory
		commentedUser.addComment(commentedUserId, commenterUserId, comment, photoName);
		return allGood;
	}

	/**
	 * Inserts a like made by user in the likedUser's photo
	 * @requires the user is authenticated
	 * @param localUserId - the localUserId of the user
	 * @param likedUserId - the userId of the likedUser
	 * @param photoName - the name of the likedUser's photo
	 * @return allGood if it all went well, needsToBeFollower otherwise
	 */
	protected String addLike(String localUserId, String likedUserId, String photoName) {
		// get the liked user with the given credentials
		User likedUser = users.get(likedUserId);
		// check if the localUser is not a follower
		if (!likedUser.isFollowed(localUserId)) {
			return needsToBeFollower;
		}
		String likerUserId = localUserId;
		// inserts a like from localUser to likedUser's photo
		likedUser.addLike(likedUserId, likerUserId, photoName);
		return allGood;
	}

	/**
	 * Inserts a dislike made by user in the dislikedUser's photo
	 * @requires the user is authenticated
	 * @param localUserId - the localUserId of the user
	 * @param dislikedUserId - the userId of the dislikedUser
	 * @param photoName - the name of the dislikedUser's photo
	 * @return allGood if it all went well, needsToBeFollower otherwise
	 */
	protected String addDislike(String localUserId, String dislikedUserId, String photoName) {
		// get the dislikedUser with the given credentials
		User dislikedUser = users.get(dislikedUserId);
		// check if the localUser is not a follower
		if (!dislikedUser.isFollowed(localUserId)) {
			return needsToBeFollower;
		}
		String dislikerUserId = localUserId;
		// inserts a like from localUser to likedUser's photo
		dislikedUser.addDislike(dislikedUserId, dislikerUserId, photoName);
		return allGood;
	}

	/**
	 * Inserts the followUsers as followers of the user with the given localUserId
	 * @param localUserId - the localUserId of the user
	 * @param followUserIds - the userIds of the followUsers
	 * @return allGood if it all went well, alreadyFollower otherwise
	 */
	protected String addFollowers(String localUserId, String[] followUserIds) {
		// get the user with the given credentials
		User localUser = users.get(localUserId);
		// check if any of the followUsers is already a follower
		if (localUser.isFollowed(followUserIds)) {
			return alreadyFollower;
		}
		// add the followers to the user
		localUser.addFollowers(localUserId, followUserIds);
		return allGood;
	}

	/**
	 * Removes the followUsers from the user user with the given localUserId
	 * 
	 * @param localUserId
	 *            - the localUserId of the user
	 * @param followUserIds
	 *            - the userIds of the followUsers
	 * @return allGood if it all went well, notFollower otherwise
	 * @throws IOException
	 */
	protected String removeFollowers(String localUserId, String[] followUserIds) throws IOException {
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
	 * @param listedUserId
	 *            - the userId of the listedUser
	 * @return allGood if it all went well, notFollower otherwise
	 */
	protected String listPhotos(String localUserId, String listedUserId) {
		// get the user with the given credentials
		User localUser = getUser(localUserId);
		User listedUser = getUser(listedUserId);

		// check if the localUser is not a follower
		if (!listedUser.isFollowed(localUser))
			return needsToBeFollower;

		// Builds the string to be sent to the client
		StringBuilder sbuilder = new StringBuilder();
		Collection<Photo> listedUserPhotos = listedUser.getPhotos();

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
	 * @param listedUserId
	 *            - the userId of the listedUser
	 * @return allGood if it all went well, notFollower otherwise
	 */
	protected String getInfoPhoto(String localUserId, String listedUserId, String photo) {
		// get the user with the given credentials
		User localUser = getUser(localUserId);
		User listedUser = getUser(listedUserId);

		// check if the localUser is not a follower
		if (!listedUser.isFollowed(localUser))
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
	 *            - the userId of the listedUser
	 * @return allGood if it all went well, notFollower otherwise
	 * @throws IOException
	 */
	protected String savePhotos(String localUserId, String copiedUserId) throws IOException {
		// Gets the given users
		User localUser = getUser(localUserId);
		User copiedUser = getUser(copiedUserId);

		// check if the localUser is not a follower
		if (!copiedUser.isFollowed(localUser))
			return needsToBeFollower;

		// add to disk
		fileManager.FMsavePhotos(localUserId, copiedUserId);

		// add to memory
		for (Photo p : copiedUser.getPhotos())
			localUser.addPhoto(p);

		return allGood;
	}
}
