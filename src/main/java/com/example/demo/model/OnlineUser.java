package com.example.demo.model;

public class OnlineUser {
	private String name;
	private long connectedAt;

	public OnlineUser() {
	}

	public OnlineUser(String name, long connectedAt) {
		this.name = name;
		this.connectedAt = connectedAt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getConnectedAt() {
		return connectedAt;
	}

	public void setConnectedAt(long connectedAt) {
		this.connectedAt = connectedAt;
	}
}

