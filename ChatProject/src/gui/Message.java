package gui;

import java.time.LocalTime;

public class Message {

	private boolean isSentBySelf;
	private String text;
	private LocalTime timestamp;

	public Message(boolean sentBySelf, String text/* , long timestamp */) {
		this.isSentBySelf = sentBySelf;
		this.text = text;
		timestamp = LocalTime.now();
	}

	public boolean isSentBySelf() {
		return isSentBySelf;
	}

	public String getText() {
		return text;
	}

	public String toString() {
		if (!isSentBySelf) {
			return "[" + timestamp + "] " + text;
		} else {
			return text + " [" + timestamp + "]";
		}
	}

}
