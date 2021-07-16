package com.study.studyservice.kafka.producer;

import com.study.studyservice.kafka.config.KafkaConfig;
import com.study.studyservice.kafka.message.StudyApplyCancelMessage;
import com.study.studyservice.kafka.message.StudyApplyCreateMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@EnableKafka
@Configuration
public class StudyApplyCancelProducerConfig {

    @Value("${kafka.server}")
    private String kafkaServer;

    @Bean(name = "studyApplyCancelProducerFactory")
    public ProducerFactory<String, StudyApplyCancelMessage> producerFactory(){
        Map<String, Object> properties = KafkaConfig.producerFactory(kafkaServer);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean(name = "studyApplyCancelKafkaTemplate")
    public KafkaTemplate<String, StudyApplyCancelMessage> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }

}
