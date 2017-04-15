package gui;

import java.time.LocalTime;
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
		return "[" + device + "] " + name;
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

	public void setName(String n) {
		name = n;
	}

	public void addMessage(boolean isSentBySelf, String text, int type, LocalTime sendTime) {
		Message m = null;
		switch (type) {
		case Message.TYPE_TEXT:
			m = new Message(isSentBySelf, text, sendTime);
			break;
		case Message.TYPE_IMAGE:
			m = new Message(isSentBySelf, Message.TYPE_TEXT, "Image: " + text, sendTime);
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
