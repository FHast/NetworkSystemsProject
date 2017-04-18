package security;

import javax.crypto.SecretKey;

public class SecurityTest {

	public static void main(String[] args) {
		
		String plainText = "sackseuchesackseuchesackseuchesackseuchesackseuche";
		System.out.println("Plain text: " + plainText);
		
		// RSA Test
		if (!RSA.isKeyPairGenerated()) {
			RSA.generateKeyPair();
		}
		
		String encrypted = RSA.encrypt(plainText, RSA.getPublicKey());
		String decrypted = RSA.decrypt(encrypted, RSA.getPrivateKey());
		
		System.out.println("RSA Test");
		System.out.println("Encrypted : " + encrypted);
		System.out.println("Decrypted : " + decrypted);
		System.out.println("Encrypted length: " + encrypted.length());
		System.out.println("Decrypted length: " + decrypted.length());
		
		// AES Test
		SecretKey key = AES.generateKey();
		String encrypted2 = AES.encrypt(plainText, key);
		String decrypted2 = AES.decrypt(encrypted2, key);
		
		System.out.println("AES Test");
		System.out.println("Encrypted : " + encrypted2);
		System.out.println("Decrypted : " + decrypted2);
	}

}
