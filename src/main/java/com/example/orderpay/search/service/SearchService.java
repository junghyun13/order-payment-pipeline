package com.example.orderpay.search.service;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.example.orderpay.member.User;
import com.example.orderpay.search.domain.OrderSearchDocument;
import com.example.orderpay.search.dto.SearchResponse;
import com.example.orderpay.search.dto.SearchResponse.OrderHit;
import com.example.orderpay.search.dto.SearchResponse.OrderHit.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;


    public SearchResponse search(User loginUser, String q, Long storeId, String status) {

        // 로그인 체크
        if (loginUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        List<Query> mustQueries = new ArrayList<>();

        // 검색어 (메뉴명, 첫 글자 기준 검색)
        if (q != null && !q.isBlank()) {
            mustQueries.add(
                    NestedQuery.of(n -> n
                            .path("items")
                            .query(PrefixQuery.of(p -> p
                                    .field("items.name")
                                    .value(q) // 입력값으로 시작하는 것만 검색
                            )._toQuery())
                    )._toQuery()
            );
        }


        // storeId 필터
        if (storeId != null) {
            mustQueries.add(
                    TermQuery.of(t -> t.field("storeId").value(storeId))._toQuery()
            );
        }

        // status 필터
        if (status != null && !status.isBlank()) {
            mustQueries.add(
                    TermQuery.of(t -> t.field("status").value(status))._toQuery()
            );
        }

        Query finalQuery = mustQueries.isEmpty()
                ? MatchAllQuery.of(m -> m)._toQuery()
                : BoolQuery.of(b -> b.must(mustQueries))._toQuery();

        try {
            var esResult =
                    elasticsearchClient.search(s -> s
                                    .index("orders")
                                    .query(finalQuery)
                                    .size(100),
                            OrderSearchDocument.class
                    );

            List<OrderHit> hitList = esResult.hits().hits().stream()
                    .map(hit -> {
                        OrderSearchDocument doc = hit.source();
                        if (doc == null) return null;

                        return OrderHit.builder()
                                .orderId(doc.getOrderId())
                                .storeId(doc.getStoreId())
                                .status(doc.getStatus())
                                .totalAmount(doc.getTotalAmount())
                                .createdAt(doc.getCreatedAt())
                                .items(
                                        doc.getItems() == null ? null :
                                                doc.getItems().stream()
                                                        .map(i -> OrderItem.builder()
                                                                .name(i.getName())
                                                                .quantity(i.getQuantity())
                                                                .build())
                                                        .toList()
                                )
                                .build();
                    })
                    .filter(Objects::nonNull)
                    .toList();

            return SearchResponse.builder()
                    .tookMs(esResult.took())
                    .hits(hitList)
                    .build();

        } catch (Exception e) {
            log.error("Elasticsearch 검색 실패", e);
            throw new RuntimeException("검색 중 오류가 발생했습니다.");
        }
    }
}
