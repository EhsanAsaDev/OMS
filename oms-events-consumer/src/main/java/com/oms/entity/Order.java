package com.oms.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity(name = "TBL_ORDER")
public class Order {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderType type;

    @OneToOne
    @JoinColumn(name = "orderEventId")
    private OrderEvent orderEvent;
}
