package com.example.demo.model;

import java.util.List;

public class HistorySnapshot {
	private String type;
	private List<ChatMessage> messages;

	public HistorySnapshot() {
	}

	public HistorySnapshot(String type, List<ChatMessage> messages) {
		this.type = type;
		this.messages = messages;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<ChatMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<ChatMessage> messages) {
		this.messages = messages;
	}
}

