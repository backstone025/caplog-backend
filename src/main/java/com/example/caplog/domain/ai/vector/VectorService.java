package com.example.caplog.domain.ai.vector;

import com.example.caplog.domain.groups.entity.Groups;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorService {
    private final QdrantVectorStore vectorStore;

    // [사용 범위] : 단일 그룹에 대해서만 저장
    public void saveGroupsVector(Groups groups) {
        // 1. 한 문장으로 스케쥴 압축 + 고유 Document Id 생성
        String formattedContent = String.format("[%s] %s",
                groups.getCategory(),
                groups.getTitle());
        log.info("[Vector] 한 문장으로 스케쥴 압축");

        String userId = groups.getUser().getUsersId().toString();
        String groupId = groups.getGroupId().toString();
        String docId = this.getDocumentId(userId, groupId);

        // 2. Document 객체 생성
        Document document = new Document(
                docId,
                formattedContent,
                Map.of(
                        "user_id", userId,
                        "group_id", groupId
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

    // [사용 범위] : 단일 그룹에 대해서만 조회
    public List<Document> searchGroupsVector(Long userId, String query) {
        if (query == null || query.isBlank()) {
            log.warn("[Vector] 검색어가 null 또는 빈 문자열입니다. Vector 검색을 생략합니다.");
            return List.of();
        }
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(5)                                                                // 가장 유사한 상위 5개
                .similarityThreshold(0.3)                                               // 유사도 30% 이상
                .filterExpression(String.format("user_id == '%s'", userId.toString()))  // 다른 사용자 데이터 접근 차단
                .build();
        log.info("Qdrant 유사도 검색 - 그룹 생성");
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        assert results != null;
        log.info("Qdrant 유사도 검색 - user_id : {} / 결과 {}개", userId, results.size());
        return results;
    }

    // [사용 범위] : 단일 그룹에 대해서만 삭제
    public void deleteGroupsVector(Groups groups) {
        if (groups == null || groups.getUser() == null) {
            log.warn("[Vector] 삭제 실패: Group 또는 User 정보가 null입니다.");
            return;
        }
        String userId = groups.getUser().getUsersId().toString();
        String groupId = groups.getGroupId().toString();
        String docId = this.getDocumentId(userId, groupId);

        log.info("[Vector] Qdrant 삭제 시도 - user_id: {}, group_id: {}", userId, groupId);
        try {
            vectorStore.delete(List.of(docId));
            log.info("[Vector] Qdrant 삭제 완료 - docId: {}", docId);
        } catch (Exception e) {
            log.error("[Vector] Qdrant 삭제 중 예외 발생 - docId: {}", docId, e);
            throw e;
        }
    }

    private String getDocumentId(String userId, String groupId) {
        String rawKey = "user" + userId + "_group" + groupId; // user id와 group id가 복합 기본키로 동작하기에 따로 검증은 안한다.
        return UUID.nameUUIDFromBytes(rawKey.getBytes()).toString();
    }
}
