package security.test;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import security.AES;

public class AESTest {

	private static String plainText;
	private static String encryptedText;
	private static KeyGenerator keyGenerator;
	private static SecretKey secretKey;

	@BeforeClass
	public static void create() {
		// Initialize variables.
		plainText = "afseafsafseafs";		
		try {
			keyGenerator = KeyGenerator.getInstance("AES/ECB/PKCS5Padding".split("/")[0]);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		keyGenerator.init(128);
		secretKey = keyGenerator.generateKey();
		encryptedText = AES.encrypt(plainText, secretKey);
	}

	@Test
	public void generateKeyTest() {
		// Test if generateKey does not throw a NoSuchAlgorithmException.
		Assert.assertNotNull(AES.generateKey());
	}
	
	@Test
	public void encryptTest() {
		// Test if encrypt does not throw a exception.
		Assert.assertNotNull(AES.encrypt(plainText, secretKey));
	}
	
	@Test
	public void decryptTest() {
		// Test if decrypt does not throw a exception.
		Assert.assertNotNull(AES.decrypt(encryptedText, secretKey));
	}
	
	@Test
	public void keyToStringTest() {
		// Test if keyToString returns something.
		Assert.assertNotNull(AES.keyToString(secretKey));
	}
	
	@Test
	public void stringToKeyTest() {
		// Test if keyToString returns something.
		Assert.assertNotNull(AES.stringToKey(plainText));
	}
}