package gui.test;

import java.time.LocalTime;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import gui.Contact;
import gui.Message;

public class ContactTest {

	private static String name;
	private static String nameChanged;
	private static int device;
	private static ArrayList<Message> messages;
	private static Contact myContact;

	@BeforeClass
	public static void create() {
		// Initialize test object and variables.
		name = "Fritz";
		nameChanged = "Hast";
		device = 1;
		messages = new ArrayList<>();
		messages.add(new Message(true, "hello", LocalTime.MIDNIGHT));
		messages.add(new Message(false, "bye", LocalTime.NOON));
		myContact = new Contact(name, device, messages);
	}

	@Test
	public void getDeviceTest() {
		// Test if getDevice returns device.
		Assert.assertEquals(device, myContact.getDevice());
	}

	@Test
	public void getMessagesTest() {
		// Test if getMessages returns messages.
		Assert.assertEquals(messages, myContact.getMessages());
	}
	
	@Test
	public void getMessagesRawTest() {
		// Test if getMessagesRaw returns something.
		Assert.assertNotNull(myContact.getMessagesRaw());
	}
	
	@Test
	public void nameTest() {
		// Test if getName returns the right name and if setName changes the name.
		Assert.assertEquals(name, myContact.getName());
		myContact.setName(nameChanged);
		Assert.assertEquals(nameChanged, myContact.getName());
	}
	
	@Test
	public void unreadMessagesTest() {
		// Test if unread messages can be set and checked.
		myContact.setUnreadMessages(true);
		Assert.assertTrue(myContact.hasUnreadMessages());
		myContact.setUnreadMessages(false);
		Assert.assertFalse(myContact.hasUnreadMessages());
	}
	
	@Test
	public void addMessageTest() {
		// Test if addMessage adds Messages.
		Assert.assertEquals(2 ,myContact.getMessages().size());
		myContact.addMessage(true, name, Message.TYPE_TEXT, LocalTime.NOON);
		Assert.assertEquals(3 ,myContact.getMessages().size());
	}
}