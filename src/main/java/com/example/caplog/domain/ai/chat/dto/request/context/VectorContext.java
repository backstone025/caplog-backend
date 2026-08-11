package com.example.caplog.domain.ai.chat.dto.request.context;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.stream.Collectors;

public record VectorContext(
        @JsonPropertyDescription("캡쳐 정보와 연관된 이력들을 압축한 문자열")
        String formattedContents
) {
    public static VectorContext from(List<Document> documentList) {
        // 추출된 연관 이력들 검사 로직
        if (documentList == null || documentList.isEmpty()) {
            return new VectorContext("(관련 이력 정보 없음)\n");
        }
        // 이력들 한 문자열로 압축
        String contents = documentList.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));
        return new VectorContext(contents);
    }
}
