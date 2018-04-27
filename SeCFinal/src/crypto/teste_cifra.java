package crypto_ponto4;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class teste_cifra {

	public static void main(String[] args) throws UnrecoverableKeyException, InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, IOException {
		
		File f = new File("servidor_teste\\lindo.txt");
		
		SecretKey sk = Crypto.getInstance().getSecretKey();
		
		//Crypto.getInstance().cipherFile(f, sk);
		
		//Crypto.getInstance().decipherFile(f, sk);

	}

}
