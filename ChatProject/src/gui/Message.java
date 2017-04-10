package gui;

public class Message {
	
	private boolean isSentBySelf;
	private String text;
	
	public Message(boolean sentBySelf, String text) {
		this.isSentBySelf = sentBySelf;
		this.text = text;
	}
	
	public boolean isSentBySelf() {
		return isSentBySelf;
	}
	
	public String getText() {
		return text;
	}
}
