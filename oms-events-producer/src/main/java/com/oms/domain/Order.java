package com.oms.domain;

import com.oms.type.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

/**
 * @author Ehsan Sh
 */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Order {
    @NotNull
    private Long id;
    @NotNull
    private OrderType type;

}   
