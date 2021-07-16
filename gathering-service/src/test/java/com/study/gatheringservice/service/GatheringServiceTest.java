package com.study.gatheringservice.service;

import com.study.gatheringservice.GatheringFixture;
import com.study.gatheringservice.client.StudyServiceClient;
import com.study.gatheringservice.domain.Shape;
import com.study.gatheringservice.exception.GatheringException;
import com.study.gatheringservice.model.gathering.GatheringResponse;
import com.study.gatheringservice.model.studyuser.StudyUserResponse;
import com.study.gatheringservice.repository.GatheringRepository;
import com.study.gatheringservice.service.impl.GatheringServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.study.gatheringservice.GatheringFixture.*;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class GatheringServiceTest {

    @InjectMocks
    private GatheringServiceImpl gatheringService;

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private StudyServiceClient studyServiceClient;

    @Test
    @DisplayName("온라인 모임을 생성한다.")
    void createIfOnlineStudy(){
        // given
        List<StudyUserResponse> studyUsers = Arrays.asList(TEST_STUDY_USER_RESPONSE1, TEST_STUDY_USER_RESPONSE2);

        given(studyServiceClient.findStudyUserByStudyId(any()))
            .willReturn(studyUsers);

        given(gatheringRepository.save(any()))
                .willReturn(createOnlineGathering());

        // when
        GatheringResponse result = gatheringService.create(1L, 1L, TEST_GATHERING_CREATE_REQUEST1);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getGatheringTime()).isEqualTo(TEST_LOCAL_DATE_TIME1);
        assertThat(result.getContent()).isEqualTo(TEST_GATHERING_CREATE_REQUEST1.getContent());
        assertThat(result.getStudyId()).isEqualTo(1L);
        assertThat(result.getShape()).isEqualTo(Shape.ONLINE);
        assertThat(result.getNumberOfPeople()).isEqualTo(1);
        assertThat(result.getPlace()).isNull();

        then(studyServiceClient).should(times(1)).findStudyUserByStudyId(any());
        then(gatheringRepository).should(times(1)).save(any());
    }

    @Test
    @DisplayName("오프라인 모임을 생성한다.")
    void createIfOfflineStudy(){
        // given
        List<StudyUserResponse> studyUsers = Arrays.asList(TEST_STUDY_USER_RESPONSE1, TEST_STUDY_USER_RESPONSE2);

        given(studyServiceClient.findStudyUserByStudyId(any()))
                .willReturn(studyUsers);

        given(gatheringRepository.save(any()))
                .willReturn(createOfflineGathering());

        // when
        GatheringResponse result = gatheringService.create(1L, 1L, TEST_GATHERING_CREATE_REQUEST2);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getGatheringTime()).isEqualTo(TEST_LOCAL_DATE_TIME1);
        assertThat(result.getContent()).isEqualTo(TEST_GATHERING_CREATE_REQUEST2.getContent());
        assertThat(result.getStudyId()).isEqualTo(1L);
        assertThat(result.getShape()).isEqualTo(Shape.OFFLINE);
        assertThat(result.getNumberOfPeople()).isEqualTo(1);
        assertThat(result.getPlace().getName()).isEqualTo(TEST_GATHERING_CREATE_REQUEST2.getPlaceName());
        assertThat(result.getPlace().getLen()).isEqualTo(TEST_GATHERING_CREATE_REQUEST2.getLen());
        assertThat(result.getPlace().getLet()).isEqualTo(TEST_GATHERING_CREATE_REQUEST2.getLet());

        then(studyServiceClient).should(times(1)).findStudyUserByStudyId(any());
        then(gatheringRepository).should(times(1)).save(any());
    }

    @Test
    @DisplayName("예외 테스트 : 스터디 참가 인원이 아닌 회원이 모임을 생성할 경우 예외가 발생한다.")
    void createWhenLoginUserIsNotStudyUser(){
        // given
        List<StudyUserResponse> studyUsers = Arrays.asList(TEST_STUDY_USER_RESPONSE1, TEST_STUDY_USER_RESPONSE2);

        given(studyServiceClient.findStudyUserByStudyId(any()))
                .willReturn(studyUsers);

        // when
        assertThrows(GatheringException.class,
                ()->gatheringService.create(3L, 1L, TEST_GATHERING_CREATE_REQUEST2));
    }

    @Test
    @DisplayName("오프라인 모임에서 온라임 모임으로 수정한다.")
    void updateOfflineToOnline(){
        // given
        given(gatheringRepository.findWithGatheringUsersById(any()))
                .willReturn(Optional.of(createOfflineGathering()));

        // when
        GatheringResponse result = gatheringService.update(1L, 1L, TEST_GATHERING_UPDATE_REQUEST1);

        // then
        assertThat(result.getGatheringTime()).isEqualTo(TEST_GATHERING_UPDATE_REQUEST1.getGatheringTime());
        assertThat(result.getShape()).isEqualTo(Shape.ONLINE);
        assertThat(result.getContent()).isEqualTo(TEST_GATHERING_UPDATE_REQUEST1.getContent());
        assertThat(result.getPlace()).isNull();

        then(gatheringRepository).should(times(1)).findWithGatheringUsersById(any());
    }

    @Test
    @DisplayName("온라인 모임에서 오프라인 모임을 수정한다.")
    void updateOnlineToOffline(){
        // given
        given(gatheringRepository.findWithGatheringUsersById(any()))
                .willReturn(Optional.of(createOnlineGathering()));

        // when
        GatheringResponse result = gatheringService.update(1L, 1L, TEST_GATHERING_UPDATE_REQUEST2);

        // then
        assertThat(result.getGatheringTime()).isEqualTo(TEST_GATHERING_UPDATE_REQUEST2.getGatheringTime());
        assertThat(result.getShape()).isEqualTo(Shape.OFFLINE);
        assertThat(result.getContent()).isEqualTo(TEST_GATHERING_UPDATE_REQUEST2.getContent());
        assertThat(result.getPlace().getName()).isEqualTo(TEST_GATHERING_UPDATE_REQUEST2.getPlaceName());
        assertThat(result.getPlace().getLen()).isEqualTo(TEST_GATHERING_UPDATE_REQUEST2.getLen());
        assertThat(result.getPlace().getLet()).isEqualTo(TEST_GATHERING_UPDATE_REQUEST2.getLet());

        then(gatheringRepository).should(times(1)).findWithGatheringUsersById(any());
    }

    @Test
    @DisplayName("예외 테스트 : 모임 등록자가 아닌 유저가 모임을 수정할 경우 예외가 발생한다.")
    void updateWhenIsNotRegister(){
        // given
        given(gatheringRepository.findWithGatheringUsersById(any()))
                .willReturn(Optional.of(createOnlineGathering()));

        // when
        assertThrows(GatheringException.class,()->gatheringService.update(2L, 1L, TEST_GATHERING_UPDATE_REQUEST2));

    }



}
