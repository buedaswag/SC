package server;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Server
{
	private int port;
	private String address;
	private static FileManager fm;
	// Lista de utilizadores (permite manipulacao facil em runtime)
	private static ArrayList<User> userList = new ArrayList<User>();
	// Utilizador actual (temporario; so para testes)
	protected static User currUser;
	//private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	
	/**
	 * TODO
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		setup();
		
		// Ouvir os ports
		
		String[] message = coisa.split("-");
		char opt = message.charAt(0);
		String answer;
		// Executar operacao - este bloco so le e passa argumentos
		switch(opt)
		{
			// Acrescentar fotos
			case 'a':
			{
				
			}
			// Listar fotos
			case 'l':
			{
				String user = message[1];
				answer = listPhotos(user);
				// Enviar resposta
			}
			// Informacao foto
			case 'i':
			{
				String user = message[1];
				String photo = message[2];
				answer = infoPhoto(user, photo);
				// Enviar resposta
			}
			// Copiar fotos
			case 'g':
			{
				String user = message[1];
				answer = savePhotos(user);
				// Enviar resposta
			}
			// Comentar foto
			case 'c':
			{
				String comment = message[1];
				String user = message[2];
				String photo = message[3];
				answer = addComment(comment, user, photo);
				// Enviar resposta
			}
			// Botar like
			case 'L':
			{
				String user = message[1];
				String photo = message[2];
				answer = addLike(user, photo);
				// Enviar resposta
			}
			// Botar dislike
			case 'D':
			{
				String user = message[1];
				String photo = message[2];
				answer = addDislike(user, photo);
			}
			// Adicionar seguidores
			case 'f':
			{
				String user = message[1];
				String[] followers = message[2].split(",");
				// Followers e do tipo "user1,user2,user3..."
				answer = addFollowers(user, followers);
			}
			// Remover seguidores
			case 'r':
			{
				String user = message[1];
				String followers[] = message[2].split(",");
				answer = removeFollowers(user, followers);
			}
			// Operacao ilegal
			default:
			{
				System.out.println("Foi recebida uma operacao invalida");
			}
				
		}
			// Realizar operacao
		
		// Fechar tudo
		
		
	}
	
	// ================== OPERACOES =================== //
	// Estes metodos comunicam com o FileManager e      //
	// actualizam tanto a memoria fisica como a do      //
	// programa.                                        //
	
	/**
	 * Autentica um utilizador, se este existir
	 * Caso contrario, cria-o e regista-o na base de dados
	 * @param name - O nome do utilizador
	 * @param password - A sua password
	 * @throws IOException 
	 */
	public static boolean authenticate(String name, String password) throws IOException
	{
		// Caso 1: cliente existe
		for(User u : userList)
		{
			// Password certa?
			if(u.getName().equals(name))
			{
				if(u.getPassword().equals(password))
				{
					currUser = u;
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		// Caso 2: cliente nao existe
		fm.FMaddUser(name, password);
		return true;
	}
	
	/**
	 * Lista as fotos de um utilizador
	 * @param user - O utilizador
	 * @return A String com informacao caso tenha sucesso, null caso contrario
	 */
	public static String listPhotos(String user)
	{
		// User existe?
		User temp = getByName(user);
		if(temp == null) return null;
		ArrayList<Photo> photos = null;
		
		// Utilizador segue user?
		if(isFollower(user))
		{
			photos = fm.FMlistPhotos(user);
		}
		else
		{
			return null;
		}
		
		// Preencher lista de informacao
		String list = new String();
		for(Photo p : photos)
		{
			list.concat(p.getName() + " - " + p.getDate());// + p.getDate().
		}
		return list;
	}
	
	/**
	 * Obtem a informacao de uma foto
	 * @param user - O autor da foto
	 * @param photo - O nome da foto
	 * @return A String com informacao se teve sucesso, null em caso contrario
	 */
	public static String infoPhoto(String user, String photo)
	{
		if(isFollower(user))
		{
			
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Copia as fotos de um utilizador para o cliente actual
	 * @param user - O utilizador
	 */
	public void savePhotos(String user)
	{
		
	}
	
	/**
	 * Acrescenta um comentario a uma foto de um utilizador
	 * @param comment - O comentario
	 * @param user - O utilizador
	 * @param photo - A foto
	 * @return "success" caso tenha sucesso, null caso contrario
	 */
	public static String addComment(String comment, String user, String photo)
	{
		if(!isFollower(user))
		{
			return null;
		}
		else
		{
			fm.FMaddComment(comment, user, photo);
		}
		return "success";
	}
	
	/**
	 * Adiciona um like a uma foto de um utilizador
	 * @param user - O utilizador
	 * @param photo - A foto
	 * @return "success" caso tenha sucesso, null caso contrario
	 */
	public static String addLike(String user, String photo)
	{
		if(!isFollower(user))
		{
			return null;
		}
		else
		{
			fm.FMaddLike(user, photo);
		}
		return "success";
	}
	
	/**
	 * Adiciona um dislike a uma foto de um utilizador
	 * @param user - O utilizador
	 * @param photo - A foto
	 * @return "success" caso tenha sucesso, null caso contrario
	 */
	public static String addDislike(String user, String photo)
	{
		if(!isFollower(user))
		{
			return null;
		}
		else
		{
			fm.addDislike(user, photo);
		}
		return "success";
	}
	
	/**
	 * Adiciona uma lista de utilizadores como seguidores
	 * @param user - O utilizador
	 * @param photo - Os futuros seguidores
	 * @return "success" caso tenha sucesso, null caso contrario
	 */
	public static String addFollowers(String user, String[] users)
	{
		// Conversao da lista de nomes uma lista de seguidores
		ArrayList<String> temp = new ArrayList<String>();
		@SuppressWarnings("unused")
		User u;
		for(String s: users)
		{
			if((u = Server.getByName(s)) != null)
			{
				temp.add(s);
			}
			else
			{
				return null;
			}
		}
		// O utilizador actual e seguidor do currUser?
		if(isFollower(user))
		{
			return null;
		}
		// Algum dos utilizadores a acrescentar ja e seguidor?
		for(String s: temp)
		{
			if(isFollower(s))
				return null;
		}
		// Actualizacao na memoria de execucao
		currUser.addFollowers(temp);
		// Actualizacao na memoria fisica
		fm.addFollowers(user, users);
		return "success";
	}
	
	/**
	 * Remove uma lista de utilizadores como seguidores
	 * @param user - O utilizador
	 * @param photo - Os seguidores a remover
	 * @return "success" caso tenha sucesso, null caso contrario
	 */
	@SuppressWarnings("unused")
	public static String removeFollowers(String user, String[] users)
	{
		// Conversao da lista de nomes uma lista de seguidores
		ArrayList<String> temp = new ArrayList<String>();
		User u;
		for(String s: users)
		{
			if((u = Server.getByName(s)) != null)
			{
				temp.add(s);
			}
			else
			{
				return null;
			}
		}
		// O utilizador actual e seguidor do currUser?
		if(isFollower(user))
		{
			return null;
		}
		// Algum dos utilizadores a remover nao e seguidor?
		for(String s: temp)
		{
			if(!isFollower(s))
				return null;
		}
		// Actualizacao na memoria de execucao
		currUser.addFollowers(temp);
		// Actualizacao na memoria fisica
		fm.addFollowers(user, users);
		return "success";
	}
	
	
	// ================== UTILIDADES ================== //
	
	/**
	 * Inicia o servidor, criando directorios e ficheiros
	 * de registo se necessario. Nao cria interfaces de rede;
	 * tudo o que diz respeito a portos, TCP e outras coisas
	 * giras fica ao encargo do handler.
	 * @throws IOException
	 */
	public static void setup() throws IOException
	{
		fm = new FileManager();
		userList = fm.loadUsers();
		
	}
	
	public static User getByName(String id)
	{
		for(User u : userList)
		{
			if(u.getName().equals(id))
				return u;
		}
		return null;
	}
	
	/**
	 * Verifica se o utilizador actual tem user como seguidor
	 * @return - idem
	 */
	public static boolean isFollower(String user)
	{
		for(String f : currUser.getFollowers())
		{
			if(f.equals(user))
				return true;
		}
		return false;
	}
}
