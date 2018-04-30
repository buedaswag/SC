package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.Queue;

import javax.crypto.SecretKey;

import crypto.Crypto;
import crypto.Crypto;
import crypto.SignUtils;

/**
 * Represents a dislike made by dislikerUser in a Photo
 * @author migdi, max, antonio
 *
 */
public class Dislike {

	/**********************************************************************************************
	 * findAll and load variables and methods
	 **********************************************************************************************
	 */
	private static String fileSeparator = System.getProperty("file.separator");
	private static final String dislikesTxtName = "dislikes.txt";
	private static final String dislikesSigName = "dislikes.txt.sig";
	private static FileReader fileReader;
	private static BufferedReader buffReader;
	
	/**
	 * Finds all the dislikes in the photo's directory and loads them into memory.
	 * @param photoDirectorie
	 * @return dislikes
	 * @throws Exception 
	 */
	protected static Queue<Dislike> findAll(File photoDirectorie) throws Exception {
		//create the buffers for reading from the file and the Queue
		Queue<Dislike> dislikes = new LinkedList<>();
		File dislikesTxt = new File(photoDirectorie + fileSeparator + dislikesTxtName);
		File dislikesSig = new File(photoDirectorie + fileSeparator + dislikesSigName);
		
		SecretKey sk = Crypto.getInstance().getSecretKey();
		Crypto.getInstance().decipherFile(dislikesTxt, sk);
		if(!dislikesSig.exists()) {
			BufferedReader br = new BufferedReader(new FileReader (dislikesTxt));
			if(br.readLine() != null)
				SignUtils.writeSignature(dislikesTxt);
			br.close();
		}
		else
			if(!SignUtils.verifySignature(dislikesTxt, dislikesSig))
				throw new Exception("ERROR: a dislikes file has been compromised!");
		fileReader = new FileReader(dislikesTxt);
		buffReader = new BufferedReader(fileReader);
		String line;
		/*
		 * get all the info to load each comment to memory 
		 * (commenterUserId and the comment)
		 * Reads the current comments from the commentsTxt file
		 */
		while ((line = buffReader.readLine()) != null) {
			dislikes.add(Dislike.find(line));
		}
		fileReader.close();
		buffReader.close();
		Crypto.getInstance().cipherFile(dislikesTxt, sk);
		return dislikes;
	}

	/**
	 * Find all the information necessary to load the dislike to memory (dislikerUserId).
	 * @param line - a line in the commentsTxt file in the form <dislikerUserId>
	 * @return dislike - the dislike object corresponding to a line in the dislikesTxt file
	 */
	private static Dislike find(String line) {
		return Dislike.load(line);
	}

	/**
	 * Constructs a dislike object with the given parameters.
	 * @param dislikerUserId
	 * @return dislike
	 */
	private static Dislike load(String dislikerUserId) {
		return new Dislike(dislikerUserId);
	}
	
	/**********************************************************************************************
	 * insert and update variables and methods
	 **********************************************************************************************
	 */
	private static String databaseRootDirName = "database";
	
	/**
	 * Creates an empty dislikesTxt file.
	 * @param photoDir - the directory where the dislikesTxt file will be created.
	 * @throws IOException 
	 */
	public static void insertAll(File photoDir) throws IOException {
		new File(photoDir + fileSeparator + dislikesTxtName).createNewFile();
	}
	
	/**
	 * Copies the dislikesTxt from the photo's source folder to the destiny folder.
	 * @param srcPath - the source path.
	 * @param dstPath - the path to the destiny file.
	 * @throws IOException 
	 */
	public static void insertAll(Path srcPath, Path dstPath) throws IOException {
		Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * Inserts a like made by the likerUser
	 * @param dislikedUserId
	 * @param dislikerUserId
	 * @param photoName
	 * @return dislike
	 * @throws Exception 
	 */
	protected static Dislike insert(String dislikedUserId, String dislikerUserId, String photoName) throws Exception {
		String line = dislikerUserId;
		File dislikesTxt = new File(
				databaseRootDirName + fileSeparator + 
				dislikedUserId + fileSeparator + 
				photoName.split("\\.")[0] + fileSeparator + 
				dislikesTxtName);
		SecretKey sk = Crypto.getInstance().getSecretKey();
		Crypto.getInstance().decipherFile(dislikesTxt, sk);
		FileWriter fileWriter = new FileWriter(dislikesTxt, true);
		BufferedWriter buffWriter = new BufferedWriter(fileWriter);
		buffWriter.write(line);
		buffWriter.newLine();
		buffWriter.close();
		SignUtils.writeSignature(dislikesTxt);
		Crypto.getInstance().cipherFile(dislikesTxt, sk);
		return new Dislike(dislikerUserId);
	}
	
	/**
	 * Creates a deep copy of the given Queue<Dislike>.
	 * @param copiedDislikes - the dislike to be copied.
	 * @return copyDislikes - the copy of copiedDislikes.
	 */
	public static Queue<Dislike> deepCopy(Queue<Dislike> copiedDislikes) {
		//make a deep copy of each photo and add it to copyPhotos.
		Queue<Dislike> copyDislikes = new LinkedList<>();
		for (Dislike copiedDislike : copiedDislikes) {
			copyDislikes.add(deepCopy(copiedDislike));
		}
		return copyDislikes;
	}

	/**
	 * Creates a deep copy of the given Dislike.
	 * @param copiedDislike - the dislike to be copied.
	 * @return copyDislike - the copy of copiedDislike.
	 */
	private static Dislike deepCopy(Dislike copiedLike) {
		return new Dislike(copiedLike.getDislikerUserId());
	}

	/**********************************************************************************************
	 * User variables , methods and constructors
	 **********************************************************************************************
	 */

	private String dislikerUserId;

	/**
	 * Constructor: creates a new dislike object from the dislikerUserId
	 * @param user
	 */
	protected Dislike(String dislikerUserId) {
		this.dislikerUserId = dislikerUserId;
	}

	/**
	 * 
	 * @return the user that made this comment
	 */
	protected String getDislikerUserId() {
		return this.dislikerUserId;
	}

	/**
	 * Returns the String representation of this Dislike object.
	 */
	public String toString() {
		return getDislikerUserId();
	}
}