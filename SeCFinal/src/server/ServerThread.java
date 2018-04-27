package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import crypto.Crypto;

//Threads utilizadas para comunicacao com os clientes
/**
 * Performs user authentication and the requested operation
 * 
 * @author antonio, max, miguel
 *
 */
class ServerThread extends Thread {

	private File tempPath;

	private Socket socket;
	private ObjectInputStream inStream;
	private static String fileSeparator = System.getProperty("file.separator");

	/**
	 * Constructor, initializes this ServerThread
	 * 
	 * @param socket
	 * @param server
	 * @throws IOException 
	 */
	protected ServerThread(Socket socket) throws IOException {
		this.socket = socket;
		inStream = new ObjectInputStream(socket.getInputStream());
		//create the temp directory
		tempPath = new File("temp");
		tempPath.mkdir();
	}

	/**
	 * Runs this thread. Performs user authentication and the requested operation
	 */
	public void run() {
		try {
			// get the user's credentials
			String localUserId = null;
			String password = null;
			String[] args = null;
			try {
				localUserId = (String) inStream.readObject();
				password = (String) inStream.readObject();
				System.out.println("thread: after receiving the password and the localUserId");
				authenticate(localUserId, password);
				// get the arguments for the operation
				args = (String[]) inStream.readObject();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//TODO wrong password
			authenticate(localUserId, password);

			// Execute the requested operation
			executeOperation(localUserId, args, inStream);

			// close stream and socket
			inStream.close();
			socket.close();

			//close the thread
			System.out.println("thread: closing");
			return;

		} catch (IOException | InvalidKeyException | UnrecoverableKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | KeyStoreException | CertificateException | NoSuchProviderException | SignatureException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sends an error message to the socket, if !error.equals("ok")
	 * @param error - the error message
	 * @throws IOException 
	 */
	private void sendError(String error) throws IOException {
		ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		outStream.writeObject(error);
	}

	/**
	 * Authenticates this user towards the System
	 * 
	 * @param localUserId
	 * @param password
	 * @throws Exception 
	 */
	private void authenticate(String localUserId, String password) throws Exception {
		Server.getInstance().authenticate(localUserId, password);
	}

	/**
	 * Executes the operation requested in args
	 * @param args the operation name and parameters looks like this 
	 * ["a","sex.JPG","drugs.img","leagueOfLegends.png"]
	 * or
	 * ["c","miguel","ferias","que foto tao linda das tuas ferias"]
	 * @throws Exception 
	 */
	private void executeOperation(String localUserId, String[] args, 
			ObjectInputStream inStream) throws Exception {
		//get the operation to execute
		char opt = args[0].charAt(0);
		String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
		String result;
		// execute the operation - este bloco so le e passa argumentos
		switch (opt) {
		// Acrescentar fotos
		case 'a': {
			/*
			 * ask the server to add these photos,
			 * and sends an error message if they cannot be added
			 */
			if (Server.getInstance().checkDuplicatePhotos(localUserId, newArgs))
				sendError("You already have at least one photo with one of the names given.");
			else {
				//tells the server that the photos can be added
				sendError("ok");
				//receives the photos and stores them in the corresponding user's temp folder
				File photosPath = receivePhotos(localUserId, newArgs, inStream);
				//add the photos to the server
				Server.getInstance().addPhotos(localUserId, photosPath);

			}
			break;
		}
		// Comentar foto
		case 'c': {
			//get the operation parameters 
			String comment = newArgs[0];
			String commentedUserId = newArgs[1];
			String photoName = newArgs[2];

			/*
			 * ask the server to add this comment,
			 * and sends an error message if the localUser is not a follower
			 */
			result = Server.getInstance().addComment(comment, localUserId, commentedUserId, photoName);
			sendError(result);
			break;
		}
		case 'L': {
			String likedUserId = newArgs[0];
			String photoName = newArgs[1];
			result = Server.getInstance().addLike(localUserId, likedUserId, photoName);
			sendError(result);
			break;
		}
		case 'D' : {
			String dislikedUserid = newArgs[0];
			String photoName = newArgs[1];
			result = Server.getInstance().addDislike(localUserId, dislikedUserid, photoName);
			sendError(result);
			break;
		}
		case 'f': {
			result = Server.getInstance().addFollowers(localUserId, newArgs);
			sendError(result);
			break;
		}
		case 'r' : {
			result = Server.getInstance().removeFollowers(localUserId, newArgs[0].split(","));
			sendError(result);
			break;
		}
		case 'l' : {
			String listedUserid = newArgs[0];
			result = Server.getInstance().listPhotos(localUserId, listedUserid);
			sendError(result);
			break;
		}
		case 'i' : {
			String listedUserid = newArgs[0];
			String photoName = newArgs[1];
			result = Server.getInstance().getInfoPhoto(localUserId, listedUserid, photoName);
			sendError(result);
			break;
		}
		case 'g' : {
			String copiedUserId = newArgs[0];
			result = Server.getInstance().copyPhotos(localUserId, copiedUserId);
			sendError(result);
			break;
		}
		}
	}

	/**
	 * Receives the photos from the TCP port and stores them in the
	 * given user's temp folder, and then ciphers them.
	 * @param localUserId
	 * @param newArgs
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnrecoverableKeyException 
	 * @throws InvalidKeyException 
	 */
	//TODO and then ciphers them
	private File receivePhotos(String localUserId, String[] newArgs, 
			ObjectInputStream inStream) throws IOException, InvalidKeyException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, KeyStoreException, CertificateException {
		//create path for this user and for his/her photos
		File localUserIdPath = new File (tempPath + fileSeparator + localUserId);
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
			File photo = new File(photosPath + fileSeparator + name);
			fos = new FileOutputStream(photo);

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
