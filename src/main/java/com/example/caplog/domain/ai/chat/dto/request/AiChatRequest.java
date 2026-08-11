package com.example.caplog.domain.ai.chat.dto.request;

import com.example.caplog.domain.ai.chat.dto.request.context.CaptureContext;
import com.example.caplog.domain.ai.chat.dto.request.context.VectorContext;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AiChatRequest(
        @JsonPropertyDescription("캡쳐 사진 추출 정보 컨텍스트")
        CaptureContext captureContext,

        @JsonPropertyDescription("캡쳐 정보와 연관된 이력들 컨텍스트")
        VectorContext vectorContext
) {
}
