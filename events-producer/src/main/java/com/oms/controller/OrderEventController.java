package com.oms.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oms.domain.OrderEvent;
import com.oms.producer.OrderEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
    public ResponseEntity<OrderEvent> postOrderEvent(@RequestBody  @Valid OrderEvent orderEvent) throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {

        log.info("before sentOrderEvent");
        orderEventProducer.sentOrderEvent_Approach2(orderEvent);
        log.info("after sentOrderEvent");
        return  ResponseEntity.status(HttpStatus.CREATED).body(orderEvent);
    }

    @PutMapping
    public ResponseEntity<?> putOrderEvent(@RequestBody  @Valid OrderEvent orderEvent) throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {

        if(orderEvent.getId()== null){
            return  ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("orderEvent.id must has a value");
        }

        log.info("before sentOrderEvent");
        orderEventProducer.sentOrderEvent_Approach2(orderEvent);
        log.info("after sentOrderEvent");
        return  ResponseEntity.status(HttpStatus.OK).body(orderEvent);
    }

}   
