import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class User {

	private static final String path = new String("database");

	private String userid;
	private String password;
	private List<String> followers;
	private List<Photo> photos;

	/**
	 * constructs a new User object
	 * @param userid of this User
	 * @param password of this User
	 */
	public User(String userid, String password) {
		this.userid = userid;
		this.password = password;
		this.followers = new ArrayList<>();
		this.photos = new ArrayList<>();
	}

	/** adiciona os users que ainda nao seguem o user corrent ah sua lista de seguidores
	 * 
	 * @param followUsers os seguidores a adicionar 
	 */
	public void addFollowers(List<String> followUsers) {

		for (String follower : followUsers) {
			if (!followers.contains(follower)) {
				followers.add(follower);
			}

		}

	}

	/**
	 * adds this user's photo
	 * @param photo
	 */
	public void addPhoto(Photo photo) {
		photos.add(photo);
	}

	/**
	 * 
	 * @return this user's userid
	 */
	public String getUserid() {
		return userid;
	}


	/**
	 * 
	 * @return this user's password
	 */
	public String getPassword() {
		return password;
	}


	/**
	 * @throws IOException 
	 * @requires this is an existing user in the file system
	 * @requires exists a followers.txt file in this user's directory
	 */
	public void loadFollowers() throws IOException {
		//load followers
		File followersFile = new File(path + "\\" + getUserid() + "\\" + "followers.txt");

		BufferedReader br = new BufferedReader(new FileReader(followersFile));

		String userid;
		while ((userid = br.readLine()) != null)
			followers.add(userid);

	}

	public void loadPhotos() {
		//load photos
		String photosFile = path + "\\" + getUserid() + "\\" + "Photos";
		// todas as pastas de fotos
		File[] directories = new File(photosFile).listFiles(File::isDirectory);

		for (File photo : directories) {
			Photo p = new Photo(photo.getName());
			photos.add(p);
		}

	}

	public void loadComments(Photo p) throws IOException {

		String photoFile = path + "\\" + getUserid() + "\\" + "Photos" + "\\" + p.getName();

		String comentarios = photoFile + "comments.txt";

		File ficheiroComments = new File(comentarios);

		List<Comment> listaComentarios = new LinkedList();

		BufferedReader br = new BufferedReader(new FileReader(ficheiroComments));

		String comment;
		while ((comment = br.readLine()) != null)
			listaComentarios.add(new Comment(this,comment));	


		p.addComments(listaComentarios);

	}




}
