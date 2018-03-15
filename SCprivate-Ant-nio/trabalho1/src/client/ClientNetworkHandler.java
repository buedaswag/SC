package client;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientNetworkHandler implements NetworkHandler
{
	private String addr;
	private int port;
	private Socket clientSocket;
	
	/**
	 * Construtor do handler de rege
	 * @param serverAddress - O endereco do servidor no formato ip:porto
	 */
	public ClientNetworkHandler(String serverAddress)
	{
		String[] param = serverAddress.split(":");
		addr = param[0];
		port = Integer.parseInt(param[1]);
	}
	
	/**
	 * Abre o socket TCP
	 */
	public void startConnection() throws UnknownHostException, IOException
	{
		this.clientSocket = new Socket(addr, port);
	}
	
	/**
	 * Fecha o socket TCP e todos os recursos associados
	 */
	public void endConnection() throws IOException
	{
		// Fechar streams
		clientSocket.close();
	}
}
