package crypto_ponto4;

import java.awt.RenderingHints.Key;

/***************************************************************************
 *   Seguranca e Confiabilidade 2016/17
 *
 *
 ***************************************************************************/

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class Server{
	
	private final String FICHEIRO_OUT = "servidor_teste\\testfile.txt";

	public static void main(String[] args) {
		System.out.println("servidor: main");
		Server server = new Server();
		server.startServer();
	}

	public void startServer (){
		ServerSocket sSoc = null;

		try {
			sSoc = new ServerSocket(23456);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}
		//sSoc.close();
	}



	//Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("thread do server para cada cliente");
		}

		private void saveFile(Socket clientSock) throws IOException {

			ObjectInputStream dis = new ObjectInputStream(clientSock.getInputStream());
			FileOutputStream fos = new FileOutputStream(FICHEIRO_OUT);
			byte[] buffer = new byte[1024];

			int filesize = dis.readInt(); // Send file size in separate msg
			System.out.println("Tamanho : " + filesize + "bytes");
			int read = 0;
			int totalRead = 0;
			int remaining = filesize;
			while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
				totalRead += read;
				remaining -= read;
				System.out.println("read " + totalRead + " bytes.");
				fos.write(buffer, 0, read);
			}

		}



		public void run(){
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String user = null;
				String passwd = null;

				try {
					user = (String)inStream.readObject();
					passwd = (String)inStream.readObject();
					System.out.println("thread: depois de receber a password e o user");
					saveFile(socket);
					
					
					// ***************************************************
					// Ponto 1 :
					SecretKey sk = Crypto.getSecretKey();
					System.out.println(sk);
					
				    // Ponto 2:
					File f = new File(FICHEIRO_OUT);
					Crypto.cipherFile(f, sk);
				
					//Ponto 3:
					// chave publica
					PublicKey chavePublica = Crypto.getPublicKey("server", "myKeys.keystore");
					//cifra a chave secreta 'k'
					byte[] wrapped = Crypto.cipherKey(sk, chavePublica);
					//armazena chave num ficheiro "testfile.txt.key"
					FileOutputStream fos = new FileOutputStream(FICHEIRO_OUT + ".key");
					fos.write(wrapped);
					fos.close();
					
					// SEGUNDA PARTE DO ALGORITMO
					// Ponto 1:
					File keyFile = new File(f + ".key");
					FileInputStream fis = new FileInputStream(keyFile);
					byte[] keyEncoded = new byte[(int) keyFile.length()];
					fis.read(keyEncoded);
					
					PrivateKey pk = Crypto.privateKey("server", "myKeys.keystore");
					SecretKey skLida = Crypto.decipherKey(keyEncoded, pk);
					System.out.println(sk.equals(skLida));
					Crypto.decipherFile(f, sk);
					// ***************************************************
					
					// **************************************************
				    // Assinar Ficheiro
					File ficheiroAAssinar = new File(FICHEIRO_OUT);
					Crypto.signFile(ficheiroAAssinar);
					
				}catch (ClassNotFoundException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchProviderException e1) {
					e1.printStackTrace();
				} catch (BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SignatureException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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