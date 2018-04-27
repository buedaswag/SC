package server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.Queue;

import javax.crypto.SecretKey;

import crypto.Crypto;
import crypto.SignUtils;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 *         Represents a comment made by the user in the photo
 */
public class Comment {

	/**********************************************************************************************
	 * findAll and load variables and methods
	 **********************************************************************************************
	 */
	private static String fileSeparator = System.getProperty("file.separator");
	private static final String commentsTxtName = "comments.txt";
	private static final String commentsSigName = "comments.txt.sig";
	
	/**
	 * Finds all the comments in the photo's directory and loads them into memory.
	 * @param photoDirectorie
	 * @return comments
	 * @throws Exception 
	 */
	protected static Queue<Comment> findAll(File photoDirectorie) throws Exception {
		//create the buffers for reading from the file and the Queue
		Queue<Comment> comments = new LinkedList<>();
		File commentsTxt = new File(photoDirectorie + fileSeparator + commentsTxtName);
		File commentsSig = new File(photoDirectorie + fileSeparator + commentsSigName);
		SecretKey sk = Crypto.getInstance().getSecretKey();
		Crypto.getInstance().decipherFile(commentsTxt, sk);
		if(!commentsSig.exists()) {
			BufferedReader br = new BufferedReader(new FileReader (commentsTxt));
			if(br.readLine() != null)
				SignUtils.writeSignature(commentsTxt);
			br.close();
		}
		else
			if(!SignUtils.verifySignature(commentsTxt, commentsSig)) {
				throw new Exception("ERROR: a comments file has been compromised!");
			}
		FileReader fileReader;
		BufferedReader buffReader = null;
		fileReader = new FileReader(commentsTxt);

		buffReader = new BufferedReader(fileReader);
		String line;
		/*
		 * get all the info to load each comment to memory 
		 * (commenterUserId and the comment)
		 * Reads the current comments from the commentsTxt file
		 */
		while ((line = buffReader.readLine()) != null) {
			comments.add(Comment.find(line));
		}
		fileReader.close();
		buffReader.close();
		Crypto.getInstance().cipherFile(commentsTxt, sk);
		return comments;
	}

	/**
	 * Find all the information necessary to load the comment to memory
	 * (commenterUserId and the comment).
	 * @param line - a line in the commentsTxt file in the form <commenterUserId>:<comment>
	 * @return comment - the Comment object corresponding to a line in the commentsTxt file
	 */
	private static Comment find(String line) {
		String[] lineComponents = line.split(":");
		String commenterUserId = lineComponents[0];
		String comment = lineComponents[1];
		return Comment.load(commenterUserId, comment);
	}

	/**
	 * Constructs a Photo object with the given parameters.
	 * @param commenterUserId
	 * @param comment
	 * @return
	 */
	private static Comment load(String commenterUserId, String comment) {
		return new Comment(commenterUserId, comment);
	}

	/**********************************************************************************************
	 * insert and update variables and methods
	 **********************************************************************************************
	 */
	private static String databaseRootDirName = "database";

	/**
	 * Creates an empty commentsTxt file.
	 * @param photoDir - the directory where the commentsTxt file will be created.
	 * @throws IOException 
	 */
	public static void insertAll(File photoDir) throws IOException {
		new File(photoDir + fileSeparator + commentsTxtName).createNewFile();
	}
	
	/**
	 * Copies the commentsTxt from the photo's source folder to the destiny folder.
	 * @param srcPath - the source path.
	 * @param dstPath - the path to the destiny file.
	 * @throws IOException 
	 */
	public static void insertAll(Path srcPath, Path dstPath) throws IOException {
		Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * Inserts a comment made by the commenterUser
	 * @param comment
	 * @param commentedUserId
	 * @param commenterUserId
	 * @param photoName - the name of the commentedUser's photo
	 * @return comment
	 * @throws Exception 
	 */
	public static Comment insert(String commentedUserId, String commenterUserId, String comment, 
			String photoName) throws Exception {
		String line = commenterUserId + ":" + comment;
		File commentsTxt = new File(databaseRootDirName + fileSeparator + commentedUserId + 
				fileSeparator + photoName.split("\\.")[0] + fileSeparator + commentsTxtName);
		SecretKey sk = Crypto.getInstance().getSecretKey();
		Crypto.getInstance().decipherFile(commentsTxt, sk);
		FileWriter fileWriter = new FileWriter(commentsTxt, true);
		BufferedWriter buffWriter = new BufferedWriter(fileWriter);
		buffWriter.write(line);
		buffWriter.newLine();
		buffWriter.close();
		Crypto.getInstance().cipherFile(commentsTxt, sk);
		return new Comment(commenterUserId, comment);
	}

	/**
	 * Creates a deep copy of the given Queue<Comment>.
	 * @param copiedComments - the comments to be copied.
	 * @return copyComments - the copy of copiedComments.
	 */
	public static Queue<Comment> deepCopy(Queue<Comment> copiedComments) {
		//make a deep copy of each photo and add it to copyPhotos.
		Queue<Comment> copyComments = new LinkedList<>();
		for (Comment copiedPhoto : copiedComments) {
			copyComments.add(deepCopy(copiedPhoto));
		}
		return copyComments;
	}

	/**
	 * Creates a deep copy of the given Comment.
	 * @param copiedComment - the comment to be copied.
	 * @return copyComment - the copy of copiedComment.
	 */
	private static Comment deepCopy(Comment copiedComment) {
		return new Comment(copiedComment.getCommenterUserid(), copiedComment.getComment());
	}

	/**********************************************************************************************
	 * Comment variables , methods and constructors
	 **********************************************************************************************
	 */
	private String commenterUserId;
	private String comment;

	/**
	 * Constructor: creates a new comment made by the user with the given userid
	 * in the Photo to which this comment belongs to. Sets the date to the current
	 * date
	 * 
	 * @requires user is a follower of the user who uploaded the photo
	 * @param commenterUserId
	 * @param comment
	 */
	protected Comment(String commenterUserId, String comment) {
		this.commenterUserId = commenterUserId;
		this.comment = comment;
	}

	// Information methods
	/**
	 * 
	 * @return the user id of this user
	 */
	protected String getCommenterUserid() {
		return this.commenterUserId;
	}

	/**
	 * 
	 * @return the String comment
	 */
	protected String getComment() {
		return this.comment;
	}

	/**
	 * @return A String representation of this object
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(commenterUserId);
		sb.append(":");
		sb.append(comment);
		return sb.toString();
	}
}