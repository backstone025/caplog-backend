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

    // 해당 타입이 포함되어 있는지 확인하는 로직
    public static boolean isContain(String profileImageType){
        // 받은 타입의 문자열의 공백 확인
        if(profileImageType == null || profileImageType.isBlank()){
            return false;
        }

        // 대응되는 타입이 있는지 검사
        for(ProfileImage profileImage : ProfileImage.values()){
            if(profileImage.name().equals(profileImageType)){
                return true;
            }
        }
        return false;
    }
}
