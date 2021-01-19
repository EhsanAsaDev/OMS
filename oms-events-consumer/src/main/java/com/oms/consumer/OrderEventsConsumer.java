package com.oms.consumer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.oms.service.OrderEventsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class OrderEventsConsumer {


    @Autowired
    OrderEventsService orderEventsService;

    @KafkaListener(topics = {"order-events"})
    public void onMessage(ConsumerRecord<Long,String> consumerRecord) throws JsonProcessingException {
        log.info("ConsumerRecord : {} ", consumerRecord );
        orderEventsService.processOrderEvent(consumerRecord);

    }
}
