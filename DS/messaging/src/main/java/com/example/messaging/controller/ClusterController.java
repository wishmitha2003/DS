package com.example.messaging.controller;

import com.example.messaging.model.Message;
import com.example.messaging.service.NodeClusterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cluster")
public class ClusterController {

    private final NodeClusterService nodeClusterService;

    public ClusterController(NodeClusterService nodeClusterService) {
        this.nodeClusterService = nodeClusterService;
    }

    @GetMapping("/node/{nodeId}")
    public List<Message> getNodeMessages(@PathVariable String nodeId) {
        return nodeClusterService.getNodeMessages(nodeId);
    }

    @PostMapping("/fail/{nodeId}")
    public Map<String, Object> failNode(@PathVariable String nodeId) {
        nodeClusterService.failNode(nodeId);
        return nodeClusterService.clusterStatus();
    }

    @PostMapping("/recover/{nodeId}")
    public Map<String, Object> recoverNode(@PathVariable String nodeId) {
        nodeClusterService.recoverNode(nodeId);
        return nodeClusterService.clusterStatus();
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return nodeClusterService.clusterStatus();
    }
}
