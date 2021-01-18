package com.oms.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author Ehsan Sh
 */


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrderEvent {
    private Long id;
    @NotNull
    @Valid
    private Order order;
}   
