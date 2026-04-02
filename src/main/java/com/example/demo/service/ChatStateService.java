package com.example.demo.service;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.OnlineUser;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ChatStateService {
	private static final int HISTORY_LIMIT = 50;

	public record UserSession(String sessionId, String name, long connectedAt, String clientId) {
	}

	private final Map<String, UserSession> sessionsById = new ConcurrentHashMap<>();
	private final Deque<ChatMessage> history = new ArrayDeque<>(HISTORY_LIMIT);
	private final Object historyLock = new Object();

	public void upsertSession(String sessionId, String name, long connectedAt, String clientId) {
		if (sessionId == null) return;
		sessionsById.put(sessionId, new UserSession(sessionId, name, connectedAt, clientId));
	}

	public UserSession removeSession(String sessionId) {
		if (sessionId == null) return null;
		return sessionsById.remove(sessionId);
	}

	public int onlineCount() {
		return sessionsById.size();
	}

	public List<OnlineUser> onlineUsers() {
		List<UserSession> sessions = new ArrayList<>(sessionsById.values());
		sessions.sort(Comparator.comparingLong(UserSession::connectedAt));
		List<OnlineUser> users = new ArrayList<>(sessions.size());
		for (UserSession s : sessions) {
			users.add(new OnlineUser(s.name(), s.connectedAt()));
		}
		return users;
	}

	public void addToHistory(ChatMessage msg) {
		if (msg == null) return;
		synchronized (historyLock) {
			if (history.size() >= HISTORY_LIMIT) {
				history.removeFirst();
			}
			history.addLast(msg);
		}
	}

	public List<ChatMessage> historySnapshot() {
		synchronized (historyLock) {
			return new ArrayList<>(history);
		}
	}
}

