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
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import crypto_ponto4.Crypto;

/**
 * Represents a like made by likerUser in a Photo
 * @author migdi, max, antonio
 *
 */
public class Like {

	/**********************************************************************************************
	 * findAll and load variables and methods
	 **********************************************************************************************
	 */
	private static String fileSeparator = System.getProperty("file.separator");
	private static final String likesTxtName = "likes.txt";
	private static final String likesSigName = "likes.txt.sig";
	private static FileReader fileReader;
	private static BufferedReader buffReader;

	/**
	 * Finds all the likes in the photo's directory and loads them into memory.
	 * @param photoDirectorie
	 * @return likes
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws NoSuchProviderException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws UnrecoverableKeyException 
	 * @throws SignatureException 
	 * @throws ClassNotFoundException 
	 */
	protected static Queue<Like> findAll(File photoDirectorie) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, ClassNotFoundException, SignatureException {
		//create the buffers for reading from the file and the Queue
		Queue<Like> likes = new LinkedList<>();
		File likesTxt = new File(photoDirectorie + fileSeparator + likesTxtName);
		File likesSig = new File(photoDirectorie + fileSeparator + likesSigName);
		
		SecretKey sk = Crypto.getInstance().getSecretKey();
		Crypto.getInstance().decipherFile(likesTxt, sk);
		if(likesSig.exists()) {
			boolean isValid = Crypto.getInstance().checkSignature(likesTxt, likesSig);
			if(!isValid)
				throw new SecurityException("ERROR: Invalid file signature!");
		}
		fileReader = new FileReader(likesTxt);
		buffReader = new BufferedReader(fileReader);
		String line;
		/*
		 * get all the info to load each comment to memory 
		 * (commenterUserId and the comment)
		 * Reads the current comments from the commentsTxt file
		 */
		while ((line = buffReader.readLine()) != null) {
			likes.add(Like.find(line));
		}
		fileReader.close();
		buffReader.close();
		Crypto.getInstance().cipherFile(likesTxt, sk);
		return likes;
	}

	/**
	 * Find all the information necessary to load the like to memory (likerUserId).
	 * @param line - a line in the commentsTxt file in the form <likerUserId>
	 * @return like - the Like object corresponding to a line in the likesTxt file
	 */
	private static Like find(String line) {
		return Like.load(line);
	}

	/**
	 * Constructs a Like object with the given parameters.
	 * @param likerUserId
	 * @return like
	 */
	private static Like load(String likerUserId) {
		return new Like(likerUserId);
	}
	
	/**********************************************************************************************
	 * insert and update variables and methods
	 **********************************************************************************************
	 */
	private static String databaseRootDirName = "database";
	
	/**
	 * Creates an empty likesTxt file.
	 * @param photoDir - the directory where the likesTxt file will be created.
	 * @throws IOException 
	 */
	public static void insertAll(File photoDir) throws IOException {
		new File(photoDir + fileSeparator + likesTxtName).createNewFile();
	}
	
	/**
	 * Copies the likesTxt from the photo's source folder to the destiny folder.
	 * @param srcPath - the source path.
	 * @param dstPath - the path to the destiny file.
	 * @throws IOException 
	 */
	public static void insertAll(Path srcPath, Path dstPath) throws IOException {
		Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * Inserts a like made by the likerUser
	 * @param likedUserId
	 * @param likerUserId
	 * @param photoName
	 * @return like
	 * @throws IOException 
	 * @throws NoSuchProviderException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws UnrecoverableKeyException 
	 * @throws BadPaddingException 
	 * @throws SignatureException 
	 */
	protected static Like insert(String likedUserId, String likerUserId, String photoName) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, SignatureException {
		String line = likerUserId;
		File likesTxt = new File(
				databaseRootDirName + fileSeparator + 
				likedUserId + fileSeparator + 
				photoName.split("\\.")[0] + fileSeparator + 
				likesTxtName);
		FileWriter fileWriter = new FileWriter(likesTxt, true);
		BufferedWriter buffWriter = new BufferedWriter(fileWriter);
		buffWriter.write(line);
		buffWriter.newLine();
		buffWriter.close();
		SecretKey sk = Crypto.getInstance().getSecretKey();
		Crypto.getInstance().signFile(likesTxt);
		Crypto.getInstance().cipherFile(likesTxt, sk);
		return new Like(likerUserId);
	}
	
	/**
	 * Creates a deep copy of the given Queue<Like>.
	 * @param copiedLikes - the likes to be copied.
	 * @return copyLikes - the copy of copiedLikes.
	 */
	public static Queue<Like> deepCopy(Queue<Like> copiedLikes) {
		//make a deep copy of each photo and add it to copyPhotos.
		Queue<Like> copyLikes = new LinkedList<>();
		for (Like copiedLike : copiedLikes) {
			copyLikes.add(deepCopy(copiedLike));
		}
		return copyLikes;
	}

	/**
	 * Creates a deep copy of the given Like.
	 * @param copiedLike - the like to be copied.
	 * @return copyLike - the copy of copiedLike.
	 */
	private static Like deepCopy(Like copiedLike) {
		return new Like(copiedLike.getLikerUserId());
	}

	/**********************************************************************************************
	 * Like variables , methods and constructors
	 **********************************************************************************************
	 */

	private String likerUserId;

	/**
	 * Constructor: creates a new like object from the likerUserId
	 * @param user
	 */
	protected Like(String likerUserId) {
		this.likerUserId = likerUserId;
	}

	/**
	 * 
	 * @return the user that made this comment
	 */
	protected String getLikerUserId() {
		return this.likerUserId;
	}
	
	/**
	 * Returns the String representation of this Like object.
	 */
	public String toString() {
		return getLikerUserId();
	}
}