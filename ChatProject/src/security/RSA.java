package security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class RSA {

	public static final String ALGORITHM = "RSA/ECB/PKCS1Padding";
	public static final String PRIVATE_KEY_FILE = "private_key";
	public static final String PUBLIC_KEY_FILE = "public_key";
	public static final int KEY_SIZE_BITS = 1024;

	public static void generateKeyPair() {
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static boolean isKeyPairGenerated() {
		return new File(PRIVATE_KEY_FILE).exists() && new File(PUBLIC_KEY_FILE).exists();
	}

	public static byte[] encrypt(String text, PublicKey key) {
		byte[] cipherText = null;
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cipherText;
	}

	public static String decrypt(byte[] text, PrivateKey key) {
		byte[] decrypted = null;
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
			decrypted = cipher.doFinal(text);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new String(decrypted);
	}

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
