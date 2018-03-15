package server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

//Threads utilizadas para comunicacao com os clientes
/**
 * Performs user authentication and the requested operation
 * @author antonio, max, miguel
 *
 */
class ServerThread extends Thread {

	private Socket inSoc;
	private Server server;

	/**
	 * Constructor, initializes this ServerThread
	 * @param inSoc
	 * @param server
	 */
	public ServerThread(Socket inSoc, Server server) {
		this.inSoc = inSoc;
		this.server = server;
	}

	/**
	 * Runs this thread. Performs user authentication and the 
	 * requested operation
	 */
	public void run() {
		try {
			ObjectInputStream inStream = new ObjectInputStream(inSoc.getInputStream());
			
			//get the user's credentials
			String userid = null;
			String password = null;
			String[] args = null;
			try {
				userid = (String) inStream.readObject();
				password = (String) inStream.readObject();
				System.out.println("thread: depois de receber a password e o userid");
				authenticate(userid, password);
			//get the arguments for the operation
				args = (String[]) inStream.readObject();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			authenticate(userid, password);
			
			//Execute the requested operation
			executeOperation(userid, password, args, inStream);
			
			//close stream and socket
			inStream.close();
			inSoc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Authenticates this user towards the System
	 * 
	 * @param userid
	 * @param password
	 * @throws IOException
	 */
	private void authenticate(String userid, String password) throws IOException {
		server.authenticate(userid, password);
	}

	/**
	 * Executes the operation requested in args
	 * @param args the operation name and parameters looks like this 
	 * ["addPhoto","sex","drugs","leagueOfLegends"]
	 */
	private void executeOperation(String userid, String password, String[] args, 
			ObjectInputStream inStream) {
		//get the operation to execute
		char opt = args[0].charAt(0);
		String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
		String answer;
		// execute the operation - este bloco so le e passa argumentos
		switch (opt) {
		// Acrescentar fotos
		case 'a': {
			//check for duplicate photos
			if (server.checkDuplicatePhotos(userid, password, newArgs))
				//TODO enviar erro para  o cliente
			else
				server.addPhoto
			break;
		}
		// Listar fotos
		/*
		case 'l': {
			String user = message[1];
			answer = listPhotos(user);
			// Enviar resposta
		}
		// Informacao foto
		case 'i': {
			String user = message[1];
			String photo = message[2];
			answer = infoPhoto(user, photo);
			// Enviar resposta
		}
		// Copiar fotos
		case 'g': {
			String user = message[1];
			answer = savePhotos(user);
			// Enviar resposta
		}
		*/
		// Comentar foto
		case 'c': {
			String comment = message[1];
			String user = message[2];
			String photo = message[3];
			answer = addComment(comment, user, photo);
			// Enviar resposta
		}
		// Botar like
		case 'L': {
			String user = message[1];
			String photo = message[2];
			answer = addLike(user, photo);
			// Enviar resposta
		}
		// Botar dislike
		/*
		case 'D': {
			String user = message[1];
			String photo = message[2];
			answer = addDislike(user, photo);
		}
		*/
		// Adicionar seguidores
		case 'f': {
			String user = message[1];
			String[] followers = message[2].split(",");
			// Followers e do tipo "user1,user2,user3..."
			answer = addFollowers(user, followers);
		}
		// Remover seguidores
		/*
		case 'r': {
			String user = message[1];
			String followers[] = message[2].split(",");
			answer = removeFollowers(user, followers);
		}
		// Operacao ilegal
		default: {
			System.out.println("Foi recebida uma operacao invalida");
		}
		*/
		}
		// Realizar operacao

		// Fechar tudo

	}
}
