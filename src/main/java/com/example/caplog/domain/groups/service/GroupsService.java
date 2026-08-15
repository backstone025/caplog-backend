package com.example.caplog.domain.groups.service;

import com.example.caplog.domain.groups.dto.GroupsUpdateRequest;
import com.example.caplog.domain.groups.dto.GroupsUpdateResponse;
import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.groups.exception.GroupsException;
import com.example.caplog.domain.groups.repository.GroupsRepository;
import com.example.caplog.domain.groups.type.Category;
import com.example.caplog.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupsService {
    private final GroupsRepository groupsRepository;

    public GroupsUpdateResponse updateGroups(Long groupId, GroupsUpdateRequest request){
        // 그룹 추출
        Groups group = groupsRepository.findById(groupId)
                .orElseThrow(() -> new GeneralException(GroupsException.GROUP_NOT_FOUND));
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
}
