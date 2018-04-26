package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * 
 * @author Antonio Dias 47811
 * @author Maximo Oliveira 49024
 * @author Miguel Dias 46427
 *
 */
public class ClientNetworkHandler {
	private String addr;
	private int port;
	private Socket socket;
	private ObjectOutputStream outStream;
	private ObjectInputStream inStream;
	private FileInputStream fis;

	/**
	 * 
	 * @param serverAddress
	 *            the address of the server
	 * @throws IOException
	 */
	protected ClientNetworkHandler(String serverAddress) throws IOException {
		System.setProperty("javax.net.ssl.trustStore", "client.keystore");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		
		String[] param = serverAddress.split(":");
		addr = param[0];
		port = Integer.parseInt(param[1]);

		SocketFactory ssf = SSLSocketFactory.getDefault();
		this.socket = (SSLSocket) ssf.createSocket(addr, port);
		
		outStream = new ObjectOutputStream(socket.getOutputStream());
		System.out.println("In the client, after connecting through the socket");
	}

	/**
	 * 
	 * @return this socket
	 */
	protected Socket getSocket() {
		return this.socket;
	}

	/**
	 * Closes the TCP socket and all the resources associated to it
	 */
	protected void endConnection() throws IOException {
		outStream.close();
		if (fis != null)
			fis.close();
		socket.close();
	}

	/**
	 * Sends a file to the server
	 * 
	 * @param f
	 *            the file to be sent
	 * @requires - A connection with the server
	 * @throws IOException
	 */
	protected void enviarFile(File f) throws IOException {
		// Opens streams
		fis = new FileInputStream(f);
		byte[] buffer = new byte[1024];

		// Declaring counter
		int count;
		int fileSize = (int) f.length();

		outStream.writeInt(fileSize);
		while ((count = fis.read(buffer)) > 0) {
			outStream.write(buffer, 0, count);
		}
		fis.close();
	}

	/**
	 * Sends a message in bytecode
	 * 
	 * @param message
	 *            the message to be sent
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	protected void send(String[] message) throws IOException, ClassNotFoundException {
		outStream.writeObject(message);
	}

	/**
	 * Sends the authentication details to the server
	 * 
	 * @param userID
	 *            the userid given
	 * @param pass
	 *            the password given
	 * @throws IOException
	 */
	protected void authenticate(String userID, String pass) throws IOException {
		// ercrever username e password na socket
		outStream.writeObject(userID);
		outStream.writeObject(pass);
	}

	/**
	 * Receives a message from the socket and returns it. This is used to receive
	 * error messages through the socket.
	 * 
	 * @return message - the message from the socket
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	protected String receive() throws IOException, ClassNotFoundException {
		inStream = new ObjectInputStream(socket.getInputStream());
		return (String) inStream.readObject();
	}
}
