package com.microservices.project.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderRequest {

    private long productId;
    private long totalAmount;
    private long quantity;
    private PaymentMode paymentMode;

}
