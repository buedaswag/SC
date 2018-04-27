package cryptoUtils;

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

import server.Server;

public class MacUtils {
	private static MacUtils macUtils = null;
	private static String usersTxtName = "users.txt";
	private static String fileSeparator = System.getProperty("file.separator");
	private static String databaseRootDirName = "database";
	private static File usersTxt = new File(databaseRootDirName + fileSeparator + usersTxtName);
	private String alias = null;
	private static final String keystoreName = "myKeys.keystore";
	private String keystorePassword = null;
	private String macPassword = null;
	private String macFileName = null;
	private File macFile = null;

	/**
	 * The first call to this function should be followed by a call to init(...)
	 * 
	 * @return macUtils - The single instance of Class MacUtils
	 * @throws IOException
	 */
	public static MacUtils getInstance() throws IOException {
		if (macUtils == null) {
			macUtils = new MacUtils();
		}
		return macUtils;
	}

	/**
	 * Initiates this object with the given password, if it wasn't previously
	 * defined.
	 * 
	 * @param macPassword
	 */
	public void init(String alias, String keystorePassword, String macPassword) {
		if (this.macPassword == null || this.keystorePassword == null) {
			this.alias = alias;
			this.keystorePassword = keystorePassword;
			this.macPassword = macPassword;
			String[] fileNameComponents = usersTxtName.split("\\.");
			this.macFileName = fileNameComponents[0] + ".mac." + fileNameComponents[1];
			this.macFile = new File(databaseRootDirName + fileSeparator + macFileName);
		}
	}

	/**
	 * Checks whether or not the users.txt file is protected with a MAC code.
	 * 
	 * @param file
	 * @return True if passwords.mac.txt exists, False otherwise
	 */
	public boolean isMacProtected() {
		// if init wasn't called yet, nothing to be done
		if (this.macPassword == null || this.keystorePassword == null) {
			throw new java.lang.UnsupportedOperationException("You must call init first");
		}
		return macFile.exists();
	}

	/**
	 * Protects the users.txt file by creating its MAC file
	 * 
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws InvalidKeyException
	 * @throws InvalidKeySpecException
	 * @requires !isMacProtected
	 */
	public void macProtect() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
			UnrecoverableKeyException, InvalidKeyException, InvalidKeySpecException {
		// if init wasn't called yet, nothing to be done
		if (this.macPassword == null || this.keystorePassword == null) {
			throw new java.lang.UnsupportedOperationException("You must call init first");
		}
		// deletes any previous MAC files
		macFile.delete();
		// create and load key from keystore
		SecretKey key = generateSecretKey(macPassword);
		// initiate MAC
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(key);
		// read content from the file to be protected
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(usersTxt));
		byte[] fileContent = new byte[in.available()];
		in.read(fileContent);
		in.close();
		mac.update(fileContent);
		// generates and writes the MAC into a file
		BufferedOutputStream macOut = new BufferedOutputStream(new FileOutputStream(macFile));
		macOut.write(mac.doFinal());
		macOut.flush();
		macOut.close();
		mac.reset();
	}

	/**
	 * Checks if the users.txt file has been tampered with
	 * @requires isMacProtected was called before this method;
	 * @return True if the file is valid, False otherwise
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 * @throws InvalidKeyException
	 */
	public boolean checkMac() throws IOException, NoSuchAlgorithmException, CertificateException,
			UnrecoverableKeyException, KeyStoreException, InvalidKeyException {
		// if init wasn't called yet, nothing to be done
		if (this.macPassword == null || this.keystorePassword == null) {
			throw new java.lang.UnsupportedOperationException("You must call init first");
		}
		// obtain registered MAC
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(macFile));
		byte[] existingMAC = new byte[in.available()];
		in.read(existingMAC);
		in.close();
		// calculate MAC from the existing plaintext file
		Mac mac = Mac.getInstance("HmacSHA256");
		KeyStore ks = KeyStore.getInstance("JCEKS");
		FileInputStream fis;
		fis = new FileInputStream(keystoreName);
		ks.load(fis, keystorePassword.toCharArray());
		// loads the existing secret key from the keystore
		SecretKey key = (SecretKey) ks.getKey(alias, keystorePassword.toCharArray());
		mac.init(key);
		// reads content from the plaintext file
		in = new BufferedInputStream(new FileInputStream(usersTxt));
		while (in.available() > 0) {
			byte[] fileExisting = new byte[in.available()];
			in.read(fileExisting);
			mac.update(fileExisting);
		}
		in.close();
		byte[] calculatedMAC = mac.doFinal();
		// compare MACs
		if (calculatedMAC.length != existingMAC.length) {
			return false;
		}
		for (int i = 0; i < existingMAC.length || i < calculatedMAC.length; i++) {
			if (existingMAC[i] != calculatedMAC[i]) {
				return false;
			}
		}
		System.out.println("Equal MACs!");
		return true;
	}

	/**
	 * Generates a secret key using an inserted password as seed
	 * 
	 * @param password
	 *            The administrator's password
	 * @return A new SecretKey to generate the MAC code with
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public SecretKey generateSecretKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException,
			KeyStoreException, CertificateException, IOException {
		// if init wasn't called yet, nothing to be done
		if (this.macPassword == null || this.keystorePassword == null) {
			throw new java.lang.UnsupportedOperationException("You must call init first");
		}
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
