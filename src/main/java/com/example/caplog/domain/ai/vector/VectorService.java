package com.example.caplog.domain.ai.vector;

import com.example.caplog.domain.schedule.entity.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorService {
    private final QdrantVectorStore vectorStore;

    public void saveScheduleVector(Schedule schedule) {
        // 1. 한 문장으로 스케쥴 압축
        String formattedContent = String.format("[%s] %s | %s ~ %s | description: %s",
                schedule.getCategory(),
                schedule.getTitle(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getDescription());

        // 2. Document 객체 생성
        Document document = new Document(formattedContent,
                Map.of(
                        "user_id", schedule.getUser().getId(),
                        "schedule_id", schedule.getId()
                ));

        // 3. Qdrant에 저장
        vectorStore.add(List.of(document));
    }

    public List<Document> searchScheduleVector(Long userId, String query) {
        SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(5)                                                // 가장 유사한 상위 5개
                .withSimilarityThreshold(0.5)                               // 유사도 50% 이상
                .withFilterExpression("user_id == " + userId); // 다른 사용자 데이터 접근 차단

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        log.info("Qdrant 유사도 검색 (user_id : {}) - {}개", userId, results);
        return results;
    }
}
