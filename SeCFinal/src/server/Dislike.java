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
	private static final String dislikesTxtName = "disdislikes.txt";
	private static FileReader fileReader;
	private static BufferedReader buffReader;

	/**
	 * Finds all the disdislikes in the photo's directory and loads them into memory.
	 * @param photoDirectorie
	 * @return disdislikes
	 */
	protected static Queue<Dislike> findAll(File photoDirectorie) {
		//create the buffers for reading from the file and the Queue
		Queue<Dislike> dislikes = new LinkedList<>();
		File dislikesTxt = new File(photoDirectorie + fileSeparator + dislikesTxtName);
		try {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	private static FileWriter fileWriter;
	private static BufferedWriter buffWriter;
	private static String databaseRootDirName = "database";
	
	/**
	 * Inserts a like made by the likerUser
	 * @param dislikedUserId
	 * @param dislikerUserId
	 * @param photoName
	 * @return dislike
	 */
	protected static Dislike insert(String dislikedUserId, String dislikerUserId, String photoName) {
		String line = dislikerUserId;
		File likesTxt = new File(databaseRootDirName + fileSeparator + dislikedUserId + 
				photoName.split("\\.")[0] + dislikesTxtName);
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
		return new Dislike(dislikerUserId);
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
	protected String getdislikerUserId() {
		return this.dislikerUserId;
	}


}