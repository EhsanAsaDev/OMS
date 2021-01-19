package com.oms.entity;


import lombok.*;

import javax.persistence.*;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class OrderEvent {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(mappedBy = "orderEvent", cascade = {CascadeType.ALL})
    @ToString.Exclude
    private Order order;

}
