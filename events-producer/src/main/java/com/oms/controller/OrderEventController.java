package com.oms.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oms.domain.OrderEvent;
import com.oms.producer.OrderEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Ehsan Sh
 */


@RestController
@RequestMapping("api/v1/order-event")
@Slf4j
public class OrderEventController {

    private final OrderEventProducer orderEventProducer;

    public OrderEventController(OrderEventProducer orderEventProducer) {
        this.orderEventProducer = orderEventProducer;
    }

    @PostMapping
    public ResponseEntity<OrderEvent> postOrderEvent(@RequestBody OrderEvent orderEvent) throws JsonProcessingException {

        log.info("before sentOrderEvent");
        orderEventProducer.sentOrderEvent(orderEvent);
        log.info("after sentOrderEvent");
        return  ResponseEntity.status(HttpStatus.CREATED).body(orderEvent);
    }

}   
