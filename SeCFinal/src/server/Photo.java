package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import crypto_ponto4.Crypto;

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
	private static String fileSeparator = System.getProperty("file.separator");
	private static final String commentsTxtName = "comments.txt";
	private static final String likesTxtName = "likes.txt";
	private static final String dislikesTxtName = "dislikes.txt";
	private static SecretKey SECRET_KEY = null;

	/**
	 * Finds all the photos in this user's directory and loads them into memory.
	 * @param userId
	 * @return
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
	protected static Collection<Photo> findAll(String userId) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, ClassNotFoundException, SignatureException {
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
	private static Photo find(String userId, File photoDirectorie) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, ClassNotFoundException, SignatureException {
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
			case "likes.txt.sig":
				break;
			case "dislikes.txt.sig":
				break;
			case "comments.txt.sig":
				break;
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
	 * @return 
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
	 */
	protected static Collection<Photo> insertAll(String localUserId, File photosPath) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException {
		//the Collection to be returned
		Collection<Photo> photos = new LinkedList<>();
		//the directories for this user's photos
		String localUserDirName = databaseRootDirName + fileSeparator + localUserId;
		for (File photo : photosPath.listFiles()) {
			photos.add(Photo.insert(photo, localUserDirName));
			//delete the photo in the temp folder
			photo.delete();
		}
		return photos;
	}

	/**
	 * Insert the photos from  photos from copiedUser to localUser.
	 * @param copiedPhotos - the Collection of photo objects from the copiedUser.
	 * @param localUserDir - the path to the folder where the copiedUser's photos will be inserted.
	 * @param copiedUserDir - the path to the folder containing the copiedUser's photos.
	 * @return photos - the Collection of the inserted photos.
	 * @throws IOException 
	 */
	public static Collection<Photo> insertAll(Collection<Photo> copiedPhotos, File localUserDir, 
			File copiedUserDir) throws IOException {
		//copy the objects
		//the Collection to be returned
		Collection<Photo> photos = Photo.deepCopy(copiedPhotos);
		//copy the files
		for (File photoDir : copiedUserDir.listFiles(File::isDirectory)) {
			Photo.insert(photoDir, localUserDir);
		}
		return photos;
	}

	/**
	 * Move photo (and corresponding commentsTxt, likesTxt and dislikesTxt) from temporary folder 
	 * to the corresponding user's folder.
	 * @param photoFileOrigin - the the photo file inside the origin folder
	 * @param localUserDirName - the name of the directory of the localUser
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
	 */
	private static Photo insert(File photoFileTemp, String localUserDirName) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException {
		//name of the photo file inside the temp folder, without the corresponding extension
		String photoNameNoExtention = photoFileTemp.getName().split("\\.")[0];
		//create the destiny directory for the photo
		String photoDestenyDirName = localUserDirName + fileSeparator + photoNameNoExtention;
		File photoDestenyDir = new File(photoDestenyDirName);
		photoDestenyDir.mkdir();
		//create the File corresponding to the photo in the photoDestenyDir
		File photoInDestiny = 
				new File(photoDestenyDirName + fileSeparator + photoFileTemp.getName());
		//move the photo from temp dir to photoDestenyDir
		photoFileTemp.renameTo(photoInDestiny);
		//cipher the photo
		SecretKey sk = Crypto.getInstance().getSecretKey();
		Crypto.cipherFile(photoInDestiny, sk);
		//creates the empty likesTxt, dislikesTxt and commentsTxt in this photo's directory.
		Comment.insertAll(photoDestenyDir);
		Like.insertAll(photoDestenyDir);
		Dislike.insertAll(photoDestenyDir);
		return new Photo(photoInDestiny.getName(), photoInDestiny.lastModified());
	}

	/**
	 * Copies the files from the photo's source folder to the destiny folder.
	 * @param srcDir - the path to the source folder.
	 * @param dstDir - the path to the destiny folder.
	 * @return photo - the inserted photo.
	 * @throws IOException 
	 */
	private static void insert(File srcDir, File dstDir) throws IOException {
		//create the destiny directory for the photo
		File dstPhotoDir = new File(
				dstDir.getPath() + fileSeparator +
				srcDir.getName()); 
		dstPhotoDir.mkdir();
		//for each file in the srcDir
		File[] files = srcDir.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			Path srcPath = file.toPath();
			Path dstPath = Paths.get(dstPhotoDir.getPath() + fileSeparator + file.getName());
			switch (fileName) {
			case commentsTxtName:
				Comment.insertAll(srcPath, dstPath);
				break;
			case dislikesTxtName:
				Like.insertAll(srcPath, dstPath);
				break;
			case likesTxtName:
				Dislike.insertAll(srcPath, dstPath);
				break;
				//the file is the photo
			default:
				Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
				break;
			}
		}
	}

	/**
	 * Creates a deep copy of the given Collection<Photo>.
	 * @param copiedPhotos - the photos to be copied.
	 * @return copyPhotos - the copy of copiedPhotos.
	 */
	private static Collection<Photo> deepCopy(Collection<Photo> copiedPhotos) {
		//make a deep copy of each photo and add it to copyPhotos.
		Collection<Photo> copyPhotos = new LinkedList<>();
		for (Photo copiedPhoto : copiedPhotos) {
			copyPhotos.add(deepCopy(copiedPhoto));
		}
		return copyPhotos;
	}

	/**
	 * Creates a deep copy of the given Photo.
	 * @param copiedPhoto - the photo to be copied.
	 * @return copyPhoto - the copy of copiedPhoto.
	 */
	private static Photo deepCopy(Photo copiedPhoto) {
		return new Photo(
				copiedPhoto.getName(),
				copiedPhoto.getDate().getTime(),
				Comment.deepCopy(copiedPhoto.getComments()),
				Like.deepCopy(copiedPhoto.getLikes()),
				Dislike.deepCopy(copiedPhoto.getDislikes()));
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
		this.comments = new LinkedList<>();
		this.likes = new LinkedList<>();
		this.dislikes = new LinkedList<>();
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
	 * @return a collection of this photo's comments
	 */
	protected Queue<Comment> getComments() {
		return comments;
	}

	/**
	 * 
	 * @return a collection of this photo's likes
	 */
	private Queue<Like> getLikes() {
		return likes;
	}

	/**
	 * 
	 * @return a collection of this photo's dislikes
	 */
	private Queue<Dislike> getDislikes() {
		return dislikes;
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
	 */
	protected void addComment(String comment, String commentedUserId, String commenterUserId, 
			String photoName) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, SignatureException {
		comments.add(Comment.insert(comment, commentedUserId, commenterUserId, photoName));
	}

	/**
	 * Adds a like made by likerUser in this user's photo
	 * @param likedUserId - the userId of the likedUser
	 * @param likerUserId - the userId of the likerUser
	 * @param photoName - the name of this user's photo
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
	 */
	protected void addLike(String likedUserId, String likerUserId, String photoName) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, SignatureException {
		likes.add(Like.insert(likedUserId, likerUserId, photoName));
	}

	/**
	 * Adds a dislike made by likerUser in this user's photo
	 * @param dislikedUserId - the userId of the dislikedUser
	 * @param dislikerUserId - the userId of the dislikerUser 
	 * @param photoName - the name of this user's photo
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
	protected void addDislike(String dislikedUserId, String dislikerUserId, String photoName) throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, SignatureException {
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