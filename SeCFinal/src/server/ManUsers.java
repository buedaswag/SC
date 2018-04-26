package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 */
public class ManUsers {

	private static String databaseRootDirName = "database";
	private static String fileSeparator = System.getProperty("file.separator");
	private static String usersTxtName = "users.txt";
	private static File databaseRootDir = new File(databaseRootDirName);
	private static File usersTxt = new File(databaseRootDirName + fileSeparator + usersTxtName);
	private static ObjectInputStream inStream;
	private static final String addUser = "addUser";
	private static final String removeUser = "removeUser";
	private static final String updatePassword = "updatePassword";
	private static final String wrongMac = 
			"The usersTxt file has a wrong MAC, the file is compromised!";
	private static final String notMacProtected = "The usersTxt file was not MAC protected";
	private static final String wrongCredentials = "Wrong credentials!";
	private static final String existingUser = "This user was already added!";
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		//set up the database
		if (!setupDatabase()) {
			//the file was compromised
			return;
		}
		//get the port
		int port = new Integer(args[0]);
		/*
		 * listen to the TCP port and take care of each request
		 */
		ServerSocket sSoc = null;
		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		while (true) {
			System.out.println("ManUsers: Wating for connections...");
			Socket inSoc = sSoc.accept();
			//setup the object stream o communicate with the client
			inStream = new ObjectInputStream(inSoc.getInputStream());
			//get the message from the client 
			String[] message = (String[]) inStream.readObject();
			//validate the message and execute the corresponding request
			executeOperation(message);
		}
	}

	/**
	 * Sets up the database and returns true if everything went well, false otherwise.
	 * Checks if the usersTxt file exists and creates it if it doesn't.
	 * Checks the MAC protection of the file, 
	 * adds it if the file is not protected (printing a warning message), 
	 * or terminates immediately if the MAC is wrong (printing a warning message).
	 * @throws IOException 
	 */
	private static boolean setupDatabase() throws IOException {
		//if databaseRootDir does not exist, create it and the usersTxt file and protect the file.
		if (!databaseRootDir.exists()) {
			databaseRootDir.mkdir();
			usersTxt.createNewFile();
			macProtect(usersTxt);
			return true;
		} 
		//if the database exists but not the usersTxt
		if (!usersTxt.exists()) {
			usersTxt.createNewFile();
			macProtect(usersTxt);
			return true;
		}
		//if both the database and the usersTxt exist, check the file for MAC protection. 
		if (isMacProtected(usersTxt)) {
			return checkMac(usersTxt);
		} else {
			macProtect(usersTxt);
		}
		return true;
	}

	/**
	 * Protects a file with a MAC.
	 * @param file
	 */
	// TODO
	private static void macProtect(File file) {
		// TODO Auto-generated method stub

	}

	/**
	 * Checks if the MAC protecting a file is correct.
	 * @param file
	 * @return
	 */
	// TODO
	private static boolean checkMac(File file) {
		if (false) {
			System.out.println(wrongMac);
		}
		return true;
	}

	/**
	 * Checks if the given file is protected by a MAC
	 * @param file
	 * @return
	 */
	//TODO
	private static boolean isMacProtected(File file) {
		if (false) {
			System.out.println(notMacProtected);
		}
		return true;
	}

	/**
	 * Executes the operation requested by the user in the given message
	 * @param message - the operation parameters in the format 
	 * 		["operationName", "parameter1", ... , "parameterN"]
	 * @throws IOException 
	 */
	private static void executeOperation(String[] message) throws IOException {
		String operation = message[0];
		switch (operation) {
		case addUser: {
			if (message.length == 3) {
				addUser(message[1], message[2]);
			} else {
				System.out.println("Invalid arguments for addUser.");
			}
			break;
		}
		case removeUser: {
			if (message.length == 3) {
				removeUser(message[1], message[2]);
			} else {
				System.out.println("Invalid arguments for removeUser.");
			}
			break;
		}
		case updatePassword: {
			if (message.length == 4) {
				updatePassword(message[1], message[2], message[3]);
			} else {
				System.out.println("Invalid arguments for changePassword.");
			}
			break;
		}
		default: {
			System.out.println("Woops! operation not supported");
		}
		}
	}

	/**
	 * Adds a line to the usersTxt file, in the format localUserId:salt:salted_password_hash, 
	 * if there is no user with the given localUserId yet.
	 * @param localUserId
	 * @param password
	 * @throws IOException 
	 */
	public static void addUser(String localUserId, String password) throws IOException {
		//get the String[] representation of the usersTxt file
		Collection<String> usersTxtContent = plainFileToStringCollection(usersTxt);
		//check if the user was already added
		for (String line : usersTxtContent) {
			if (line.startsWith(localUserId)) {
				System.out.println(existingUser);
				return;
			}
		}
		//Inserts the user and ciphers the password.
		User.insert(localUserId, password);
		System.out.println(addUser + " successfully executed!");
	}

	/**
	 * Removes the user with the given userId from the system, including the usersTxt, the 
	 * corresponding user folder, and from every followersTxt file.
	 * Checks if the password given is correct, and does nothing if it's not correct.
	 * @param localUserId
	 * @param password
	 * @throws IOException 
	 */
	public static void removeUser(String localUserId, String password) throws IOException {
		//get the String[] representation of the usersTxt file
		Collection<String> usersTxtContent = plainFileToStringCollection(usersTxt);
		//check if the user was already added
		String userLine = null;
		Iterator<String> iterator = usersTxtContent.iterator();
		while (iterator.hasNext() && userLine == null) {
			String line = iterator.next();
			if (line.startsWith(localUserId)) {
				userLine = line;
			}
		}
		if (userLine != null && checkPassword(userLine, password)) {
			//Removes the user from the collection
			usersTxtContent.remove(userLine);
			//removes the user from the system
			User.remove(localUserId, usersTxtContent);
			System.out.println(removeUser + " successfully executed!");
		} else {
			System.out.println(wrongCredentials);
		}
	}

	/**
	 * Update the usersTxt file, in the format localUserId:salt:salted_password_hash,
	 * in which salted_password_hash is the salted hash of the newPassword. Checks if the
	 * oldPassword given is correct, and does nothing if it's not correct.
	 * @param localUserId
	 * @param oldPassword
	 * @param newPassword
	 * @throws IOException 
	 */
	public static void updatePassword(String localUserId, String oldPassword, String newPassword) throws IOException {
		//get the String[] representation of the usersTxt file
		Collection<String> usersTxtContent = plainFileToStringCollection(usersTxt);
		//check if the user was already added
		String userLine = null;
		Iterator<String> iterator = usersTxtContent.iterator();
		while (iterator.hasNext() && userLine == null) {
			String line = iterator.next();
			if (line.startsWith(localUserId)) {
				userLine = line;
			}
		}
		if (userLine != null && checkPassword(userLine, oldPassword)) {
			//Removes the user from the collection
			usersTxtContent.remove(userLine);
			//removes the user from the system
			User.updatePassword(localUserId, newPassword, usersTxtContent);
			System.out.println(updatePassword + " successfully executed!");
		} else {
			System.out.println(wrongCredentials);
		}
	}

	/**
	 * Checks if the salted hash of the given oldPassword is equal to the one present in the given
	 * line.
	 * @param line - the line from the text file
	 * @param oldPassword
	 * @return
	 */
	//TODO
	private static boolean checkPassword(String line, String oldPassword) {
		//public static boolean isExpectedPassword(String password, byte[] salt, byte[] expectedHash)
		/*
		byte[] salt = stringToByteArray
		String saltString = line.split(":")[1];
		String[] saltStrings = saltString.split("(?!^)");
		int saltLength = saltString.length();
		byte[] salt = new byte[saltLength];
		for (int i = 0; i < saltLength; i++) {
			salt[i] = Byte.parseByte(saltStrings[i]);     
		}
		 */
		return true;
	}

	/**
	 * Deciphers the given file and returns its content as a String[]
	 * @param file
	 * @return fileContent - the String[] representation of the given file
	 * @throws IOException 
	 */
	//TODO DECIPHER THE FILE
	private static Collection<String> plainFileToStringCollection(File file) 
			throws IOException {
		//get the Collection<String>
		Collection<String> lines = new LinkedList<>();
		FileReader fileReader;
		BufferedReader buffReader = null;
		/*
		 * DECIPHER THE FILE
		 */
		fileReader = new FileReader(file);
		buffReader = new BufferedReader(fileReader);
		String line;
		while ((line = buffReader.readLine()) != null) {
			lines.add(line);
		}
		buffReader.close();
		return lines;
	}
}
