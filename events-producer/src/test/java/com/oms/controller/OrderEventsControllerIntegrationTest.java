package com.oms.controller;


import com.oms.domain.Order;
import com.oms.domain.OrderEvent;
import com.oms.type.OrderType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"order-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
public class OrderEventsControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<Long, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new LongDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    //@Timeout(15)
    void postOrderEvent() throws InterruptedException {
        //given
        Order order = Order.builder()
                .id(123l)
                .type(OrderType.TYPE1)
                .build();

        OrderEvent orderEvent = OrderEvent.builder()
                .id(null)
                .order(order)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<OrderEvent> request = new HttpEntity<>(orderEvent, headers);

        //when
        ResponseEntity<OrderEvent> responseEntity = restTemplate.exchange("/api/v1/order-event", HttpMethod.POST, request, OrderEvent.class);

        //then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        ConsumerRecord<Long, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "order-events");
        //Thread.sleep(3000);
        String expectedRecord = "{\"id\":null,\"order\":{\"id\":123,\"type\":\"TYPE1\"}}";
        String value = consumerRecord.value();
        assertEquals(expectedRecord, value);

    }

    @Test
    @Timeout(5)
    void putOrderEvent() throws InterruptedException {
        //given
        Order order = Order.builder()
                .id(1L)
                .type(OrderType.TYPE2)
                .build();

        OrderEvent orderEvent = OrderEvent.builder()
                .id(123L)
                .order(order)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<OrderEvent> request = new HttpEntity<>(orderEvent, headers);

        //when
        ResponseEntity<OrderEvent> responseEntity = restTemplate.exchange("/api/v1/order-event", HttpMethod.PUT, request, OrderEvent.class);

        //then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ConsumerRecord<Long, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "order-events");
        //Thread.sleep(3000);
        String expectedRecord = "{\"id\":123,\"order\":{\"id\":1,\"type\":\"TYPE2\"}}";
        String value = consumerRecord.value();
        assertEquals(expectedRecord, value);

    }
}