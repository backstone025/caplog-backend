package com.example.caplog.domain.ai.vector;

import com.example.caplog.domain.schedule.ScheduleRepository;
import com.example.caplog.domain.schedule.entity.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorService {
    private final QdrantVectorStore vectorStore;
    private final ScheduleRepository scheduleRepository;

    public void saveScheduleVector(Schedule schedule) {
        // 1. 한 문장으로 스케쥴 압축 + 고유 Document Id 생성
        String formattedContent = String.format("[%s] %s | %s ~ %s | description: %s",
                schedule.getCategory(),
                schedule.getTitle(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getDescription());
        log.info("[Vector] 한 문장으로 스케쥴 압축");

        String userId = schedule.getUser().getId().toString();
        String scheduleId = schedule.getId().toString();
        String docId = this.getDocumentId(userId, scheduleId);

        // 2. Document 객체 생성
        Document document = new Document(
                docId,
                formattedContent,
                Map.of(
                        "user_id", userId,
                        "schedule_id", scheduleId
                ));
        log.info("[Vector] Document 객체 생성");

        // 3. Qdrant에 저장
        try {
            vectorStore.add(List.of(document));
            log.info("[Vector] Qdrant에 저장 성공!");
        } catch (Exception e) {
            log.error("[Vector] Qdrant 저장 중 에러 발생: ", e);
            throw e;
        }
    }

    public List<Document> searchScheduleVector(Long userId, String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(5)                                                                // 가장 유사한 상위 5개
                .similarityThreshold(0.3)                                               // 유사도 30% 이상
                .filterExpression(String.format("user_id == '%s'", userId.toString()))  // 다른 사용자 데이터 접근 차단
                .build();
        log.info("Qdrant 유사도 검색 - 일정 생성");
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        assert results != null;
        log.info("Qdrant 유사도 검색 - user_id : {} / 결과 {}개", userId, results.size());
        return results;
    }

    public void deleteScheduleVector(Schedule schedule) {
        if (schedule == null || schedule.getUser() == null) {
            log.warn("[Vector] 삭제 실패: Schedule 또는 User 정보가 null입니다.");
            return;
        }
        String userId = schedule.getUser().getId().toString();
        String scheduleId = schedule.getId().toString();
        String docId = this.getDocumentId(userId, scheduleId);

        log.info("[Vector] Qdrant 삭제 시도 - user_id: {}, schedule_id: {}", userId, scheduleId);
        try {
            vectorStore.delete(List.of(docId));
            log.info("[Vector] Qdrant 삭제 완료 - docId: {}", docId);
        } catch (Exception e) {
            log.error("[Vector] Qdrant 삭제 중 예외 발생 - docId: {}", docId, e);
            throw e;
        }
    }

    private String getDocumentId(String userId, String scheduleId) {
        String rawKey = "user" + userId + "_schedule" + scheduleId;
        return UUID.nameUUIDFromBytes(rawKey.getBytes()).toString();
    }
}
