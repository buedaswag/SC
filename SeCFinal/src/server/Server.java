package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import myServer4.ServerThread;

public class Server {
	private int port;
	private String address;
	private static FileManager fileManager;
	// Lista de utilizadores (permite manipulacao facil em runtime)
	private static List<User> users;

	// Utilizador actual (temporario; so para testes)
	protected static User currUser;
	// private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy
	// HH:mm");

	/**
	 * Constructor for the Server class
	 * initiates the server which represents the system
	 * ... TO COMPLETE
	 * Inicia o servidor, criando directorios e ficheiros de registo se necessario.
	 * Nao cria interfaces de rede; tudo o que diz respeito a portos, TCP e outras
	 * coisas giras fica ao encargo do handler.
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
		//set up the server, and get the port
		Server server = new Server();
		int port = new Integer(args[1]);

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
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				//set up a thread
				ServerThread newServerThread = new ServerThread(inSoc, server);
				newServerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		//sSoc.close();
	}


	// ================== OPERACOES =================== //
	// Estes metodos comunicam com o FileManager e //
	// actualizam tanto a memoria fisica como a do //
	// programa. //

	/**
	 * Autentica um utilizador, se este existir Caso contrario, cria-o e regista-o
	 * na base de dados
	 * 
	 * @param userid
	 * @param password
	 * @throws IOException
	 * @throws IOException
	 */
	public boolean authenticate(String name, String password) throws IOException {
		// Caso 1: cliente existe
		for (User u : userList) {
			// Password certa?
			if (u.getName().equals(name)) {
				if (u.getPassword().equals(password)) {
					currUser = u;
					return true;
				} else {
					return false;
				}
			}
		}
		// Caso 2: cliente nao existe
		fm.FMaddUser(name, password);
		return true;
	}
	
	/**
	 * checks the photos of the user with the given userid
	 * if he already has a photo with any of the names given in photos,
	 * returns false, otherwise, returns true
	 * @param userid
	 * @param password
	 * @return
	 */
	public boolean checkDuplicatePhotos(String userid, String password, String[] photos) {
		
		//TODO INCOMPLETE!
		
		return false;
		
	}
	
	/**
	 * adds the photos with the given names to the user with the given userid
	 * @param photo
	 */
	public void addPhoto(Photo photo) {
		//TODO
	}

	/**
	 * Lista as fotos de um utilizador
	 * 
	 * @param user
	 *            - O utilizador
	 * @return A String com informacao caso tenha sucesso, null caso contrario
	 */
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

	/**
	 * Obtem a informacao de uma foto
	 * 
	 * @param user
	 *            - O autortttttt da foto
	 * @param photo
	 *            - O nome da foto
	 * @return A String com informacao se teve sucesso, null em caso contrario
	 */
	public String infoPhoto(String user, String photo) {
		if (isFollower(user)) {

		} else {
			return null;
		}
	}

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
	public String addDislike(String user, String photo) {
		if (!isFollower(user)) {
			return null;
		} else {
			fm.addDislike(user, photo);
		}
		return "success";
	}

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
	@SuppressWarnings("unused")
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

	// ================== UTILIDADES ================== //

	public User getByName(String id) {
		for (User u : userList) {
			if (u.getName().equals(id))
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
