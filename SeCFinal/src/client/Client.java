package client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import java.net.InetAddress;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 */
public class Client {
	private static String userID;
	private static String pass;
	private static String serverAddress;
	private static ClientNetworkHandler handlerTCP;
	private static String filePath = new String("repositorio");
	private static String allGood = "ok";
	private static Pattern ipPattern = Pattern
			.compile("^(([01]?\\d\\d?|" + "2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
		// Verifica validade dos argumentos
		if (!checkArgs(args)) {
			System.out.println("Argumentos invalidos!");
			return;
		}

		// Registers the session variables
		userID = args[1];
		pass = args[2];
		serverAddress = args[3];
		char option = args[4].charAt(1);

		handlerTCP = new ClientNetworkHandler(serverAddress);

		// Authenticates the user and does the operation he asked for
		authenticate(userID, pass);
		switch (option) {
		// === ADD PHOTOS (-a)
		case 'a': {
			addPhotos("a", args[5]);
			break;
		}
		// === ADDS A COMMENT (-c)
		case 'c': {
			String comment = args[5];
			String user = args[6];
			String photo = args[7];
			addComment(comment, user, photo);
			break;
		}
		// === LIKES A PHOTO (-L)
		case 'L': {
			String user = args[5];
			String photo = args[6];
			addLike(user, photo);
			break;
		}
		// === DISLIKES A PHOTO (-D)
		case 'D': {
			String dislikedUser = args[5];
			String photo = args[6];
			addDislike(dislikedUser, photo);
			break;
		}
		// === ADDS FOLLOWERS (-f)
		case 'f': {
			String followers = args[5];
			addFollowers(followers);
			break;
		}
		// === REMOVES FOLLOWERS (-r)
		case 'r': {
			String followersToRemove = args[5];
			removeFollowers(followersToRemove);
			break;
		}
		// === LISTS PHOTOS (-l)
		case 'l': {
			String userToList = args[5];
			listPhotos(userToList);
			break;
		}
		// === GET INFO (-i)
		case 'i': {
			String userToList = args[5];
			String photo = args[6];
			getInfo(userToList, photo);
			break;
		}
		// === SAVE PHOTOS (-g)
		case 'g': {
			String copiedUserser = args[5];
			savePhotos(copiedUserser);
			break;
		}
		default: {
			System.out.println("Operacao nao reconhecida!");
			break;
		}

		}

		// print the message received from the server

		// Close the connection
		handlerTCP.endConnection();
	}

	// ================== OPERATIONS ================== //

	/**
	 * Try´s to authenticate the user
	 * 
	 * @param userID
	 *            the userid
	 * @param pass
	 *            the password
	 * @throws IOException
	 */
	public static void authenticate(String userid, String pass) throws IOException {
		handlerTCP.authenticate(userID, pass);
	}

	/**
	 * Adds the photos with the given names to the user. Prints a message that tells
	 * the user what happened.
	 * 
	 * @param photos
	 *            the photos names
	 * @throws IOException
	 */
	public static void addPhotos(String operation, String photos) throws IOException, ClassNotFoundException {
		/*
		 * First send the parameters to the socket so the server can check for errors
		 */
		// lists photos from the local repository
		File local = new File(filePath);
		File[] files = local.listFiles();

		// send photo names and separate the photo names
		String[] fileNames = photos.split(",");
		String[] op = { operation };

		// the parameters to send to the server for error checking
		String[] message = concat(op, fileNames);
		handlerTCP.send(message);

		/*
		 * (after sending the parameters) check with the server if there was no error.
		 * If there was an error, prints the error, otherwise, sends the photos to the
		 * socket and prints the success message
		 */
		String currFile;
		String error = handlerTCP.receive();
		if (error.equals(allGood)) {
			for (String s : fileNames) {
				for (File f : files) {
					// check if the photo has an extension
					currFile = f.getName();
					if (s.equals(currFile)) {
						handlerTCP.enviarFile(f);
					}
				}
			}
			System.out.println("Fotos enviadas com sucesso!");
		} else
			System.out.println(error);
	}

