package com.microservices.project.service;

import com.microservices.project.entity.Order;
import com.microservices.project.exception.CustomOrderException;
import com.microservices.project.external.client.PaymentService;
import com.microservices.project.external.client.ProductService;
import com.microservices.project.external.request.PaymentRequest;
import com.microservices.project.external.response.PaymentResponse;
import com.microservices.project.model.OrderRequest;
import com.microservices.project.model.OrderResponse;
import com.microservices.project.model.ProductResponse;
import com.microservices.project.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Objects;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Placing the order request {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(),
                orderRequest.getQuantity());

        log.info("Creating order with status CREATED");

        Order order = Order.builder()
                .productId(orderRequest.getProductId())
                .amount(orderRequest.getTotalAmount())
                .quantity(orderRequest.getQuantity())
                .orderStatus("CREATED")
                .orderDate(Instant.now())
                .build();

        order = orderRepository.save(order);

        log.info("Calling payment service to complete the payment.");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .paymentMode(orderRequest.getPaymentMode())
                .orderId(order.getOrderId())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus;

        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully. Changing the order status to PLACED.");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Error occurred in Payment. Changing the order status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order placed successfully with order id: {}", order.getOrderId());

        return order.getOrderId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get order details for Order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomOrderException("Order Not Found for order Id: "+orderId,
                        "NOT_FOUND",
                        404));

        log.info("Invoking Product Service to fetch the Product details for the product Id: {}", order.getProductId());

        /*ProductResponse productResponse = restTemplate.getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(),
                ProductResponse.class);*/

        ProductResponse productResponse = restTemplate.exchange("http://PRODUCT-SERVICE/product/" + order.getProductId()
                        , HttpMethod.GET,
                        null, ProductResponse.class)
                .getBody();

        OrderResponse.ProductDetails productDetails = null;

        if (Objects.nonNull(productResponse)) {
            productDetails = OrderResponse.ProductDetails.builder()
                    .productId(productResponse.getProductId())
                    .productName(productResponse.getProductName())
                    .build();
        }

        log.info("Getting the Payment Details from the Payment Service for the order Id: {}", order.getOrderId());

        /*PaymentResponse paymentResponse = paymentService.getPaymentDetailsByOrderId(orderId)
                .getBody();*/

        PaymentResponse paymentResponse = restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/" +  orderId,
                PaymentResponse.class);

        OrderResponse.PaymentDetails paymentDetails = null;

        if (Objects.nonNull(paymentResponse)) {
            paymentDetails = OrderResponse.PaymentDetails.builder()
                    .paymentId(paymentResponse.getPaymentId())
                    .paymentDate(paymentResponse.getPaymentDate())
                    .paymentMode(paymentResponse.getPaymentMode())
                    .paymentStatus(paymentResponse.getStatus())
                    .build();
        }

        return OrderResponse.builder()
                .orderDate(order.getOrderDate())
                .orderId(order.getOrderId())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();
    }
}
