package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Biblioteca de manipulacao de ficheiros para a classe Server Extende Server
 * para tirar partido de algumas variaveis da classe
 * 
 * @author António
 */
public class FileManager extends Server {
	private static final String path = new String("database");
	private static final String userDB = new String("users.txt");
	private static final File database = new File(path + "\\" + userDB);
	private static FileReader fr;
	private static BufferedReader br;
	private static FileWriter fw;
	private static BufferedWriter bw;

	/**
	 * Cria o handler de gestao de ficheiros
	 * 
	 * @throws IOException
	 */
	public FileManager() throws IOException {
		File dir = new File("database");
		if (!dir.exists()) {
			dir.mkdir();
		}
		// Ja existe uma base de dados?
		if (!database.exists()) {
			database.createNewFile();
		}
	}

	/**
	 * Le e devolve uma lista dos utilizadores registados
	 * 
	 * @return - A lista dos utilizadores na base de dados
	 * @throws IOException
	 */
	public ArrayList<User> loadUsers() throws IOException {
		fr = new FileReader(database.getAbsoluteFile());
		br = new BufferedReader(fr);
		fw = new FileWriter(database.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);
		User temp;
		ArrayList<User> tempList = new ArrayList<User>();

		// Le os seguidores actuais
		for (String line; (line = br.readLine()) != null;) {
			String[] data = line.split(":");
			temp = new User(data[0], data[1]);
			loadFollowers(temp);
			loadPhotos(temp);
			tempList.add(new User(data[0], data[1]));
		}
		closeBuffers();
		return tempList;
	}

	/**
	 * Fecha os leitores/escritores de ficheiros
	 * 
	 * @throws IOException
	 */
	private static void closeBuffers() throws IOException {
		bw.close();
		fw.close();
		br.close();
		fr.close();
	}

	// =================== OPERACOES =================== //

	/**
	 * Acrescenta um utilizador novo a base de dados Este metodo so e chamado quando
	 * nao houver registo previo desse utilizador
	 * 
	 * @param user
	 *            - O ID de utilizador
	 * @param pass
	 *            - A password
	 * @param userList
	 *            - A lista de utilizadores registados
	 * @throws IOException
	 */
	public void FMaddUser(String user, String pass) throws IOException {
		// Se nao, preparar buffers para registo
		File file = new File(path + "\\" + userDB);

		fw = new FileWriter(file.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);

		// Registar utilizador no ficheiro de base de dados
		String temp = user + ":" + pass;
		bw.write(temp);
		bw.newLine();

		// Criar directoria de fotos do utilizador
		new File(path + "\\" + user).mkdir();
		// Criar lista de seguidores
		File followers = new File(path + "\\" + user + "\\" + "followers.txt");
		if (!followers.exists()) {
			followers.createNewFile();
		}
		closeBuffers();
	}

	public void FMaddPhotos(String user, File photo) {
		String[] info = photo.getName().split(".");
		// remove a parte ".jpg" do nome da foto
		String nomeFoto = info[0];

		// pasta onde a foto vai ser colocada
		String pastaFoto = path + "\\" + user + "\\" + nomeFoto;

		// criamos uma nova pasta para guardar la a foto
		new File(pastaFoto).mkdir();

		// O sitio onde vamos guardar a foto
		File file = new File(pastaFoto + "\\" + photo.getName());

		// TYPE_INT_ARGB means that we are representing the Alpha,
		// Red, Green and Blue component of the image pixel using 8 bit integer
		// value.
		BufferedImage image = new BufferedImage(1260, 840, BufferedImage.TYPE_INT_ARGB);
		image = ImageIO.read(photo);
		ImageIO.write(image, info[2], file);

		// cria ficheiro comments e reactions (para guardar os comentarios e os
		// likes/dislikes)
		new File(pastaFoto + "\\" + "comments.txt").createNewFile();
		new File(pastaFoto + "\\" + "reactions.txt").createNewFile();
	}

