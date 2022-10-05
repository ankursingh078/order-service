package com.microservices.project.service;

import com.microservices.project.model.OrderRequest;
import com.microservices.project.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
