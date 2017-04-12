package gui;

import java.util.ArrayList;

public class Contact {

	private String name;
	private int device;
	private ArrayList<Message> messages;

	public Contact(String name, int device, ArrayList<Message> messages) {
		this.name = name;
		this.device = device;
		this.messages = messages;
	}

	public int getDevice() {
		return device;
	}

	@Override
	public String toString() {
		return name;
	}

	public ArrayList<Message> getMessages() {
		return messages;
	}

	public Message[] getMessagesRaw() {
		return messages.toArray(new Message[0]);
	}

	public String getName() {
		return name;
	}

	public void addMessage(boolean isSentBySelf, String text, int type) {
		switch (type) {
		case Message.TYPE_TEXT:
			messages.add(new Message(isSentBySelf, text));
			break;
		case Message.TYPE_IMAGE:
			messages.add(new Message(isSentBySelf, Message.TYPE_TEXT, "Image: " + text));
			messages.add(new Message(isSentBySelf, Message.TYPE_IMAGE, text));
			break;
		case Message.TYPE_FILE:
			messages.add(new Message(isSentBySelf, Message.TYPE_FILE, text));
			break;
		}

	}
}
