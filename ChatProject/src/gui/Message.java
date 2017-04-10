package gui;

public class Message {
	
	private boolean isSentBySelf;
	private String text;
	//private long timestamp;
	
	public Message(boolean sentBySelf, String text/*, long timestamp*/) {
		this.isSentBySelf = sentBySelf;
		this.text = text;
		//this.timestamp = timestamp;
	}
	
	public boolean isSentBySelf() {
		return isSentBySelf;
	}
	
	public String getText() {
		return text;
	}
	
	
}
