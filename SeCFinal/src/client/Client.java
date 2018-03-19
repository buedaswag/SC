package client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

public class Client {
	private static String userID;
	private static String pass;
	private static String serverAddress;
	private static ClientNetworkHandler handlerTCP;
	private static String filePath = new String("repositorio");

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
		// Verifica validade dos argumentos
		if (!checkArgs(args)) {
			System.out.println("Argumentos invalidos!");
			return;
		}

		// Regista variaveis de sessao
		userID = args[1];
		pass = args[2];
		serverAddress = args[3];
		char option = args[4].charAt(1);

		// Inicia as coisinhas da comunicacao
		handlerTCP = new ClientNetworkHandler(serverAddress);
		handlerTCP.startConnection();

		// Autentica o utilizador e faz a operacao
		authenticate(userID, pass);
		switch (option) {
		// === ADICIONAR FOTOS
		case 'a': {
			// Preencher array com recurso ao sistema de ficheiros
			if (!addPhotos(args[5555])) {
				System.out.println("Ja adicionou pelo menos uma destas fotos!");
				return;
			}
			System.out.println("Fotos enviadas com sucesso!");
			break;
		}
		// === LISTAR FOTOS DE UM UTILIZADOR
		case 'l': {
			String user = args[5];
			String list = listPhotos(user);
			// Verificar se ocorreu erro
			if (list == null) {
				System.out.println("Nao esta registado como seguidor de " + user + "!");
				return;
			}
			String[] photos = list.split(",");
			System.out.println("Fotos de " + user + ":");
			for (String s : photos) {
				System.out.println(s);
			}
			break;
		}
		// === OBTER INFORMACAO DE UMA FOTO
		case 'i': {
			// Qual a estrutura de dados?
		}
		// === GUARDAR FOTOS DE UM UTILIZADOR
		case 'g': {
			String user = args[5];
			if (!savePhotos(user)) {
				System.out.println("Nao esta registado como seguidor de " + user + "!");
				return;
			}
			System.out.println("Fotos guardadas com sucesso!");
			break;
		}
		// === ADICIONAR UM COMENTARIO
		case 'c': {
			String comment = args[5];
			String user = args[6];
			String photo = args[7];
			if (!addComment(comment, user, photo)) {
				System.out.println("Nao esta registado como seguidor de " + user + "!");
				return;
			}
			System.out.println("Comentario enviado com sucesso!");
			break;
		}
		// === ADICIONAR LIKE
		case 'L': {
			String user = args[5];
			String photo = args[6];
			if (!addLike(user, photo)) {
				System.out.println("Nao esta registado como seguidor de " + user + "!");
				return;
			}
			System.out.println("Like enviado com sucesso!");
			break;
		}
		// === ADICIONAR DISLIKE
		case 'D': {
			String user = args[5];
			String photo = args[6];
			if (!addDislike(user, photo)) {
				System.out.println("Nao esta registado como seguidor de " + user + "!");
				return;
			}
			System.out.println("Dislike enviado com sucesso!");
			break;
		}
		// === REGISTAR SEGUIDORES
		case 'f': {
			String followers = args[5];
			if (!addFollowers(followers)) {
				System.out.println("Um dos utilizadores ja faz parte da sua lista de seguidores!");
				return;
			}
			System.out.println("Utilizadores registados com sucesso!");
			break;
		}
		// === REMOVER SEGUIDORES
		case 'r': {
			String followers = args[5];
			if (!removeFollowers(followers)) {
				System.out.println("Um dos utilizadores nao faz parte da sua lista de seguidores!");
				return;
			}
			System.out.println("Utilizadores removidos com sucesso!");
			break;
		}
		default: {
			System.out.println("Operacao nao reconhecida!");
			break;
		}
		}

		System.out.println("A fechar sessao...");

