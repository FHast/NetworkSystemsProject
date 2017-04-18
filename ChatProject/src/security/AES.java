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

	public static String encrypt(String plainText, SecretKey secretKey)  {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes("UTF-8")));
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static String decrypt(String encryptedText, SecretKey secretKey) {
			try {
				cipher.init(Cipher.DECRYPT_MODE, secretKey);
				return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)), "UTF-8");
			} catch (InvalidKeyException e) {
				e.printStackTrace();
				return null;
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
				return null;
			} catch (BadPaddingException e) {
				e.printStackTrace();
				return null;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			
		
	}

}
