package com.example.caplog.domain.schedule.controller;

import com.example.caplog.domain.schedule.dto.ScheduleDetailsResponse;
import com.example.caplog.domain.schedule.service.ScheduleService;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/details/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleDetailsResponse>>
    getScheduleDetails(
            @PathVariable Long scheduleId
    ) {

        ScheduleDetailsResponse response =
                scheduleService.getScheduleDetails(scheduleId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}