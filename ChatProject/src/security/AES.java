package security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class provides basic functionalities for AES encryption.
 */
public class AES {

	// constants
	private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
	private static final int KEY_SIZE_BITS = 128;

	private static Cipher cipher;

	static {
		try {
			cipher = Cipher.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			// won't happen
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// won't happen
			e.printStackTrace();
		}
	}

	/**
	 * Generates a random SecretKey.
	 * @return The generated key
	 */
	public static SecretKey generateKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM.split("/")[0]);
			keyGenerator.init(KEY_SIZE_BITS);
			return keyGenerator.generateKey();
		} catch (NoSuchAlgorithmException e) {
			// won't happen anyway
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Encrypts the plain text with the given secret key.
	 * @param plainText The unencrypted plain text
	 * @param secretKey The secret key
	 * @return
	 */
	public static String encrypt(String plainText, SecretKey secretKey) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes("UTF-8")));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Decrypts the cipher text with the given secret key.
	 * @param encryptedText The encrypted cipher text
	 * @param secretKey The given secret key
	 * @return
	 */
	public static String decrypt(String encryptedText, SecretKey secretKey) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Encodes the SecretKey to a base64 String.
	 * @param sk The SecretKey
	 * @return
	 */
	public static String keyToString(SecretKey sk) {
		return Base64.getEncoder().encodeToString(sk.getEncoded());
	}

	/**
	 * Parse a String to create a SecretKey.
	 * @param s
	 * @return
	 */
	public static SecretKey stringToKey(String s) {
		byte[] de = Base64.getDecoder().decode(s);
		return new SecretKeySpec(de, 0, de.length, "AES");
	}
}
