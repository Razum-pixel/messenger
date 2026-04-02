package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.ChatMessage.MessageType;
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
	private final SimpMessagingTemplate messagingTemplate;

	public ChatController(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	@MessageMapping("/chat.join")
	public void join(@Payload ChatMessage message, SimpMessageHeaderAccessor headers, Principal principal) {
		String sender = normalizeSender(message != null ? message.getSender() : null, principal);
		headers.getSessionAttributes().put("sender", sender);

		ChatMessage out = new ChatMessage();
		out.setType(MessageType.JOIN);
		out.setSender(sender);
		out.setContent(sender + " присоединился(ась) к чату");
		out.setTimestamp(System.currentTimeMillis());

		messagingTemplate.convertAndSend(TOPIC_PUBLIC, out);
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

		ChatMessage out = new ChatMessage();
		out.setType(MessageType.CHAT);
		out.setSender(sender);
		out.setContent(message != null ? message.getContent() : null);
		out.setTimestamp(System.currentTimeMillis());

		messagingTemplate.convertAndSend(TOPIC_PUBLIC, out);
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
}

