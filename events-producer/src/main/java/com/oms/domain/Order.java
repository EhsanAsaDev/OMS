package com.oms.domain;

import com.oms.type.OrderType;
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
public class Order {
    private Long id;
    private OrderType type;

}   
