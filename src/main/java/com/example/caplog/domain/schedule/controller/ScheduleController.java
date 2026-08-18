package com.example.caplog.domain.schedule.controller;

import com.example.caplog.domain.schedule.dto.response.ScheduleDetailsResponse;
import com.example.caplog.domain.schedule.dto.response.ScheduleListResponse;
import com.example.caplog.domain.schedule.service.ScheduleService;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<ScheduleListResponse>> getScheduleList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "TOTAL") String category,
            @RequestParam(defaultValue = "") String searchWords
    ) {

        ScheduleListResponse result =
                scheduleService.getScheduleList(
                        page,
                        category,
                        searchWords
                );

        return ResponseEntity.ok(
                ApiResponse.success(result)
        );
    }

    @GetMapping("/details/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleDetailsResponse>>
    getScheduleDetails(
            @PathVariable Long scheduleId
    ) {

        ScheduleDetailsResponse result =
                scheduleService.getScheduleDetails(
                        scheduleId
                );

        return ResponseEntity.ok(
                ApiResponse.success(result)
        );
    }
}
