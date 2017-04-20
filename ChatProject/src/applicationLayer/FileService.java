package applicationLayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

import org.apache.commons.io.FileUtils;

/**
 * Abstracts the file operations to a minimum level and provides basic functionalities.
 */
public class FileService {
	// The directory where the files should be stored
	private static final String root = "received/";

	/**
	 * Converts file contents to a base64 encoded String.
	 * @param url The path to file to be read from
	 * @return
	 */
	public static String fileToString(String url) {
		try {
			// create file object
			File file = new File(url);
			// read file
			byte[] bytes = FileUtils.readFileToByteArray(file);
			// encode content
			byte[] encoded = Base64.getEncoder().encode(bytes);
			return new String(encoded);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Stores the base64 encoded file content to a file inside the received folder. The filename is
	 * generated using the current time.
	 * @param data The file content as a base64 encoded string
	 * @param filetype The file extension
	 * @return The absolute path to the created file
	 * @throws FileNotFoundException
	 */
	public static String stringToFile(String data, String filetype) throws FileNotFoundException {
		try {
			// construct file name
			String s = LocalDateTime.now().toString().split("[.]")[0];
			// create file object
			File file = new File(root + s + "." + filetype);
			// decode data and write to file
			byte[] encoded = data.getBytes();
			byte[] decoded = Base64.getDecoder().decode(encoded);
			FileUtils.writeByteArrayToFile(file, decoded);
			// return the absolute path
			return file.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new FileNotFoundException();
	}

	/**
	 * Returns the file extension of the given absolute path.
	 * @param url Absolute path to the file
	 * @return
	 */
	public static String getAppendix(String url) {
		String[] split = url.split("[.]");
		return split[split.length - 1];
	}

	/**
	 * Returns the file name.
	 * @param url The relative path to the file
	 * @return
	 */
	public static String getName(String url) {
		return url.replaceAll("[.]" + getAppendix(url), "");
	}

	/**
	 * Converts the content of the given file to a base64 encoded String.
	 * @param name Name of the file
	 * @param appendix File extension
	 * @return
	 */
	public static String fileToString(String name, String appendix) {
		return fileToString(name + "." + appendix);
	}
}
