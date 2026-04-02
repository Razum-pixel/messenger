package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.ChatMessage.MessageType;
import com.example.demo.model.HistorySnapshot;
import com.example.demo.model.UsersUpdate;
import com.example.demo.service.ChatStateService;
import java.security.Principal;
import java.util.UUID;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

	private static final String TOPIC_PUBLIC = "/topic/public";
	private static final String TOPIC_TYPING = "/topic/typing";
	private static final String TOPIC_USERS = "/topic/users";
	private static final String TOPIC_HISTORY_PREFIX = "/topic/history.";
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatStateService chatState;

	public ChatController(SimpMessagingTemplate messagingTemplate, ChatStateService chatState) {
		this.messagingTemplate = messagingTemplate;
		this.chatState = chatState;
	}

	@MessageMapping("/chat.join")
	public void join(@Payload ChatMessage message, SimpMessageHeaderAccessor headers, Principal principal) {
		String sender = normalizeSender(message != null ? message.getSender() : null, principal);
		headers.getSessionAttributes().put("sender", sender);
		String clientId = message != null ? message.getClientId() : null;
		if (clientId != null && !clientId.isBlank()) {
			headers.getSessionAttributes().put("clientId", clientId);
		}

		long now = System.currentTimeMillis();
		chatState.upsertSession(headers.getSessionId(), sender, now, clientId);

		ChatMessage out = new ChatMessage();
		out.setType(MessageType.JOIN);
		out.setSender(sender);
		out.setContent(sender + " присоединился(ась) к чату");
		out.setTimestamp(now);

		messagingTemplate.convertAndSend(TOPIC_PUBLIC, out);

		UsersUpdate update = new UsersUpdate(chatState.onlineCount(), chatState.onlineUsers());
		messagingTemplate.convertAndSend(TOPIC_USERS, update);

		if (clientId != null && !clientId.isBlank()) {
			messagingTemplate.convertAndSend(
				TOPIC_HISTORY_PREFIX + clientId,
				new HistorySnapshot("SNAPSHOT", chatState.historySnapshot())
			);
		}
	}

	@MessageMapping("/chat.send")
	public void send(@Payload ChatMessage message, SimpMessageHeaderAccessor headers, Principal principal) {
		String senderFromSession = null;
		if (headers.getSessionAttributes() != null) {
			Object s = headers.getSessionAttributes().get("sender");
			if (s instanceof String str) {
				senderFromSession = str;
			}
		}

		String sender = normalizeSender(senderFromSession != null ? senderFromSession : (message != null ? message.getSender() : null), principal);

		if (isEmptyChatMessage(message)) {
			return;
		}
		if (message != null && message.getAttachmentUrl() != null && !message.getAttachmentUrl().isBlank()) {
			if (!isAllowedUploadUrl(message.getAttachmentUrl())) {
				return;
			}
		}

		ChatMessage out = new ChatMessage();
		out.setType(MessageType.CHAT);
		out.setSender(sender);
		out.setContent(message != null ? message.getContent() : null);
		out.setTimestamp(System.currentTimeMillis());
		if (message != null) {
			out.setAttachmentUrl(message.getAttachmentUrl());
			out.setMimeType(message.getMimeType());
			out.setOriginalFilename(message.getOriginalFilename());
		}

		chatState.addToHistory(out);
		messagingTemplate.convertAndSend(TOPIC_PUBLIC, out);
	}

	@MessageMapping("/chat.typing")
	public void typing(@Payload ChatMessage message, SimpMessageHeaderAccessor headers, Principal principal) {
		String senderFromSession = null;
		if (headers.getSessionAttributes() != null) {
			Object s = headers.getSessionAttributes().get("sender");
			if (s instanceof String str) {
				senderFromSession = str;
			}
		}
		String sender = normalizeSender(senderFromSession != null ? senderFromSession : (message != null ? message.getSender() : null), principal);

		ChatMessage out = new ChatMessage();
		out.setType(MessageType.TYPING);
		out.setSender(sender);
		out.setContent(null);
		out.setTimestamp(System.currentTimeMillis());
		if (message != null && message.getClientId() != null && !message.getClientId().isBlank()) {
			out.setClientId(message.getClientId());
		}

		messagingTemplate.convertAndSend(TOPIC_TYPING, out);
	}

	private String normalizeSender(String sender, Principal principal) {
		if (sender != null && !sender.trim().isEmpty()) {
			return sender.trim();
		}
		if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
			return principal.getName();
		}
		return "Гость-" + UUID.randomUUID().toString().substring(0, 8);
	}

	private static boolean isEmptyChatMessage(ChatMessage message) {
		if (message == null) {
			return true;
		}
		String c = message.getContent();
		boolean noText = c == null || c.trim().isEmpty();
		String a = message.getAttachmentUrl();
		boolean noFile = a == null || a.trim().isEmpty();
		return noText && noFile;
	}

	private static boolean isAllowedUploadUrl(String url) {
		String u = url.trim();
		if (u.contains("..")) {
			return false;
		}
		return u.startsWith("/uploads/") || u.startsWith("/stickers/");
	}
}

