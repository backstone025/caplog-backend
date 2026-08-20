package com.example.caplog.domain.ai.chat.service;

import com.example.caplog.domain.ai.vector.VectorService;
import com.example.caplog.domain.upload.dto.UploadResponse;
import com.example.caplog.global.error.code.GlobalErrorCode;
import com.example.caplog.global.error.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    public UploadResponse processImageAnalysis(MultipartFile file, Long imageId, Long userId) {
        log.info("[AiExtractService] Vision AI 이미지 분석 시작 - ImageId: {}, UserId: {}", imageId, userId);

        // =========================================================================
        // [1단계] MultipartFile -> Media 직접 변환 후 Vision LLM 호출
        // =========================================================================
        Media imageMedia = createImageMedia(file);

        String step1PromptText = """
                당신은 캡처 이미지 분석 및 일정 추출 전문가입니다.
                제공된 [이미지]를 직접 시각적으로 분석하여, 이미지 내에 포함된 모든 일정, 이벤트, 시험, 날짜, 시간, 장소, 과목명 등의 핵심 정보를 상세하게 분석하고 요약하세요.
                
                [분석 지침]
                1. 이미지 안에 포함된 모든 행사, 일정, 공지, 시간표 내용을 명확하게 파악하세요.
                2. 언급된 날짜와 시작/종료 시간이 있다면 놓치지 말고 정확히 정리하세요.
                3. 내용에 기반하여 어떤 형태의 일정인지(수업, 학교 행사, 개인 일정 등)를 상세히 설명하세요.
                4. 시각적으로 확인되지 않는 내용을 억지로 추측하거나 지어내지 마세요.
                """;

        log.info("[AiExtractService] 1단계: Vision LLM 직접 호출 (바이트 바이너리 전송) - 파일명: {}", file.getOriginalFilename());

        String analysisResult = chatClient.prompt()
                .user(userSpec -> userSpec.text(step1PromptText).media(imageMedia))
                .call()
                .content();

        log.info("[AiExtractService] 1단계 시각 분석 완료 - 내용: {}", analysisResult);

        // =========================================================================
        // [2단계] 1단계 분석 결과를 바탕으로 DTO 규격(JSON) 구조화 생성
        // =========================================================================
        BeanOutputConverter<UploadResponse> converter = new BeanOutputConverter<>(UploadResponse.class);

        String step2PromptText = """
                당신은 일정 및 이벤트 데이터 구조화 전문 AI입니다.
                아래 [1차 시각 분석 결과]를 참고하여, 지정된 JSON 형식 응답 데이터를 생성하세요.
                
                [1차 시각 분석 결과]
                {analysisResult}
                
                [언어 설정 - 필수]
                - **모든 텍스트 항목(title, details, aiSummary, scheduleAiSummary, group 등)은 반드시 한국어(Korean)로 작성하세요.**
                
                [분석 및 생성 지침]
                1. title: 전체 일정을 대표하는 제목을 한국어로 작성하세요.반드시 출력 형식의 모든 필드를 포함하세요.최상위 title 필드는 절대 생략하지 마세요. events 내부의 title과 별개로, 최상위 title도 반드시 작성해야 합니다
                2. imageId: 제공된 이미지 ID({imageId})를 그대로 지정하세요.
                3. events: 추출된 세부 일정/이벤트 목록을 생성하세요.
                   - title: 이벤트 제목 (한국어)
                   - details: 상세 설명 (한국어)
                   - aiSummary: 이벤트 요약 (한국어)
                   - videoUrl: 관련 영상 URL이 있다면 포함, 없으면 null
                   - date: 프론트 전용 날짜 포맷 (예시: 2026. 08. 20 오후 02:30)
                   - startAt: 시작 일시 (포맷: yyyy-MM-ddTHH:mm:ss)
                   - endAt: 종료 일시 (포맷: yyyy-MM-ddTHH:mm:ss)
                4. category: 다음 중 가장 적절한 하나를 선택하세요 (STUDY, SCHOOL, DAILY, ETC)
                5. scheduleAiSummary: 전체 일정 분석 결과에 대한 종합 AI 요약 (한국어)
                6. group: 관련된 그룹명이나 과목명이 명시되어 있다면 한국어로 작성하고, 없으면 null로 처리하세요.
                
                {format}
                """;

        PromptTemplate step2Template = new PromptTemplate(step2PromptText);
        step2Template.add("analysisResult", analysisResult);
        step2Template.add("imageId", imageId);
        step2Template.add("format", converter.getFormat());

        log.info("[AiExtractService] 2단계: JSON 구조화 LLM 호출");
        String responseContent = chatClient.prompt(step2Template.create())
                .call()
                .content();
        log.info("[AiExtractService] 2단계 AI 원본 응답: {}", responseContent);

        UploadResponse rawResponse = converter.convert(responseContent);

        if (rawResponse == null) {
            log.error("[AiExtractService] AI 응답 DTO 변환 실패");
            throw new IllegalStateException("AI 응답 변환에 실패했습니다.");
        }

        if (rawResponse.title() == null || rawResponse.title().isBlank()) {
            log.error(
                    "[AiExtractService] AI 분석 결과 title 누락 - responseContent: {}",
                    responseContent
            );
            throw new IllegalStateException("AI 분석 결과 title이 비어 있습니다.");
        }


        log.info("[AiExtractService] LLM 응답 DTO 파싱 완료 - Title: '{}', Category: '{}', RawGroup: '{}'",
                rawResponse.title(), rawResponse.category(), rawResponse.group());

        // =========================================================================
        // [3단계] VectorDB를 통한 유사 그룹 매칭
        // =========================================================================
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
        }

        log.info("[AiExtractService] 최종 이미지 분석 완료 - ImageId: {}, MatchedGroupId: {}", imageId, matchedGroupId);

        return rawResponse.withGroupInfo(matchedGroupId, matchedGroupName);
    }

    // MultipartFile을 Spring AI Media 객체로 변환하는 헬퍼 메서드
    private Media createImageMedia(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            Resource resource = new ByteArrayResource(bytes);

            String contentType = file.getContentType();
            MimeType mimeType = (contentType != null && !contentType.isBlank())
                    ? MimeTypeUtils.parseMimeType(contentType)
                    : MimeTypeUtils.IMAGE_PNG;

            return new Media(mimeType, resource);
        } catch (IOException e) {
            log.error("[AiExtractService] MultipartFile 바이트 읽기 실패 - 파일명: {}", file.getOriginalFilename(), e);
            throw new GeneralException(GlobalErrorCode.IMAGE_NOT_FOUND);
        }
    }

    public String generateGroupTitle(String firstTitle, String secondTitle) {
        String promptText = """
            당신은 일정들을 하나의 그룹으로 묶기 위한 그룹명 생성 전문가입니다.

            아래 두 일정의 제목을 보고,
            두 일정을 함께 묶을 수 있는 자연스럽고 간결한 한국어 그룹명을 생성하세요.

            [규칙]
            - 반드시 그룹명만 출력하세요.
            - 설명이나 따옴표는 출력하지 마세요.
            - 너무 길지 않게 작성하세요.
            - 두 일정의 공통 주제가 드러나야 합니다.
            - 단순히 두 제목을 이어 붙이지 마세요.

            첫 번째 일정 제목:
            {firstTitle}

            두 번째 일정 제목:
            {secondTitle}
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        template.add("firstTitle", firstTitle);
        template.add("secondTitle", secondTitle);

        String groupTitle = chatClient.prompt(template.create()).call().content();

        if (groupTitle == null || groupTitle.isBlank()) {
            return firstTitle + " / " + secondTitle;
        }

        return groupTitle.trim();
    }
}