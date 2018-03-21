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
		tempPath.mkdir();
	}

	/**
	 * Runs this thread. Performs user authentication and the requested operation
	 */
	public void run() {
		try {
			ObjectInputStream inStream = new ObjectInputStream(inSoc.getInputStream());

			// get the user's credentials
			String localUserId = null;
			String password = null;
			String[] args = null;
			try {
				localUserId = (String) inStream.readObject();
				password = (String) inStream.readObject();
				System.out.println("thread: depois de receber a password e o localUserId");
				authenticate(localUserId, password);
				// get the arguments for the operation
				args = (String[]) inStream.readObject();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			authenticate(localUserId, password);

			// Execute the requested operation
			executeOperation(localUserId, password, args, inStream);

			// close stream and socket
			inStream.close();
			inSoc.close();
			
			//close the thread
			return;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Authenticates this user towards the System
	 * 
	 * @param localUserId
	 * @param password
	 * @throws IOException
	 */
	private void authenticate(String localUserId, String password) throws IOException {
		server.authenticate(localUserId, password);
	}

	/**
	 * Executes the operation requested in args
	 * @param args the operation name and parameters looks like this 
	 * ["a","sex.JPG","drugs.img","leagueOfLegends.png"]
	 * or
	 * ["c","miguel","ferias","que foto tao linda das tuas ferias"]
	 * @throws IOException 
	 */
	//TODO enviar erro para  o cliente
	private void executeOperation(String localUserId, String password, String[] args, 
			ObjectInputStream inStream) throws IOException {
		//get the operation to execute
		char opt = args[0].charAt(0);
		String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
		String answer;
		// execute the operation - este bloco so le e passa argumentos
		switch (opt) {
		// Acrescentar fotos
		case 'a': {
			//check for duplicate photos
			if (server.checkDuplicatePhotos(localUserId, password, newArgs))
				//TODO enviar erro para  o cliente
				System.out.println("duplicate photos");
			else {
				//adicionar as fotos ao sistema de ficheiros
				File photosPath = receivePhotos(localUserId, newArgs, inStream);

				//pedir ao server para ir buscar � temp
				server.addPhotos(localUserId, password, newArgs, photosPath);
			}
			break;
		}
		// Comentar foto
		case 'c': {
			//get the operation parameters 
			String comment = newArgs[0];
			String commentedUserId = newArgs[1];
			String photo = newArgs[2];

			//ask the server to add this comment
			server.addComment(comment, localUserId, commentedUserId, photo);
			//TODO enviar erro para  o cliente
			break;
		}
		// Botar like
		case 'L': {
			String likedUserId = newArgs[0];
			String name = newArgs[1];
			server.addLike(localUserId, likedUserId, name);
			//TODO enviar erro para  o cliente
			break;
		}
		// Adicionar seguidores
		case 'f': {
			server.addFollowers(localUserId, newArgs);
			//TODO enviar erro para  o cliente
			break;
		}
		// Fechar tudo
		}
	}

		/**
		 * Receives the photos from the TCP port and stores them in the
		 * given user's temp folder
		 * @param localUserId
		 * @param newArgs
		 * @throws IOException 
		 */
		private File receivePhotos(String localUserId, String[] newArgs, 
				ObjectInputStream inStream) throws IOException {
			//create path for this user and for his/her photos
			File localUserIdPath = new File (tempPath + "\\" + localUserId);
			localUserIdPath.mkdir();
			File photosPath = new File (localUserIdPath + "\\photos");
			photosPath.mkdir();
			
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
				while((read = inStream.read(buffer, 0, 
						Math.min(buffer.length, remaining))) > 0) {
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
