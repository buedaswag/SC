package server;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 */
public class FileManager {
	private String home;
	private String userDBPath;
	private File database;
	private SimpleDateFormat df;
	private FileReader fr;
	private BufferedReader br;
	private FileWriter fw;
	private BufferedWriter bw;
	private String SEP;

	/**
	 * Creates the handler for the file manipulation
	 * 
	 * @throws IOException
	 */
	public FileManager() throws IOException {
		home = new String("database");
		userDBPath = new String("users.txt");
		SEP = System.getProperty("file.separator");
		database = new File(home + SEP + userDBPath);
		df = new SimpleDateFormat("dd/MM/yyyy HH'h'mm");

		File dir = new File("database");
		if (!dir.exists()) {
			dir.mkdir();
		}
		// Is there already a database?
		if (!database.exists()) {
			database.createNewFile();
		}
	}

	/**
	 * Reads and returns a list of the registered users
	 * 
	 * @return - The list of users in the database
	 * @throws IOException
	 */
	public List<User> loadUsers() throws IOException {
		// initializing buffers
		fr = new FileReader(database.getAbsoluteFile());
		br = new BufferedReader(fr);
		fw = new FileWriter(database.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);
		User u;
		List<User> userList = new ArrayList<User>();

		// Reads the current users
		String line;
		while ((line = br.readLine()) != null) {
			// splits the line from 'userid:password'
			String[] data = line.split(":");
			u = new User(data[0], data[1]);
			// adds the user to the list
			userList.add(u);
			// loads the followers of this user
			FMloadFollowers(u);
			// loads the photos of this user
			FMloadPhotos(u);
		}

		closeBuffers();
		return userList;
	}

	/**
	 * Closes the buffers
	 * 
	 * @throws IOException
	 */
	private void closeBuffers() throws IOException {
		bw.close();
		fw.close();
		br.close();
		fr.close();
	}

	// ===================OPERATIONS =================== //

	/**
	 * Acrescenta um novo utilizador ao sistema de ficheiros
	 * 
	 * @param user
	 *            - O nome do utilizador
	 * @param pass
	 *            - A password do utilizador
	 * @throws IOException
	 */
	public void FMaddUser(String user, String pass) throws IOException {
		// Destination file
		File file = new File(home + SEP + userDBPath);

		fw = new FileWriter(file.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);

		// Registers user in the database
		String temp = user + ":" + pass;
		bw.write(temp);
		bw.newLine();

		// Criar directoria de fotos do utilizador
		new File(home + SEP + user).mkdir();
		// Criar lista de seguidores
		File followers = new File(home + SEP + user + SEP + "followers.txt");
		if (!followers.exists()) {
			followers.createNewFile();
		}
		closeBuffers();
	}

	/**
	 * Adds a photo to a user
	 * 
	 * @param user
	 *            - the user
	 * @param photo
	 *            - the photo to be added
	 * @throws IOException
	 */
	public void FMaddPhoto(String user, File photo) throws IOException {
		String[] info = photo.getName().split("\\.");
		// removes the extension of the photo
		String nomeFoto = info[0];

		// location where the photo will be stored
		String pastaFoto = home + SEP + user + SEP + nomeFoto;

		// Create a new directory to store the photo
		new File(pastaFoto).mkdir();

		// Where the photo will be stored
		File file = new File(pastaFoto + SEP + photo.getName());

		photo.renameTo(file);

		// creates file likes dislikes and comments (to store information)
		new File(pastaFoto + SEP + "comments.txt").createNewFile();
		new File(pastaFoto + SEP + "likes.txt").createNewFile();
		new File(pastaFoto + SEP + "dislikes.txt").createNewFile();
	}

	/**
	 * Gets and return the user info
	 * 
	 * @param user
	 *            - the userid
	 * @param photo
	 *            - the name of the photo without the extension
	 * @return - and array with the format
	 *         {likes,dislikes,comentario1,comentario2...}
	 * @throws IOException
	 */
	public String[] FMgetInfo(String user, String photo) throws IOException {

		File likes = new File(home + SEP + userDBPath + SEP + user + SEP + photo + SEP + "likes.txt");
		File dislikes = new File(home + SEP + userDBPath + SEP + user + SEP + photo + SEP + "dislikes.txt");
		File comments = new File(home + SEP + userDBPath + SEP + user + SEP + photo + SEP + "comments.txt");

		// Reading of likes
		fr = new FileReader(likes.getAbsoluteFile());
		br = new BufferedReader(fr);
		String numLikes = br.readLine();

		// Reading of dislikes
		fr = new FileReader(dislikes.getAbsoluteFile());
		br = new BufferedReader(fr);
		String numDislikes = br.readLine();

		// Reading of comments
		fr = new FileReader(comments.getAbsoluteFile());
		br = new BufferedReader(fr);

		// Add likes and dislikes to a temporary list
		List<String> commentList = new ArrayList<String>();
		commentList.add(numLikes);
		commentList.add(numDislikes);

		// Adds comments to a temporary list
		String line;
		while ((line = br.readLine()) != null) {
			commentList.add(line);
		}

		// ArrayList to String[] conversion
		String[] info = commentList.toArray(new String[commentList.size()]);
		closeBuffers();
		return info;

	}

