package com.study.common;

public class NotFoundException extends RuntimeException {

    public static final String USER_NOT_FOUND = "존재하지 않는 회원입니다.";
    public static final String CATEGORY_NOT_FOUND = "존재하지 않는 카테고리입니다.";
    public static final String AREA_NOT_FOUND = "존재하지 않는 지역입니다.";
    public static final String KEYWORD_NOT_FOUND = "존재하지 않는 키워드입니다.";
    public static final String STUDY_NOT_FOUND = "존재하지 않는 스터디입니다.";

    public NotFoundException(String message) {
        super(message);
    }
}
