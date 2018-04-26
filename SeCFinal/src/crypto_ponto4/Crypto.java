package crypto_ponto4;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Crypto {

	private static Crypto crypto = null;
	private static SecretKey SECRET_KEY = null;
	private static PrivateKey PRIVATE_KEY = null;
	
	private static File secretKeyDir = new File("secret_key");
	private static String fileSeparator = System.getProperty("file.separator");
	private static File secretKeyFile = new File(secretKeyDir + fileSeparator + "secretKey.key");
	
	public static Crypto getInstance() throws IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException {
		if(crypto == null)
			crypto = new Crypto();
		return crypto;
	}
	
	private Crypto () throws UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, IOException {
		SECRET_KEY = getSecretKey();
		PRIVATE_KEY = privateKey("server","myKeys.keystore");
	}

	/**  Gets the secret key from the server, stored in the secretKeyDir. If it doesnt exist creates one
	 * 
	 * @return
	 * @throws UnrecoverableKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchProviderException
	 */
	public static SecretKey getSecretKey () throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException {

		// if a secret key doesnt exist, generates one and stores it wrapped the secretKeyDir
		if (!secretKeyDir.exists()) {

			secretKeyDir.mkdir();
			SecretKey sk = generateSecretKey(); 
			FileOutputStream fos = new FileOutputStream(secretKeyFile);
			fos.write(cipherKey(sk, getPublicKey("server", "myKeys.keystore")));
			fos.close();
		}

		// gets the secret key
		FileInputStream fis = new FileInputStream(secretKeyFile);
		byte[] keyEncoded = new byte[(int) secretKeyFile.length()];
		fis.read(keyEncoded);
		PrivateKey pk = Crypto.privateKey("server", "myKeys.keystore");
		SecretKey sk = Crypto.decipherKey(keyEncoded, pk);
		fis.close();

		return sk;

	}

	/** Generates a random secret key
	 *  
	 * @return the random secret key generated
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 */
	public static SecretKey generateSecretKey() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException {

		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(128);
		SecretKey secretKey = keyGenerator.generateKey();

		return secretKey;
	}

	/**
	 * 
	 * @param f o ficheiro cujo conteudo vai ser cifrado
	 * @param k a chave secreta usada para cifrar o conteudo
	 * @return o ficheiro cifrado
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	public static void cipherFile(File ficheiroACifrar, SecretKey k) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {

		// inicializa o motor de cifra com o AES
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, k);

		// abre stream de input para o ficheiro
		FileInputStream inputStream = new FileInputStream(ficheiroACifrar);
		byte[] inputBytes = new byte[(int) ficheiroACifrar.length()];
		// le o conteudo do ficheiro
		inputStream.read(inputBytes);

		// conteudo do ficheiro 'f' cifrado
		byte[] outputBytes = c.doFinal(inputBytes);

		// abre stream para ficheiro a cifrar
		FileOutputStream outputStream = new FileOutputStream(ficheiroACifrar);
		// escreve conteudo cifrado para o ficheiro
		outputStream.write(outputBytes);

		outputStream.close();
		inputStream.close();
	}

	/**
	 * 
	 * @param ficheiroADecifrar
	 * @param k a chave secreta usada para decifrar o ficheiro
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static void decipherFile(File ficheiroADecifrar, SecretKey k) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {

		FileInputStream fis = new FileInputStream(ficheiroADecifrar);

		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, k);    //SecretKeySpec é subclasse de secretKey

		byte[] inputBytes = new byte[(int) ficheiroADecifrar.length()];

		fis.read(inputBytes);

		byte[] outputBytes = c.doFinal(inputBytes);

		// o ficheiro a retornar
		FileOutputStream outputStream = new FileOutputStream(ficheiroADecifrar);
		// escreve conteudo cifrado para o ficheiro a retornar
		outputStream.write(outputBytes);

		outputStream.close();
		fis.close();



	}

	/**
	 * 
	 * @param alias queremos a chave publica deste alias
	 * @return a chave public do alias
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static PublicKey getPublicKey (String alias, String keystoreName) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

		// abre stream para a keystore
		FileInputStream kfile = new FileInputStream(keystoreName);  //keystore
		KeyStore kstore = KeyStore.getInstance("JCEKS");

		// entrar na loja com a password correta
		kstore.load(kfile, "123456".toCharArray());
		Certificate cert = kstore.getCertificate(alias);

		PublicKey pk = cert.getPublicKey();
		kfile.close();
		
		return pk;

	}

	/**
	 * 
	 * @param alias da qual queremos a chave privada
	 * @return a chave privada do alias
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static PrivateKey privateKey (String alias, String keystoreName) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

		// abre stream para a keystore
		FileInputStream kfile = new FileInputStream(keystoreName);  //keystore
		KeyStore kstore = KeyStore.getInstance("JCEKS");

		// entrar na loja com a password correta
		kstore.load(kfile, "123456".toCharArray());

		
		PrivateKey pk = (PrivateKey) kstore.getKey(alias, "123456".toCharArray());
		
		kfile.close();

		return pk;

	}


	/**
	 * 
	 * @param chaveACifrar a chave a cifrar
	 * @param publicKey a chave publica usada para cifrar a chave 'chaveACifrar'
	 * @return 'chaveACifrar' cifrada com a 'getPublicKey'
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchProviderException
	 */
	public static byte[] cipherKey (SecretKey chaveACifrar, PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, NoSuchProviderException {

		// preparar o algoritmo de cifra
		Cipher c = Cipher.getInstance("RSA");
		c.init(Cipher.WRAP_MODE, publicKey);

		return c.wrap(chaveACifrar);
	}

	/**
	 * 
	 * @param chaveADecifrar a chave a decifrar
	 * @param privateKey a chave privada usada para decifrar 
	 * @return a chave secreta que estava 'wrapped
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchProviderException
	 */
	public static SecretKey decipherKey (byte[] chaveADecifrar, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, NoSuchProviderException {

		// preparar o algoritmo de cifra
		Cipher c = Cipher.getInstance("RSA");
		c.init(Cipher.UNWRAP_MODE, privateKey);

		return  (SecretKey) c.unwrap(chaveADecifrar, "AES",  Cipher.SECRET_KEY);

	}

	public static void signFile(File fileToSign) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, SignatureException, UnrecoverableKeyException, KeyStoreException, CertificateException {
		// create signature engine
		Signature s = Signature.getInstance("SHA1withRSA");
		s.initSign(privateKey("server", "myKeys.keystore"));
		
		//read data from the given file
		FileInputStream fis = new FileInputStream(fileToSign);
		BufferedInputStream bufin = new BufferedInputStream(fis);
		byte[] buffer = new byte[1024];
		int len;
		while ((len = bufin.read(buffer)) >= 0) {
		    s.update(buffer, 0, len);
		};
		bufin.close();
		
		byte[] realSig = s.sign();
		
		// writes the signature file
		FileOutputStream sigfos = new FileOutputStream(fileToSign + ".sig");
		sigfos.write(realSig);
		sigfos.close();
	}
	
	public boolean checkSignature(File f, File sig) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, ClassNotFoundException, InvalidKeyException, SignatureException, UnrecoverableKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
		// gets the data from the plaintext file
		FileInputStream sigfis = new FileInputStream(f);
		byte[] sigToVerify = new byte[sigfis.available()]; 
		sigfis.read(sigToVerify);
		sigfis.close();
		
		Signature s = Signature.getInstance("SHA1withRSA");
		PublicKey pk = getPublicKey("server", "myKeys.keystore");
		s.initVerify(pk);
		
		FileInputStream datafis = new FileInputStream(sig);
		BufferedInputStream bufin = new BufferedInputStream(datafis);

		byte[] buffer = new byte[1024];
		int len;
		while (bufin.available() != 0) {
		    len = bufin.read(buffer);
		    s.update(buffer, 0, len);
		}
		bufin.close();
		return s.verify(sigToVerify);
	}


}