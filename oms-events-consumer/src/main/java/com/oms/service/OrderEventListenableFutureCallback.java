package com.oms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @author Ehsan Sh
 */

@Slf4j
public class OrderEventListenableFutureCallback implements ListenableFutureCallback<SendResult<Long, String>> {

    private final Long key;
    private final String value;

    public OrderEventListenableFutureCallback(Long key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void onFailure(Throwable ex) {
        log.error("Error Sending the Message and the exception is {}", ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
    }

    @Override
    public void onSuccess(SendResult<Long, String> result) {
        log.info("Message Sent SuccessFully for the key : {} and the value is {} , partition is {}", key, value, result.getRecordMetadata().partition());
    }

}
