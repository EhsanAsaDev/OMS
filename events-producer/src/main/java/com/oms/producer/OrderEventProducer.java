package com.oms.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.domain.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Ehsan Sh
 */

@Component
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<Long,String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public OrderEventProducer(KafkaTemplate<Long, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    //Async produce
    public void sentOrderEvent(OrderEvent orderEvent) throws JsonProcessingException {

        Long key = orderEvent.getId();
        String value = objectMapper.writeValueAsString(orderEvent);

        ListenableFuture<SendResult<Long,String>> listenableFuture =  kafkaTemplate.sendDefault(key,value);
        listenableFuture.addCallback(new OrderEventListenableFutureCallback(key,value));

    }

    //Sync produce
    public SendResult<Long, String> sendLibraryEventSynchronous(OrderEvent orderEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {

        Long key = orderEvent.getId();
        String value = objectMapper.writeValueAsString(orderEvent);

        SendResult<Long,String> sendResult;
        try {
            sendResult = kafkaTemplate.sendDefault(key,value).get(1, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException e) {
            log.error("ExecutionException/InterruptedException Sending the Message and the exception is {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception Sending the Message and the exception is {}", e.getMessage());
            throw e;
        }

        return sendResult;

    }

}   
