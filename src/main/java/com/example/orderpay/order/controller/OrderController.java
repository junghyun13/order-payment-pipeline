package com.example.orderpay.order.controller;



import com.example.orderpay.order.dto.OrderCancelRequest;
import com.example.orderpay.order.dto.OrderRequest;
import com.example.orderpay.order.dto.OrderResponse;
import com.example.orderpay.order.dto.OrderStatusUpdateRequest;
import com.example.orderpay.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    // 주문 목록 조회 (페이징)
    @GetMapping
    public Page<OrderResponse> listOrders(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.listOrders(storeId, status, page, size);
    }

    // 주문 상태 변경
    @PostMapping("/{id}/status")
    public OrderResponse changeStatus(
            @PathVariable("id") Long id,
            @RequestBody OrderStatusUpdateRequest request) {
        return orderService.changeStatus(id, request);
    }

    // 주문 취소
    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable("id") Long id,
            @RequestBody OrderCancelRequest request) {
        return orderService.cancelOrder(id, request);
    }
}
