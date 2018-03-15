package server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import interfaces.NetworkHandler;
import server.Server.ServerThread;

public class Server {
	private int port;
	private String address;

	public static void main(String[] args) {

		System.out.println("servidor: main");
		Server server = new Server();
		server.startServer();

	}

	public void startServer() {
		ServerSocket sSoc = null;

		try {
			sSoc = new ServerSocket(23456);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		//thread pool
		while (true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		// sSoc.close();
	}

	// Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("thread do server para cada cliente");
		}

		private void saveFile(Socket clientSock) throws IOException {

			ObjectInputStream dis = new ObjectInputStream(clientSock.getInputStream());
			FileOutputStream fos = new FileOutputStream("testfile.txt");
			byte[] buffer = new byte[1024];

			int filesize = dis.readInt(); // Send file size in separate msg
			System.out.println("Tamanho : " + filesize + "bytes");
			int read = 0;
			int totalRead = 0;
			int remaining = filesize;
			while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				totalRead += read;
				remaining -= read;
				System.out.println("read " + totalRead + " bytes.");
				fos.write(buffer, 0, read);
			}

		}

		public void run() {
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String user = null;
				String passwd = null;

				try {
					user = (String) inStream.readObject();
					passwd = (String) inStream.readObject();
					System.out.println("thread: depois de receber a password e o user");
					saveFile(socket);
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}

				outStream.close();
				inStream.close();

				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
