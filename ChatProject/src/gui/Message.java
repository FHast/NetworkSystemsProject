package gui;

import java.time.LocalTime;

public class Message {

	public static final int TYPE_TEXT = 0;
	public static final int TYPE_IMAGE = 1;
	public static final int TYPE_FILE = 2;
	public static final int TYPE_AUDIO = 3;

	private boolean isSentBySelf;
	private String text;
	private LocalTime timestamp;
	private int type;

	public Message(boolean sentBySelf, String text, LocalTime sendTime) {
		this(sentBySelf, TYPE_TEXT, text, sendTime);
	}

	public Message(boolean sentBySelf, int type, String path, LocalTime sendTime) {
		this.isSentBySelf = sentBySelf;
		this.text = path;
		this.type = type;
		this.timestamp = sendTime;
	}

	public boolean isSentBySelf() {
		return isSentBySelf;
	}

	public String getText() {
		return text;
	}

	public int getType() {
		return type;
	}
	
	public LocalTime getTime() {
		return timestamp;
	}

	public String toString() {
		int minute = timestamp.getMinute();
		String minutes = "";

		if (minute < 10) {
			minutes = "0" + minute;
		} else {
			minutes = "" + minute;
		}

		int hour = timestamp.getHour();
		String hours= "";

		if (hour < 10) {
			hours = "0" + hour;
		} else {
			hours = "" + hour;
		}

		return hours + ":" + minutes + " | " + text;
	}

}
