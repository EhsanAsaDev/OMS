package com.oms.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.domain.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @author Ehsan Sh
 */

@Component
@Slf4j
public class OrderEventProducer {

    KafkaTemplate<Long,String> kafkaTemplate;

    ObjectMapper objectMapper;

    public OrderEventProducer(KafkaTemplate<Long, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sentOrderEvent(OrderEvent orderEvent) throws JsonProcessingException {

        Long key = orderEvent.getId();
        String value = objectMapper.writeValueAsString(orderEvent);

        ListenableFuture<SendResult<Long,String>> listenableFuture =  kafkaTemplate.sendDefault(key,value);
        listenableFuture.addCallback(new OrderEventListenableFutureCallback(key,value));

    }

}   
