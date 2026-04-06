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
    private final ConsensusService consensusService;

    public MessageService(NodeClusterService nodeClusterService,
                          DeduplicationService deduplicationService,
                          ReplicationService replicationService,
                          LogicalClockService logicalClockService,
                          TimeSyncService timeSyncService,
                          ConsensusService consensusService) {
        this.nodeClusterService = nodeClusterService;
        this.deduplicationService = deduplicationService;
        this.replicationService = replicationService;
        this.logicalClockService = logicalClockService;
        this.timeSyncService = timeSyncService;
        this.consensusService = consensusService;
    }

    public Message sendMessage(SendMessageRequest request) {
        if (deduplicationService.isDuplicate(request.getClientMessageId())) {
            return findByClientMessageId(request.getClientMessageId())
                    .orElseThrow(() -> new IllegalStateException("Duplicate detected but original message not found"));
        }

        String leaderNode = consensusService.getLeader();
        long logicalClock = logicalClockService.tick();

        Message message = new Message(
                UUID.randomUUID().toString(),
                request.getClientMessageId(),
                request.getFromUser(),
                request.getToUser(),
                request.getContent(),
                timeSyncService.correctedNow(leaderNode),
                logicalClock,
                leaderNode
        );

        nodeClusterService.saveToNode(leaderNode, message);
        replicationService.replicateToFollowers(message, leaderNode);
        deduplicationService.markProcessed(request.getClientMessageId());

        return message;
    }

    public List<Message> getInbox(String userId) {
        String leader = consensusService.getLeader();

        return nodeClusterService.readNodeMessages(leader).stream()
                .filter(m -> userId.equalsIgnoreCase(m.getToUser()))
                .sorted(Comparator.comparingLong(Message::getLogicalClock)
                        .thenComparing(Message::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<Message> getAllMessages() {
        String leader = consensusService.getLeader();

        return nodeClusterService.readNodeMessages(leader).stream()
                .sorted(Comparator.comparingLong(Message::getLogicalClock)
                        .thenComparing(Message::getCreatedAt))
                .collect(Collectors.toList());
    }

    private Optional<Message> findByClientMessageId(String clientMessageId) {
        String leader = consensusService.getLeader();

        return nodeClusterService.readNodeMessages(leader).stream()
                .filter(m -> clientMessageId != null && clientMessageId.equals(m.getClientMessageId()))
                .findFirst();
    }
}