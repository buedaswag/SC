package server;

import java.io.File;
import java.io.IOException;
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
				String photo = user.getPhoto(photo);
				answer = infoPhoto(user);
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
		fm.addUser(name, password);
		return true;
	}
	
	/**
	 * Lista as fotos de um utilizador
	 * @param user - O utilizador
	 * @return "success" caso tenha sucesso, null caso contrario
	 */
	public static String listPhotos(String user)
	{
		// User existe?
		User temp = getByName(user);
		if(temp == null) return null;
		ArrayList<Photo> photos = null;
		
		// Utilizador segue user?
		if(isFollower(temp))
		{
			photos = fm.getInfo(user);
		}
		else
		{
			return null;
		}
		
		// Preencher lista de informacao
		String list = new String();
		for(Photo p : photos)
		{
			list.concat(p.getName() + " - ");// + p.getDate().
		}
		return list;
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
		if(!isFollower(getByName(user)))
		{
			return null;
		}
		else
		{
			Server.addComment(comment, user, photo);
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
		if(!isFollower(getByName(user)))
		{
			return null;
		}
		else
		{
			Server.addLike(user, photo);
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
		if(!isFollower(getByName(user)))
		{
			return null;
		}
		else
		{
			Server.addDislike(user, photo);
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
		ArrayList<User> temp = new ArrayList<User>();
		for(String s: users)
		{
			User u = Server.getByName(s);
			if(u != null)
			{
				temp.add(u);
			}
			else
			{
				return null;
			}
		}
		// O utilizador actual e seguidor do currUser?
		if(isFollower(getByName(user)))
		{
			return null;
		}
		// Algum dos utilizadores a acrescentar ja e seguidor?
		for(User u: temp)
		{
			if(isFollower(u))
				return null;
		}
		// Actualizacao na memoria de execucao
		currUser.addFollowers(temp);
		// Actualizacao na memoria fisica
		Server.addFollowers(user, users);
		return "success";
	}
	
	/**
	 * Remove uma lista de utilizadores como seguidores
	 * @param user - O utilizador
	 * @param photo - Os seguidores a remover
	 * @return "success" caso tenha sucesso, null caso contrario
	 */
	public static String removeFollowers(String user, String[] users)
	{
		// Conversao da lista de nomes uma lista de seguidores
		ArrayList<User> temp = new ArrayList<User>();
		for(String s: users)
		{
			User u = Server.getByName(s);
			if(u != null)
			{
				temp.add(u);
			}
			else
			{
				return null;
			}
		}
		// O utilizador actual e seguidor do currUser?
		if(isFollower(getByName(user)))
		{
			return null;
		}
		// Algum dos utilizadores a acrescentar ja e seguidor?
		for(User u: temp)
		{
			if(!isFollower(u))
				return null;
		}
		// Actualizacao na memoria de execucao
		currUser.removeFollowers(temp);
		// Actualizacao na memoria fisica
		Server.addFollowers(user, users);
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
	public static boolean isFollower(User user)
	{
		for(User f : user.getFollowers())
		{
			if(currUser.equals(f))
				return true;
		}
		return false;
	}
}
