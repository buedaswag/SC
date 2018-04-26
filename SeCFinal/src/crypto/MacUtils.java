package crypto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MacUtils
{
	private static final String alias = "seckey";
	private static final String ksFile = "myKeys.keystore";
	private static final String ksPassword = "123456";
	private static final String fileUsers = "passwords.txt";
	private static final String fileUsersMac = "passwords.mac.txt";
	private static final String ERROR_MAC = "ERROR: an illegal change on the " + fileUsers + " file has been detected!";
	private static final String WARNING_NO_MAC = "WARNING: MAC file not found. A new one will be created.";
	
	public static void main(String[] args) throws UnrecoverableKeyException, InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeySpecException {
		String password = "bananas123";
		if(!isMacProtected()) {
			System.out.println(WARNING_NO_MAC);
			macProtect(password);
		}
		System.out.println(checkMac());
	}
	
	/**
	 * Checks whether or not the users.txt file is protected with a MAC code
	 * @return True if passwords.mac.txt exists, False otherwise
	 */
	public static boolean isMacProtected() {
		File macFile = new File(fileUsersMac);
		return macFile.exists();
	}
	
	/**
	 * Protects the users.txt file by creating its MAC file
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws InvalidKeyException
	 * @throws InvalidKeySpecException 
	 * @requires !isMacProtected
	 */
	public static void macProtect(String password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, InvalidKeyException, InvalidKeySpecException {
		// create and load key from keystore
		SecretKey key = generateSecretKey(password);
		
		// initiate MAC
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(key);
		
		// read content from the file to be protected
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileUsers));
		byte[] fileContent = new byte[in.available()];
		in.read(fileContent);
		in.close();
			
		mac.update(fileContent);
			
		// generates and writes the MAC into a file
		BufferedOutputStream macOut = new BufferedOutputStream(new FileOutputStream(fileUsersMac));
		macOut.write(mac.doFinal());
		macOut.flush();
		macOut.close();
		mac.reset();
	}
	
	/**
	 * Checks if the users.txt file has been tampered with
	 * @return True if the file is valid, False otherwise
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 * @throws InvalidKeyException
	 */
	public static boolean checkMac() throws IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException {
		// obtain registered MAC
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileUsersMac));
		byte[] existingMAC = new byte[in.available()];
		in.read(existingMAC);
		in.close();
		
		// calculate MAC from the existing plaintext file
		Mac mac = Mac.getInstance("HmacSHA256");
		KeyStore ks = KeyStore.getInstance("JCEKS");
		FileInputStream fis;
		fis = new FileInputStream(ksFile);
		ks.load(fis, ksPassword.toCharArray());
		// loads the existing secret key from the keystore
		SecretKey key = (SecretKey) ks.getKey(alias, ksPassword.toCharArray());
		mac.init(key);
		// reads content from the plaintext file
		in = new BufferedInputStream(new FileInputStream(fileUsers));
		while(in.available() > 0) {
			byte[] fileExisting = new byte[in.available()];
			in.read(fileExisting);
			mac.update(fileExisting);
		}
		in.close();
		byte[] calculatedMAC = mac.doFinal();
		
		// compare MACs
		if(calculatedMAC.length != existingMAC.length) {
			System.out.println(ERROR_MAC);
			return false;
		}
		for(int i = 0; i < existingMAC.length || i < calculatedMAC.length; i++)
		{
			if(existingMAC[i] != calculatedMAC[i])
			{
				System.out.println(ERROR_MAC);
				return false;
			}
		}
		System.out.println("Equal MACs!");
		return true;
	}
	
	/**
	 * Generates a secret key using an inserted password as seed
	 * @param password The administrator's password
	 * @return A new SecretKey to generate the MAC code with
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static SecretKey generateSecretKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException, KeyStoreException, CertificateException, IOException {
		// generate key
		byte[] pwd = password.getBytes();
	    SecretKey secretKey = new SecretKeySpec(pwd, "HmacSHA256");
	    // gets the keystore instance
	    KeyStore ks = KeyStore.getInstance("JCEKS");
	    char[] keyStorePass = "123456".toCharArray();
	    // sets the key's parameters
	    KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keyStorePass);
	    KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(secretKey);
	    FileInputStream fis = new FileInputStream("myKeys.keystore");
	    ks.load(fis, keyStorePass);
	    // creates a secret key template
	    ks.setEntry(alias, skEntry, protParam);
	    // stores the key in the keystore
    	FileOutputStream fos;
    	fos = new FileOutputStream("myKeys.keystore");
    	ks.store(fos, keyStorePass);
    	
	    return secretKey;
	}
}
