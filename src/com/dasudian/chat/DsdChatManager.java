package com.dasudian.chat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DsdChatManager {

	private final static String TAG = "DsdChatManager";
	private static DsdChatManager dsdChatManager = null;
	// 保存所有用户的消息
	private Map<String, List<DsdMessage>> friendsMsgMap;
	
	private DsdChatManager() {
		friendsMsgMap = new HashMap<String, List<DsdMessage>>();
	}
	
	public List<DsdMessage> getMsgList(String name) {
		List<DsdMessage> msgList = friendsMsgMap.get(name);
		if (msgList == null) {
			msgList = new LinkedList<DsdMessage>();
			setMsgList(name, msgList);
		}
		return msgList;
	}
	
	public void setMsgList(String name, List<DsdMessage> msgList) {
		friendsMsgMap.put(name, msgList);
	}
	
	public static DsdChatManager getInstance() {
		if (dsdChatManager == null) {
			dsdChatManager = new DsdChatManager();
		}
		return dsdChatManager;
	}
}
