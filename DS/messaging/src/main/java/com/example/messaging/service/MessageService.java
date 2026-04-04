package com.example.messaging.service;

import com.example.messaging.dto.SendMessageRequest;
import com.example.messaging.model.Message;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final NodeClusterService nodeClusterService;

    public MessageService(NodeClusterService nodeClusterService) {
        this.nodeClusterService = nodeClusterService;
    }

    public Message sendMessage(SendMessageRequest request) {
        String writableNode = nodeClusterService.getWritableNode();

        Message message = new Message(
                UUID.randomUUID().toString(),
                request.getClientMessageId(),
                request.getFromUser(),
                request.getToUser(),
                request.getContent(),
                Instant.now(),
                0L,
                writableNode
        );

        nodeClusterService.saveToNode(writableNode, message);
        return message;
    }

    public List<Message> getInbox(String userId) {
        String primary = nodeClusterService.getPrimaryNode();
        if (primary == null) {
            throw new IllegalStateException("No active primary node");
        }

        return nodeClusterService.readNodeMessages(primary).stream()
                .filter(m -> userId.equalsIgnoreCase(m.getToUser()))
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<Message> getAllMessages() {
        String primary = nodeClusterService.getPrimaryNode();
        if (primary == null) {
            throw new IllegalStateException("No active primary node");
        }

        return nodeClusterService.readNodeMessages(primary).stream()
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .collect(Collectors.toList());
    }
}