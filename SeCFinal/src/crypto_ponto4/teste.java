package crypto_ponto4;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class teste {

	public static void main(String[] args) throws IOException, UnrecoverableKeyException, InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, SignatureException {

		File f = new File("database\\max\\cao\\comments.txt");
		SecretKey sk = Crypto.getInstance().getSecretKey();

		//Crypto.decipherFile(f, sk);
		Crypto.cipherFile(f, sk);
	}

}
