package com.example.caplog.domain.ai.chat.service;

import com.example.caplog.domain.ai.vector.VectorService;
import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.upload.dto.UploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AiExtractService {

    private final ChatClient chatClient;
    private final VectorService vectorService;

    public AiExtractService(ChatClient.Builder chatClientBuilder, VectorService vectorService) {
        this.chatClient = chatClientBuilder.build();
        this.vectorService = vectorService;
    }

    public UploadResponse processImageAnalysis(Images imageEntity, Long userId) {
        log.info("[AiExtractService] 이미지 분석 시작 - ImageId: {}, UserId: {}", imageEntity.getImageId(), userId);

        // 1. OutputConverter 생성
        BeanOutputConverter<UploadResponse> converter = new BeanOutputConverter<>(UploadResponse.class);

        // 2. 프롬프트 정의 (한국어 출력 지침 추가)
        String promptText = """
                당신은 캡처 이미지 분석 및 일정/이벤트 데이터 구조화 전문 AI입니다.
                제공된 [이미지 OCR 텍스트]를 분석하여 JSON 형식으로 응답 데이터를 생성하세요.
                
                [언어 설정 - 필수]
                - **모든 텍스트 항목(title, details, aiSummary, scheduleAiSummary, group 등)은 반드시 한국어(Korean)로 작성하세요.**
                - 영어 텍스트가 원본에 있더라도 자연스러운 한국어로 번역 및 요약하여 출력하세요.
                
                [분석 및 생성 지침]
                1. title: 이미지 내용을 대표하는 전체 제목을 한국어로 작성하세요.
                2. imageId: 제공된 이미지 ID({imageId})를 그대로 지정하세요.
                3. events: 이미지 내에서 추출된 세부 일정/이벤트 목록을 생성하세요.
                   - title: 이벤트 제목 (한국어)
                   - details: 상세 설명 (한국어)
                   - aiSummary: 이벤트 요약 (한국어)
                   - videoUrl: 관련 영상 URL이 있다면 포함, 없으면 null
                   - date: 프론트 전용 날짜 포맷 (예시: 2026. 08. 20 오후 02:30)
                   - startAt: 시작 일시 (포맷: yyyy-MM-ddTHH:mm:ss)
                   - endAt: 종료 일시 (포맷: yyyy-MM-ddTHH:mm:ss)
                4. category: 다음 중 가장 적절한 하나를 선택하세요 (STUDY, SCHOOL, DAILY, ETC)
                5. scheduleAiSummary: 전체 일정 분석 결과에 대한 종합 AI 요약 (한국어)
                6. group: 관련된 그룹명이나 분류명이 언급되었다면 한국어로 작성하고, 없으면 빈 문자열 또는 null로 처리하세요.
                
                [이미지 OCR 텍스트]
                {ocrText}
                
                {format}
                """;

        // 3. 템플릿 변수 바인딩
        PromptTemplate template = new PromptTemplate(promptText);
        template.add("imageId", imageEntity.getImageId());
        template.add("ocrText", imageEntity.getOcrText() != null ? imageEntity.getOcrText() : "");
        template.add("format", converter.getFormat());

        log.info("[AiExtractService] LLM 호출 준비 완료 - ImageId: {}, OCR 텍스트 존재 여부: {}",
                imageEntity.getImageId(), imageEntity.getOcrText() != null);

        // 4. LLM 호출 및 DTO 변환
        String responseContent = chatClient.prompt(template.create())
                .call()
                .content();

        log.debug("[AiExtractService] LLM 원본 응답 수신 - RawResponse: {}", responseContent);

        UploadResponse rawResponse = converter.convert(responseContent);
        log.info("[AiExtractService] LLM 응답 DTO 파싱 완료 - Title: '{}', Category: '{}', RawGroup: '{}'",
                rawResponse.title(), rawResponse.category(), rawResponse.group());

        // 5. VectorDB를 통한 유사 그룹 매칭 (Document 목록 반환)
        log.info("[AiExtractService] VectorDB 유사 그룹 검색 요청 - UserId: {}, Search Title: '{}'", userId, rawResponse.title());
        List<Document> searchResults = vectorService.searchGroupsVector(userId, rawResponse.title());

        Long matchedGroupId = null;
        String matchedGroupName = rawResponse.group();

        if (!searchResults.isEmpty()) {
            Document topDoc = searchResults.get(0);
            Object groupIdObj = topDoc.getMetadata().get("group_id");
            if (groupIdObj != null) {
                matchedGroupId = Long.valueOf(groupIdObj.toString());
            }
            log.info("[AiExtractService] VectorDB 그룹 매칭 성공 - MatchedGroupId: {}, Top Similarity Content: '{}'",
                    matchedGroupId, topDoc.getContent());
        } else {
            log.info("[AiExtractService] VectorDB 매칭 결과 없음 - MatchedGroupId: null");
        }

        // 6. Vector 검색 결과(matchedGroupId)를 반영한 최종 Response 생성
        log.info("[AiExtractService] 최종 이미지 분석 완료 - ImageId: {}, MatchedGroupId: {}",
                imageEntity.getImageId(), matchedGroupId);
        return rawResponse.withGroupInfo(matchedGroupId, matchedGroupName);
    }
}