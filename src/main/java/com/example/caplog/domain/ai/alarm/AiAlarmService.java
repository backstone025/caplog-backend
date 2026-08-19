package com.example.caplog.domain.ai.alarm;

import com.example.caplog.domain.schedule.entity.Schedule;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AiAlarmService {
    private final ChatClient chatClient;

    // 생성자를 통해 ChatClient 주입
    public AiAlarmService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    // Qdrant 검색에 사용할 단일 추천 키워드 추출
    public String getRandomKeyword() {
        // 단어 1개만 딱 출력하도록 프롬프트 명확화
        String promptText = """
                오늘의 추천 관심사 한글 단어 딱 1개만 추천해 주세요.
                부연 설명, 번호, 공백 없이 단어 하나만 반환해야 합니다. (예: 운동, 공부, 업무, 여행)
                """;

        PromptTemplate template = new PromptTemplate(promptText);
        String response = chatClient.prompt(template.create()).call().content();

        return response != null ? response.trim() : "일정";
    }

    // Document 메타데이터에서 Group ID 추출
    public Long extractGroupIdFromDocument(Document document){
        if (document == null || document.getMetadata() == null) {
            return null;
        }

        Object groupIdObj = document.getMetadata().get("group_id");

        if (groupIdObj != null) {
            try {
                return Long.parseLong(Objects.toString(groupIdObj));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // AI 추천 알림 문장 생성
    public String generateRecommendationMessage(Schedule schedule) {
        String prompt = String.format(
                """
                사용자의 관심 일정 제목: '%s'
                위 일정을 다시 확인하고 싶게 만들 만한 다정한 알림 문장을 작성해 주세요.
                
                [주의사항]
                1. 반말이나 지나친 농담 없이 따뜻하고 친근하게 작성할 것.
                2. 따옴표 없이 순수 문장만 작성할 것.
                3. 반드시 50자 이내의 한 문장으로 끝낼 것.
                """,
                schedule.getTitle()
        );

        String result = chatClient.prompt(prompt).call().content();

        // 생성된 문장에서 양끝 공백 및 따옴표 제거
        return result != null ? result.trim().replaceAll("^\"|\"$", "") : "저장해 두신 일정을 다시 확인해 보세요!";
    }
}
