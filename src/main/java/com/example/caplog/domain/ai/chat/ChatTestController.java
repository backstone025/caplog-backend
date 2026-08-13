package com.example.caplog.domain.ai.chat;

import com.example.caplog.domain.ai.chat.dto.request.AiChatRequest;
import com.example.caplog.domain.ai.chat.dto.request.context.CaptureContext;
import com.example.caplog.domain.ai.chat.dto.request.context.VectorContext;
import com.example.caplog.domain.ai.chat.dto.response.AiChatResponse;
import com.example.caplog.domain.ai.chat.service.ChatService;
import com.example.caplog.domain.ai.vector.VectorService;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.repository.UsersRepository;
import com.example.caplog.domain.users.service.UsersService;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/chat")
@RequiredArgsConstructor
public class ChatTestController {
    private final UsersService usersService;
    private final UsersRepository usersRepository;
    private final VectorService vectorService;
    private final ChatService chatService;

    @PostMapping("/extract")
    public ResponseEntity<ApiResponse<AiChatResponse>> extract(@RequestBody CaptureContext capture) {
        Users user = usersRepository.findById(usersService.getUserId()).orElseThrow();
        VectorContext vector = VectorContext.from(vectorService.searchScheduleVector(user.getUsersId(), capture.text()));
        AiChatRequest request = new AiChatRequest(capture, vector);

        AiChatResponse response = chatService.extractSchedules(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
