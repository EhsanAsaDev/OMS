package com.oms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.oms.controller.OrderEventController;
import com.oms.domain.Order;
import com.oms.domain.OrderEvent;
import com.oms.producer.OrderEventProducer;
import com.oms.type.OrderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderEventController.class)
@AutoConfigureMockMvc
public class OrderEventControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    OrderEventProducer orderEventProducer;

    @Test
    void postOrderEvent() throws Exception {
        //given
        Order order = Order.builder()
                .id(123l)
                .type(OrderType.TYPE1)
                .build();

        OrderEvent orderEvent = OrderEvent.builder()
                .id(null)
                .order(order)
                .build();
       
        String json = objectMapper.writeValueAsString(orderEvent);
        when(orderEventProducer.sentOrderEvent_Approach2(isA(OrderEvent.class))).thenReturn(null);

        //expect
        mockMvc.perform(post("/api/v1/order-event")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

    }

    @Test
    void postOrderEvent_4xx() throws Exception {
        //given

        Order order = Order.builder()
                .id(null)
                .type(null)
                .build();

        OrderEvent orderEvent = OrderEvent.builder()
                .id(null)
                .order(order)
                .build();

        String json = objectMapper.writeValueAsString(orderEvent);
        when(orderEventProducer.sentOrderEvent_Approach2(isA(OrderEvent.class))).thenReturn(null);
        //expect
        String expectedErrorMessage = "order.id - must not be null, order.type - must not be null";
        mockMvc.perform(post("/api/v1/order-event")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
        .andExpect(content().string(expectedErrorMessage));

    }

    @Test
    @DisplayName("update Order Event ")
    void updateOrderEvent() throws Exception {

        //given
        Order order = Order.builder()
                .id(123l)
                .type(OrderType.TYPE1)
                .build();

        OrderEvent orderEvent = OrderEvent.builder()
                .id(456L)
                .order(order)
                .build();
        String json = objectMapper.writeValueAsString(orderEvent);
        when(orderEventProducer.sentOrderEvent_Approach2(isA(OrderEvent.class))).thenReturn(null);

        //expect
        mockMvc.perform(
                put("/api/v1/order-event")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void updateOrderEvent_withNullOrderEventId() throws Exception {

        //given
        Order order = Order.builder()
                .id(123l)
                .type(OrderType.TYPE1)
                .build();

        OrderEvent orderEvent = OrderEvent.builder()
                .id(null)
                .order(order)
                .build();
        String json = objectMapper.writeValueAsString(orderEvent);
        when(orderEventProducer.sentOrderEvent_Approach2(isA(OrderEvent.class))).thenReturn(null);

        //expect
        mockMvc.perform(
                put("/api/v1/order-event")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string("orderEvent.id must has a value"));

    }


}
