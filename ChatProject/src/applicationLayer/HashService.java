package applicationLayer;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Provides functionality for hashing and salt-hashing. Also compares given
 * hashes and cleartext. The used hash: sha256 Stored in a file using salts with
 * a length of 20.
 * 
 * @author gereon
 *
 */
public class HashService {
	public static final int SALTLENGTH = 20;

	/**
	 * Hash the given password, including random salting.
	 * 
	 * @param password
	 *            the password to be hashed
	 * @return hashedPassword:salt
	 */
	public static String hash(String password) {
		return hash(password, salt());
	}

	/**
	 * Creates a random lower and uppsercase letter string with the saltlength
	 * specified.
	 * 
	 * @return a random String consisting of lower and uppercase letters
	 */
	private static String salt() {
		String salt = "";
		for (int i = 0; i < SALTLENGTH; i++) {
			int number = (int) (Math.random() * 52);
			if (number < 26) {
				// Uppercase
				salt = salt + (char) (number + 65);
			} else {
				// lowercase
				salt = salt + (char) (number + 71);
			}
		}
		// System.out.println("random salt: " + salt);
		return salt;
	}

	/**
	 * Hashes the given password with the given salt.
	 * 
	 * @param password
	 *            given cleartext password
	 * @param salt
	 *            given salt
	 * @return hashedPassword:salt
	 */
	public static String hash(String password, String salt) {
		return DigestUtils.sha256Hex(password + salt) + ":" + salt;
	}

	public static String simpleHash(String password) {
		return DigestUtils.sha256Hex(password);
	}
}
