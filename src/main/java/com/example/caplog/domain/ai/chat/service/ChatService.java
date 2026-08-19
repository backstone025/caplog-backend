package com.example.caplog.domain.ai.chat.service;

import com.example.caplog.domain.ai.chat.dto.request.AiChatRequest;
import com.example.caplog.domain.ai.chat.dto.response.AiChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatClient chatClient;

    // 생성자를 통해 ChatClient 주입
    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AiChatResponse extractSchedules(AiChatRequest request) {
        // 1. LLM 응답을 지정된 response로 변환해줄 컨버터 생성
        var outputconverter = new BeanOutputConverter<>(AiChatResponse.class);

        // 2. 프롬프트 정의
        String promtText = """
                당신은 일정 추출 전문 AI입니다.
                전달받은 [기존 일정 이력]과 [이미지 추출 텍스트]를 참고하여 새로 등록할 일정을 정교하게 추출해 주세요.
                
                [추출 규칙]
                1. 카테고리는 다음 항목 중 가장 적절한 하나를 선택해야 합니다: STUDY, SCHOOL, DAILY, ETC
                2. 날짜 및 시간 포맷은 반드시 'yyyy-MM-dd HH:mm:ss' 형태여야 합니다. (예: 2026-08-07 14:00:00)
                
                [기존 일정 이력]
                {vectorContext}
                
                [이미지 추출 텍스트]
                {captureContext}
                
                {format}
                """;

        // 3. PromptTemplate로 바인딩
        PromptTemplate template = new PromptTemplate(promtText);
        template.add("vectorContext", request.vectorContext());
        template.add("captureContext", request.captureContext());
        template.add("format", outputconverter.getFormat());

        // 4. LLM 호출 및 DTO 변환
        String rawResponse = chatClient.prompt(template.create()).call().content();

        assert rawResponse != null;
        return outputconverter.convert(rawResponse);
    }
}
