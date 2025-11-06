package com.example.orderpay.order.dto;




import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderRequest {
    private Long userId;
    private Long storeId;
    private String channel;
    private List<OrderItemRequest> items;

    public BigDecimal calculateTotal() {
        return items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemRequest {
        private Long productId;
        private int quantity;
        private BigDecimal unitPrice;
    }
}
