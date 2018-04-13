package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 */
public class Photo {

	/**********************************************************************************************
	 * findAll and load variables and methods
	 **********************************************************************************************
	 */
	private static String databaseRootDirName = "database";
	private static File databaseRootDir = new File(databaseRootDirName);
	private static String fileSeparator = System.getProperty("file.separator");
	private static final String commentsTxtName = "comments.txt";
	private static final String likesTxtName = "likes.txt";
	private static final String dislikesTxtName = "dislikes.txt";

	/**
	 * Finds all the photos in this user's directory and loads them into memory.
	 * @param userId
	 * @return
	 */
	protected static Collection<Photo> findAll(String userId) {
		//the Collection to be returned
		Collection<Photo> photos = new LinkedList<>();
		//the directories for this user's photos
		String userDirName = databaseRootDirName + fileSeparator + userId;
		File[] photoDirectories = new File(userDirName).listFiles(File::isDirectory);
		//if photoDirectories does not contain any photo directory, there are no photos, 
		//return the empty Collection.
		if (photoDirectories.length > 0) {
			/*
			 * get all the info to load each photo to memory 
			 * (photoName, uploadDate, comments, likes and dislikes)
			 */
			for (File photoDirectorie : photoDirectories) {
				photos.add(Photo.find(userId, photoDirectorie));
			}
		}
		return photos;
	}

