package networkLayer.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import networkLayer.HashService;

public class HashServiceTest {

	private static String password;
	private static String salt;

	@BeforeClass
	public static void create() {
		// Initialize variables.
		password = "password123";
		salt = "salt";
	}

	@Test
	public void hashTest() {
		// Test if hash returns something.
		Assert.assertNotNull(HashService.hash(password, salt));
	}

	@Test
	public void simpleHashTest() {
		// Test if simpleHash returns something.
		Assert.assertNotNull(HashService.simpleHash(password));
	}
}