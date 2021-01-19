package com.oms.jpa;

import com.oms.entity.OrderEvent;
import org.springframework.data.repository.CrudRepository;

public interface OrderEventsRepository extends CrudRepository<OrderEvent,Long> {
}
