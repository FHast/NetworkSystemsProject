package gui.test;

import java.time.LocalTime;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import gui.Message;

public class MessageTest {

	private static boolean sentBySelf;
	private static String text;
	private static int type;
	private static LocalTime sendTime;
	private static String timeSimplified;
	private static Message myMessage;

	@BeforeClass
	public static void create() {
		// Initialize test object and variables.
		sentBySelf = true;
		text = "hello";
		type = Message.TYPE_TEXT;
		sendTime = LocalTime.MIDNIGHT;
		timeSimplified = "00:00";
		myMessage = new Message(sentBySelf, type, text, sendTime);
	}

	@Test
	public void isSentBySelfTest() {
		// Test if isSentBySelf returns sentBySelf.
		Assert.assertEquals(sentBySelf, myMessage.isSentBySelf());
	}
	
	@Test
	public void getTextTest() {
		// Test if getText returns text.
		Assert.assertEquals(text, myMessage.getText());
	}
	
	@Test
	public void getTypeTest() {
		// Test if getType returns type.
		Assert.assertEquals(type, myMessage.getType());
	}
	
	@Test
	public void getTimeSimplifiedTest() {
		// Test if getTimeSimplified returns timeSimplified.
		Assert.assertEquals(timeSimplified, myMessage.getTimeSimplified());
	}
	
	@Test
	public void toStringTest() {
		// Test if toString returns the correct string.
		Assert.assertEquals(timeSimplified + " | " + text, myMessage.toString());
	}
}