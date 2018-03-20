package interfaces;

import java.io.IOException;
import java.net.UnknownHostException;

public interface NetworkHandler {
	
	public void startConnection(int port) throws IOException, UnknownHostException;

	public void endConnection() throws IOException;

<<<<<<< HEAD
	public void send(String[] message) throws IOException, ClassNotFoundException;
=======
	public String send(String[] message) throws IOException, ClassNotFoundException;
>>>>>>> branch 'Miguel' of https://github.com/buedaswag/SCprivate.git
}
