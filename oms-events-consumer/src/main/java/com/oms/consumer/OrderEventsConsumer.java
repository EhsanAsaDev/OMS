package com.oms.consumer;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class OrderEventsConsumer {



    @KafkaListener(topics = {"order-events"})
    public void onMessage(ConsumerRecord<Long,String> consumerRecord) {

        log.info("ConsumerRecord : {} ", consumerRecord );

    }
}
