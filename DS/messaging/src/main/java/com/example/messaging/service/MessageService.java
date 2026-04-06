package com.example.messaging.service;

import com.example.messaging.dto.SendMessageRequest;
import com.example.messaging.model.Message;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final NodeClusterService nodeClusterService;
    private final DeduplicationService deduplicationService;
    private final ReplicationService replicationService;
    private final LogicalClockService logicalClockService;
    private final TimeSyncService timeSyncService;

    public MessageService(NodeClusterService nodeClusterService,
                          DeduplicationService deduplicationService,
                          ReplicationService replicationService,
                          LogicalClockService logicalClockService,
                          TimeSyncService timeSyncService) {
        this.nodeClusterService = nodeClusterService;
        this.deduplicationService = deduplicationService;
        this.replicationService = replicationService;
        this.logicalClockService = logicalClockService;
        this.timeSyncService = timeSyncService;
    }

    public Message sendMessage(SendMessageRequest request) {
        if (deduplicationService.isDuplicate(request.getClientMessageId())) {
            return findByClientMessageId(request.getClientMessageId())
                    .orElseThrow(() -> new IllegalStateException("Duplicate detected but original message not found"));
        }

        String writableNode = nodeClusterService.getWritableNode();
        long clock = logicalClockService.tick();

        Message message = new Message(
                UUID.randomUUID().toString(),
                request.getClientMessageId(),
                request.getFromUser(),
                request.getToUser(),
                request.getContent(),
                timeSyncService.correctedNow(writableNode),
                clock,
                writableNode
        );

        nodeClusterService.saveToNode(writableNode, message);
        replicationService.replicateToFollowers(message, writableNode);
        deduplicationService.markProcessed(request.getClientMessageId());

        return message;
    }

    public List<Message> getInbox(String userId) {
        String primary = nodeClusterService.getPrimaryNode();
        if (primary == null) {
            throw new IllegalStateException("No active primary node");
        }

        return nodeClusterService.readNodeMessages(primary).stream()
                .filter(m -> userId.equalsIgnoreCase(m.getToUser()))
                .sorted(Comparator.comparingLong(Message::getLogicalClock)
                        .thenComparing(Message::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<Message> getAllMessages() {
        String primary = nodeClusterService.getPrimaryNode();
        if (primary == null) {
            throw new IllegalStateException("No active primary node");
        }

        return nodeClusterService.readNodeMessages(primary).stream()
                .sorted(Comparator.comparingLong(Message::getLogicalClock)
                        .thenComparing(Message::getCreatedAt))
                .collect(Collectors.toList());
    }

    private Optional<Message> findByClientMessageId(String clientMessageId) {
        String primary = nodeClusterService.getPrimaryNode();
        if (primary == null) {
            return Optional.empty();
        }

        return nodeClusterService.readNodeMessages(primary).stream()
                .filter(m -> clientMessageId != null && clientMessageId.equals(m.getClientMessageId()))
                .findFirst();
    }
}