package crypto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Test {

	public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
		
		File f = new File("passwords.txt");
		f.delete();
		
		// sets test variables
		String user = "user";
		String pwdC = "bananas123";
		String pwdW = "laranjas123";
		
		// calculates both the salt and the hashcode of the salt/password pair
		byte[] sal = PasswordUtils.getNextSalt();
		byte[] hash = PasswordUtils.hash(pwdC, sal);
		ByteBuffer wrapped = ByteBuffer.wrap(sal);
		int numSalt = wrapped.getInt();
		wrapped = ByteBuffer.wrap(hash);
		String hashString = Base64.getEncoder().encodeToString(hash);
		
		// writes the user's credentials to the passwords file
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(user + ":" + numSalt + ":" + hashString);
		bw.close();
		fw.close();
		
		// reads data from file
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String[] temp = line.split(":");
		String readSalt = temp[1];
		String readHash = temp[2];
		
		br.close();
		fr.close();
		
		// checks whether or not the inserted password is valid
		boolean result = PasswordUtils.isExpectedPassword(pwdC, readSalt, readHash);
		System.out.println(result);
		result = PasswordUtils.isExpectedPassword(pwdW, readSalt, readHash);	
		System.out.println(result);
	}
}