	/**
	 * Adds a comment to the commentedUser's photo Prints a message that tells the
	 * user what happened.
	 * 
	 * @param commentedUserid
	 *            - the user to get commented
	 * @param photo
	 *            - the photo to be commented
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void addComment(String comment, String commentedUserid, String photo)
			throws IOException, ClassNotFoundException {
		/*
		 * First send the parameters to the socket so the server can check for errors
		 */
		// the parameters to send to the server for error checking
		String[] message = { "c", comment, commentedUserid, photo };
		handlerTCP.send(message);

		/*
		 * (after sending the parameters) check with the server if there was no error.
		 * If there was an error, prints the error, otherwise, continues and prints the
		 * success message
		 */
		String error = handlerTCP.receive();
		if (error.equals(allGood))
			System.out.println("Comentario enviado com sucesso!");
		else
			System.out.println(error);
	}

	/**
	 * Adds a like to the photo Prints a message that tells the user what happened.
	 * 
	 * @param likedUserid
	 *            - the user to get liked
	 * @param photo
	 *            - the photo to be liked
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void addLike(String likedUserid, String photo) throws IOException, ClassNotFoundException {
		/*
		 * First send the parameters to the socket so the server can check for errors
		 */
		// the parameters to send to the server for error checking
		String[] message = { "L", likedUserid, photo };
		handlerTCP.send(message);

		/*
		 * (after sending the parameters) check with the server if there was no error.
		 * If there was an error, prints the error, otherwise, continues and prints the
		 * success message
		 */
		String error = handlerTCP.receive();
		if (error.equals(allGood))
			System.out.println("Like enviado com sucesso!");
		else
			System.out.println(error);
	}

	/**
	 * Adds a dislike to the photo Prints a message that tells the user what
	 * happened.
	 * 
	 * @param dislikedUserid
	 *            - the user to get disliked
	 * @param photo
	 *            - the photo to be disliked
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void addDislike(String dislikedUserid, String photo) throws IOException, ClassNotFoundException {
		/*
		 * First send the parameters to the socket so the server can check for errors
		 */
		// the parameters to send to the server for error checking
		String[] message = { "D", dislikedUserid, photo };
		handlerTCP.send(message);

		/*
		 * (after sending the parameters) check with the server if there was no error.
		 * If there was an error, prints the error, otherwise, continues and prints the
		 * success message
		 */
		String error = handlerTCP.receive();
		if (error.equals(allGood))
			System.out.println("Dislike enviado com sucesso!");
		else
			System.out.println(error);
	}

	/**
	 * Adds the followUsers as followers of the user Prints a message that tells the
	 * user what happened.
	 * 
	 * @param followUserIds
	 *            - the userids of the followUsers
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void addFollowers(String followUserIds) throws IOException, ClassNotFoundException {
		/*
		 * First send the parameters to the socket so the server can check for errors
		 */
		// the parameters to send to the server for error checking
		String[] op = { "f" };
		String[] usersList = followUserIds.split(",");
		String[] message = concat(op, usersList);
		handlerTCP.send(message);
		/*
		 * (after sending the parameters) check with the server if there was no error.
		 * If there was an error, prints the error, otherwise, continues and prints the
		 * success message
		 */
		String error = handlerTCP.receive();
		if (error.equals(allGood))
			System.out.println("Followers successfully added!");
		else
			System.out.println(error);
	}

	/**
	 * Removes followers from the user Prints a message that tells the user what
	 * happened.
	 * 
	 * @param followers
	 *            - the followers to be removed
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static void removeFollowers(String followers) throws ClassNotFoundException, IOException {
		/*
		 * First send the parameters to the socket so the server can check for errors
		 */
		// the parameters to send to the server for error checking
		String[] message = { "r", followers };
		handlerTCP.send(message);
		/*
		 * (after sending the parameters) check with the server if there was no error.
		 * If there was an error, prints the error, otherwise, continues and prints the
		 * success message
		 */
		String error = handlerTCP.receive();
		if (error.equals(allGood))
			System.out.println("Followers successfully remmoved!");
		else
			System.out.println(error);
	}

	/**
	 * Lists the user's photos and respective date Prints a message that tells the
	 * user what happened.
	 * 
	 * @param listedUserid
	 *            - the userid of the user whose photos will be listed
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static void listPhotos(String listedUserid) throws ClassNotFoundException, IOException {
		/*
		 * First send the parameters to the socket so the server can check for errors
		 */
		String[] message = { "l", listedUserid };
		handlerTCP.send(message);

		/*
		 * (after sending the parameters) check with the server if there was no error.
		 * If there was an error, prints the error, otherwise, continues and prints the
		 * success message
		 */
		String error = handlerTCP.receive();
		if (error.equals(allGood))
			System.out.println("Photos successfully listed!");
		else
			System.out.println(error);
	}

	/**
	 * Lists the comments, likes and dislikes of the listedUser's photo
	 * 
	 * @param listedUserid
	 * @param photo
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static void getInfo(String listedUserid, String photo) throws ClassNotFoundException, IOException {
		/*
		 * First send the parameters to the socket so the server can check for errors
		 */
		String[] message = { "i", listedUserid, photo };
		handlerTCP.send(message);

		/*
		 * (after sending the parameters) check with the server if there was no error.
		 * If there was an error, prints the error, otherwise, continues and prints the
		 * success message
		 */
		String error = handlerTCP.receive();
		if (error.equals(allGood))
			System.out.println("Comments, likes and dislikes successfully " + "listed!");
		else
			System.out.println(error);
	}

	/**
	 * Copies all the photos from copiedUser to localUser
	 * 
	 * @param copiedUserId
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private static void savePhotos(String copiedUserId) throws ClassNotFoundException, IOException {
		/*
		 * First send the parameters to the socket so the server can check for errors
		 */
		String[] message = { "g", copiedUserId };
		handlerTCP.send(message);

		/*
		 * (after sending the parameters) check with the server if there was no error.
		 * If there was an error, prints the error, otherwise, continues and prints the
		 * success message
		 */
		String error = handlerTCP.receive();
		if (error.equals(allGood))
			System.out.println("Photos successfully copied!");
		else
			System.out.println(error);
	}

	// concats two string arrays
	public static String[] concat(String[] a, String[] b) {
		String[] newArray = new String[a.length + b.length];
		System.arraycopy(a, 0, newArray, 0, a.length);
		System.arraycopy(b, 0, newArray, a.length, b.length);
		return newArray;
	}

	/**
	 * Check if the arguments passed are valid
	 * 
	 * @param args
	 *            the arguments
	 * @return True if the arguments are valid, False otherwise
	 */
	public static boolean checkArgs(String[] args) {
		// Checks the ip address and port for validity
		if (!checkAddressPort(args[3]))
			return false;
		// check the option parameter
		if (args[4].charAt(0) != '-')
			return false;

		// check option and the arguments length
		int num = args.length;
		if (num < 4)
			return false;

		char opt = args[4].charAt(1);

		// Check if the number of arguments corresponds to the given operation
		if (opt == 'a' || opt == 'l' || opt == 'g' || opt == 'f' || opt == 'r')
			if (num != 6)
				return false;
			else if (opt == 'i' || opt == 'L' || opt == 'D')
				if (num != 7)
					return false;
				else if (opt == 'c')
					if (num != 8)
						return false;
					else
						return true;
		return true;
	}

	/**
	 * Checks the ip addres and port for validity
	 * 
	 * @param addressPort
	 *            - the ip address and port, as a String, separated by ":"
	 * @return
	 */
	private static boolean checkAddressPort(String addressPort) {
		int port = -1;
		String address;
		try {
			// get the address and port
			String[] addressPortSplitted = addressPort.split(":");
			address = addressPortSplitted[0];
			port = Integer.parseInt(addressPortSplitted[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}

		// check the port
		if (port < 0 || port > 65535)
			return false;
		// check the ip address
		if (!validateIP(address))
			return false;
		// both the ip address and the port are valid
		return true;
	}

	/**
	 * Validates a given ip adress string
	 * 
	 * @param ip
	 *            - the ip address
	 * @return
	 */
	private static boolean validateIP(String ip) {
		return ipPattern.matcher(ip).matches();
	}
}
