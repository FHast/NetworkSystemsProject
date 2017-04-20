package security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

/**
 * This class provides basic functionality for RSA encryption.
 */
public class RSA {

	public static final String ALGORITHM = "RSA/ECB/PKCS1Padding";
	public static final String PRIVATE_KEY_FILE = "private_key";
	public static final String PUBLIC_KEY_FILE = "public_key";
	public static final int KEY_SIZE_BITS = 2048;

	/**
	 * Generates a Key Pair and save it to files.
	 */
	public static void generateKeyPair() {
		try {
			if (!isKeyPairGenerated()) {
				final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM.split("/")[0]);
				keyGen.initialize(KEY_SIZE_BITS);
				final KeyPair key = keyGen.generateKeyPair();

				File privateKeyFile = new File(PRIVATE_KEY_FILE);
				File publicKeyFile = new File(PUBLIC_KEY_FILE);

				// Create files to store public and private key
				if (privateKeyFile.getParentFile() != null) {
					privateKeyFile.getParentFile().mkdirs();
				}
				privateKeyFile.createNewFile();

				if (publicKeyFile.getParentFile() != null) {
					publicKeyFile.getParentFile().mkdirs();
				}
				publicKeyFile.createNewFile();

				// Saving the Public key in a file
				ObjectOutputStream publicKeyOS = new ObjectOutputStream(new FileOutputStream(publicKeyFile));
				publicKeyOS.writeObject(key.getPublic());
				publicKeyOS.close();

				// Saving the Private key in a file
				ObjectOutputStream privateKeyOS = new ObjectOutputStream(new FileOutputStream(privateKeyFile));
				privateKeyOS.writeObject(key.getPrivate());
				privateKeyOS.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Checks whether a Key Pair is already generated.
	 * @return
	 */
	public static boolean isKeyPairGenerated() {
		return new File(PRIVATE_KEY_FILE).exists() && new File(PUBLIC_KEY_FILE).exists();
	}

	/**
	 * Encrypts the given plain text using the PublicKey.
	 * @param text The plain text
	 * @param key The PublicKey
	 * @return
	 */
	public static String encrypt(String text, PublicKey key) {
		byte[] cipherText = null;
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Base64.encodeBase64String(cipherText);
	}
	
	/**
	 * Encrypts the given plain text using the PrivateKey.
	 * @param text The plain text
	 * @param key The PrivateKey
	 * @return
	 */
	public static String encrypt(String text, PrivateKey key) {
		byte[] cipherText = null;
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Base64.encodeBase64String(cipherText);
	}

	/**
	 * Decrypts the cipher text using the PublicKey.
	 * @param text The cipher text
	 * @param key The public key
	 * @return
	 */
	public static String decrypt(String text, PublicKey key) {
		byte[] decrypted = null;
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
			decrypted = cipher.doFinal(Base64.decodeBase64(text));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new String(decrypted);
	}
	
	/**
	 * Decrypt the cipher text using the PrivateKey.
	 * @param text The cipher text
	 * @param key The private key
	 * @return
	 */
	public static String decrypt(String text, PrivateKey key) {
		byte[] decrypted = null;
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
			decrypted = cipher.doFinal(Base64.decodeBase64(text));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new String(decrypted);
	}

	/**
	 * Reads the PublicKey from the file and returns it.
	 * @return
	 */
	public static PublicKey getPublicKey() {
		PublicKey key = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE));
			key = (PublicKey) ois.readObject();
			ois.close();
		} catch (ClassNotFoundException | IOException e) {
		}
		return key;
	}

	/**
	 * Encodes the PublicKey to a base64 String representation.
	 * @param pk
	 * @return
	 */
	public static String publicKeyToString(PublicKey pk) {
		return Base64.encodeBase64String(pk.getEncoded());
	}

	/**
	 * Decodes a String to a PublicKey.
	 * @param s
	 * @return
	 */
	public static PublicKey stringToPublicKey(String s) {
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(s));
			return KeyFactory.getInstance("RSA").generatePublic(keySpec);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			return null;
		}
	}

	/**
	 * Reads the PrivateKey from a file and returns it.
	 * @return
	 */
	public static PrivateKey getPrivateKey() {
		PrivateKey key = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE));
			key = (PrivateKey) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
		}

		return key;
	}

}