		// Fechar comunicacao e outras coisas
		handlerTCP.endConnection();
	}

	// ================== OPERACOES ================== //

	public static boolean authenticate(String userID, String pass) {
		return false;
	}

	/**
	 * Adiciona uma lista de fotos a um servidor
	 * 
	 * @param photos
	 *            - A lista de fotos a adicionar
	 * @return True se a operacao teve sucesso, False caso contrario
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static boolean addPhotos(String photos) throws IOException, ClassNotFoundException {
		// Lista fotos do repositorio local
		File local = new File(filePath);
		File[] files = local.listFiles();
		// Valida fotos no servidor
		if (handlerTCP.send(("a1" + photos).getBytes()) == null) {
			return false;
		}

		// Separa nomes das fotos passadas como argumentos
		String[] fileNames = photos.split(",");
		String currFile;

		for (String s : fileNames) {
			for (File f : files) {
				// Compara fotos que se pretende enviar com as que estao no repositorio local
				currFile = f.getName().split(".")[0];
				if (s.equals(currFile)) {
					handlerTCP.enviarFile(f);
				}
			}
		}
		return true;
	}

	/**
	 * Lista as fotos de um utilizador
	 * 
	 * @param user
	 *            - O ID do utilizador
	 * @return A lista de fotos
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static String listPhotos(String user) throws IOException, ClassNotFoundException {
		String message = "l-" + user;
		return (handlerTCP.send(message.getBytes()));
	}

	/**
	 * Apresenta a informacao respeitante a uma determinada foto
	 * 
	 * @param user
	 *            - O utilizador que colocou a foto
	 * @param photo
	 *            - O nome da foto (sem extensao)
	 * @return A informacao da foto
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static String info(String user, String photo) throws IOException, ClassNotFoundException {
		String message = "i-" + user + "-" + photo;
		return (handlerTCP.send(message.getBytes()));
	}

	/**
	 * Guarda as fotos de um utilizador no sistema local de ficheiros
	 * 
	 * @param user
	 *            - O ID do utilizador do qual se pretende guardar as fotos
	 * @return - True se a operaao teve sucesso, False caso contrario
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static boolean savePhotos(String user) throws IOException, ClassNotFoundException {
		String message = "g-" + user;
		return (handlerTCP.send(message.getBytes()) != null);
	}

	/**
	 * Acrescenta um comentario a uma foto
	 * 
	 * @param comment
	 *            - O comentario
	 * @param user
	 *            - O utilizador
	 * @param photo
	 *            - A foto (sem extensao de ficheiro)
	 * @return - True se a operaao teve sucesso, False caso contrario
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static boolean addComment(String comment, String user, String photo)
			throws IOException, ClassNotFoundException {
		String message = "l-" + comment + "-" + user + "-" + photo;
		return (handlerTCP.send(message.getBytes()) != null);
	}

	/**
	 * Acrescenta um like a uma foto
	 * 
	 * @param user
	 *            - O utilizador
	 * @param photo
	 *            - A foto (sem extensao de ficheiro)
	 * @return - True se a operaao teve sucesso, False caso contrario
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static boolean addLike(String user, String photo) throws IOException, ClassNotFoundException {
		String message = "L-" + user + "-" + photo;
		return (handlerTCP.send(message.getBytes()) != null);
	}

	/**
	 * Acrescenta um dislike a uma foto
	 * 
	 * @param user
	 *            - O utilizador
	 * @param photo
	 *            - A foto (sem extensao de ficheiro)
	 * @return - True se a operaao teve sucesso, False caso contrario
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static boolean addDislike(String user, String photo) throws IOException, ClassNotFoundException {
		String message = "D-" + user + "-" + photo;
		return (handlerTCP.send(message.getBytes()) != null);
	}

	/**
	 * Acrescenta um conjunto de utilizadores a lista de seguidores
	 * 
	 * @param followers
	 *            - Os utilizadores a adicionar
	 * @return - True se a operaao teve sucesso, False caso contrario
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static boolean addFollowers(String followers) throws IOException, ClassNotFoundException {
		String message = "f-" + followers;
		return (handlerTCP.send(message.getBytes()) != null);
	}

	/**
	 * Remove um conjunto de utilizadores a lista de seguidores
	 * 
	 * @param followers
	 *            - Os utilizadores a adicionar
	 * @return - True se a operaao teve sucesso, False caso contrario
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static boolean removeFollowers(String followers) throws IOException, ClassNotFoundException {
		String message = "r-" + followers;
		return (handlerTCP.send(message.getBytes()) != null);
	}

	// ================== UTILIDADES ================== //

	/**
	 * Verifica a validade dos parametros passados ao cliente
	 * 
	 * @param args
	 *            - O array com os argumentos
	 * @return True se os argumentos sao validos, False em caso contrario
	 */
	public static boolean checkArgs(String[] args) {
		// Verifica parametro da opcao e comprimento minimo dos argumentos
		int num = args.length;
		if (num < 4)
			return false;
		if (args[4].charAt(0) != '-')
			return false;
		char opt = args[4].charAt(1);

		// Verifica se o numero de argumentos corresponde a opcao escolhida
		if (opt == 'a' || opt == 'l' || opt == 'g' || opt == 'f' || opt == 'r')
			if (num != 6)
				return false;
			else if (opt == 'i' || opt == 'L' || opt == 'D')
				if (num != 7)
					return false;
				else if (opt == 'c')
					if (num != 8)
						return false;
					else
						return true;
		return true;
	}

}
