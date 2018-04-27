package crypto_ponto4;
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

public class teste {

	public static void main(String[] args) throws IOException, UnrecoverableKeyException, InvalidKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchProviderException, BadPaddingException, SignatureException {
		
		String data = "This have I thought good to deliver thee, .......";
		FileOutputStream fos = new FileOutputStream("servidor_teste\\test.txt");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		PrivateKey pk = Crypto.getInstance().privateKey("server", "myKeys.keystore");
		Signature s = Signature.getInstance("MD5withRSA");
		s.initSign(pk);
		byte buf[] = data.getBytes( );
		s.update(buf);
		oos.writeObject(data);
		oos.writeObject(s.sign( ));
		fos.close();
	

	}

}
