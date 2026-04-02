package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.ChatMessage.MessageType;
import com.example.demo.model.UsersUpdate;
import com.example.demo.service.ChatStateService;
import com.example.demo.service.ChatStateService.UserSession;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketPresenceListener {

	private static final String TOPIC_PUBLIC = "/topic/public";
	private static final String TOPIC_USERS = "/topic/users";

	private final ChatStateService chatState;
	private final SimpMessagingTemplate messagingTemplate;

	public WebSocketPresenceListener(ChatStateService chatState, SimpMessagingTemplate messagingTemplate) {
		this.chatState = chatState;
		this.messagingTemplate = messagingTemplate;
	}

	@EventListener
	public void onDisconnect(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
		UserSession removed = chatState.removeSession(sessionId);
		if (removed == null) {
			return;
		}

		ChatMessage out = new ChatMessage();
		out.setType(MessageType.LEAVE);
		out.setSender(removed.name());
		out.setContent(removed.name() + " покинул(а) чат");
		out.setTimestamp(System.currentTimeMillis());
		messagingTemplate.convertAndSend(TOPIC_PUBLIC, out);

		UsersUpdate update = new UsersUpdate(chatState.onlineCount(), chatState.onlineUsers());
		messagingTemplate.convertAndSend(TOPIC_USERS, update);
	}
}

