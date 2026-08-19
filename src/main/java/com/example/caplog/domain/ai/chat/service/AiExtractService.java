package com.example.caplog.domain.ai.chat.service;

import com.example.caplog.domain.ai.vector.VectorService;
import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.upload.dto.UploadResponse;
import com.example.caplog.global.S3.S3Service; // ★ S3Service import
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

@Slf4j
@Service
public class AiExtractService {

    private final ChatClient chatClient;
    private final VectorService vectorService;
    private final S3Service s3Service; // ★ S3Service 추가

    public AiExtractService(ChatClient.Builder chatClientBuilder,
                            VectorService vectorService,
                            S3Service s3Service) {
        this.chatClient = chatClientBuilder.build();
        this.vectorService = vectorService;
        this.s3Service = s3Service;
    }

    public UploadResponse processImageAnalysis(Images imageEntity, Long userId) {
        log.info("[AiExtractService] 이미지 분석 시작 - ImageId: {}, UserId: {}", imageEntity.getImageId(), userId);

        BeanOutputConverter<UploadResponse> converter = new BeanOutputConverter<>(UploadResponse.class);

        String promptText = """
                당신은 제공된 이미지에서 '실제 존재하는 일정 및 공지'를 직접 읽어내어 정확히 추출하는 AI입니다.
                이미지 내부의 글자를 꼼꼼하게 분석하여 JSON 응답 데이터를 생성하세요.
                
                [절대 준수 지침]
                1. **이미지 안의 글자(제목, 날짜, 시간, 장소, 과목명)를 있는 그대로 읽고 분석하세요.**
                2. 이미지에 명시되지 않은 이벤트, 시험, 날짜는 절대로 추측하거나 지어내지 마세요.
                3. 공지사항, 시험 일정, 제출 기한 등 핵심 이벤트를 세부 이벤트(events) 목록으로 구성하세요.
                4. 모든 텍스트 항목(title, details, aiSummary 등)은 한국어로 작성하세요.
                
                [분석 및 생성 지침]
                1. title: 과목명과 공지 내용을 조합한 전체 제목 (예: [데이터수학통계] 중간고사 공지)
                2. imageId: 제공된 이미지 ID({imageId})를 그대로 지정
                3. events: 이미지에서 읽어낸 세부 일정
                   - title: 이벤트 제목
                   - details: 상세 설명 (장소, 범위, 준비물 등)
                   - aiSummary: 이벤트 한 줄 요약
                   - date: 프론트 표기용 날짜 (예: 2025. 04. 22 오후 03:00)
                   - startAt: 시작 일시 (포맷: yyyy-MM-ddTHH:mm:ss)
                   - endAt: 종료 일시 (포맷: yyyy-MM-ddTHH:mm:ss, 미표시 시 startAt +1시간)
                4. category: STUDY, SCHOOL, DAILY, ETC 중 선택
                5. scheduleAiSummary: 종합 AI 요약
                6. group: 언급된 그룹/과목명이 있다면 작성, 없으면 null
                
                {format}
                """;

        PromptTemplate template = new PromptTemplate(promptText);
        template.add("imageId", imageEntity.getImageId());
        template.add("format", converter.getFormat());

        try {
            // ★ PENDING 상태와 상관없이 S3Key를 이용하여 S3 URL 직접 호출
            String imageUrl = s3Service.getUrl(imageEntity.getImageKey());
            log.info("[AiExtractService] S3 이미지 URL 생성 완료: {}", imageUrl);

            UrlResource imageResource = new UrlResource(imageUrl);
            Media imageMedia = new Media(MimeTypeUtils.IMAGE_JPEG, imageResource);

            UserMessage userMessage = new UserMessage(template.render(), List.of(imageMedia));

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model("gpt-4o")
                    .temperature(0.0)
                    .build();

            String responseContent = chatClient.prompt(new Prompt(userMessage, options))
                    .call()
                    .content();

            UploadResponse rawResponse = converter.convert(responseContent);

            // VectorDB 매칭 및 최종 반환
            List<Document> searchResults = vectorService.searchGroupsVector(userId, rawResponse.title());
            Long matchedGroupId = null;
            String matchedGroupName = rawResponse.group();

            if (!searchResults.isEmpty()) {
                Document topDoc = searchResults.get(0);
                Object groupIdObj = topDoc.getMetadata().get("group_id");
                if (groupIdObj != null) {
                    matchedGroupId = Long.valueOf(groupIdObj.toString());
                }
            }

            return rawResponse.withGroupInfo(matchedGroupId, matchedGroupName);

        } catch (Exception e) {
            log.error("[AiExtractService] 이미지 분석 중 예외 발생", e);
            throw new RuntimeException("이미지 분석 실패: " + e.getMessage());
        }
    }
}