package com.example.messaging.service;

import com.example.messaging.model.Message;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NodeClusterService {

    private final Map<String, Boolean> nodeHealth = new LinkedHashMap<>();
    private final Map<String, List<Message>> nodeStorage = new ConcurrentHashMap<>();
    private String primaryNode;

    @PostConstruct
    public void init() {
        nodeHealth.put("nodeA", true);
        nodeHealth.put("nodeB", true);
        nodeHealth.put("nodeC", true);

        nodeStorage.put("nodeA", Collections.synchronizedList(new ArrayList<>()));
        nodeStorage.put("nodeB", Collections.synchronizedList(new ArrayList<>()));
        nodeStorage.put("nodeC", Collections.synchronizedList(new ArrayList<>()));

        primaryNode = "nodeA";
    }

    public synchronized boolean isNodeActive(String nodeId) {
        return nodeHealth.getOrDefault(nodeId, false);
    }

    public synchronized String getPrimaryNode() {
        return primaryNode;
    }

    public synchronized List<String> getActiveNodes() {
        List<String> active = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : nodeHealth.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                active.add(entry.getKey());
            }
        }
        return active;
    }

    public synchronized String getWritableNode() {
        if (primaryNode != null && isNodeActive(primaryNode)) {
            return primaryNode;
        }
        return electFailoverNode();
    }

    public synchronized void promoteNode(String nodeId) {
        if (!isNodeActive(nodeId)) {
            throw new IllegalStateException("Cannot promote inactive node: " + nodeId);
        }
        primaryNode = nodeId;
    }

    public synchronized void failNode(String nodeId) {
        if (!nodeHealth.containsKey(nodeId)) {
            throw new IllegalArgumentException("Unknown node: " + nodeId);
        }

        nodeHealth.put(nodeId, false);

        if (nodeId.equals(primaryNode)) {
            electFailoverNode();
        }
    }

    public synchronized void recoverNode(String nodeId) {
        if (!nodeHealth.containsKey(nodeId)) {
            throw new IllegalArgumentException("Unknown node: " + nodeId);
        }

        nodeHealth.put(nodeId, true);
        syncRecoveredNode(nodeId);

        if (primaryNode == null) {
            primaryNode = nodeId;
        }
    }

    private synchronized String electFailoverNode() {
        for (Map.Entry<String, Boolean> entry : nodeHealth.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                primaryNode = entry.getKey();
                return primaryNode;
            }
        }
        primaryNode = null;
        throw new IllegalStateException("No active nodes available");
    }

    public synchronized void syncRecoveredNode(String recoveredNode) {
        if (primaryNode == null || !isNodeActive(primaryNode)) {
            return;
        }

        List<Message> source = nodeStorage.get(primaryNode);
        List<Message> target = nodeStorage.get(recoveredNode);

        target.clear();
        for (Message message : source) {
            target.add(copyMessage(message));
        }
    }

    public void saveToNode(String nodeId, Message message) {
        List<Message> messages = nodeStorage.get(nodeId);
        if (messages == null) {
            throw new IllegalArgumentException("Unknown node: " + nodeId);
        }
        messages.add(copyMessage(message));
    }

    public List<Message> readNodeMessages(String nodeId) {
        List<Message> messages = nodeStorage.get(nodeId);
        if (messages == null) {
            return new ArrayList<>();
        }

        List<Message> copy = new ArrayList<>();
        synchronized (messages) {
            for (Message message : messages) {
                copy.add(copyMessage(message));
            }
        }
        return copy;
    }

    public List<Message> getNodeMessages(String nodeId) {
        return readNodeMessages(nodeId);
    }

    public synchronized Map<String, Object> clusterStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("primaryNode", primaryNode);
        result.put("nodeHealth", new LinkedHashMap<>(nodeHealth));

        Map<String, Integer> storageSizes = new LinkedHashMap<>();
        for (String nodeId : nodeStorage.keySet()) {
            storageSizes.put(nodeId, nodeStorage.get(nodeId).size());
        }
        result.put("storageSizes", storageSizes);

        return result;
    }

    private Message copyMessage(Message message) {
        return new Message(
                message.getId(),
                message.getClientMessageId(),
                message.getFromUser(),
                message.getToUser(),
                message.getContent(),
                message.getCreatedAt(),
                message.getLogicalClock(),
                message.getSourceNode()
        );
    }
}
