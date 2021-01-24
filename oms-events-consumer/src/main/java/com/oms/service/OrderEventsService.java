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
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Optional;

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

        if (orderEvent.getId() != null && orderEvent.getId() == 0l) {
            throw new RecoverableDataAccessException("Temporary Network Issue");
        }


        switch(orderEvent.getOrder().getType()){
            case TYPE1:
                save(orderEvent);
                break;
            case TYPE2:
                //validate the orderEvent
                validate(orderEvent);
                save(orderEvent);
                break;
            default:
                log.info("Invalid Library Event Type");
        }
        save(orderEvent);

    }

    private void validate(OrderEvent orderEvent) {
        if(orderEvent.getId()==null){
            throw new IllegalArgumentException("Order Event Id is missing");
        }

        Optional<OrderEvent> orderEventOptional = orderEventsRepository.findById(orderEvent.getId());
        if(!orderEventOptional.isPresent()){
            throw new IllegalArgumentException("Not a valid library Event");
        }
        log.info("Validation is successful for the library Event : {} ", orderEventOptional.get());
    }


    private void save(OrderEvent orderEvent) {
        orderEvent.getOrder().setOrderEvent(orderEvent);
        orderEventsRepository.save(orderEvent);
        log.info("Successfully Persisted the order event {} ", orderEvent);
    }

    public void handleRecovery(ConsumerRecord<Long,String> record){
        log.error("handleRecovery for {}", record);

        Long key = record.key();
        String message = record.value().replace(":0",":-1");

        ListenableFuture<SendResult<Long,String>> listenableFuture = kafkaTemplate.sendDefault(key, message);
        listenableFuture.addCallback(new OrderEventListenableFutureCallback(key,message));

    }

}
