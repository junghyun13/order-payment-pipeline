package com.example.orderpay.order.controller;



import com.example.orderpay.order.dto.OrderRequest;
import com.example.orderpay.order.dto.OrderResponse;
import com.example.orderpay.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable("id") Long id) {
        return orderService.getOrder(id);
    }
}
