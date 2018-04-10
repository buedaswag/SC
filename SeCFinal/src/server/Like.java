package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

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
	private static FileReader fileReader;
	private static BufferedReader buffReader;

	/**
	 * Finds all the likes in the photo's directory and loads them into memory.
	 * @param photoDirectorie
	 * @return likes
	 */

	protected static Queue<Like> findAll(File photoDirectorie) {
		//create the buffers for reading from the file and the Queue
		Queue<Like> likes = new LinkedList<>();
		File likesTxt = new File(photoDirectorie + fileSeparator + likesTxtName);
		try {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	 * @param photoName 
	 * @param likerUserId
	 * @param likedUserId 
	 **********************************************************************************************
	 */
	private static FileWriter fileWriter;
	private static BufferedWriter buffWriter;
	private static String databaseRootDirName = "database";
	
	/**
	 * Inserts a like made by the likerUser
	 * @param likedUserId
	 * @param likerUserId
	 * @param photoName
	 * @return
	 */
	protected static Like insert(String likedUserId, String likerUserId, String photoName) {
		String line = likerUserId;
		File likesTxt = new File(databaseRootDirName + fileSeparator + likedUserId + 
				photoName.split("\\.")[0] + likesTxtName);
		try {
			fileWriter = new FileWriter(likesTxt, true);
			buffWriter = new BufferedWriter(fileWriter);
			buffWriter.write(line);
			buffWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				buffWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new Like(likerUserId);
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


}