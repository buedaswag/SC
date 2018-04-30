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
		String alias = "server";
		String keystorePath = "myKeys.keystore";
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
		FileInputStream kfile = new FileInputStream("myKeys.keystore");
		KeyStore kstore = KeyStore.getInstance("JCEKS");
		kstore.load(kfile, password.toCharArray());
		Certificate cert = kstore.getCertificate("server"); 
		
		// verifies the signature
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
}

