package com.example.orderpay.order.controller;



import com.example.orderpay.auth.LoginUser;
import com.example.orderpay.member.User;
import com.example.orderpay.order.dto.*;
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
    public OrderResponse createOrder(@LoginUser User loginUser,@RequestBody OrderRequest request) {
        return orderService.createOrder(loginUser,request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@LoginUser User loginUser,@PathVariable("id") Long id) {
        return orderService.getOrder(loginUser,id);
    }

    // 주문 목록 조회 (페이징)
    @GetMapping
    public Page<OrderResponse> listOrders(@LoginUser User loginUser,
            @RequestParam(name = "storeId",required = false) Long storeId,
                                          @RequestParam(name = "page", defaultValue = "0") int page,
                                          @RequestParam(name = "size", defaultValue = "10") int size) {
        return orderService.listOrders(loginUser,storeId, page, size);
    }

    // 주문 상태 변경
    @PostMapping("/{id}/status")
    public OrderResponse changeStatus(@LoginUser User loginUser,
            @PathVariable("id") Long id,
            @RequestBody OrderStatusUpdateRequest request) {
        return orderService.changeStatus(loginUser,id, request);
    }

    // 주문 취소
    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@LoginUser User loginUser,
            @PathVariable("id") Long id,
            @RequestBody OrderCancelRequest request) {
        return orderService.cancelOrder(loginUser,id, request);
    }

    @GetMapping("/summary") //전체총금액 조회 사장또는 고객
    public Object getOrderSummary(@LoginUser User loginUser,
                                  @RequestParam(name = "storeId", required = false) Long storeId,
                                  @RequestParam(name = "customerId", required = false) Long customerId) {
        return orderService.getOrderSummary(loginUser, storeId, customerId);
    }

    @PostMapping("/{id}/pay") //✅ 결제 완료 처리
    public OrderResponse payOrder(@LoginUser User loginUser,
                                  @PathVariable("id") Long id) {
        return orderService.completePayment(loginUser, id);
    }



    // 날짜별 매출 조회
    @PostMapping("/{storeId}/sales") // storeId를 경로 변수로 받음
    public StoreDailySales getSalesByDate(
            @LoginUser User loginUser,
            @PathVariable("storeId") Long storeId,
            @RequestBody SalesRequest request
    ) {
        return orderService.getSalesByDate(loginUser, storeId, request.getDate());
    }


    @GetMapping("/customers") //특정고객의 주문 내역조회
    public Page<OrderResponse> getCustomerOrders(
            @LoginUser User loginUser,
            @RequestParam(name = "storeId") Long storeId,
            @RequestParam(name = "customerId") Long customerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        return orderService.getCustomerOrders(loginUser, storeId, customerId, page, size);
    }



}
