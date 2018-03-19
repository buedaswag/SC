//antes de tentes
package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {
	private int port;
	private String address;
	private static FileManager fileManager;
	// Lista de utilizadores (permite manipulacao facil em runtime)
	private static List<User> users;

	// private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy
	// HH:mm");

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
	}

	/**
	 * TODO
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
		// sSoc.close();
	}

	// ================== OPERACOES =================== //
	// Estes metodos comunicam com o FileManager e //
	// actualizam tanto a memoria fisica como a do //
	// programa. //

	/**
	 * gets the user with the given credentials from the List users
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
	 * Autentica um utilizador, se este existir Caso contrario, cria-o e regista-o
	 * na base de dados
	 * 
	 * @param localUserId
	 * @param password
	 * @throws IOException
	 * @throws IOException
	 */
	public boolean authenticate(String localUserId, String password) throws IOException {
		// Caso 1: cliente existe
		for (User u : users) {
			// Password certa?
			if (u.getUserid().equals(localUserId)) {
				if (u.getPassword().equals(password)) {
					return true;
				} else {
					return false;
				}
			}
		}
		// Caso 2: cliente nao existe adicionar ao disco e 'a memoria
		fileManager.FMaddUser(localUserId, password);
		users.add(new User(localUserId, password));
		return true;
	}

	/**
	 * @requires all the photos have been loaded from the file system
	 * @requires the user is authenticated
	 * 
	 * checks the photos of the user with the given localUserId
	 * if he already has a photo with any of the names given in photos,
	 * returns false, otherwise, returns true
	 * @param localUserId
	 * @param password
	 * @return
	 */
	public boolean checkDuplicatePhotos(String localUserId, String password, String[] names) {
		User user = getUser(localUserId);
		return user.hasPhotos(names);
	}

	/**
	 * adds the photos with the given names to the user with the given localUserId,
	 * by adding them to the persistent storange and to the program memory,
	 * and after its done, deletes the photos from the given directory
	 * 
	 * @param localUserId 
	 * @param password
	 * @param names
	 * @param photosPath the path to the photos in the user's temp folder
	 * @throws IOException 
	 */
	public void addPhotos(String localUserId, String password, String[] names,
			File photosPath) throws IOException {
		//get the corresponding user
		User user = getUser(localUserId);
		//adds the photos to the persistent storage
		fileManager.FMmovePhotos(localUserId, names, photosPath);
		//adds the photos to the program memory
		user.addPhotos(names);
	}

	/**
	 * Adds a comment made by user in the commentedUser's photo
	 * 
	 * @requires the user is authenticated
	 * @param comment - the comment to be made
	 * @param localUserId - the localUserId of the user
	 * @param commentedUserid - the userid of the commentedUser 
	 * @param name - the name of the commentedUser's photo
	 * @return "success" if it all went well, null otherwise
	 * @throws IOException 
	 */
	public String addComment(String comment, String localUserId, 
			String commentedUserid, String name) throws IOException {
		//get the user with the given credentials
		User user = getUser(localUserId);
		//get the commented user with the given credentials
		User commentedUser = getUser(commentedUserid);
		if (!commentedUser.isFollower(user)) {
			return null;
		} else {
			//adds comment to the file system
			fileManager.FMaddComment(comment, localUserId, commentedUserid, name);
			//adds comment from user to commentedUser's photo 
			commentedUser.addComment(comment, localUserId, name);		
		}
		return "success";
	}

	/**
	 * Adds a like made by user in the likedUser's photo 
	 * 
	 * @requires the user is authenticated
	 * @param localUserId - the localUserId of the user
	 * @param likedUserid - the userid of the likedUser 
	 * @param name - the name of the likedUser's photo
	 * @return "success" if it all went well, null otherwise
	 * @throws IOException 
	 */
	//TODO falta por a responsabilidade de por na ram aqui 
	public String addLike(String localUserId, String likedUserid,
			String name) throws IOException {
		//get the user with the given credentials
		User user = getUser(localUserId);
		//get the liked user with the given credentials
		User likedUser = getUser(likedUserid);

		if(likedUser.isFollower(user))

			if (!isFollower(user)) {
				return null;
			} else {
				//adds like to the file system
				fileManager.FMaddLike(localUserId, likedUserid, name);
				//adds like from user to likedUser's photo 
				likedUser.addLike(localUserId, name);
			}
		return "success";
	}

	/**
	 * Verifica se o utilizador actual tem user como seguidor
	 * 
	 * @return - idem
	 */
	//TODO
	public boolean isFollower(User user) {
		return user.isFollower(user);
	}

	/**
	 * Adds the followUsers as followers of the user
	 * user with the given localUserId
	 * 
	 * @param localUserId - the localUserId of the user
	 * @param followUserIds - the userids of the followUsers
	 * @return "success" if it all went well, null otherwise
	 * @throws IOException 
	 */
	//TODO
	public String addFollowers(String localUserId, String[] followUserIds) throws IOException {
		//get the user with the given credentials
		User user = getUser(localUserId);
		
		//check if any of the followUsers is already a follower
		if(user.isFollower(followUserIds))
			return null;
		
		//adds like to the file system
		fileManager.FMaddFollowers(followUserIds, localUserId);
		
		//add the followers to the user
		user.addFollowers(Arrays.asList(followUserIds));
		
		return "success";
	}
}
