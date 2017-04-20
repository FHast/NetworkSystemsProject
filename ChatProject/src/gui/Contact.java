package gui;

import java.time.LocalTime;
import java.util.ArrayList;

/**
 * This class represents a contact in the contact list of the main GUI frame.
 */
public class Contact {

	private String name;
	private int device;
	private ArrayList<Message> messages;
	private boolean unreadMessages = false;

	/**
	 * Creates a contact.
	 * @param name The name that is shown inside the contact list
	 * @param device The last digit of the IP
	 * @param messages The message list
	 */
	public Contact(String name, int device, ArrayList<Message> messages) {
		this.name = name;
		this.device = device;
		this.messages = messages;
	}

	/**
	 * The device is the last digit of the IP.
	 * @return device
	 */
	public int getDevice() {
		return device;
	}

	/**
	 * Creates a textual representation of the Contact (shows device + name).
	 */
	@Override
	public String toString() {
		return "[" + device + "] " + name;
	}

	/**
	 * The message list.
	 * @return messages
	 */
	public ArrayList<Message> getMessages() {
		return messages;
	}

	/**
	 * Converts the internal ArrayList<Message> to Message[].
	 * @return The Message array
	 */
	public Message[] getMessagesRaw() {
		return messages.toArray(new Message[0]);
	}

	/**
	 * Returns the contact's name.
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the contact's name
	 * @param n The new name
	 */
	public void setName(String n) {
		name = n;
	}

	/**
	 * Sets whether the contact has unread messages.
	 * @param b true = there are unread messages, false = no unread messages
	 */
	public void setUnreadMessages(boolean b) {
		unreadMessages = b;
	}
	
	/**
	 * Returns true if there unread messages, false if otherwise.
	 * @return
	 */
	public boolean hasUnreadMessages() {
		return unreadMessages;
	}
	
	/**
	 * Adds a message to the message list.
	 * @param isSentBySelf If the message is sent the contact
	 * @param text The message text
	 * @param type The type of message (defined in Message)
	 * @param sendTime Time of sending
	 */
	public void addMessage(boolean isSentBySelf, String text, int type, LocalTime sendTime) {
		unreadMessages = true;
		
		Message m = null;
		switch (type) {
		case Message.TYPE_TEXT:
			m = new Message(isSentBySelf, text, sendTime);
			break;
		case Message.TYPE_IMAGE:
			// first message is used for description
			addMessage(isSentBySelf, text, Message.TYPE_TEXT, sendTime);
			m = new Message(isSentBySelf, Message.TYPE_IMAGE, text, sendTime);
			break;
		case Message.TYPE_FILE:
			m = new Message(isSentBySelf, Message.TYPE_FILE, text, sendTime);
			break;
		}

		int index = 0;
		while (index < messages.size() && messages.get(index).getTime().isBefore(sendTime)) {
			index++;
		}
		if (index == messages.size()) {
			messages.add(m);
		} else {
			messages.add(index, m);
		}
	}
}
