package com.example.caplog.domain.ai.vector;

import com.example.caplog.domain.schedule.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


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
}
