package com.example.caplog.domain.ai.vector;

import com.example.caplog.domain.schedule.DemoScheduleRepository;
import com.example.caplog.domain.schedule.entity.DemoSchedule;
import com.example.caplog.domain.schedule.type.Category;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.repository.UsersRepository;
import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.global.response.ApiResponse;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/test/vector")
@RequiredArgsConstructor
public class VectorTestController {
    private final VectorService vectorService;
    private final AuthService authService;
    private final UsersRepository usersRepository;
    private final DemoScheduleRepository demoScheduleRepository;

    @PostMapping("/save")
    @Transactional
    public ApiResponse<String> saveVector(@RequestBody VectorSaveRequest request) {
        Users user = usersRepository.findById(authService.getUserId()).orElseThrow();
        DemoSchedule mockDemoSchedule = DemoSchedule.builder()
                .user(user)
                .category(Category.DEFAULT)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(1))
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        demoScheduleRepository.saveAndFlush(mockDemoSchedule);
        log.info("[DB] 저장 : {}", request.getTitle());
        vectorService.saveScheduleVector(mockDemoSchedule);
        log.info("[Vector] 저장 : {}", request.getTitle());
        return ApiResponse.success("Qdrant에 성공적으로 저장되었습니다.");
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Document>>> searchVector(@RequestParam("query") String query) {
        Long userId = authService.getUserId();
        List<Document> results = vectorService.searchScheduleVector(userId, query);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @DeleteMapping("/delete")
    @Transactional
    public ApiResponse<String> deleteVector(@RequestParam("id") Long id) {
        DemoSchedule mockDemoSchedule = demoScheduleRepository.findById(id).orElseThrow();
        String title = mockDemoSchedule.getTitle();

        vectorService.deleteScheduleVector(mockDemoSchedule);
        log.info("[Vector] 삭제 : {}", title);
        demoScheduleRepository.delete(mockDemoSchedule);
        log.info("[DB] 삭제 : {}", title);
        return ApiResponse.success("delete [ID: " + id + "] title : " + title);
    }

    @Getter
    @NoArgsConstructor
    public static class VectorSaveRequest {
        private String title;
        private String description;
    }
}
