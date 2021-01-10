package com.oms.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ehsan Sh
 */


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrderEvent {
    private Long id;
    private Order order;
}   
