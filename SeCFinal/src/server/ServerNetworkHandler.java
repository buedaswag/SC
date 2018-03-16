package server;

import java.io.IOException;
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
	 */
	public ServerNetworkHandler(int port) {
		startConnection(port);
	}

	@Override
	public void startConnection(int port) throws IOException, UnknownHostException {
		sSoc = null;
		try {
			sSoc = new ServerSocket(23456);
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
