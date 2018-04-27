package crypto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;


public class SignUtils
{	
	/*
	Function call logic:
	File testFile = new File("test.txt");
	decipherFile(testFile)
	File sigFile = new File("test.txt.sig");
	if(!sigFile.exists()) {
		writeSignature(testFile);
		cipherFile(testFile);
	}
	decipherFile(testFile);
	System.out.println(verifySignature(testFile, sigFile));
	cipherFile(testFile);
	
	*/

	/**
	 * Writes a signature to a given file
	 * @param f The file to be signed
	 * @throws KeyStoreException
	 * @throws UnrecoverableKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 */
	public static void writeSignature(File f) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException, SignatureException, InvalidKeyException {
		
		// keystore-related variables
		String alias = "keyrsa";
		String keystorePath = "myKeys";
		String password = "123456";
		
		// read data from file
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String data = br.readLine();
		String line;
		while((line = br.readLine()) != null) {
			data = data + line;
		}
		br.close();
		
		// load the secret key
		KeyStore ks = KeyStore.getInstance("JCEKS");	
		FileInputStream fis = new FileInputStream(keystorePath);
		ks.load(fis, password.toCharArray());
		
		// build the signature engine
		PrivateKey pk = (PrivateKey) ks.getKey(alias, password.toCharArray()); //obtém a chave privada de alguma forma
		Signature s = Signature.getInstance("MD5withRSA");
		s.initSign(pk);
		byte buf[] = data.getBytes();
		s.update(buf);

		// outputs the signature file
		FileOutputStream fos = new FileOutputStream(f.getAbsolutePath() + ".sig");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(s.sign());
		fos.close();
	}
	
	/**
	 * Verifies the signature of a given file
	 * @param f The file to verify
	 * @param sig The existing signature file
	 * @return True if the file is found to be valid, False otherwise
	 * @throws Exception
	 * @requires f is uncrypted
	 */
	public static boolean verifySignature(File f, File sig) throws Exception {
		
		String password = "123456";
		
		// reads the file contents
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String data = br.readLine();
		String line;
		while((line = br.readLine()) != null) {
			data = data + line;
		}
		br.close();
		fr.close();
		
		// reads the signature file
		FileInputStream fis = new FileInputStream(sig);
		ObjectInputStream ois = new ObjectInputStream(fis);
		byte signature[] = (byte[]) ois.readObject();
		
		// starts the signature engine
		FileInputStream kfile = new FileInputStream("myKeys");
		KeyStore kstore = KeyStore.getInstance("JCEKS");
		kstore.load(kfile, password.toCharArray());
		Certificate cert = kstore.getCertificate("keyrsa"); 
		
		// verifies the signature
		System.out.println(data);
		PublicKey pk = cert.getPublicKey();
		Signature s = Signature.getInstance("MD5withRSA");
		s.initVerify(pk);
		s.update(data.getBytes());
		fis.close();
		if (s.verify(signature))
			return true;
		else
			return false;
	}
	
	/*
	 
	Testing methods; encryption and decryption should be done on the top-level class that verifies the signatures
	private static void cipherFile(File f) throws InvalidKeyException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, KeyStoreException, CertificateException, NoSuchProviderException, IOException {
		Crypto.getInstance().cipherFile(f, Crypto.getInstance().getSecretKey());
	}
	
	private static void decipherFile(File f) throws InvalidKeyException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, KeyStoreException, CertificateException, NoSuchProviderException, IOException {
		Crypto.getInstance().decipherFile(f, Crypto.getInstance().getSecretKey());
	}
	
	*/
}

