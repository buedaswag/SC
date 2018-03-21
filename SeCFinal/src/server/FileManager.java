package server;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Biblioteca de manipulacao de ficheiros para a classe Server Extende Server
 * para tirar partido de algumas variaveis da classe
 * 
 * @author Ant�nio
 */
public class FileManager {
	private String path;
	private String userDB;
	private File database;
	private SimpleDateFormat df;
	private FileReader fr;
	private BufferedReader br;
	private FileWriter fw;
	private BufferedWriter bw;

	/**
	 * Cria o handler de gestao de ficheiros
	 * 
	 * @throws IOException
	 */
	public FileManager() throws IOException {
		path = new String("database");
		userDB = new String("users.txt");
		database = new File(path + "\\" + userDB);
		df = new SimpleDateFormat("dd/MM/yyyy HH'h'mm");

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

		// Le os utilizadores actuais
		String line;
		while((line = br.readLine()) != null) {
			String[] data = line.split(":");
			temp = new User(data[0], data[1]);
			tempList.add(temp);
			FMloadFollowers(temp);
			FMloadPhotos(temp);
		}
		closeBuffers();
		return tempList;
	}

	/**
	 * Fecha os leitores/escritores de ficheiros
	 * 
	 * @throws IOException
	 */
	private void closeBuffers() throws IOException {
		bw.close();
		fw.close();
		br.close();
		fr.close();
	}

	// =================== OPERACOES =================== //

