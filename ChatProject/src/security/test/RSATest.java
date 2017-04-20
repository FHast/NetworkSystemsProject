package security.test;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import security.RSA;

public class RSATest {

	private static String text;
	private static String textPublic;
	private static PublicKey publicKey;
	private static PrivateKey privateKey;

	@BeforeClass
	public static void create() {
		// Initialize variables.
		text = "afseafsafseafs";
		textPublic = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuaF8foyn3pklabgkryDG6Ht25Npv7UN/UFxNSpwlW2U4QWJdzOoc9HqBbzAnlBf21JzF5ap5zowvtK0fduIhMGdB5zQqJfD/kZLbWNbwZGRrEY1oT0MLTHLpW5M643BvJXGRF8FGkMYa4yOJ5GRK4/S+mlIvEx+I8tZ8xwKtoJUpSUM96xAo3OZrBspH7BoZyjte/PdHD7Aapaa6ctvLvqJcgyPx4rgA9TVrHTYKLWv2g6zXceeJB6LRocIvvZzWMgLNiQU5TMFzclCbLeD2KBFgDuCZGaFb6YEBLA13p8J1TvViLrJLXjiS+mpJofW8P/3t21CF6+uvs359ygTD9wIDAQAB";
		new File("private_key").delete();
		new File("public_key").delete();
		RSA.generateKeyPair();
		publicKey = RSA.getPublicKey();
		privateKey = RSA.getPrivateKey();
	}

	@Test
	public void isKeyPairGeneratedTest() {
		// Test if isKeyPairGenerated returns the correct boolean.
		
		// Delete existing pairs.
		new File("private_key").delete();
		new File("public_key").delete();
		
		Assert.assertFalse(RSA.isKeyPairGenerated());
		
		
		// Create files to store public and private key.
		File privateKeyFile = new File("private_key");
		File publicKeyFile = new File("public_key");
		if (privateKeyFile.getParentFile() != null) {
			privateKeyFile.getParentFile().mkdirs();
		}
		try {
			privateKeyFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (publicKeyFile.getParentFile() != null) {
			publicKeyFile.getParentFile().mkdirs();
		}
		try {
			publicKeyFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Assert.assertTrue(RSA.isKeyPairGenerated());
	}
	
	@Test
	public void encryptPublicTest() {
		// Test if encrypt does not throw an Exception, using the public key.
		Assert.assertNotNull(RSA.encrypt(text, publicKey));
	}

	@Test
	public void encryptPrivateTest() {
		// Test if encrypt does not throw an Exception, using the private key.
		Assert.assertNotNull(RSA.encrypt(text, privateKey));
	}
	
	@Test
	public void decryptPublicTest() {
		// Test if decrypt does not throw an Exception, using the public key.
		Assert.assertNotNull(RSA.decrypt(RSA.encrypt(text, privateKey), publicKey));
	}
	
	@Test
	public void decryptPrivateTest() {
		// Test if decrypt does not throw an Exception, using the private key.
		Assert.assertNotNull(RSA.decrypt(RSA.encrypt(text, publicKey), privateKey));
	}
	
	@Test
	public void getPublicKeyTest() {
		// Test if getPublicKey does not throw an Exception.
		Assert.assertNotNull(RSA.getPublicKey());
	}
	
	@Test
	public void getPrivateKeyTest() {
		// Test if getPrivateKey does not throw an Exception.
		Assert.assertNotNull(RSA.getPrivateKey());
	}
	
	@Test
	public void publicKeyToStringTest() {
		// Test if publicKeyToString does not throw an Exception.
		Assert.assertNotNull(RSA.publicKeyToString(publicKey));
	}
	
	@Test
	public void stringToPublicKeyTest() {
		// Test if stringToPublicKey does not throw an Exception.
		Assert.assertNotNull(RSA.stringToPublicKey(textPublic));
	}
	
	@AfterClass
	public static void cleanUp() {
		// Delete keys.
		new File("private_key").delete();
		new File("public_key").delete();
	}
}