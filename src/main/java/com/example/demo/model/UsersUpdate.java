package com.example.demo.model;

import java.util.List;

public class UsersUpdate {
	private int count;
	private List<OnlineUser> users;

	public UsersUpdate() {
	}

	public UsersUpdate(int count, List<OnlineUser> users) {
		this.count = count;
		this.users = users;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<OnlineUser> getUsers() {
		return users;
	}

	public void setUsers(List<OnlineUser> users) {
		this.users = users;
	}
}