	/**
	 * Adds a comment to a photo
	 * 
	 * @param comment
	 *            the comment to be added
	 * @param user
	 *            the author of the photo
	 * @param commentedUserId
	 *            - the user whose photo will be commented upon
	 * @param photo
	 *            - the name of the photo to be commented
	 * @throws IOException
	 */
	public void FMaddComment(String comment, String localUserId, String commentedUserId, String photo)
			throws IOException {
		// Constroi linha a escrever no ficheiro
		Date local = new Date();
		String finalLine = localUserId + ":" + df.format(local) + ":" + comment;

		// Opens comments file and buffers
		File comments = new File(home + SEP + commentedUserId + SEP + photo.split("\\.")[0] + SEP + "comments.txt");
		fw = new FileWriter(comments.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);

		// writing on the file
		bw.write(finalLine);
		bw.newLine();
		closeBuffers();
	}

	/**
	 * Adds a like to a photo
	 * 
	 * @param localUserId
	 *            - the auhtor of the like
	 * @param likedUserId
	 *            - the user that gets liked
	 * @param photo
	 *            - the name of the photo that will get a like
	 * @throws IOException
	 */
	public void FMaddLike(String localUserId, String likedUserId, String photo) throws IOException {
		String finalLine = localUserId;
		File likes = new File(home + SEP + likedUserId + SEP + photo.split("\\.")[0] + SEP + "likes.txt");
		fw = new FileWriter(likes.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);

		// writing on the file
		bw.write(finalLine);
		bw.newLine();
		closeBuffers();
	}

	/**
	 * Adds a dislike do the photo
	 * 
	 * @param localUserId
	 *            - the user that dislikes the photo
	 * @param likedUserId
	 *            - the user that gets the dislike
	 * @param photo
	 *            - the name of the photo where the dislike goes
	 * @throws IOException
	 */
	public void FMaddDislike(String localUserId, String dislikedUserId, String photo) throws IOException {
		String finalLine = localUserId;
		File dislikes = new File(home + SEP + dislikedUserId + SEP + photo.split("\\.")[0] + SEP + "dislikes.txt");
		fw = new FileWriter(dislikes.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);

		// writing on the file
		bw.write(finalLine);
		bw.newLine();
		closeBuffers();
	}

	/**
	 * Acrescenta uma lista de seguidores a um utilizador
	 * 
	 * @param followers
	 *            - A lista com os nomes dos seguidores a acrescentar
	 * @param localUserId
	 *            - O utilizador
	 * @return - True se a operacao teve sucesso, False caso contrario
	 * @throws IOException
	 */
	public boolean FMaddFollowers(String[] followers, String localUserId) throws IOException {
		// Opens resources and necessary streams
		File file = new File(home + SEP + localUserId + SEP + "followers.txt");

		fw = new FileWriter(file.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);

		fr = new FileReader(file.getAbsoluteFile());
		br = new BufferedReader(fr);

		// Adds followers
		for (int i = 0; i < followers.length; i++) {
			bw.write(followers[i]);
			bw.newLine();
		}

		// Close streams
		closeBuffers();
		return true;
	}

	/**
	 * Remove uma lista de seguidores a um utilizador
	 * 
	 * @param followers
	 * @return
	 * @throws IOException
	 */
	public boolean FMremoveFollowers(String[] followersToRemove, String userid) throws IOException {
		// Open resources and necessry streams
		File file = new File(home + SEP + userid + SEP + "followers.txt");

		fr = new FileReader(file.getAbsoluteFile());
		br = new BufferedReader(fr);

		List<String> currFollowers = new ArrayList<String>();

		// Read the current followers
		String line;
		while ((line = br.readLine()) != null) {
			currFollowers.add(line);
		}

		// formatting the followersToRemove from ["max,antonio"] to ["max","antonio"]
		String[] removingFollowers = String.join(",", followersToRemove).split(",");

		// removes the followers
		for (int i = 0; i < removingFollowers.length; i++) {
			if (currFollowers.contains(removingFollowers[i])) {
				currFollowers.remove(removingFollowers[i]);
			}
		}

		// Create followers buffers
		closeBuffers();
		System.gc();
		file.delete();

		// Abre buffers para novo ficheiro de seguidores
		File newFollowers = new File(home + SEP + userid + SEP + "followers.txt");
		fw = new FileWriter(newFollowers.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);

		for (String s : currFollowers) {
			bw.write(s);
			bw.newLine();
		}
		bw.close();
		fw.close();
		return true;
	}

	// ====================================================================================================

	/**Loads the users followers to memory
	 * 
	 * @param u the user whose followers will be loaded into memory
	 * @throws IOException
	 */
	public void FMloadFollowers(User u) throws IOException {
		// load followers
		File followersFile = new File(home + SEP + u.getUserid() + SEP + "followers.txt");

		BufferedReader readerLoadFollowers = new BufferedReader(new FileReader(followersFile));

		String localUserId;
		while ((localUserId = readerLoadFollowers.readLine()) != null)
			u.addFollower(localUserId);
		readerLoadFollowers.close();
	}

