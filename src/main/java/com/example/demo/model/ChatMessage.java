package com.example.demo.model;

public class ChatMessage {

	public enum MessageType {
		JOIN,
		CHAT,
		LEAVE
	}

	private MessageType type;
	private String sender;
	private String content;
	private long timestamp;
	private String clientId;

	public ChatMessage() {
	}

	public ChatMessage(MessageType type, String sender, String content, long timestamp) {
		this.type = type;
		this.sender = sender;
		this.content = content;
		this.timestamp = timestamp;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}

