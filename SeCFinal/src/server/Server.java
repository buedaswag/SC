package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Server {
	private int port;
	private String address;
	private static FileManager fm;
	// Lista de utilizadores (permite manipulacao facil em runtime)
	private static List<User> users;

	// Utilizador actual (temporario; so para testes)
	protected static User currUser;
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
		fm = new FileManager();
		users = fm.loadUsers();
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
		int port = Integer.parseInt(args[1]);

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
	 * @param userid
	 * @param password
	 * @return the user, or null if its not on the List
	 */
	private User getUser(String userid, String password) {
		for (User u : users)
			if (u.getUserid().equals(userid))
				return u;
		return null;
	}
	
	/**
	 * @requires the user exists
	 * 
	 * @param userid the userid to be checked
	 * @param password the password to be checked
	 * @return true if the password is correct
	 */
	private boolean checkPassword(String userid, String password) {
		boolean result = false;
		for (User u : users) {
			// Password certa?
			if (u.getUserid().equals(userid)) {
				if (u.getPassword().equals(password))
					result = true;
				else
					result = false;
			}
		}
		return result;
	}

	/**
	 * Autentica um utilizador, se este existir Caso contrario, cria-o e regista-o
	 * na base de dados
	 * 
	 * @param userid
	 * @param password
	 * @throws IOException
	 * @throws IOException
	 */
	public boolean authenticate(String userid, String password) throws IOException {
		
		if (checkPassword(userid, password)) {			
			currUser = getUser(userid, password);
			return true;
			}else {
				return false;
			}
		
		// Caso 2: cliente nao existe
		fm.FMaddUser(userid, password);
		return true;
	}

	/**
<<<<<<< HEAD
	 * @requires all the photos have been loaded from the file system
	 * @requires the user is authenticated
	 * 
	 * checks the photos of the user with the given userid
	 * if he already has a photo with any of the names given in photos,
	 * returns false, otherwise, returns true
=======
	 * checks the photos of the user with the given userid if he already has a photo
	 * with any of the names given in photos, returns false, otherwise, returns true
>>>>>>> branch 'master' of https://github.com/buedaswag/SCprivate.git
	 * 
	 * @param userid
	 * @param password
	 * @return
	 */
	public boolean checkDuplicatePhotos(String userid, String password, String[] names) {
		User user = getUser(userid, password);
		return user.hasPhotos(names);
		
	}	



	public void addPhotos(String userid, String password, String[] names,
			File photosPath) {
		User user = getUser(userid, password);
		//adds the photos to this user
		user.addPhotos(names, photosPath);

	}
	
	public void addPhoto(Photo photo) {
		// TODO
	}

	/**
	 * Lista as fotos de um utilizador
	 * 
	 * @param user
	 *            - O utilizador
	 * @return A String com informacao caso tenha sucesso, null caso contrario
	 */
	/*
	public String listPhotos(String user) {
		// User existe?
		User temp = getByName(user);
		if (temp == null)
			return null;
		ArrayList<Photo> photos = null;

		// Utilizador segue user?
		if (isFollower(user)) {
			photos = fm.FMlistPhotos(user);
		} else {
			return null;
		}

		// Preencher lista de informacao
		String list = new String();
		for (Photo p : photos) {
			list.concat(p.getName() + " - " + p.getDate());// + p.getDate().
		}
		return list;
	}
	*/
	/**
	 * Obtem a informacao de uma foto
	 * 
	 * @param user
	 *            - O autortttttt da foto
	 * @param photo
	 *            - O nome da foto
	 * @return A String com informacao se teve sucesso, null em caso contrario
	 */
	/*
	public String infoPhoto(String user, String photo) {
		if (isFollower(user)) {

		} else {
			return null;
		}
	}
	*/
	/**
	 * Copia as fotos de um utilizador para o cliente actual
	 * 
	 * @param user
	 *            - O utilizador
	 */
	public void savePhotos(String user) {

	}

	/**
	 * Acrescenta um comentario a uma foto de um utilizador
	 * 
	 * @param comment
	 *            - O comentario
	 * @param user
	 *            - O utilizador
	 * @param photo
	 *            - A foto
	 * @return "success" caso tenha sucesso, null caso contrario
	 */
	public String addComment(String comment, String user, String photo) {
		if (!isFollower(user)) {
			return null;
		} else {
			fm.FMaddComment(comment, user, photo);
		}
		return "success";
	}

	/**
	 * Adiciona um like a uma foto de um utilizador
	 * 
	 * @param user
	 *            - O utilizador
	 * @param photo
	 *            - A foto
	 * @return "success" caso tenha sucesso, null caso contrario
	 */
	public String addLike(String user, String photo) {
		if (!isFollower(user)) {
			return null;
		} else {
			fm.FMaddLike(user, photo);
		}
		return "success";
	}

	/**
	 * Adiciona um dislike a uma foto de um utilizador
	 * 
	 * @param user
	 *            - O utilizador
	 * @param photo
	 *            - A foto
	 * @return "success" caso tenha sucesso, null caso contrario
	 */
	/*
	public String addDislike(String user, String photo) {
		if (!isFollower(user)) {
			return null;
		} else {
			fm.addDislike(user, photo);
		}
		return "success";
	}
	*/
	/**
	 * Adiciona uma lista de utilizadores como seguidores
	 * 
	 * @param user
	 *            - O utilizador
	 * @param photo
	 *            - Os futuros seguidores
	 * @return "success" caso tenha sucesso, null caso contrario
	 */
	public String addFollowers(String user, String[] users) {
		// Conversao da lista de nomes uma lista de seguidores
		ArrayList<String> temp = new ArrayList<String>();
		@SuppressWarnings("unused")
		User u;
		for (String s : users) {
			if ((u = Server.getByName(s)) != null) {
				temp.add(s);
			} else {
				return null;
			}
		}
		// O utilizador actual e seguidor do currUser?
		if (isFollower(user)) {
			return null;
		}
		// Algum dos utilizadores a acrescentar ja e seguidor?
		for (String s : temp) {
			if (isFollower(s))
				return null;
		}
		// Actualizacao na memoria de execucao
		currUser.addFollowers(temp);
		// Actualizacao na memoria fisica
		fm.addFollowers(user, users);
		return "success";
	}

	/**
	 * Remove uma lista de utilizadores como seguidores
	 * 
	 * @param user
	 *            - O utilizador
	 * @param photo
	 *            - Os seguidores a remover
	 * @return "success" caso tenha sucesso, null caso contrario
	 */
	
	public String removeFollowers(String user, String[] users) {
		// Conversao da lista de nomes uma lista de seguidores
		ArrayList<String> temp = new ArrayList<String>();
		User u;
		for (String s : users) {
			if ((u = Server.getByName(s)) != null) {
				temp.add(s);
			} else {
				return null;
			}
		}
		// O utilizador actual e seguidor do currUser?
		if (isFollower(user)) {
			return null;
		}
		// Algum dos utilizadores a remover nao e seguidor?
		for (String s : temp) {
			if (!isFollower(s))
				return null;
		}
		// Actualizacao na memoria de execucao
		currUser.addFollowers(temp);
		// Actualizacao na memoria fisica
		fm.addFollowers(user, users);
		return "success";
	}

	// ================== UTILIDADES ==================

	public User getByName(String id) {
		for (User u : users) {
			if (u.getUserid().equals(id))
				return u;
		}
		return null;
	}
	
	/**
	 * Verifica se o utilizador actual tem user como seguidor
	 * 
	 * @return - idem
	 */
	public boolean isFollower(String user) {
		for (String f : currUser.getFollowers()) {
			if (f.equals(user))
				return true;
		}
		return false;
	}
}