	/**
	 * Find all the information necessary to load the photo from the given directory to memory
	 * (photoName, uploadDate, comments, likes and dislikes)
	 * @param userId
	 * @param photoDirectorie
	 */
	private static Photo find(String userId, File photoDirectorie) {
		//the composing elements of the photo to be returned 
		String photoName = null;
		long uploadDate = -1;
		Queue<Comment> comments = null;
		Queue<Like> likes = null;
		Queue<Dislike> dislikes = null;
		//for each file in the photoDirectorie
		File[] files = photoDirectorie.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			switch (fileName) {
			case commentsTxtName:
				comments = Comment.findAll(photoDirectorie);
				break;
			case dislikesTxtName:
				likes = Like.findAll(photoDirectorie);
				break;
			case likesTxtName:
				dislikes = Dislike.findAll(photoDirectorie);
				break;
				//the file is the photo
			default:
				photoName = fileName;
				uploadDate = file.lastModified();
				break;
			}
		}
		return Photo.load(photoName, uploadDate, comments, likes, dislikes);
	}

	/**
	 * Constructs a Photo object with the given parameters.
	 * @param photoName
	 * @param uploadDate
	 * @param comments
	 * @param likes
	 * @param dislikes
	 * @return
	 */
	private static Photo load(String photoName, long uploadDate, Queue<Comment> comments,
			Queue<Like> likes, Queue<Dislike> dislikes) {
		return new Photo(photoName, uploadDate, comments, likes, dislikes);
	}

	/**********************************************************************************************
	 * insert and update variables and methods
	 **********************************************************************************************
	 */

	/**
	 * Move photos from temporary folder to the corresponding user's folder
	 * @param localUserId 
	 * @param photoNames - the names of the photos
	 * @param photosPath - the temporary folder
	 * @param delete - if delete == true, deletes the photos from the original folder.
	 * @return 
	 */
	protected static Collection<Photo> insertAll(String localUserId, String[] photoNames, File photosPath, 
			boolean delete) {
		//the Collection to be returned
		Collection<Photo> photos = new LinkedList<>();
		//the directories for this user's photos
		String localUserDirName = databaseRootDirName + fileSeparator + localUserId;
		if (delete) {
			for (File photo : photosPath.listFiles()) {
				Photo.insert(localUserId, photo, localUserDirName);
				//delete the photo
				photo.delete();
			}
		} else {
			for (File photo : photosPath.listFiles()) {
				Photo.insert(localUserId, photo, localUserDirName);
			}
		}
		return photos;
	}

	/**
	 * Move photo from temporary folder to the corresponding user's folder and delete the photo
	 * @param localUserId 
	 * @param photoFileOrigin - the the photo file inside the origin folder
	 * @param localUserDirName - the name of the directory of the localUser
	 */
	private static void insert(String localUserId, File photoFileOrigin, String localUserDirName) {
		//name of the photo file inside the origin folder, 
		//with the corresponding extention removed
		String photoNameNoExtention = photoFileOrigin.getName().split("\\.")[0];
		//create the destiny directory for the photo
		String photoDestenyDirName = localUserDirName + fileSeparator + photoNameNoExtention;
		File photoDestenyDir = new File(photoDestenyDirName);
		photoDestenyDir.mkdir();
		//create the File corresponding to the photo in the photoDestenyDir
		File photoInDestiny = 
				new File(photoDestenyDirName + fileSeparator + photoFileOrigin.getName());
		//move the photo from temp dir to photoDestenyDir
		photoFileOrigin.renameTo(photoInDestiny);
		//create files: likesTxt, dislikesTxt and commentsTxt
		try {
			new File(photoDestenyDirName + fileSeparator + "comments.txt").createNewFile();
			new File(photoDestenyDirName + fileSeparator + "likes.txt").createNewFile();
			new File(photoDestenyDirName + fileSeparator + "dislikes.txt").createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**********************************************************************************************
	 * Photo variables , methods and constructors
	 **********************************************************************************************
	 */
	private String photoName;
	private Queue<Comment> comments;
	private Queue<Like> likes;
	private Queue<Dislike> dislikes;
	private Date uploadDate;
	private SimpleDateFormat df;

	/**
	 * Constructor: creates a new Photo object form the given parameters.
	 * @param photoName
	 * @param uploadDate
	 */
	protected Photo(String photoName, long uploadDate) {
		this.photoName = photoName;
		this.uploadDate = new Date(uploadDate);
		this.comments = new LinkedBlockingDeque<Comment>();
		this.df = new SimpleDateFormat("dd/MM/yyyy HH'h'mm");
	}

	/**
	 * Constructor: creates a new Photo object form the given parameters.
	 * @param photoName
	 * @param uploadDate
	 * @param comments
	 * @param likes
	 * @param dislikes
	 */
	private Photo(String photoName, long uploadDate, Queue<Comment> comments,
			Queue<Like> likes, Queue<Dislike> dislikes) {
		this.photoName = photoName;
		this.uploadDate = new Date(uploadDate);
		this.comments = comments;
		this.likes = likes;
		this.dislikes = dislikes;
	}


	/**
	 * 
	 * @return the name of this photo
	 */
	protected String getName() {
		return this.photoName;
	}

	/**
	 * 
	 * @return the upload date of this photo
	 */
	protected Date getDate() {
		return this.uploadDate;
	}

	/**
	 * Returns this photo's number of likes
	 * 
	 * @return
	 */
	protected int getTotalLikes() {
		return this.likes.size();
	}

	/**
	 * Returns this photo's number of dislikes
	 * 
	 * @return
	 */
	protected int getTotalDislikes() {
		return this.dislikes.size();
	}

	/**
	 * 
	 * @return a list of this photo's comments
	 */
	protected Collection<Comment> getComments() {
		return comments;
	}

	/**
	 * Get the info for the specified photo from this user, with the format: "photoName - uploadDate\n"
	 * @return infoPhoto - a String containing all of this user's photos listed according to the
	 * format
	 */
	protected String getInfoPhoto() {
		// Builds the string to be sent to the client
		StringBuilder infoPhoto = new StringBuilder();
		// Generate answer
		infoPhoto.append("Likes: " + this.likes.size() + "\n");
		infoPhoto.append("Dislikes: " + this.dislikes.size() + "\n");
		infoPhoto.append("Comments: " + "\n");
		for (Comment c : this.comments) {
			infoPhoto.append(c.getComment() + "\n");
		}
		// Conversion to string and return
		return infoPhoto.toString();
	}

	protected void addComments(Collection<Comment> comments) {
		this.comments.addAll(comments);
	}

	/**
	 * Adds a comment made by commenterUser in this photo
	 * @param comment - the comment to be made
	 * @param commentedUserId - the userId of the commentedUser
	 * @param commenterUserId - the userId of the commenterUser
	 */
	protected void addComment(String comment, String commentedUserId, String commenterUserId, 
			String photoName) {
		comments.add(Comment.insert(comment, commentedUserId, commenterUserId, photoName));
	}

	/**
	 * Adds a like made by likerUser in this user's photo
	 * @param likedUserId - the userId of the likedUser
	 * @param likerUserId - the userId of the likerUser
	 * @param photoName - the name of this user's photo
	 */
	protected void addLike(String likedUserId, String likerUserId, String photoName) {
		likes.add(Like.insert(likedUserId, likerUserId, photoName));
	}

	/**
	 * Adds a dislike made by likerUser in this user's photo
	 * @param dislikedUserId - the userId of the dislikedUser
	 * @param dislikerUserId - the userId of the dislikerUser 
	 * @param photoName - the name of this user's photo
	 */
	protected void addDislike(String dislikedUserId, String dislikerUserId, String photoName) {
		dislikes.add(Dislike.insert(dislikedUserId, dislikerUserId, photoName));
	}

	/**
	 * @return A String representation of this object
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(photoName);
		sb.append(" ");
		sb.append(likes);
		sb.append(" ");
		sb.append(dislikes);
		sb.append(" ");
		sb.append(uploadDate);
		return sb.toString();
	}
}