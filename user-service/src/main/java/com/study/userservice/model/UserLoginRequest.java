package com.study.userservice.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserLoginRequest {

    private Long kakaoId; // 카카오 Id;

    private String nickName; // 닉네임

    // TODO 이미지가 필수적으로 있는지 확인
    private String thumbnailImage; // 썸네일 이미지

    // TODO 이미지가 필수적으로 있는지 확인
    private String profileImage; // 프로필 이미지

    private String ageRange;

    private String gender;
}
