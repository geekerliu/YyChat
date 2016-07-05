package com.dasudian.chat;


public class DsdMessage {

	// 消息类型
	public static final int MESSAGE_TYPE_SEND_TXT = 0;
	public static final int MESSAGE_TYPE_RECV_TXT = 1;
	public static final int MESSAGE_TYPE_RECV_IMAGE = 2;
	public static final int MESSAGE_TYPE_SEND_IMAGE = 3;
	public static final int MESSAGE_TYPE_COUNT = 4;

	private int type;
	private String content;
	private String imagePath;

	private DsdMessage(int type, String content, String imagePath) {
		this.type = type;
		this.content = content;
		this.imagePath = imagePath;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[type:" + type + "content:" + content + "]");
		return sb.toString();
	}
	
	/**
	 * 获取txt类型的消息对象
	 * @param type MESSAGE_TYPE_SEND_TXT或则MESSAGE_TYPE_RECV_TXT 
	 * @param content 消息内容
	 * @return
	 */
	public static DsdMessage createTxtMessage(int type, String content) {
		return new DsdMessage(type, content, null);
	}
	
	public static DsdMessage createImageMessage(int type, String imagePath) {
		return new DsdMessage(type, null, imagePath);
	}
}
