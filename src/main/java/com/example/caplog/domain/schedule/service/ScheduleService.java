package com.example.caplog.domain.schedule.service;

import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.event.entity.Event;
import com.example.caplog.domain.event.repository.EventRepository;
import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.groups.repository.GroupsRepository;
import com.example.caplog.domain.groups.type.Category;
import com.example.caplog.domain.images.service.ImagesService;
import com.example.caplog.domain.schedule.dto.ScheduleDeleteResponse;
import com.example.caplog.domain.schedule.dto.ScheduleDetailsResponse;
import com.example.caplog.domain.schedule.dto.ScheduleListResponse;
import com.example.caplog.domain.schedule.dto.ScheduleUpdateRequest;
import com.example.caplog.domain.schedule.dto.ScheduleUpdateResponse;
import com.example.caplog.domain.schedule.entity.Schedule;
import com.example.caplog.domain.schedule.repository.ScheduleRepository;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.global.error.code.GlobalErrorCode;
import com.example.caplog.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.schedule.dto.ScheduleEventRangeResponse;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final EventRepository eventRepository;
    private final AuthService authService;
    private final ImagesService imagesService;
    private final GroupsRepository groupsRepository;

    @Transactional(readOnly = true)
    public ScheduleDetailsResponse getScheduleDetails(Long scheduleId) {

        // 1. 현재 로그인 사용자
        Users currentUser = authService.getCurrentUser();

        // 2. 일정 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new GeneralException(GlobalErrorCode.SCHEDULE_NOT_FOUND)
                );

        // 3. 해당 일정의 Event + Images 조회
        List<Event> events =
                eventRepository.findAllByScheduleWithImages(schedule);

        // 4. 일정 소유자 검증
        validateScheduleOwner(schedule, events, currentUser);

        // 5. 이미지 URL 목록
        List<String> imageUrls = events.stream()
                .map(Event::getImages)
                .filter(Objects::nonNull)
                .distinct()
                .map(imagesService::getUrl)
                .toList();

        // 6. Event 응답 변환
        List<ScheduleDetailsResponse.EventInfo> eventInfos =
                events.stream()
                        .map(event -> {

                            boolean hasDate =
                                    event.getStartAt() != null
                                            || event.getEndAt() != null;

                            return new ScheduleDetailsResponse.EventInfo(
                                    event.getEventId(),
                                    event.getTitle(),
                                    hasDate,
                                    event.getStartAt(),
                                    event.getEndAt(),
                                    event.getDetails()
                            );
                        })
                        .toList();

        // 7. 최종 응답
        return new ScheduleDetailsResponse(
                schedule.getScheduleId(),
                imageUrls,
                schedule.getTitle(),
                schedule.getAiSummary(),
                eventInfos.size(),
                eventInfos
        );
    }


    private void validateScheduleOwner(
            Schedule schedule,
            List<Event> events,
            Users currentUser
    ) {

        /*
         * 1. 그룹에 포함된 일정
         *
         * Schedule → Groups → Users
         */
        Groups group = schedule.getGroups();

        if (group != null) {

            if (!Objects.equals(
                    group.getUser().getUsersId(),
                    currentUser.getUsersId()
            )) {
                throw new GeneralException(
                        GlobalErrorCode.SCHEDULE_NOT_FOUND
                );
            }

            return;
        }

        /*
         * 2. 그룹 없는 일정
         *
         * Schedule → Event → Images → Users
         */
        boolean owner = events.stream()
                .map(Event::getImages)
                .filter(Objects::nonNull)
                .anyMatch(image ->
                        Objects.equals(
                                image.getUser().getUsersId(),
                                currentUser.getUsersId()
                        )
                );

        if (!owner) {
            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_NOT_FOUND
            );
        }
    }

    /**
     * 일정 수정
     */
    @Transactional
    public ScheduleUpdateResponse updateSchedule(
            Long scheduleId,
            ScheduleUpdateRequest request
    ) {

        // 1. 로그인 사용자
        Users user = authService.getCurrentUser();

        // 2. 요청값 검증
        validateUpdateRequest(request);

        ScheduleUpdateRequest.ScheduleInfo requestSchedule =
                request.schedule();

        // 3. 수정 대상 Schedule 조회 + 사용자 검증
        Schedule schedule = scheduleRepository
                .findByScheduleIdAndUser(scheduleId, user)
                .orElseThrow(() ->
                        new GeneralException(
                                GlobalErrorCode.SCHEDULE_NOT_FOUND
                        )
                );

        // 수정 전 그룹 기억
        Groups originalGroup = schedule.getGroups();

        // 4. 카테고리 변환
        Category category = parseCategory(
                requestSchedule.category()
        );

        // 5. Schedule 제목 / AI 요약 수정
        schedule.updateSchedule(
                requestSchedule.title(),
                requestSchedule.aiSummary()
        );

        // 6. 수정 후 들어갈 그룹 결정
        Groups destinationGroup = resolveDestinationGroup(
                schedule,
                originalGroup,
                requestSchedule.groupId(),
                category,
                user
        );

        /*
         * changeGroup()으로 변경된 FK를 DB에 먼저 반영한다.
         * 그래야 기존 그룹에 실제로 몇 개가 남았는지 정확하게 조회 가능.
         */
        scheduleRepository.flush();

        // 7. 기존 그룹이 완전히 비었으면 삭제
        cleanupOriginalGroup(
                originalGroup,
                destinationGroup
        );

        // 8. Event 수정
        List<ScheduleUpdateResponse.EventInfo> eventResponses =
                updateEvents(
                        schedule,
                        request.events()
                );

        scheduleRepository.flush();

        // 9. 최종 그룹에 Schedule이 몇 개 있는지 확인
        long finalScheduleCount =
                scheduleRepository.countByGroups(
                        destinationGroup
                );

        /*
         * 1개면 단일정보
         * 2개 이상이면 실제 그룹
         */
        boolean hasGroup =
                finalScheduleCount >= 2;

        // 10. 응답
        return new ScheduleUpdateResponse(
                new ScheduleUpdateResponse.ScheduleInfo(
                        schedule.getScheduleId(),
                        schedule.getTitle(),
                        schedule.getAiSummary(),
                        destinationGroup.getCategory().name(),
                        hasGroup,
                        destinationGroup.getGroupId(),
                        destinationGroup.getTitle()
                ),
                eventResponses
        );
    }


    /**
     * 수정 후 Schedule이 어느 Groups에 들어갈지 결정
     */
    private Groups resolveDestinationGroup(
            Schedule schedule,
            Groups originalGroup,
            Long targetGroupId,
            Category category,
            Users user
    ) {

        /*
         * 주제를 선택하지 않은 경우
         *
         * → 현재 Schedule을 단일정보로 만든다.
         */
        if (targetGroupId == null) {

            return moveToSingleGroup(
                    schedule,
                    originalGroup,
                    category,
                    user
            );
        }

        // 선택한 주제 Groups 조회
        Groups targetGroup = groupsRepository
                .findByGroupIdAndUser(
                        targetGroupId,
                        user
                )
                .orElseThrow(() ->
                        new GeneralException(
                                GlobalErrorCode.GROUP_NOT_FOUND
                        )
                );

        /*
         * 현재 Schedule이 원래 속해 있던 그룹을
         * 그대로 선택한 경우
         */
        if (targetGroup.getGroupId()
                .equals(originalGroup.getGroupId())) {

            /*
             * 카테고리까지 동일
             *
             * → 그룹 이동 필요 없음
             * → Schedule 제목/내용만 수정
             */
            if (targetGroup.getCategory() == category) {
                return originalGroup;
            }

            /*
             * 같은 그룹인데 카테고리를 변경한 경우
             *
             * 그룹 전체 카테고리를 바꾸면 안 된다.
             *
             * 현재 그룹에 Schedule이 하나뿐이라면
             * 이 정보 자체가 단일정보이므로
             * 기존 Groups.category만 변경.
             *
             * 여러 개라면
             * 현재 Schedule만 빠져서 새 단일정보 Groups 생성.
             */
            return moveToSingleGroup(
                    schedule,
                    originalGroup,
                    category,
                    user
            );
        }

        /*
         * 선택한 주제는 선택한 category에 속해야 한다.
         */
        if (targetGroup.getCategory() != category) {

            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_INVALID_DETAILS
            );
        }

        // 선택한 Groups 안의 Schedule 개수
        long targetScheduleCount =
                scheduleRepository.countByGroups(
                        targetGroup
                );

        /*
         * Schedule 1개
         *
         * → 화면상 단일정보
         * → 수정 대상 Schedule과 합쳐서 새로운 그룹 생성
         */
        if (targetScheduleCount == 1) {

            return mergeWithSingleGroup(
                    schedule,
                    targetGroup,
                    category,
                    user
            );
        }

        /*
         * Schedule 2개 이상
         *
         * → 이미 존재하는 실제 그룹
         * → 해당 그룹으로 이동
         */
        schedule.changeGroup(targetGroup);

        return targetGroup;
    }


    /**
     * Schedule을 단일정보 상태로 만든다.
     */
    private Groups moveToSingleGroup(
            Schedule schedule,
            Groups originalGroup,
            Category category,
            Users user
    ) {

        long originalScheduleCount =
                scheduleRepository.countByGroups(
                        originalGroup
                );

        /*
         * 원래 Groups에 Schedule이 하나뿐이었다면
         *
         * 이미 단일정보 상태이므로
         * 새로운 Groups를 만들 필요가 없다.
         *
         * 정보 제목을 수정했다고
         * Groups.title까지 바꾸지는 않는다.
         *
         * 카테고리만 변경한다.
         */
        if (originalScheduleCount == 1) {

            originalGroup.updateCategory(category);

            return originalGroup;
        }

        /*
         * 기존 그룹에 Schedule이 여러 개 있었다면
         *
         * 현재 Schedule만 빠져나와
         * Schedule 1개짜리 새로운 Groups 생성.
         *
         * 단일정보일 때 Groups.title은 화면에 표시하지 않지만
         * DB 값은 필요하므로 일단 Schedule 제목을 저장.
         */
        Groups newGroup = Groups.createGroups(
                user,
                schedule.getTitle(),
                category
        );

        groupsRepository.save(newGroup);

        schedule.changeGroup(newGroup);

        return newGroup;
    }


    /**
     * 선택한 Groups가 Schedule 1개짜리 단일정보일 경우
     * <p>
     * 기존 단일 Schedule + 수정 Schedule
     * → 새로운 그룹 생성
     */
    private Groups mergeWithSingleGroup(
            Schedule schedule,
            Groups targetGroup,
            Category category,
            Users user
    ) {

        List<Schedule> targetSchedules =
                scheduleRepository.findByGroups(
                        targetGroup
                );

        // 혹시 데이터가 꼬였을 경우 방어
        if (targetSchedules.size() != 1) {

            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_INVALID_DETAILS
            );
        }

        Schedule targetSchedule =
                targetSchedules.get(0);

        // 자기 자신과 묶는 것 방지
        if (targetSchedule.getScheduleId()
                .equals(schedule.getScheduleId())) {

            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_INVALID_DETAILS
            );
        }

        /*
         * 수정 Schedule 제목 + 기존 단일정보 제목을 보고
         * 새로운 그룹명 생성.
         *
         * 지금은 가짜 메서드.
         * 나중에 업로드 확정에서 사용하는 AI 그룹명 생성 코드로 교체.
         */
        String newGroupTitle =
                generateGroupTitle(
                        schedule.getTitle(),
                        targetSchedule.getTitle()
                );

        Groups newGroup =
                Groups.createGroups(
                        user,
                        newGroupTitle,
                        category
                );

        groupsRepository.save(newGroup);

        /*
         * 두 Schedule 모두 새로운 Groups로 이동
         */
        schedule.changeGroup(newGroup);
        targetSchedule.changeGroup(newGroup);

        /*
         * FK 변경 먼저 DB 반영
         */
        scheduleRepository.flush();

        /*
         * targetGroup은 원래 Schedule 1개짜리였고
         * 그 Schedule까지 새 Groups로 이동했으므로
         * 이제 비어 있다.
         */
        groupsRepository.delete(targetGroup);

        return newGroup;
    }


    /**
     * 수정 전 Groups 정리
     */
    private void cleanupOriginalGroup(
            Groups originalGroup,
            Groups destinationGroup
    ) {

        /*
         * 최종 그룹이 원래 그룹과 동일
         *
         * → 아무 작업도 하지 않음.
         */
        if (originalGroup.getGroupId()
                .equals(destinationGroup.getGroupId())) {

            return;
        }

        scheduleRepository.flush();

        /*
         * 수정한 Schedule이 이동한 뒤
         * 원래 그룹에 몇 개가 남았는지 조회
         */
        long remainingCount =
                scheduleRepository.countByGroups(
                        originalGroup
                );

        /*
         * 0개면 Groups 삭제
         */
        if (remainingCount == 0) {

            groupsRepository.delete(
                    originalGroup
            );
        }

        /*
         * 1개가 남더라도 Groups는 삭제하지 않는다.
         *
         * 우리 구조에서는
         * Schedule 1개짜리 Groups = 단일정보이기 때문.
         *
         * 그리고 그룹 제목도 수정하지 않는다.
         * 목록조회에서 Schedule이 1개면
         * Groups.title이 아니라 Schedule.title을 보여준다.
         */
    }


    /**
     * Event 수정
     */
    private List<ScheduleUpdateResponse.EventInfo> updateEvents(
            Schedule schedule,
            List<ScheduleUpdateRequest.EventInfo> requests
    ) {

        if (requests == null) {

            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_INVALID_DETAILS
            );
        }

        return requests.stream()
                .map(request -> {

                    validateEvent(request);

                    /*
                     * 요청으로 들어온 Event가
                     * 해당 Schedule에 실제로 포함된 Event인지 검증
                     */
                    Event event = eventRepository
                            .findByEventIdAndSchedule(
                                    request.id(),
                                    schedule
                            )
                            .orElseThrow(() ->
                                    new GeneralException(
                                            GlobalErrorCode.SCHEDULE_INVALID_DETAILS
                                    )
                            );

                    event.updateEvent(
                            request.title(),
                            request.details(),
                            request.startAt(),
                            request.endAt()
                    );

                    return new ScheduleUpdateResponse.EventInfo(
                            event.getEventId(),
                            event.getTitle(),
                            event.getStartAt(),
                            event.getEndAt(),
                            event.getDetails()
                    );
                })
                .toList();
    }


    /**
     * Event 요청값 검증
     */
    private void validateEvent(
            ScheduleUpdateRequest.EventInfo event
    ) {

        if (event.id() == null
                || event.title() == null
                || event.title().isBlank()) {

            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_INVALID_DETAILS
            );
        }

        /*
         * 날짜 정보가 아닌 일반 정보면
         *
         * startAt = null
         * endAt = null
         *
         * 이어도 정상.
         */

        /*
         * 시작/종료 시간이 둘 다 있을 때만
         * 날짜 순서 확인
         */
        if (event.startAt() != null
                && event.endAt() != null
                && event.endAt()
                .isBefore(event.startAt())) {

            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_INVALID_DETAILS
            );
        }
    }


    /**
     * 전체 수정 요청 검증
     */
    private void validateUpdateRequest(
            ScheduleUpdateRequest request
    ) {

        if (request == null
                || request.schedule() == null
                || request.events() == null) {

            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_INVALID_DETAILS
            );
        }

        ScheduleUpdateRequest.ScheduleInfo schedule =
                request.schedule();

        if (schedule.title() == null
                || schedule.title().isBlank()
                || schedule.category() == null
                || schedule.category().isBlank()) {

            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_INVALID_DETAILS
            );
        }
    }


    /**
     * Category 문자열 → Enum
     */
    private Category parseCategory(
            String category
    ) {

        try {

            return Category.valueOf(
                    category.toUpperCase()
            );

        } catch (Exception e) {

            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_INVALID_DETAILS
            );
        }
    }


    /**
     * 임시 그룹명 생성
     */
    private String generateGroupTitle(
            String firstTitle,
            String secondTitle
    ) {

        /*
         * TODO
         *
         * 업로드 확정 기능에서 사용하고 있는
         * AI 그룹명 생성 코드로 교체.
         */

        return firstTitle
                + " / "
                + secondTitle;
    }

    @Transactional(readOnly = true)
    public ScheduleListResponse getSchedules(
            int page,
            String category,
            String searchWords
    ) {

        // 1. 현재 로그인 사용자
        Users user = authService.getCurrentUser();

        // 2. page 검증
        if (page < 0) {
            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_INVALID_PAGE
            );
        }

        // 3. category 검증 및 변환
        Category selectedCategory = parseListCategory(category);

        // 검색어
        String keyword = searchWords == null
                ? ""
                : searchWords.trim().toLowerCase(Locale.ROOT);

        // 4. 현재 사용자의 모든 Groups 조회
        List<Groups> groups =
                groupsRepository.findByUser(user);

        // 5. 카테고리 필터 + 화면용 데이터 변환
        List<ScheduleListResponse.ListItem> allItems =
                groups.stream()

                        // TOTAL이면 전체
                        // 아니면 선택한 카테고리만
                        .filter(group ->
                                selectedCategory == null
                                        || group.getCategory() == selectedCategory
                        )

                        // Groups를 단일정보/그룹정보로 판단해서 DTO 변환
                        .map(this::convertToListItem)

                        // 비어있는 Groups 방어
                        .filter(Objects::nonNull)

                        // 검색어 필터
                        .filter(item ->
                                matchesSearch(item, keyword)
                        )

                        .toList();


        // 6. 페이징
        int pageSize = 10;
        int totalElements = allItems.size();

        int totalPage = (int) Math.ceil(
                (double) totalElements / pageSize
        );

        /*
         * 데이터가 하나도 없는 경우
         *
         * page=0 → 정상
         * page=1 이상 → 잘못된 페이지
         */
        if (totalElements == 0) {

            if (page != 0) {
                throw new GeneralException(
                        GlobalErrorCode.SCHEDULE_INVALID_PAGE
                );
            }

            return new ScheduleListResponse(
                    new ScheduleListResponse.PageInfo(
                            0,
                            0
                    ),
                    List.of()
            );
        }

        // 존재하지 않는 페이지
        if (page >= totalPage) {
            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_INVALID_PAGE
            );
        }

        int fromIndex = page * pageSize;

        int toIndex = Math.min(
                fromIndex + pageSize,
                totalElements
        );

        List<ScheduleListResponse.ListItem> pagedItems =
                allItems.subList(
                        fromIndex,
                        toIndex
                );

        // 7. 응답
        return new ScheduleListResponse(
                new ScheduleListResponse.PageInfo(
                        totalPage,
                        page
                ),
                pagedItems
        );
    }

    private ScheduleListResponse.ListItem convertToListItem(
            Groups group
    ) {

        // 해당 Groups 안에 들어있는 Schedule들
        List<Schedule> schedules =
                scheduleRepository.findByGroups(group);

        // 비어있는 그룹은 목록에 표시하지 않음
        if (schedules.isEmpty()) {
            return null;
        }

        /*
         * Schedule 1개
         * → 화면상 단일정보
         *
         * Schedule 2개 이상
         * → 화면상 그룹
         */
        boolean isGroup =
                schedules.size() >= 2;


        /*
         * id
         *
         * 그룹이면 groupId
         * 단일정보면 scheduleId
         */
        Long id;

        /*
         * 화면 제목
         *
         * 그룹이면 Groups.title
         * 단일정보면 Schedule.title
         */
        String title;

        if (isGroup) {

            id = group.getGroupId();
            title = group.getTitle();

        } else {

            Schedule singleSchedule =
                    schedules.get(0);

            id = singleSchedule.getScheduleId();
            title = singleSchedule.getTitle();
        }


        /*
         * NEW 여부
         *
         * 그룹이면 내부 Schedule 중 하나라도 NEW면 true
         * 단일정보면 해당 Schedule 기준
         */
        boolean isNew =
                schedules.stream()
                        .anyMatch(this::isScheduleNew);


        /*
         * 대표 이미지 한 장
         */
        String captureImg =
                findRepresentativeImage(schedules);


        return new ScheduleListResponse.ListItem(
                isGroup,
                id,
                isNew,
                schedules.size(),
                captureImg,
                title,
                group.getCategory().name()
        );
    }

    private String findRepresentativeImage(
            List<Schedule> schedules
    ) {

        for (Schedule schedule : schedules) {

            List<Event> events =
                    eventRepository.findBySchedule(schedule);

            for (Event event : events) {

                if (event.getImages() == null) {
                    continue;
                }

                String imageUrl =
                        imagesService.getUrl(
                                event.getImages()
                        );

                if (imageUrl != null) {
                    return imageUrl;
                }
            }
        }

        // 이미지가 없으면 null
        return null;
    }

    private boolean isScheduleNew(
            Schedule schedule
    ) {

        // 아직 한 번도 상세조회하지 않은 정보
        if (schedule.getViewedAt() == null) {
            return true;
        }

        // 마지막으로 본 이후 수정됨
        return schedule.getUpdatedAt() != null
                && schedule.getUpdatedAt()
                .isAfter(schedule.getViewedAt());
    }
    private boolean matchesSearch(
            ScheduleListResponse.ListItem item,
            String keyword
    ) {

        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        if (item.title() == null) {
            return false;
        }

        return item.title()
                .toLowerCase(Locale.ROOT)
                .contains(keyword);
    }


    private Category parseListCategory(
            String category
    ) {

        if (category == null
                || category.isBlank()) {

            throw new GeneralException(
                    GlobalErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (category.equalsIgnoreCase(
                "TOTAL"
        )) {

            return null;
        }

        try {

            return Category.valueOf(
                    category.toUpperCase()
            );

        } catch (IllegalArgumentException e) {

            throw new GeneralException(
                    GlobalErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    @Transactional
    public ScheduleDeleteResponse deleteSchedule(Long scheduleId) {

        // 1. 현재 로그인 사용자
        Users user = authService.getCurrentUser();

        // 2. 본인의 Schedule 조회
        Schedule schedule = scheduleRepository
                .findByScheduleIdAndUser(scheduleId, user)
                .orElseThrow(() ->
                        new GeneralException(
                                GlobalErrorCode.SCHEDULE_DELETE_NOT_FOUND
                        )
                );

        // 3. 현재 그룹 기억
        Groups group = schedule.getGroups();

        // 4. Schedule에 연결된 Event 조회
        List<Event> events =
                eventRepository.findBySchedule(schedule);

        // 5. Event가 가지고 있는 이미지 저장
        List<Images> images = events.stream()
                .map(Event::getImages)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        /*
         * 6. Schedule 삭제
         *
         * Event는 Schedule FK의 ON DELETE CASCADE로 같이 삭제
         */
        scheduleRepository.delete(schedule);
        scheduleRepository.flush();

        /*
         * 7. 이미지 삭제
         *
         * 이미지 1개 = Schedule 1개가 보장되므로
         * 다른 곳에서 사용하는지 별도 검사하지 않음.
         */
        for (Images image : images) {
            imagesService.delete(image.getImageId());
        }

        /*
         * 8. 그룹 안에 Schedule이 몇 개 남았는지 확인
         */
        long remainingScheduleCount =
                scheduleRepository.countByGroups(group);

        /*
         * 0개면 원래 단일정보였던 것.
         * 빈 Groups도 삭제.
         *
         * 1개 이상이면 Groups 유지.
         * 1개가 남으면 목록에서 단일정보로 보여줌.
         */
        if (remainingScheduleCount == 0) {
            groupsRepository.delete(group);
        }

        return ScheduleDeleteResponse.success();
    }

    //날짜 범위 내 이벤트 반환
    @Transactional(readOnly = true)
    public ScheduleEventRangeResponse getEventsByDateRange(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {

        // 1. 날짜 검증
        validateDateRange(
                startDateTime,
                endDateTime
        );

        // 2. 로그인 사용자
        Users user =
                authService.getCurrentUser();

        // 3. 범위 내 Event 전체 조회
        List<Event> events =
                eventRepository.findEventsByDateRange(
                        user,
                        startDateTime,
                        endDateTime
                );

        // 4. 날짜별 Event 개수 계산
        Map<LocalDate, Long> dateCountMap =
                new LinkedHashMap<>();

        for (Event event : events) {

            LocalDate date =
                    event.getStartAt()
                            .toLocalDate();

            dateCountMap.put(
                    date,
                    dateCountMap.getOrDefault(
                            date,
                            0L
                    ) + 1
            );
        }

        // 5. dateCounts 응답 변환
        List<ScheduleEventRangeResponse.DateCount> dateCounts =
                dateCountMap.entrySet()
                        .stream()
                        .map(entry ->
                                new ScheduleEventRangeResponse.DateCount(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();

        // 6. Event 응답 변환
        List<ScheduleEventRangeResponse.EventInfo> eventInfos =
                events.stream()
                        .map(event -> {

                            String captureImg = null;

                            if (event.getImages() != null) {
                                captureImg =
                                        imagesService.getUrl(
                                                event.getImages()
                                        );
                            }

                            return new ScheduleEventRangeResponse.EventInfo(
                                    event.getSchedule().getScheduleId(),
                                    event.getEventId(),
                                    event.getStartAt(),
                                    captureImg,
                                    event.getTitle()
                            );
                        })
                        .toList();

        // 7. 최종 응답
        return new ScheduleEventRangeResponse(
                startDateTime,
                endDateTime,
                dateCounts,
                eventInfos
        );
    }

    private void validateDateRange(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {

        if (startDateTime == null
                || endDateTime == null
                || startDateTime.isAfter(endDateTime)) {

            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_INVALID_DATE_RANGE
            );
        }
    }

}