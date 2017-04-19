package applicationLayer.test;

import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import applicationLayer.FileService;

public class FileServiceTest {

	private static String data;
	private static String name;
	private static String appendix;

	@BeforeClass
	public static void create() {
		// Initialize variables.
		data = "junit-4.12.jar";
		name = "junit-4.12";
		appendix = "jar";
	}

	@Test
	public void fileToStringTest() {
		// Test if fileToString returns something.
		Assert.assertNotNull(FileService.fileToString(data));
		Assert.assertNotNull(FileService.fileToString(name, appendix));
	}
	
	@Test
	public void stringToFileTest() throws FileNotFoundException {
		// Test if stringToFile creates file.
		Assert.assertTrue(FileService.stringToFile(FileService.fileToString(data), FileService.getAppendix(data)).endsWith(appendix));
	}

	@Test
	public void getAppendixTest() {
		// Test if getAppendix returns the correct appendix.
		Assert.assertEquals(appendix, FileService.getAppendix(data));
	}
	
	@Test
	public void getNameTest() {
		// Test if getName returns the correct name.
		Assert.assertEquals(name, FileService.getName(data));
	}
}