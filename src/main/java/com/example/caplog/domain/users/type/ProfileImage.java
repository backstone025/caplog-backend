package com.example.caplog.domain.users.type;

import java.util.concurrent.ThreadLocalRandom;

public enum ProfileImage {
    BLUE("users/profileImg/blue.png"),
    GREEN("users/profileImg/green.png"),
    ORANGE("users/profileImg/orange.png");

    private final String key;   // 이미지 URL 추출용 key

    ProfileImage(String key){
        this.key = key;
    }

    // URL 추출용 key를 조회하는 메소드
    public String getKey() {
        return key;
    }

    // 무작위로 프로필 이미지 타입 지정해주는 메소드
    public static ProfileImage randomSet() {
        ProfileImage[] values = ProfileImage.values();
        int randomIndex = ThreadLocalRandom.current().nextInt(values.length);
        return values[randomIndex];
    }
}
