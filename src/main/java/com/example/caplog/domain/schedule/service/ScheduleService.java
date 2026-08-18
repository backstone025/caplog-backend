package com.example.caplog.domain.schedule.service;

import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.event.entity.Event;
import com.example.caplog.domain.event.repository.EventRepository;
import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.groups.repository.GroupsRepository;
import com.example.caplog.domain.groups.type.Category;
import com.example.caplog.domain.schedule.dto.request.ScheduleUpdateRequest;
import com.example.caplog.domain.schedule.dto.response.ScheduleListResponse;
import com.example.caplog.domain.schedule.dto.response.ScheduleUpdateResponse;
import com.example.caplog.domain.schedule.entity.Schedule;
import com.example.caplog.domain.schedule.repository.ScheduleRepository;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.global.S3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.caplog.domain.schedule.dto.response.ScheduleDetailsResponse;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private static final int PAGE_SIZE = 10;

    private final AuthService authService;
    private final ScheduleRepository scheduleRepository;
    private final GroupsRepository groupsRepository;
    private final EventRepository eventRepository;
    private final S3Service s3Service;

    /**
     * 카테고리 + 검색어 기반
     * 단일 Schedule + Groups 통합 목록 조회
     */
    public ScheduleListResponse getScheduleList(
            int page,
            String category,
            String searchWords
    ) {

        Users user = authService.getCurrentUser();

        String keyword =
                searchWords == null
                        ? ""
                        : searchWords.trim();

        List<Schedule> singleSchedules;
        List<Groups> groups;

        /*
         * 1. TOTAL이면 카테고리 조건 없이 조회
         */
        if ("TOTAL".equalsIgnoreCase(category)) {

            singleSchedules =
                    scheduleRepository.findSingleSchedules(
                            user,
                            keyword
                    );

            groups =
                    groupsRepository.findGroups(
                            user,
                            keyword
                    );

        } else {

            Category categoryEnum;

            try {
                categoryEnum =
                        Category.valueOf(
                                category.toUpperCase()
                        );
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "유효하지 않은 카테고리입니다."
                );
            }

            singleSchedules =
                    scheduleRepository.findSingleSchedulesByCategory(
                            user,
                            categoryEnum,
                            keyword
                    );

            groups =
                    groupsRepository.findGroupsByCategory(
                            user,
                            categoryEnum,
                            keyword
                    );
        }

        /*
         * 2. 단일 Schedule + Group을
         * 공통 목록 타입으로 합침
         */
        List<ListSource> sources =
                new ArrayList<>();

        for (Schedule schedule : singleSchedules) {

            sources.add(
                    new ListSource(
                            false,
                            schedule.getScheduleId(),
                            schedule.getCreatedAt(),
                            schedule.getUpdatedAt()
                    )
            );
        }

        for (Groups group : groups) {

            sources.add(
                    new ListSource(
                            true,
                            group.getGroupId(),
                            group.getCreatedAt(),
                            group.getUpdatedAt()
                    )
            );
        }

        /*
         * 3. 최신순 정렬
         *
         * 단일 일정:
         * updatedAt이 있으면 updatedAt,
         * 없으면 createdAt
         *
         * 그룹:
         * 새로운 정보가 추가되면
         * group.updatedAt 갱신되므로 위로 올라옴
         */
        sources.sort(
                Comparator.comparing(
                        ListSource::sortTime,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                ).reversed()
        );

        /*
         * 4. 페이징
         */
        int totalElements =
                sources.size();

        int totalPage =
                (int) Math.ceil(
                        (double) totalElements / PAGE_SIZE
                );

        validatePage(
                page,
                totalPage
        );

        int start =
                page * PAGE_SIZE;

        int end =
                Math.min(
                        start + PAGE_SIZE,
                        totalElements
                );

        List<ListSource> pageSources =
                sources.subList(
                        start,
                        end
                );

        /*
         * 5. 실제 응답 DTO 변환
         */
        List<ScheduleListResponse.ListItem> responseItems =
                pageSources.stream()
                        .map(this::toListItem)
                        .toList();

        return new ScheduleListResponse(
                new ScheduleListResponse.PageInfo(
                        totalPage,
                        page
                ),
                responseItems
        );
    }

    /**
     * 단일 / 그룹 분기
     */
    private ScheduleListResponse.ListItem toListItem(
            ListSource source
    ) {

        if (source.isGroup()) {
            return toGroupItem(
                    source.id()
            );
        }

        return toSingleItem(
                source.id()
        );
    }

    /**
     * 단일 Schedule 카드 변환
     */
    private ScheduleListResponse.ListItem toSingleItem(
            Long scheduleId
    ) {

        Schedule schedule =
                scheduleRepository.findById(
                                scheduleId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "일정을 찾을 수 없습니다."
                                )
                        );

        List<Event> events =
                eventRepository.findBySchedule(
                        schedule
                );

        /*
         * Event 정보
         */
        List<ScheduleListResponse.EventInfo> eventInfos =
                events.stream()
                        .map(event ->
                                new ScheduleListResponse.EventInfo(
                                        event.getEventId(),
                                        event.getTitle(),
                                        formatDateTime(
                                                event.getStartAt()
                                        ),
                                        buildEventDetails(
                                                event
                                        )
                                )
                        )
                        .toList();

        /*
         * 이미지
         *
         * 현재 Event -> Images 관계를 이용해서
         * Schedule의 캡처 이미지 조회
         */
        List<ScheduleListResponse.PictureInfo> pictures =
                events.stream()
                        .filter(event ->
                                event.getImages() != null
                        )
                        .map(event ->
                                new ScheduleListResponse.PictureInfo(
                                        s3Service.getUrl(
                                                event.getImages()
                                                        .getImageKey()
                                        )
                                )
                        )
                        .distinct()
                        .toList();

        return new ScheduleListResponse.ListItem(
                false,
                schedule.getScheduleId(),
                schedule.isNew(),
                1,
                pictures,

                new ScheduleListResponse.ScheduleInfo(
                        schedule.getTitle(),
                        schedule.getAiSummary(),
                        false,
                        null
                ),

                eventInfos
        );
    }

    /**
     * 그룹 카드 변환
     */
    private ScheduleListResponse.ListItem toGroupItem(
            Long groupId
    ) {

        Groups group =
                groupsRepository.findById(
                                groupId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "그룹을 찾을 수 없습니다."
                                )
                        );

        /*
         * 그룹 내부 Schedule 전체 조회
         */
        List<Schedule> schedules =
                scheduleRepository
                        .findByGroupsGroupId(
                                groupId
                        );

        /*
         * 그룹 내부의 모든 Event 조회
         */
        List<Event> events =
                schedules.stream()
                        .flatMap(schedule ->
                                eventRepository
                                        .findBySchedule(
                                                schedule
                                        )
                                        .stream()
                        )
                        .toList();

        /*
         * 그룹 카드에 보여줄 이미지들
         */
        List<ScheduleListResponse.PictureInfo> pictures =
                events.stream()
                        .filter(event ->
                                event.getImages() != null
                        )
                        .map(event ->
                                new ScheduleListResponse.PictureInfo(
                                        s3Service.getUrl(
                                                event.getImages()
                                                        .getImageKey()
                                        )
                                )
                        )
                        .distinct()
                        .toList();

        /*
         * 그룹의 Event 정보
         */
        List<ScheduleListResponse.EventInfo> eventInfos =
                events.stream()
                        .map(event ->
                                new ScheduleListResponse.EventInfo(
                                        event.getEventId(),
                                        event.getTitle(),
                                        formatDateTime(
                                                event.getStartAt()
                                        ),
                                        buildEventDetails(
                                                event
                                        )
                                )
                        )
                        .toList();

        /*
         * 그룹 대표 AI 요약
         *
         * 지금 Groups에는 aiSummary가 없으므로
         * 그룹 내부 Schedule 중 첫 번째 aiSummary를 사용.
         *
         * 추후 Groups에 aiSummary를 직접 저장한다면
         * group.getAiSummary()로 변경하는 게 더 좋음.
         */
        String aiSummary =
                schedules.stream()
                        .map(
                                Schedule::getAiSummary
                        )
                        .filter(
                                Objects::nonNull
                        )
                        .filter(summary ->
                                !summary.isBlank()
                        )
                        .findFirst()
                        .orElse(null);

        return new ScheduleListResponse.ListItem(
                true,
                group.getGroupId(),
                group.isNew(),
                schedules.size(),
                pictures,

                new ScheduleListResponse.ScheduleInfo(
                        group.getTitle(),
                        aiSummary,
                        true,

                        new ScheduleListResponse.GroupInfo(
                                group.getGroupId(),
                                group.getTitle()
                        )
                ),

                eventInfos
        );
    }

    /**
     * Event의 장소 + details를 API용 문자열로 합침
     */
    private String buildEventDetails(
            Event event
    ) {

        List<String> parts =
                new ArrayList<>();

        if (event.getLocation() != null
                && !event.getLocation().isBlank()) {

            parts.add(
                    "장소: "
                            + event.getLocation()
            );
        }

        if (event.getDetails() != null
                && !event.getDetails().isBlank()) {

            parts.add(
                    event.getDetails()
            );
        }

        return parts.isEmpty()
                ? null
                : String.join(
                " / ",
                parts
        );
    }

    /**
     * LocalDateTime -> API 응답 문자열
     */
    private String formatDateTime(
            LocalDateTime dateTime
    ) {

        if (dateTime == null) {
            return null;
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd HH:mm:ss"
                );

        return dateTime.format(
                formatter
        );
    }

    /**
     * 잘못된 페이지 검사
     */
    private void validatePage(
            int page,
            int totalPage
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "입력한 페이지는 범위 내 유효하지 않습니다."
            );
        }

        /*
         * 데이터가 하나도 없을 경우
         * page=0은 빈 결과로 허용
         */
        if (totalPage == 0) {

            if (page > 0) {
                throw new IllegalArgumentException(
                        "입력한 페이지는 범위 내 유효하지 않습니다."
                );
            }

            return;
        }

        if (page >= totalPage) {
            throw new IllegalArgumentException(
                    "입력한 페이지는 범위 내 유효하지 않습니다."
            );
        }
    }

    /**
     * 단일 Schedule과 Group을
     * 동일한 목록에서 정렬하기 위한 내부 DTO
     */
    private record ListSource(
            boolean isGroup,
            Long id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public LocalDateTime sortTime() {

            return updatedAt != null
                    ? updatedAt
                    : createdAt;
        }
    }

    //단일일정 상세 조회
    @Transactional
    public ScheduleDetailsResponse getScheduleDetails(
            Long scheduleId
    ) {

        Users user =
                authService.getCurrentUser();

        // 1. 로그인한 사용자의 일정 조회
        Schedule schedule =
                scheduleRepository
                        .findByScheduleIdAndUser(
                                scheduleId,
                                user
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 일정입니다."
                                )
                        );

        // 2. 일정에 연결된 Event 조회
        List<Event> events =
                eventRepository.findBySchedule(
                        schedule
                );

        // 3. Event -> Images -> 이미지 URL
        List<String> imageUrls =
                events.stream()
                        .filter(event ->
                                event.getImages() != null
                        )
                        .map(event ->
                                s3Service.getUrl(
                                        event.getImages()
                                                .getImageKey()
                                )
                        )
                        .distinct()
                        .toList();

        // 4. 상세 조회했으므로 NEW 해제
        schedule.markAsViewed();

        // 5. 응답
        return ScheduleDetailsResponse.from(
                schedule,
                events,
                imageUrls
        );
    }

    @Transactional
    public ScheduleUpdateResponse updateSchedule(
            Long scheduleId,
            ScheduleUpdateRequest request
    ) {

        Users user =
                authService.getCurrentUser();

        // 1. 본인의 Schedule 조회
        Schedule schedule =
                scheduleRepository
                        .findByScheduleIdAndUser(
                                scheduleId,
                                user
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 일정입니다."
                                )
                        );

        validateUpdateRequest(request);

        // 2. 그룹 처리
        Groups group = null;

        if (Boolean.TRUE.equals(
                request.schedule().hasGroup()
        )) {

            if (request.schedule().groupId() == null) {
                throw new IllegalArgumentException(
                        "그룹 아이디가 필요합니다."
                );
            }

            group = groupsRepository
                    .findById(
                            request.schedule().groupId()
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "존재하지 않는 그룹입니다."
                            )
                    );

            // 다른 사용자의 그룹에 넣지 못하게 검증
            if (!group.getUser()
                    .getUsersId()
                    .equals(user.getUsersId())) {

                throw new IllegalArgumentException(
                        "유효하지 않은 그룹입니다."
                );
            }
        }

        Category category;

        try {
            category = Category.valueOf(
                    request.schedule()
                            .category()
                            .toUpperCase()
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "유효하지 않은 카테고리입니다."
            );

        }

        // 3. Schedule 수정
        schedule.updateSchedule(
                group,
                request.schedule().title(),
                request.schedule().aiSummary(),
                category
        );

        // 4. Event 수정
        if (request.events() != null) {

            for (ScheduleUpdateRequest.EventInfo eventRequest
                    : request.events()) {

                Event event =
                        eventRepository
                                .findByEventIdAndSchedule(
                                        eventRequest.id(),
                                        schedule
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "존재하지 않는 이벤트입니다."
                                        )
                                );

                event.updateEvent(
                        eventRequest.title(),
                        eventRequest.location(),
                        eventRequest.details(),
                        parseDateTime(
                                eventRequest.startAt()
                        ),
                        parseDateTime(
                                eventRequest.endAt()
                        )
                );
            }
        }

        // 5. 수정된 Event 다시 조회
        List<Event> events =
                eventRepository.findBySchedule(
                        schedule
                );

        return ScheduleUpdateResponse.from(
                schedule,
                events
        );
    }

    private LocalDateTime parseDateTime(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(
                    value,
                    DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd HH:mm:ss"
                    )
            );

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "올바르지 않은 날짜 형식입니다."
            );
        }
    }

    private void validateUpdateRequest(
            ScheduleUpdateRequest request
    ) {

        if (request == null
                || request.schedule() == null
                || request.schedule().title() == null
                || request.schedule().title().isBlank()) {

            throw new IllegalArgumentException(
                    "입력하신 상세 정보에 유효하지 않은 값이 포함되어 있습니다."
            );
        }
    }
}