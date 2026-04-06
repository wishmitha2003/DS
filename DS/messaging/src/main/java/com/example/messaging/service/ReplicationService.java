package com.example.messaging.service;

import com.example.messaging.model.Message;
import org.springframework.stereotype.Service;

@Service
public class ReplicationService {

    private final NodeClusterService nodeClusterService;

    public ReplicationService(NodeClusterService nodeClusterService) {
        this.nodeClusterService = nodeClusterService;
    }

    public void replicateToFollowers(Message message, String leaderNode) {
        for (String nodeId : nodeClusterService.getActiveNodes()) {
            if (!nodeId.equals(leaderNode)) {
                nodeClusterService.saveToNode(nodeId, message);
            }
        }
    }
}