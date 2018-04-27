package server;

import java.io.File;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 */
public class Server {
	private static Server server = null;
	private static SecretKey SECRET_KEY = null;
	private static Map<String, User> users;
	private String needsToBeFollower = "You must be a follower of the given user to do that";
	private String allGood = "ok";
	private String alreadyFollower = "At least one of the users given is already a follower";
	private String notFollower = "At least one of the users given is not a follower";

	/**
	 * Constructor for the Server class initiates the server which represents the
	 * system ... TO COMPLETE Inicia o servIdor, criando directorios e ficheiros de
	 * registo se necessario. Nao cria interfaces de rede; tudo o que diz respeito a
	 * portos, TCP e outras coisas giras fica ao encargo do handler.
	 * @throws Exception 
	 */
	private Server() throws Exception {
		users = User.findAll();
	}

	/**
	 * 
	 * @return server - The single instance of Class Server
	 * @throws Exception 
	 */
	public static Server getInstance() throws Exception {
		if(server == null)
			server = new Server();
		return server;
	}

	/**
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Server.getInstance();
		//get the port
		int port = new Integer(args[0]);
		
		System.setProperty("javax.net.ssl.keyStore", "server.keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");

		/*
		 * listen to the SSL port and set up a thread for each request
		 */
		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		ServerSocket sSoc = null;
		
		try {
			sSoc = (SSLServerSocket) ssf.createServerSocket(port);
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
	 * @throws IOException 
	 */
	protected boolean authenticate(String localUserId, String password) throws IOException{
		// Case 1 : the user exists
		if (users.containsKey(localUserId)) {
			if (users.get(localUserId).getPassword().equals(password)) {
				return true;
			} else {
				System.out.println("Wrong password!");
				return false;
			}
		}
		// Case 2: user doesn't exist, print an error
		System.out.println("Unregistered user!");
		return false;
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
	protected String addPhotos(String localUserId,File photosPath) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException {
		// get the corresponding user
		User localUser = users.get(localUserId);
		// inserts the photos to this user
		localUser.addPhotos(localUserId, photosPath);
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
	 * @throws Exception 
	 */
	protected String addComment(String comment, String localUserId, String commentedUserId, 
			String photoName) throws Exception {
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
	 * @throws Exception 
	 */
	protected String addLike(String localUserId, String likedUserId, String photoName) throws Exception {
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
	 * @throws Exception 
	 */
	protected String addDislike(String localUserId, String dislikedUserId, String photoName) throws Exception {
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
	protected String addFollowers(String localUserId, String[] followUserIds) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, SignatureException {
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
	 * Removes the followUsers from the user with the given localUserId
	 * @param localUserId - the localUserId of the user
	 * @param followUserIds - the userIds of the followUsers
	 * @return allGood if it all went well, alreadyFollower otherwise
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
	protected String removeFollowers(String localUserId, String[] followUserIds) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException {
		// get the user with the given credentials
		User localUser = users.get(localUserId);
		// check if any of the followUsers is not a follower of the localUser
		if (localUser.isNotFollowed(followUserIds)) {
			return notFollower;
		}
		// removes the followers from the user
		localUser.removeFollowers(localUserId, followUserIds);
		return allGood;
	}

	/**
	 * Lists all the photos and upload dates of the listedUser.
	 * @param localUserId - the localUserId of the user
	 * @param listedUserId - the userId of the listedUser
	 * @return allGood if it all went well, notFollower otherwise
	 */
	protected String listPhotos(String localUserId, String listedUserId) {
		// get the listedUser with the given credentials
		User listedUser = users.get(listedUserId);
		// check if the localUser is not a follower
		if (!listedUser.isFollowed(localUserId)) {
			return needsToBeFollower;
		}
		//list the user's photos according to a specific format
		return listedUser.listPhotos();
	}

	/**
	 * Lists all the comments, likes and dislikes of the photo from the user
	 * @param localUserId - the localUserId of the user
	 * @param listedUserId - the userId of the listedUser
	 * @return allGood if it all went well, notFollower otherwise
	 */
	protected String getInfoPhoto(String localUserId, String listedUserId, String photoName) {
		// get the listedUser with the given credentials
		User listedUser = users.get(listedUserId);
		// check if the localUser is not a follower
		if (!listedUser.isFollowed(localUserId)) {
			return needsToBeFollower;
		}
		return listedUser.getInfoPhoto(photoName);
	}

	/**
	 * Copies all the photos from copiedUser to localUser.
	 * @param localUserId - the localUserId of the user
	 * @param copiedUserId - the userId of the copiedUser
	 * @return allGood if it all went well, notFollower otherwise
	 * @throws IOException 
	 */
	protected String copyPhotos(String localUserId, String copiedUserId) throws IOException {
		// get the copiedUser and the localUser with the given credentials
		User copiedUser = users.get(copiedUserId);
		User localUser = users.get(localUserId);
		// check if the localUser is not a follower
		if (!copiedUser.isFollowed(localUserId)) {
			return needsToBeFollower;
		}
		localUser.copyPhotos(localUserId, copiedUserId, copiedUser);
		return allGood;
	}
	
	public SecretKey getSecretKey() {
		return SECRET_KEY;
	}
	
}