	/**
	 * Lista as fotos de um utilizador
	 * 
	 * @param user
	 *            - O utilizador
	 * @return
	 * @throws IOException
	 */
	public String[] FMlistPhotos(String user) throws IOException {
		File file = new File(path + "\\" + database + "\\" + user);
		String[] folders = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		closeBuffers();
		return folders;
	}

	public String[] FMgetInfo(String user, String photo) throws IOException {
		
		File reactions = new File(path + "\\" + userDB + "\\" + user + "\\"
				+ photo + "\\" + "reactions.txt");
		File comments = new File(path + "\\" + userDB + "\\" + user + "\\"
				+ photo + "\\" + "comments.txt");

		// Leitura dos likes/dislikes
		fr = new FileReader(reactions.getAbsoluteFile());
		br = new BufferedReader(fr);
		String likes = br.readLine();
		String dislikes = br.readLine();

		// Leitura dos comentarios
		fr = new FileReader(comments.getAbsoluteFile());
		br = new BufferedReader(fr);

		// Acrescento do numero de likes e dislikes
		ArrayList<String> commentList = new ArrayList<String>();
		commentList.add(likes);
		commentList.add(dislikes);

		// Acrescento dos comentarios
		String line;
		while ((line = br.readLine()) != null) {
			commentList.add(line);
		}

		// Conversao da ArrayList para String[]
		String[] info = commentList.toArray(new String[commentList.size()]);
		closeBuffers();
		return info;
	
	}
	
