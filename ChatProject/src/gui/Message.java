package gui;

import java.time.LocalTime;

/**
 * Model for a message. Used by the message list in the GUI.
 */
public class Message {

	// types of messages
	public static final int TYPE_TEXT = 0;
	public static final int TYPE_IMAGE = 1;
	public static final int TYPE_FILE = 2;

	private boolean isSentBySelf;
	private String text;
	private LocalTime timestamp;
	private int type;

	/**
	 * Creates a new text message. To create a message of another type, use the other constructor.
	 * @param sentBySelf Whether the message is sent by the user
	 * @param text message text
	 * @param sendTime Time of sending
	 */
	public Message(boolean sentBySelf, String text, LocalTime sendTime) {
		this(sentBySelf, TYPE_TEXT, text, sendTime);
	}

	/**
	 * Creates a new message of the given type.
	 * @param sentBySelf Whether the message is sent by the user
	 * @param type Type of message (see Message.TYPE_*)
	 * @param path Message text if type == TYPE_TEXT, otherwise absolute path to the file
	 * @param sendTime Time of sending
	 */
	public Message(boolean sentBySelf, int type, String path, LocalTime sendTime) {
		this.isSentBySelf = sentBySelf;
		this.text = path;
		this.type = type;
		this.timestamp = sendTime;
	}

	/**
	 * Whether the message is sent by user
	 * @return
	 */
	public boolean isSentBySelf() {
		return isSentBySelf;
	}

	/**
	 * Returns the text of the message if the message is of type text; otherwise the absolute path to the file.
	 * @return
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns the type of the message (see the constants Message.TYPE_*)
	 * @return
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Return the time of sending
	 * @return
	 */
	public LocalTime getTime() {
		return timestamp;
	}
	
	/**
	 * Creates a textual representation of the sending time.
	 * @return
	 */
	public String getTimeSimplified() {
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

		return hours + ":" + minutes;
	}

	/**
	 * Creates a textual represantation of the message (includes time + text/path)
	 */
	public String toString() {
		return getTimeSimplified() + " | " + text;
	}

}
