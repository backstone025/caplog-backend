package com.example.caplog.domain.ai.chat.service;

import com.example.caplog.domain.ai.chat.dto.request.AiChatRequest;
import com.example.caplog.domain.ai.chat.dto.response.AiChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.model.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AiChatResponse extractSchedules(AiChatRequest request) {

        var outputConverter =
                new BeanOutputConverter<>(AiChatResponse.class);

        String promptText = """
                당신은 일정 추출 전문 AI입니다.
                전달받은 [기존 일정 이력]과 [이미지 추출 텍스트]를 참고하여
                새로 등록할 일정을 정교하게 추출해 주세요.

                [추출 규칙]
                1. 카테고리는 다음 항목 중 가장 적절한 하나를 선택해야 합니다:
                   STUDY, SCHOOL, DAILY, ETC
          
                2. 날짜 및 시간 포맷은 반드시
                   'yyyy-MM-dd HH:mm:ss' 형태여야 합니다.

                [기존 일정 이력]
                {vectorContext}

                [이미지 추출 텍스트]
                {captureContext}

                {format}
                """;

        PromptTemplate template =
                new PromptTemplate(promptText);

        template.add(
                "vectorContext",
                request.vectorContext()
        );

        template.add(
                "captureContext",
                request.captureContext()
        );

        template.add(
                "format",
                outputConverter.getFormat()
        );

        String rawResponse = chatClient
                .prompt(template.create())
                .call()
                .content();

        if (rawResponse == null) {
            throw new IllegalStateException(
                    "AI 응답이 없습니다."
            );
        }

        return outputConverter.convert(rawResponse);
    }


    public AiChatResponse analyzeImage(
            byte[] imageBytes,
            String contentType
    ) {

        var outputConverter =
                new BeanOutputConverter<>(AiChatResponse.class);

        String promptText = """
                당신은 캡처 이미지를 분석하여
                일정 정보를 추출하는 AI입니다.

                첨부된 이미지를 직접 읽고 다음 정보를 추출하세요.

                1. 전체 대표 제목
                2. 핵심 텍스트
                3. 독립적으로 구분되는 정보
                4. 일정
                5. 장소, 준비물, 주의사항 등 중요 세부정보
                6. AI 요약
                
                [카테고리 분류 규칙]
                        - 공부:
                          시험, 중간고사, 기말고사, 과제, 공부 계획,
                          자격증 공부, 개인 학습 등 학습 자체와 관련된 일정
                
                        - 학교:
                          수강신청, 학사일정, 등록금, 장학금, 학교 행사,
                          휴강, 학사 행정, 교내 공지 등 학교 생활 및 행정과 관련된 내용.
                
                        - 일상:
                          약속, 병원, 운동, 여행, 식사, 개인 일정 등 일상생활 관련 일정
                
                        - 기타:
                          위 세 카테고리로 명확하게 분류하기 어려운 일정
                [독립 정보 판단 규칙]
                
                - 하나의 이미지 안에서 서로 다른 주제나 목적을 가진 정보가
                  각각 독립적으로 이해될 수 있다면 items로 분리하세요.
                
                - 각 독립 정보에는 각각 title, aiSummary, details,
                  schedules를 생성하세요.
                
                - 단순히 문단이 나뉘어 있다는 이유만으로 분리하지 마세요.
                
                - 하나의 동일한 주제를 설명하는 여러 문장은 하나의 정보로 처리하세요.
                
                - 독립 정보가 없다면 items는 빈 배열로 반환하고,
                  이미지 전체의 aiSummary와 details를 사용하세요.

                [추출 규칙]

                - 이미지에 실제로 존재하는 정보만 사용하세요.
                - 존재하지 않는 내용을 추측해서 만들지 마세요.
                - 일정이 여러 개라면 모두 추출하세요.
                - 일정이 없다면 schedules는 빈 배열로 반환하세요.
                - 카테고리는 다음 중 하나만 사용하세요:
                  DEFAULT, WORK, PERSONAL, STUDY, ETC
                - 날짜 및 시간은 반드시
                  'yyyy-MM-dd HH:mm:ss' 형식으로 반환하세요.
                - 날짜 또는 시간이 명확하지 않다면
                  임의로 생성하지 마세요.

                {format}
                """;

        MimeType mimeType;

        if (contentType == null) {
            mimeType = MimeTypeUtils.IMAGE_PNG;
        } else {
            mimeType = MimeType.valueOf(contentType);
        }

        ByteArrayResource imageResource =
                new ByteArrayResource(imageBytes);

        Media media =
                new Media(
                        mimeType,
                        imageResource
                );

        String rawResponse = chatClient
                .prompt()
                .user(user -> user
                        .text(promptText)
                        .param(
                                "format",
                                outputConverter.getFormat()
                        )
                        .media(media)
                )
                .call()
                .content();

        if (rawResponse == null) {
            throw new IllegalStateException(
                    "AI 이미지 분석 응답이 없습니다."
            );
        }

        return outputConverter.convert(rawResponse);
    }
}