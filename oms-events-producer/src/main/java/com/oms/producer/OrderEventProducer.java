package com.oms.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.domain.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Ehsan Sh
 */

@Component
@Slf4j
public class OrderEventProducer {

    private final String topic = "order-events";

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
    public SendResult<Long, String> sentOrderEventSynchronous(OrderEvent orderEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {

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

    public ListenableFuture<SendResult<Long,String>> sentOrderEvent_Approach2(OrderEvent orderEvent) throws JsonProcessingException {

        Long key = orderEvent.getId();
        String value = objectMapper.writeValueAsString(orderEvent);

        ProducerRecord<Long,String> producerRecord = buildProducerRecord(key, value, topic);

        ListenableFuture<SendResult<Long,String>> listenableFuture =  kafkaTemplate.send(producerRecord);
        listenableFuture.addCallback(new OrderEventListenableFutureCallback(key,value));

        return listenableFuture;
    }


    private ProducerRecord<Long, String> buildProducerRecord(Long key, String value, String topic) {

        List<Header> recordHeaders = List.of(new RecordHeader("event-source", "Rest Call".getBytes()));

        return new ProducerRecord<Long, String>(topic, null, key, value, recordHeaders);
    }
}   
