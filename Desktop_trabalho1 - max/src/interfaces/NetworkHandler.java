package interfaces;

import java.io.IOException;
import java.net.UnknownHostException;

public interface NetworkHandler
{
	public void startConnection() throws IOException, UnknownHostException;
	
	public void endConnection() throws IOException;
}
