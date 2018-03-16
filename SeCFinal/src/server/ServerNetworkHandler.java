package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import interfaces.NetworkHandler;

public class ServerNetworkHandler implements NetworkHandler {

	/*
	 * the socket, ObjectInputStream for the TCP communication
	 * 
	 */
	ServerSocket sSoc;
	ObjectInputStream dis;

	/**
	 * Constructor for the ServerNetworkHandler
	 * 
	 * @param port
	 *            the TCP port where the connection is made
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public ServerNetworkHandler(int port) throws UnknownHostException, IOException {
		startConnection(port);
	}

	@Override
	public void startConnection(int port) throws IOException, UnknownHostException {
		sSoc = null;
		try {
			sSoc = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	@Override
	public void endConnection() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String send(byte[] message) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}


}
