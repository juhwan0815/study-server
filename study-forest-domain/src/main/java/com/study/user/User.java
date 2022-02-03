package com.study.user;

import com.study.common.BaseEntity;
import com.study.userKeyword.UserKeyword;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id; // 회원 ID

    private Long kakaoId; // 카카오 ID

    private String nickName; // 닉네임

    private String ageRange; // 나이대

    private String gender; // 성별

    @Enumerated(EnumType.STRING)
    private UserRole role; // 권한

    private String imageUrl;

    private String fcmToken; // fcmToken

    private Long areaId; // 지역 ID

    private Integer distance; // 검색 거리

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserKeyword> keywords = new ArrayList<>(); // 회원의 키워드들

    public static User createUser(Long kakaoId, String nickName, String ageRange, String gender, UserRole role) {
        User user = new User();
        user.kakaoId = kakaoId;
        user.nickName = nickName;
        user.ageRange = ageRange;
        user.gender = gender;
        user.role = role;
        user.distance = 3;
        return user;
    }

    public void changeImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void changeFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void changeProfile(String nickName) {
        this.nickName = nickName;
    }

    public void changeArea(Long areaId) {
        this.areaId = areaId;
    }

    public void changeDistance(Integer distance) {
        this.distance = distance;
    }

    public void addKeyword(String content) {
        boolean result = keywords.stream().anyMatch(keyword -> keyword.getContent().equals(content));
        if (result) {
            throw new RuntimeException(content + "는 이미 관심 키워드로 추가한 키워드입니다.");
        }

        UserKeyword keyword = UserKeyword.createKeyword(content, this);
        keywords.add(keyword);
    }

    public void deleteKeyword(Long keywordId) {
        UserKeyword findKeyword = keywords.stream()
                .filter(keyword -> keyword.getId().equals(keywordId))
                .findFirst().orElseThrow(() -> new RuntimeException(keywordId + "는 존재하지 않는 키워드 ID 입니다."));

        keywords.remove(findKeyword);
    }


}
