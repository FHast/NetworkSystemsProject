package applicationLayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;

public class ImageService {
	public static String fileToString(String url) {
		try {
			File file = new File(url);
			String data = FileUtils.readFileToString(file, "Base64");
			return data;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public static void stringToFile(String data, String filetype) {
		try {
			String s = "" + new Random().nextInt();
			File file = new File(s + "." + filetype);
			FileUtils.writeStringToFile(file, data, "Base64");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String s = fileToString("chaticon.png");
		System.out.println(s);
		stringToFile(s, "txt");
	}
}
