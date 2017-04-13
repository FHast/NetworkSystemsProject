package applicationLayer;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

import org.apache.commons.io.FileUtils;

public class FileService {
	private static final String root = "received/";

	public static String fileToString(String url) {
		try {
			File file = new File(url);
			byte[] bytes = FileUtils.readFileToByteArray(file);
			byte[] encoded = Base64.getEncoder().encode(bytes);
			return new String(encoded);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String stringToFile(String data, String filetype) throws FileNotFoundException {
		try {
			String s = LocalDateTime.now().toString().split("[.]")[0];
			File file = new File(root + s + "." + filetype);
			byte[] encoded = data.getBytes();
			byte[] decoded = Base64.getDecoder().decode(encoded);
			FileUtils.writeByteArrayToFile(file, decoded);
			return file.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new FileNotFoundException();
	}

	public static String getAppendix(String url) {
		String[] split = url.split("[.]");
		return split[split.length - 1];
	}

	public static String getName(String url) {
		return url.replaceAll("[.]" + getAppendix(url), "");
	}

	public static String fileToString(String name, String appendix) {
		return fileToString(name + "." + appendix);
	}

	public static void main(String[] args) {
		// Desktop.getDesktop().open(new File("received"));
		// System.out.println(fileToString("json-simple-1.1.1.jar"));
	}
}
