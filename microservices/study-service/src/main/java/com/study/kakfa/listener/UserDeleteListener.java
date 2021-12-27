package com.study.kakfa.listener;


import com.study.kakfa.UserDeleteMessage;
import com.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDeleteListener {

    private final StudyService studyService;

    @KafkaListener(topics = "${kafka.topic.user.delete}",
            groupId = "${kafka.consumer.user.delete}",
            containerFactory = "userDeleteListenerContainerFactory")
    public void userDeleteListen(@Payload UserDeleteMessage userDeleteMessage,
                                 @Headers MessageHeaders messageHeaders) {
        studyService.deleteStudyUserAndWaitUser(userDeleteMessage);
    }
}
