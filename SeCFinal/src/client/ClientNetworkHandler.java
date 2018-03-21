package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import interfaces.NetworkHandler;

public class ClientNetworkHandler implements NetworkHandler {
	private String addr;
	private int port;
	private Socket clientSocket;
	private ObjectOutputStream out;
	private FileInputStream fis;

	/**
	 * Construtor do handler de rege
	 * 
	 * @param serverAddress
	 *            - O endereco do servidor no formato ip:porto
	 * @throws IOException
	 */
	public ClientNetworkHandler(String serverAddress) throws IOException {
		String[] param = serverAddress.split(":");
		addr = param[0];
		port = Integer.parseInt(param[1]);

		this.clientSocket = new Socket(addr, port);
		out = new ObjectOutputStream(clientSocket.getOutputStream());
		System.out.println("no cliente, depois de se ligar pela socket");
	}

	public Socket getSocket() {
		return this.clientSocket;
	}

	/**
	 * Fecha o socket TCP e todos os recursos associados
	 */
	public void endConnection() throws IOException {
		out.close();
		if(fis!=null)
			fis.close();
		clientSocket.close();
	}

	/**
	 * Envia um ficheiro para o servidor associado ao handler
	 * 
	 * @param f
	 *            - O ficheiro a enviar
	 * @requires - Foi efectuada ligacao com o servidor
	 * @throws IOException
	 */
	public void enviarFile(File f) throws IOException {
		// Abre streams de input e output
		fis = new FileInputStream(f);
		byte[] buffer = new byte[1024];

		// Declaracao de contadores de envio
		int count;
		int fileSize = (int) f.length();

		out.writeInt(fileSize);
		while ((count = fis.read(buffer)) > 0) {
			out.write(buffer, 0, count);
		}
	}

	/**
	 * Envia uma mensagem em bytecode
	 * @return 
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @returns - True se o servidor aceitou a mensagem, False em caso contrario
	 */
	//TODO
	public void send(String[] message) throws IOException, ClassNotFoundException {
		out.writeObject(message);
	}

	@Override
	public void startConnection(int port) throws IOException, UnknownHostException {
		// TODO Auto-generated method stub

	}

	public void authenticate(String userID, String pass) throws IOException {
		//ercrever username e password na socket
		out.writeObject(userID);
		out.writeObject(pass);
	}
}
