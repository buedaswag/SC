package cryptoUtils;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.spec.PBEKeySpec;

/**
 * A utility class to hash passwords and check passwords vs hashed values. It uses a combination of hashing and unique
 * salt. The algorithm used is PBKDF2WithHmacSHA1 which, although not the best for hashing password (vs. bcrypt) is
 * still considered robust and <a href="https://security.stackexchange.com/a/6415/12614"> recommended by NIST </a>.
 * The hashed value has 128 bits.
 */
public class PasswordUtils {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final int ITERATIONS = 10000;
	private static final int KEY_LENGTH = 128;



	/**
	 * Returns a random salt to be used to hash a password.
	 *
	 * @return a 16 bytes random salt
	 */
	public static byte[] getNextSalt() {
		byte[] salt = new byte[16];
		RANDOM.nextBytes(salt);
		return salt;
	}

	/**
	 * Returns a salted and hashed password using the provided hash.
	 * Note - side effect: the password is destroyed (the char[] is filled with zeros)
	 *
	 * @param password the password to be hashed
	 * @param salt     a 16 bytes salt, ideally obtained with the getNextSalt method
	 *
	 * @return the hashed password with a pinch of salt
	 */
	public static byte[] hash(String password, byte[] salt) {

		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

		Arrays.fill(password.toCharArray(), Character.MIN_VALUE);
		try {		
			MessageDigest md = MessageDigest.getInstance("SHA-256");
	        md.update(password.getBytes());
			return md.digest();
		}
		catch (NoSuchAlgorithmException e) {
			throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
		}
		finally {
			spec.clearPassword();
		}

	}
	
	/**
	 * Returns true if the given password and salt match the hashed value, false otherwise.
	 * Note - side effect: the password is destroyed (the char[] is filled with zeros)
	 *
	 * @param password     the password to check
	 * @param salt         the salt used to hash the password
	 * @param expectedHash the expected hashed value of the password
	 *
	 * @return true if the given password and salt match the hashed value, false otherwise
	 * @throws NoSuchAlgorithmException 
	 */
	public static boolean isExpectedPassword(String password, String salt, String expectedHash) throws NoSuchAlgorithmException {
		// parses the arguments into the proper datatypes for comparison
		int saltInt = Integer.parseInt(salt);
		byte[] saltBytes = BigInteger.valueOf(saltInt).toByteArray();
		// generates an hashcode based on the user's input
		String passHash = Base64.getEncoder().encodeToString(hash(password, saltBytes));
		
		// compares the expected and generated hashcodes
		if(passHash.length() != expectedHash.length())
			return false;
		for(int i = 0; i < passHash.length(); i++) {
			if(passHash.charAt(i) != expectedHash.charAt(i))
				return false;
		}
		return true;
	}

}