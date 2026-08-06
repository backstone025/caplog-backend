package com.example.caplog.domain.ai.vector;

import com.example.caplog.domain.schedule.ScheduleRepository;
import com.example.caplog.domain.schedule.entity.Schedule;
import com.example.caplog.domain.schedule.type.Category;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.repository.UsersRepository;
import com.example.caplog.domain.users.service.UsersService;
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
    private final UsersService usersService;
    private final UsersRepository usersRepository;
    private final ScheduleRepository scheduleRepository;

    @PostMapping("/save")
    @Transactional
    public ApiResponse<String> saveVector(@RequestBody VectorSaveRequest request) {
        Users user = usersRepository.findById(usersService.getUserId()).orElseThrow();
        Schedule mockSchedule = Schedule.builder()
                .user(user)
                .category(Category.DEFAULT)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(1))
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
        scheduleRepository.saveAndFlush(mockSchedule);
        log.info("[DB] 저장 : {}", request.getTitle());
        vectorService.saveScheduleVector(mockSchedule);
        log.info("[Vector] 저장 : {}", request.getTitle());
        return ApiResponse.success("Qdrant에 성공적으로 저장되었습니다.");
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Document>>> searchVector(@RequestParam("query") String query) {
        Long userId = usersService.getUserId();
        List<Document> results = vectorService.searchScheduleVector(userId, query);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @DeleteMapping("/delete")
    @Transactional
    public ApiResponse<String> deleteVector(@RequestParam("id") Long id) {
        Schedule mockSchedule = scheduleRepository.findById(id).orElseThrow();
        String title = mockSchedule.getTitle();

        vectorService.deleteScheduleVector(mockSchedule);
        log.info("[Vector] 삭제 : {}", title);
        scheduleRepository.delete(mockSchedule);
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
