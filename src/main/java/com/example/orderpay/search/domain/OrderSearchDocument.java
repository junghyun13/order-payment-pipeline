package com.example.orderpay.search.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setting(settingPath = "elasticsearch/settings.json")
@Document(indexName = "orders")
public class OrderSearchDocument {
    @Id
    private String id;
    private Long orderId;
    private Long storeId;

    @Field(type = FieldType.Keyword)
    private String status;

    private Long totalAmount;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private OffsetDateTime createdAt;

    @Field(type = FieldType.Nested)
    private List<OrderItem> items;

    // Nested 클래스 정의
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItem {
        @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
        private String name;

        @Field(type = FieldType.Integer)
        private int quantity;
    }
}