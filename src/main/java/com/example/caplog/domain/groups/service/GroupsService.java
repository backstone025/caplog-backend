package com.example.caplog.domain.groups.service;

import com.example.caplog.domain.ai.vector.VectorService;
import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.groups.dto.GroupsGetCategoriesResponse;
import com.example.caplog.domain.groups.dto.GroupsGetGroupListResponse;
import com.example.caplog.domain.groups.dto.GroupsUpdateRequest;
import com.example.caplog.domain.groups.dto.GroupsUpdateResponse;
import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.groups.exception.GroupsException;
import com.example.caplog.domain.groups.repository.GroupsRepository;
import com.example.caplog.domain.groups.type.Category;
import com.example.caplog.domain.schedule.repository.ScheduleRepository;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupsService {
    private final AuthService authService;
    private final GroupsRepository groupsRepository;
    private final VectorService vectorService;

    // #4 그룹/단일 일정 전체 조회 API
    public GroupsGetGroupListResponse getGroups(int page){
        Users user = authService.getCurrentUser();
        int pageSize = 100;

        // 1. PageRequest 생성
        Pageable pageable = PageRequest.of(page, pageSize);

        // 2. DB 조회
        Page<Groups> groupsPage = groupsRepository.findAllByUser(user, pageable);

        // 3. 페이지 범위 유효성 검증
        checkPageRange(groupsPage);

        // 4. DTO 변환 및 반환
        return GroupsGetGroupListResponse.from(groupsPage);
    }

    private void checkPageRange(Page<Groups> groupsPage){
        int page = groupsPage.getNumber();
        int totalPages = groupsPage.getTotalPages();

        // 경우 1. 음수 페이지 요청 시
        // 경우 2. 데이터가 없을 때 (totalPages == 0) -> page가 0보다 크면 에러 (0 허용)
        // 경우 3. 데이터가 있을 때 (totalPages > 0) -> page가 totalPages 이상이면 에러 (0 ~ totalPages-1 허용)
        if (page < 0 || (totalPages == 0 && page > 0) || (totalPages > 0 && page >= totalPages)) {
            throw new GeneralException(GroupsException.GROUP_PAGE_BAD_RANGE);
        }
    }

    // #5 그룹 카테고리 목록 조회 API
    public GroupsGetCategoriesResponse getCategories() {
        List<Category> categories = Arrays.asList(Category.values());
        return new GroupsGetCategoriesResponse(categories);
    }

    // #6 그룹 수정 API
    public GroupsUpdateResponse updateGroups(Long groupId, GroupsUpdateRequest request){
        // 그룹 추출
        Groups group = groupsRepository.findById(groupId)
                .orElseThrow(() -> new GeneralException(GroupsException.GROUP_NOT_FOUND));
        // 해당 그룹이 사용자 소유인지 검증하는 로직 추가
        checkGroupUser(group);

        // 요청에서 그룹 이름 검사
        String title = request.groupName();
        checkGroupsNameFrom(title, groupId);
        // 요청에서 카테고리 추출
        Category category = Category.from(request.category());

        // 그룹 업데이트(더티 체킹)
        group.updateGroups(group.getUser(), title, category);
        return new GroupsUpdateResponse(title, category);
    }

    private void checkGroupsNameFrom(String groupName, Long groupId){
        // 공백 체크
        if(groupName == null || groupName.isBlank()){
            throw new GeneralException(GroupsException.GROUP_NAME_BAD_FORM);
        }
        // 중복 체크(자기 자신 제외)
        if(groupsRepository.existsByTitleAndGroupIdNot(groupName, groupId)){
            throw new GeneralException(GroupsException.GROUP_NAME_ALREADY_EXIST);
        }
    }

    // #7 그룹 삭제 API
    public void  deleteGroups(Long groupId){
        Groups group = groupsRepository.findById(groupId).orElseThrow(
                () -> new GeneralException(GroupsException.GROUP_NOT_FOUND)
        );
        // 해당 그룹이 사용자 소유인지 검증하는 로직 추가
        checkGroupUser(group);

        // NOTE: 이미지 삭제는 당장 구현하지 않음 -> 추후 필요하면 batch 방식을 채택할 계획이다.

        // 연관 그룹 vector DB 삭제
        vectorService.deleteGroupsVector(group);

        // groups-schedule-event Cascade 연쇄 삭제
        groupsRepository.deleteById(groupId);
    }

    // 해당 그룹은 로그인한 사용자의 것인지 검증하는 기능
    private void checkGroupUser(Groups group){
        Long userId = authService.getUserId();
        if(!Objects.equals(group.getGroupId(), userId)){
            throw new GeneralException(GroupsException.GROUP_USER_NOT_LOGIN_USER);
        }
    }
}
