package com.oms.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.domain.Order;
import com.oms.domain.OrderEvent;
import com.oms.type.OrderType;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.protocol.types.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import scala.Int;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderEventProducerUnitTest {

    @Mock
    KafkaTemplate<Integer,String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    OrderEventProducer eventProducer;

    @Test
    void sendOrderEvent_Approach2_failure() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        Order order = Order.builder()
                .id(123l)
                .type(OrderType.TYPE1)
                .build();

        OrderEvent orderEvent = OrderEvent.builder()
                .id(null)
                .order(order)
                .build();
        SettableListenableFuture future = new SettableListenableFuture();

        future.setException(new RuntimeException("Exception Calling Kafka"));
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        //when

        assertThrows(Exception.class, ()-> eventProducer.sentOrderEvent_Approach2(orderEvent).get());

    }

    @Test
    void sendOrderEvent_Approach2_success() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        Order order = Order.builder()
                .id(123l)
                .type(OrderType.TYPE1)
                .build();

        OrderEvent orderEvent = OrderEvent.builder()
                .id(null)
                .order(order)
                .build();
        String record = objectMapper.writeValueAsString(orderEvent);
        SettableListenableFuture future = new SettableListenableFuture();

        ProducerRecord<Integer, String> producerRecord = new ProducerRecord("order-events", orderEvent.getId(),record );
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("order-events", 1),
                1,1,342,System.currentTimeMillis(), 1, 2);
        SendResult<Integer, String> sendResult = new SendResult<Integer, String>(producerRecord,recordMetadata);

        future.set(sendResult);
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        //when

        ListenableFuture<SendResult<Long,String>> listenableFuture =  eventProducer.sentOrderEvent_Approach2(orderEvent);

        //then
        SendResult<Long,String> sendResult1 = listenableFuture.get();
        assert sendResult1.getRecordMetadata().partition()==1;

    }


}
