package com.example.messaging.controller;

import com.example.messaging.dto.SendMessageRequest;
import com.example.messaging.model.Message;
import com.example.messaging.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public Message send(@RequestBody SendMessageRequest request) {
        return messageService.sendMessage(request);
    }

    @GetMapping("/inbox/{userId}")
    public List<Message> inbox(@PathVariable String userId) {
        return messageService.getInbox(userId);
    }

    @GetMapping("/all")
    public List<Message> all() {
        return messageService.getAllMessages();
    }
}