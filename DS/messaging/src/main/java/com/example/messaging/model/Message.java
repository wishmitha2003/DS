package com.example.messaging.model;

import java.time.Instant;

public class Message {
    private String id;
    private String clientMessageId;
    private String fromUser;
    private String toUser;
    private String content;
    private Instant createdAt;
    private long logicalClock;
    private String sourceNode;

    public Message() {
    }

    public Message(String id, String clientMessageId, String fromUser, String toUser,
                   String content, Instant createdAt, long logicalClock, String sourceNode) {
        this.id = id;
        this.clientMessageId = clientMessageId;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.content = content;
        this.createdAt = createdAt;
        this.logicalClock = logicalClock;
        this.sourceNode = sourceNode;
    }

    public String getId() {
        return id;
    }

    public String getClientMessageId() {
        return clientMessageId;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getLogicalClock() {
        return logicalClock;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setClientMessageId(String clientMessageId) {
        this.clientMessageId = clientMessageId;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setLogicalClock(long logicalClock) {
        this.logicalClock = logicalClock;
    }

    public void setSourceNode(String sourceNode) {
        this.sourceNode = sourceNode;
    }
}
