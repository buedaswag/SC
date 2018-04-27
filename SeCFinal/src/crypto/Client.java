package crypto;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

public class Client {

	private String host;
	private int port;
	private int bufferSize;
	private String file;
	private Socket echoSocket;
	private String user;
	private String pass;

	public Client() throws UnknownHostException, IOException {
		//ip e port do servidor
		host = "localhost";
		port = 23456;
		bufferSize = 1024;
		file = "test.txt";
		echoSocket =  new Socket(host, port);
		user = "user";
		pass = "password";

	}
	

	public static void main(String args[]) throws IOException, ClassNotFoundException {
		System.out.println("cliente: main");
		Client client = new Client();
		client.startClient();
	}

	public void enviarFile(String file) throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(echoSocket.getOutputStream());
		File f = new File(file);
		FileInputStream fis = new FileInputStream(f);
		byte[] buffer = new byte[1024];

		int count;
		int fileSize = (int) f.length();
		out.writeInt(fileSize);
		while ((count = fis.read(buffer)) > 0) {
			out.write(buffer, 0, count);
		}

		fis.close();
		out.close();

	}


	public void startClient() throws IOException, ClassNotFoundException {
		//abrir Socket e ObjectInputStream e ObjectOutputStream
		ObjectInputStream in = new ObjectInputStream(echoSocket.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(echoSocket.getOutputStream());

		//capturar o username e a pass
		String username = user;
		String password = pass;

		//ercrever username e password na socket
		out.writeObject(username);
		out.writeObject(password);

		// envio do ficheiro
		enviarFile("cliente_teste\\" + file);

		//fechar socket e ObjectInputStream e ObjectOutputStream
		out.close();
		in.close();
		echoSocket.close();
	}

}