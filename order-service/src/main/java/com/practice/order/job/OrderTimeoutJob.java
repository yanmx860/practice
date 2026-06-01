package com.practice.order.job;

import com.practice.order.service.OrderTimeoutService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTimeoutJob {

    @Autowired
    private OrderTimeoutService orderTimeoutService;

    @XxlJob("orderTimeoutJob")
    public void handle() {
        log.info("order timeout job start");
        int count = orderTimeoutService.cancelExpiredOrders();
        log.info("order timeout job end, canceled {} orders", count);
    }
}
