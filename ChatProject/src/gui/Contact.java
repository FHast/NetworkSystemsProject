package gui;

import java.net.InetAddress;
import java.util.ArrayList;

public class Contact {
	
	private String name;
	private InetAddress ip;
	private ArrayList<Message> messages;
	
	public Contact(String name, InetAddress ip, ArrayList<Message> messages) {
		this.name = name;
		this.ip = ip;
		this.messages = messages;
	}
	
	public InetAddress getIP() {
		return ip;
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
	
	public void addMessage(boolean isSentBySelf, String text) {
		messages.add(new Message(isSentBySelf,text));
	}
}
