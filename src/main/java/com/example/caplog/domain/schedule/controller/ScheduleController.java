package com.example.caplog.domain.schedule.controller;

import com.example.caplog.domain.schedule.dto.*;
import com.example.caplog.domain.schedule.service.ScheduleService;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<ScheduleListResponse>>
    getSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "TOTAL") String category,
            @RequestParam(defaultValue = "") String searchWords
    ) {

        ScheduleListResponse response =
                scheduleService.getSchedules(
                        page,
                        category,
                        searchWords
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

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

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleUpdateResponse>>
    updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleUpdateRequest request
    ) {

        ScheduleUpdateResponse response =
                scheduleService.updateSchedule(
                        scheduleId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<ScheduleEventRangeResponse>>
    getEventsByDateRange(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDateTime,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDateTime
    ) {

        ScheduleEventRangeResponse response =
                scheduleService.getEventsByDateRange(
                        startDateTime,
                        endDateTime
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>>
    deleteSchedule(
            @PathVariable Long scheduleId
    ) {

        scheduleService.deleteSchedule(scheduleId);

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }
}