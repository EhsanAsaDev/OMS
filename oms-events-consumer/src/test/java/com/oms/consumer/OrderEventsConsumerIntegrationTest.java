package com.oms.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oms.entity.Order;
import com.oms.entity.OrderEvent;
import com.oms.entity.OrderType;
import com.oms.jpa.OrderEventsRepository;
import com.oms.service.OrderEventsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedKafka(topics = {"order-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}"
, "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class OrderEventsConsumerIntegrationTest {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaTemplate<Long, String> kafkaTemplate;

    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry;

    @SpyBean
    OrderEventsConsumer orderEventsConsumerSpy;

    @SpyBean
    OrderEventsService orderEventsServiceSpy;

    @Autowired
    OrderEventsRepository orderEventsRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()){
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @AfterEach
    void tearDown() {
        orderEventsRepository.deleteAll();
    }

    @Test
    void publishNewOrderEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        //given
        String json = "{\"id\":null,\"order\":{\"id\":123,\"type\":\"TYPE1\"}}";
        kafkaTemplate.sendDefault(json).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(orderEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(orderEventsServiceSpy, times(1)).processOrderEvent(isA(ConsumerRecord.class));

        List<OrderEvent> orderEventList = (List<OrderEvent>) orderEventsRepository.findAll();
        assert orderEventList.size() ==1;
        orderEventList.forEach(orderEvent -> {
            assert orderEvent.getId()!=null;
            assertEquals(123, orderEvent.getOrder().getId());
        });

    }

    @Test
    void publishUpdateOrderEvent() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        String json = "{\"id\":null,\"order\":{\"id\":123,\"type\":\"TYPE1\"}}";
        OrderEvent orderEvent = objectMapper.readValue(json, OrderEvent.class);
        orderEvent.getOrder().setOrderEvent(orderEvent);
        orderEvent = orderEventsRepository.save(orderEvent);
        //publish the update OrderEvent

        Order updatedOrder = Order.builder().
                id(123L).type(OrderType.TYPE2).build();
        orderEvent.setOrder(updatedOrder);
        String updatedJson = objectMapper.writeValueAsString(orderEvent);
        kafkaTemplate.sendDefault(orderEvent.getId(), updatedJson).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        //verify(orderEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        //verify(orderEventsServiceSpy, times(1)).processOrderEvent(isA(ConsumerRecord.class));
        OrderEvent persistedOrderEvent = orderEventsRepository.findById(orderEvent.getId()).get();
        assertEquals(OrderType.TYPE2, persistedOrderEvent.getOrder().getType());
    }

    @Test
    void publishModifyOrderEvent_Not_A_Valid_OrderEventId() throws JsonProcessingException, InterruptedException, ExecutionException {
        //given
        Long orderEventId = 123L;

        String json = "{\"id\"" + orderEventId +",\"order\":{\"id\":123,\"type\":\"TYPE1\"}}";
        System.out.println(json);
        kafkaTemplate.sendDefault(orderEventId, json).get();
        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);


        verify(orderEventsConsumerSpy, atLeast(1)).onMessage(isA(ConsumerRecord.class));
        verify(orderEventsServiceSpy, atLeast(1)).processOrderEvent(isA(ConsumerRecord.class));

        Optional<OrderEvent> orderEventOptional = orderEventsRepository.findById(orderEventId);
        assertFalse(orderEventOptional.isPresent());
    }

    @Test
    void publishModifyOrderEvent_Null_OrderEventId() throws JsonProcessingException, InterruptedException, ExecutionException {
        //given
        Long orderEventId = null;
        String json = "{\"id\":" + orderEventId +",\"order\":{\"id\":123,\"type\":\"TYPE2\"}}";
        kafkaTemplate.sendDefault(orderEventId, json).get();
        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);


        verify(orderEventsConsumerSpy, atLeast(1)).onMessage(isA(ConsumerRecord.class));
        verify(orderEventsServiceSpy, atLeast(1)).processOrderEvent(isA(ConsumerRecord.class));
    }

    @Test
    void publishModifyOrderEvent_000_OrderEventId() throws JsonProcessingException, InterruptedException, ExecutionException {
        //given
        Long orderEventId = 000l;
        String json = "{\"id\":" + orderEventId +",\"order\":{\"id\":123,\"type\":\"TYPE2\"}}";
        kafkaTemplate.sendDefault(orderEventId, json).get();
        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);


        verify(orderEventsConsumerSpy, atLeast(3)).onMessage(isA(ConsumerRecord.class));
        verify(orderEventsServiceSpy, atLeast(1)).handleRecovery(isA(ConsumerRecord.class));
    }

}
