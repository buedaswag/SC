package server;

import java.io.DataInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;

//Threads utilizadas para comunicacao com os clientes
/**
 * Performs user authentication and the requested operation
 * 
 * @author antonio, max, miguel
 *
 */
class ServerThread extends Thread {
	
	private File tempPath;
	
	
	private Socket inSoc;
	private Server server;

	/**
	 * Constructor, initializes this ServerThread
	 * 
	 * @param inSoc
	 * @param server
	 */
	public ServerThread(Socket inSoc, Server server) {
		this.inSoc = inSoc;
		this.server = server;
		//create the temp directory
		tempPath = new File("temp");
		
	}

	/**
	 * Runs this thread. Performs user authentication and the requested operation
	 */
	public void run() {
		try {
			ObjectInputStream inStream = new ObjectInputStream(inSoc.getInputStream());

			// get the user's credentials
			String userid = null;
			String password = null;
			String[] args = null;
			try {
				userid = (String) inStream.readObject();
				password = (String) inStream.readObject();
				System.out.println("thread: depois de receber a password e o userid");
				authenticate(userid, password);
				// get the arguments for the operation
				args = (String[]) inStream.readObject();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			authenticate(userid, password);

			// Execute the requested operation
			executeOperation(userid, password, args, inStream);

			// close stream and socket
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
	 * ["a","sex","drugs","leagueOfLegends"]
	 * or
	 * ["c","miguel","ferias","que foto tao linda das tuas ferias"]
	 */
	//TODO enviar erro para  o cliente
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
				System.out.println("duplicate photos");
			else {
				//adicionar as fotos ao sistema de ficheiros
				File photosPath = receivePhotos(userid, newArgs, inStream);
				/*
				 * pedir ao server para ir buscar á temp
				 */
				server.addPhotos(userid, password, newArgs, photosPath);
				
			}
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
			//get the operation parameters 
			String comment = args[1];
			String userid = args[2];
			String photo = args[3];
			
			//ask the server to add this comment
			server.addComment(comment, userid, photo);
			//TODO enviar erro para  o cliente
			
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

	/**
	 * Receives the photos from the TCP port and stores them in the
	 * given user's temp folder
	 * @param userid
	 * @param newArgs
	 * @throws IOException 
	 */
	private File receivePhotos(String userid, String[] newArgs, 
			ObjectInputStream inStream) throws IOException {
		//create path for this user and for his/her photos
		File useridPath = new File (tempPath + "\\" + userid);
		File photosPath = new File (useridPath + "\\photos");
		
		/* create the buffer to receive the chunks and the 
		 * FileOutputStream to write to the file
		 */
		FileOutputStream fos;
		byte[] buffer = new byte[1024];
		int filesize = 0, read = 0, remaining = 0, totalRead = 0;
		
		for (String name : newArgs) {
			/*
			 * create FileOutputStream that writes to this user's 
			 * photo temp directory
			 */
			fos = new FileOutputStream(photosPath + "\\" + name);
			
			//read the file size
			filesize = inStream.readInt();
			
			/*
			 * reads the file in chunks and writes the chunks to the 
			 * specified directory
			 */
			remaining = filesize;
			while((read = inStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				totalRead += read;
				remaining -= read;
				fos.write(buffer, 0, read);
			}
			//close FileOutputStream, reset variavles
			fos.close();
			totalRead = 0;
		}
		return photosPath;
	}
}