	/**
	 * Obtem as fotos de um dado seguidor
	 * 
	 * @param user
	 *            - O utilizador
	 * @return - A lista de fotos desse utilizador
	 * @throws IOException
	 */
	public ArrayList<File> FMgetPhotos(String user) throws IOException {
		// Criar variaveis
		File f;
		ArrayList<File> photoList = new ArrayList<File>();

		// Listar as fotos de um utilizador
		String[] paths = FMlistPhotos(user);
		for (String p : paths) {
			// Obter a imagem do directorio (por extensao)
			f = new File(path + "\\" + user + "\\" + p);
			File[] files = f.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					boolean isImage = name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg")
							|| name.toLowerCase().endsWith(".jpeg");
					return isImage;
				}
			});
			// Adicionar foto a lista final
			photoList.add(files[0]);
		}
		return photoList;
	}

	/**
	 * Acrescenta um comentario a uma foto
	 * 
	 * @param comment
	 *            - O comentario
	 * @param user
	 *            - O utilizador
	 * @param photo
	 *            - A foto
	 * @throws IOException
	 */
	public void FMaddComment(String comment, String user, String photo) throws IOException {
		// Constroi linha a escrever no ficheiro
		Date local = new Date();
		String finalLine = user + ":" + df.format(local) + ":" + comment;

		// Abre ficheiro de comentarios e buffers respectivos
		File comments = new File(path + "\\" + user + "\\" + photo + "\\" + "comments.txt");
		fw = new FileWriter(comments.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);

		// Escrita no ficheiro
		bw.write(finalLine);
		bw.newLine();
		closeBuffers();
	}

	/**
	 * Acrescenta um like a uma foto
	 * 
	 * @param user
	 *            - O utilizador
	 * @param photo
	 *            - A foto
	 * @throws IOException
	 */
	public static void FMaddLike(String user, String photo) throws IOException {
		// Abre ficheiro antigo e cria novo
		File file = new File(path + "\\" + user + "\\" + photo + "\\" + "reactions.txt");
		File novo = new File(path + "\\" + user + "\\" + photo + "\\" + "reactions2.txt");

		fw = new FileWriter(novo.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);
		br = new BufferedReader(new FileReader(file));

		// Fazer magia!
		if (follows(user)) {
			int likesAtuais = Integer.parseInt(br.readLine());
			String atualizaLikes = String.valueOf(likesAtuais + 1);
			String disLikesAtuais = br.readLine();
			bw.write(atualizaLikes);
			bw.newLine();
			bw.write(disLikesAtuais);
			System.out.println(file.getName());
		}

		closeBuffers();
		// WTFFF
		System.gc();
		file.delete();
		novo.renameTo(file);
	}

	/**
	 * Acrescenta um dislike a uma foto
	 * 
	 * @param user
	 *            - O utilizador
	 * @param photo
	 *            - A foto
	 * @throws IOException
	 */
	public static void dislike(String user, String photo) throws IOException {
		// Abre ficheiro antigo e cria novo
		File file = new File(path + "\\" + user + "\\" + photo + "\\" + "reactions.txt");
		File novo = new File(path + "\\" + user + "\\" + photo + "\\" + "reactions2.txt");

		fw = new FileWriter(novo.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);
		br = new BufferedReader(new FileReader(file));

		// Fazer magia!
		if (follows(user)) {
			String likesAtuais = br.readLine();
			int dislikesAtuais = Integer.parseInt(br.readLine());
			String atualizaDislikes = String.valueOf(dislikesAtuais + 1);
			bw.write(likesAtuais);
			bw.newLine();
			bw.write(atualizaDislikes);
			System.out.println(file.getName());
		}

		closeBuffers();
		// WTFFF
		System.gc();
		file.delete();
		novo.renameTo(file);
	}

	/**
	 * Acrescenta uma lista de seguidores a um utilizador
	 * 
	 * @param followers
	 *            - O array com os seguidores a acrescentar
	 * @throws IOException
	 */
	public boolean FMaddFollowers(String[] followers, String user) throws IOException {
		// Abre recursos e streams necessarios
		File file = new File(path + "\\" + user + "\\" + "followers.txt");

		fw = new FileWriter(file.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);

		fr = new FileReader(file.getAbsoluteFile());
		br = new BufferedReader(fr);

		// Acrescentar seguidores
		for (int i = 0; i < followers.length; i++) {
			bw.write(followers[i]);
			bw.newLine();
		}

		// Fechar streams
		closeBuffers();
		return true;
	}

	public boolean FMremoveFollowers(String[] followers) throws IOException {
		// Abre recursos e streams necessarios
		File file = new File(path + "\\" + currUser + "\\" + "followers.txt");

		fr = new FileReader(file.getAbsoluteFile());
		br = new BufferedReader(fr);

		ArrayList<String> currFollowers = new ArrayList<String>();

		// Le os seguidores actuais
		String line;
		while ((line = br.readLine()) != null) {
			currFollowers.add(line);
		}

		// Remove os utilizadores
		for (int i = 0; i < followers.length; i++) {
			if (!currFollowers.remove(followers[i])) {
				return false;
			}
		}

		// Criar lista de seguidores
		closeBuffers();
		System.gc();
		file.delete();

		// Abre buffers para novo ficheiro de seguidores
		File newFollowers = new File(path + "\\" + currUser + "\\" + "followers.txt");
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

	/**
	 * Verifica se o utilizador local segue "user"
	 * 
	 * @param user
	 *            - O utilizador
	 * @return - Se o utilizador local segue "user"
	 * @throws IOException
	 */
	public static boolean follows(String user) throws IOException {
		File file = new File(path + "\\" + user + "\\" + "followers.txt");

		BufferedReader br = new BufferedReader(new FileReader(file));

		String userLido;
		while ((userLido = br.readLine()) != null) {
			if (userLido.equals(currUser)) {
				br.close();
				return true;
			}
		}
		br.close();
		return false;
	}

	public void loadFollowers(User u) throws IOException {
		// load followers
		File followersFile = new File(path + "\\" + u.getUserid() + "\\" + "followers.txt");

		br = new BufferedReader(new FileReader(followersFile));

		String userid;
		while ((userid = br.readLine()) != null)
			u.addFollower(userid);

		br.close();
	}

	/**
	 * load photos from database to the user
	 */
	public void loadPhotos(User u) {
		// load photos
		String photosFile = path + "\\" + u.getUserid() + "\\" + "Photos";
		// todas as pastas de fotos
		File[] directories = new File(photosFile).listFiles(File::isDirectory);

		// buscar as fotos presentes em cada directoria
		for (File photo : directories) {
			Photo p = new Photo(photo.getName());
			// colocar a foto na lista de fotos do utilizador
			u.addPhoto(p);
		}

	}
}
