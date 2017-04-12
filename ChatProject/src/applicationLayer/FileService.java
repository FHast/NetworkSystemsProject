package applicationLayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

import org.apache.commons.io.FileUtils;

public class FileService {
	public static String fileToString(String url) {
		try {
			File file = new File(url);
			byte[] bytes = FileUtils.readFileToByteArray(file);
			byte[] encoded = Base64.getEncoder().encode(bytes);
			return new String(encoded);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void stringToFile(String data, String filetype) {
		try {
			String s = "" + new Random().nextInt();
			File file = new File(s + "." + filetype);
			byte[] encoded = data.getBytes();
			byte[] decoded = Base64.getDecoder().decode(encoded);
			FileUtils.writeByteArrayToFile(file, decoded);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getAppendix(String url) {
		return url.split("[.]")[1];
	}

	public static String getName(String url) {
		return url.split("[.]")[0];
	}

	public static String fileToString(String name, String appendix) {
		return fileToString(name + "." + appendix);
	}
}
