package com.oms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.entity.OrderEvent;
import com.oms.jpa.OrderEventsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderEventsService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaTemplate<Long, String> kafkaTemplate;

    @Autowired
    private OrderEventsRepository orderEventsRepository;

    public void processOrderEvent(ConsumerRecord<Long, String> consumerRecord) throws JsonProcessingException {
        OrderEvent orderEvent = objectMapper.readValue(consumerRecord.value(), OrderEvent.class);
        log.info("orderEvent : {} ", orderEvent);

        if (orderEvent.getId() != null && orderEvent.getId() == 000) {
            throw new RecoverableDataAccessException("Temporary Network Issue");
        }

        save(orderEvent);

    }


    private void save(OrderEvent orderEvent) {
        orderEvent.getOrder().setOrderEvent(orderEvent);
        orderEventsRepository.save(orderEvent);
        log.info("Successfully Persisted the order event {} ", orderEvent);
    }

  /*  public void handleRecovery(ConsumerRecord<Integer,String> record){

        Integer key = record.key();
        String message = record.value();

        ListenableFuture<SendResult<Integer,String>> listenableFuture = kafkaTemplate.sendDefault(key, message);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, message, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, message, result);
            }
        });
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error Sending the Message and the exception is {}", ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message Sent SuccessFully for the key : {} and the value is {} , partition is {}", key, value, result.getRecordMetadata().partition());
    }*/
}