	/**
	 * Load the photos from a user to memory
	 * 
	 * @param u the user whose photos will be loaded into memory
	 * @throws IOException
	 */
	public void FMloadPhotos(User u) throws IOException {
		/*
		 * for each folder in the user directory create a photo object with the
		 * corresponding attributes and add it to the user
		 */
		// load photos
		String photosFile = home + SEP + u.getUserid();
		// todas as pastas de fotos
		File[] directories = new File(photosFile).listFiles(File::isDirectory);
		//
		String a = null, b = null, x = null;
		if (directories != null)
			// buscar as fotos presentes em cada directoria
			for (File photo : directories) {
				// get the actual photo
				File[] files = photo.listFiles();
				for (File f : files) {
					// check if the file in the directory is my photo
					x = f.getName();
					a = x.split("\\.")[0];
					b = photo.getName();
					if (a.equals(b)) {
						Photo p = new Photo(x, f.lastModified());
						// colocar a foto na lista de fotos do utilizador
						u.addPhoto(p);
					}
				}
			}

		// for each photo do this:
		for (Photo p : u.getPhotos()) {
			/*
			 * for each line in the file comments.txt, create a comment object with the
			 * corresponding attributes
			 */
			// Le os utilizadores actuais

			File file = new File(home + SEP + u.getUserid() + SEP + p.getName().split("\\.")[0] + SEP + "comments.txt");

			FileReader filer = new FileReader(file);

			BufferedReader buffr = new BufferedReader(filer);
			String line;
			while ((line = buffr.readLine()) != null) {
				p.addComment(line, u.getUserid());
			}

			filer.close();
			buffr.close();

			/*
			 * for each line in the file likes.txt, add a like to the counter in the
			 * corresponding photo object
			 */
			File fileLikes = new File(
					home + SEP + u.getUserid() + SEP + p.getName().split("\\.")[0] + SEP + "likes.txt");

			FileReader fileLik = new FileReader(fileLikes);

			BufferedReader buffr1 = new BufferedReader(fileLik);

			int likes = 0;
			String s;
			while ((s = buffr1.readLine()) != null) {
				p.addLike("");
			}

			fileLik.close();
			buffr1.close();

			/*
			 * for each line in the file dislikes.txt, add a dislike to the counter in the
			 * corresponding photo object
			 */
			File filedisLikes = new File(
					home + SEP + u.getUserid() + SEP + p.getName().split("\\.")[0] + SEP + "dislikes.txt");

			FileReader fileDisLik = new FileReader(filedisLikes);

			BufferedReader buffr2 = new BufferedReader(fileDisLik);

			int dislikes = 0;
			String s1;
			while ((s1 = buffr2.readLine()) != null) {
				p.addDisLike("");
			}

			fileDisLik.close();
			buffr2.close();
		}
	}

	/**
	 * Move photos from temporary folder to final folder
	 * 
	 * @param localUserId- O utilizador
	 * @param names - Os nomes das fotos a copiar
	 * @param photosPath - A pasta temporaria onde estao as fotos
	 * @throws IOException
	 */
	public void FMmovePhotos(String localUserId, String[] names, File photosPath) throws IOException {
		// Abre directorio temporario, lista ficheiros e cria lista final
		File dir = new File(photosPath.getAbsolutePath());
		List<File> files = new ArrayList<File>(Arrays.asList(dir.listFiles()));

		// Iterar sobre lista de ficheiros a copiar
		for (File f : files) {
			// Colocar na directoria nova
			FMaddPhoto(localUserId, f);
			// Apaga a foto da directoria antiga
			f.delete();
		}
	}

	/**
	 * Copies the photos from the copiedUser to the localUser
	 * 
	 * @param localUserId
	 * @param copiedUserId
	 * @throws IOException
	 */
	public void FMsavePhotos(String localUserId, String copiedUserId) throws IOException {
		// get the user's database
		String user1Path = home + SEP + copiedUserId;
		String photoName = null;
		String tempPath;
		// Path objects to use on the copy function
		Path src;
		Path dst;
		// new local user's directory
		File newDir;
		// list the target user's database
		File[] dirs = new File(user1Path).listFiles(File::isDirectory);
		if (dirs != null) {
			// list user's folders
			for (File f : dirs) {
				// gets the photo's name
				photoName = f.getName();
				tempPath = home + SEP + localUserId + SEP + photoName + SEP;
				// create the local user's new folder
				newDir = new File(tempPath);
				newDir.mkdir();
				// creates the destination path

				File[] dir = f.listFiles();
				// list a folder's files (photo, likes, dislikes and comment files)
				for (File curr : dir) {
					// locks the current file as the source Path
					src = curr.toPath();
					dst = Paths.get(tempPath + curr.getName());
					// dst = new File(tempPath).toPath();
					// copies the photo
					Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}
}