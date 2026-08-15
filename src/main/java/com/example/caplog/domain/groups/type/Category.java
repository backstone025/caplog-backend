package com.example.caplog.domain.groups.type;

import com.example.caplog.domain.groups.exception.GroupsException;
import com.example.caplog.global.error.exception.GeneralException;

import java.util.Arrays;

public enum Category {
    TOTAL,  // 전체 그룹(검색 전용 타입이므로 생성시 넣지 않도록 한다.)
    STUDY,  // 공부
    SCHOOL, // 학교
    DAILY,  // 일상
    ETC     // 기타
    ;

    public static Category from(String category) {
        if (category == null) {
            return Category.ETC;    // 입력이 없을 경우, 기본적으로 기타로 넣는다.
        }
        return Arrays.stream(Category.values())
                .filter(c -> c.name().equalsIgnoreCase(category))
                .findFirst()
                .orElseThrow(() -> new GeneralException(GroupsException.GROUP_CATEGORY_BAD_FORM));
    }
}