	/**
	 * Acrescenta um novo utilizador ao sistema de ficheiros
	 * @param user - O nome do utilizador
	 * @param pass - A password do utilizador
	 * @throws IOException
	 */
	public void FMaddUser(String user, String pass) throws IOException {
		//Se nao, preparar buffers para registo
		File file = new File(path + "\\" + userDB);

		fw = new FileWriter(file.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);

		//Registar utilizador no ficheiro de base de dados
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

	/**
	 * Acrescenta uma foto a uma conta de utilizador
	 * @param user - O nome do utilizador
	 * @param photo - O nome da foto
	 * @throws IOException
	 */
	public void FMaddPhoto(String user, File photo) throws IOException {
		String[] info = photo.getName().split("\\.");
		// remove a parte ".jpg" do nome da foto
		String nomeFoto = info[0];

		// pasta onde a foto vai ser colocada
		String pastaFoto = path + "\\" + user + "\\" + nomeFoto;

		// criamos uma nova pasta para guardar la a foto
		new File(pastaFoto).mkdir();

		// O sitio onde vamos guardar a foto
		File file = new File(pastaFoto + "\\" + photo.getName());

		photo.renameTo(file);

		// cria ficheiro comments e reactions (para guardar os comentarios e os
		// likes/dislikes)
		new File(pastaFoto + "\\" + "comments.txt").createNewFile();
		new File(pastaFoto + "\\" + "reactions.txt").createNewFile();
	}

	/**
	 * Obtem e devolve a informacao de uma foto
	 * @param user - O nome do utilizador
	 * @param photo - O nome da foto (sem extensao)
	 * @return - Um array com o formato {likes,dislikes,comentario1,comentario2...}
	 * @throws IOException
	 */
	public String[] FMgetInfo(String user, String photo) throws IOException {

		File reactions = new File(path + "\\" + userDB + "\\" + user + "\\" + photo + "\\" + "reactions.txt");
		File comments = new File(path + "\\" + userDB + "\\" + user + "\\" + photo + "\\" + "comments.txt");

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
	 * Acrescenta um comentario a uma foto
	 * @param comment - O comentario
	 * @param user - O autor do comentario
	 * @param commentedUserId - O destinatario do comentario
	 * @param photo - O nome da foto a comentar
	 * @throws IOException
	 */
	public void FMaddComment(String comment, String localUserId, 
			String commentedUserId, String photo) throws IOException {
		// Constroi linha a escrever no ficheiro
		Date local = new Date();
		String finalLine = localUserId + ":" + df.format(local) 
			+ ":" + comment;

		// Abre ficheiro de comentarios e buffers respectivos
		File comments = new File(path + "\\" + commentedUserId + "\\" 
				+ photo.split("\\.")[0] + "\\" + "comments.txt");
		fw = new FileWriter(comments.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);

		// Escrita no ficheiro
		bw.write(finalLine);
		bw.newLine();
		closeBuffers();
	}

	/**
	 * Acrescenta um like a uma foto
	 * @param localUserId - O remetente do like (necessario mais tarde)
	 * @param likedUserId - O destinatario do like
	 * @param photo - O nome da foto para onde vai o like
	 * @throws IOException
	 */
	public void FMaddLike(String localUserId, String likedUserId, String photo) throws IOException {
		String finalLine = localUserId;
		File reactions = new File(path + "\\" + likedUserId + "\\" 
				+ photo.split("\\.")[0] + "\\" + "reactions.txt");
		fw = new FileWriter(reactions.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);
		
		// Escrita no ficheiro
		bw.write(finalLine);
		bw.newLine();
		closeBuffers();
	}

	/**
	 * Acrescenta uma lista de seguidores a um utilizador
	 * @param followers - A lista com os nomes dos seguidores a acrescentar
	 * @param localUserId - O utilizador
	 * @return - True se a operacao teve sucesso, False caso contrario
	 * @throws IOException
	 */
	public boolean FMaddFollowers(String[] followers, String localUserId) throws IOException {
		// Abre recursos e streams necessarios
		File file = new File(path + "\\" + localUserId + "\\" + "followers.txt");

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

	// ====================================================================================================

	/**
	 * Acrescenta uma lista de seguidores a um utilizador
	 * @param followers - A lista com os nomes dos seguidores a acrescentar
	 * @param localUserId - O utilizador
	 * @return - True se a operacao teve sucesso, False caso contrario
	 * @throws IOException
	 */
	public void FMloadFollowers(User u) throws IOException {
		// load followers
		File followersFile = new File(path + "\\" + u.getUserid() + "\\" 
				+ "followers.txt");

		BufferedReader readerLoadFollowers = 
				new BufferedReader(new FileReader(followersFile));

		String localUserId;
		while ((localUserId = readerLoadFollowers.readLine()) != null)
			u.addFollower(localUserId);
		readerLoadFollowers.close();
	}

	/**
	 * Carrega as fotos de um utilizador para memoria
	 * @param u - O objecto utilizador
	 * @throws IOException 
	 */
	//TODO
	public void FMloadPhotos(User u) throws IOException {
		/*
		 * for each folder in the user directory create a photo object
		 * with the corresponding attributes and add it to the user
		 */
		// load photos
		String photosFile = path + "\\" + u.getUserid();
		// todas as pastas de fotos
		File[] directories = new File(photosFile).listFiles(File::isDirectory);
		//
		String a = null, b = null, x = null;
		if (directories != null)
			// buscar as fotos presentes em cada directoria
			for (File photo : directories) {
				//get the actual photo
				File[] files = photo.listFiles();
				for (File f : files) {
					//check if the file in the directory is my photo
					x = f.getName();
					a = x.split("\\.")[0];
					b = photo.getName();
					if (a.equals(b)) {
						Photo p = new Photo(x);
						// colocar a foto na lista de fotos do utilizador
						u.addPhoto(p);
					}	
				}
			}

		//for each photo do this:
		for (Photo p : u.getPhotos()) {
			/*
			 * for each line in the file comments.txt, create a comment 
			 * object with the corresponding attributes
			 */
			// Le os utilizadores actuais

			File file = new File(path + "\\" + u.getUserid() + "\\" 
					+ p.getName().split("\\.")[0] + "\\" + "comments.txt");
			
			FileReader filer = 
					new FileReader(file);

			BufferedReader buffr = new BufferedReader(filer);
			String line;
			while((line = buffr.readLine()) != null) {
				p.addComment(line, u.getUserid());
			}

			filer.close();
			buffr.close();

			/*
			 * for each line in the file reactions.txt, add a like to
			 * the counter in the corresponding photo object
			 */
			File fileReactions = new File(path + "\\" + u.getUserid() + "\\" 
					+ p.getName().split("\\.")[0] + "\\" + "reactions.txt");

			FileReader fileRea = 
					new FileReader(fileReactions);

			BufferedReader buffr1 = new BufferedReader(fileRea);

			int likes = 0;
			String s;
			while ((s=buffr1.readLine())!=null) {
				p.addLike("");
			}

			fileRea.close();
			buffr1.close();

		}
	}

	/**
	 * Move fotos do directorio temporario para o definitivo
	 * @param localUserId - O utilizador
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
}