package com.example.orderpay.search.repository;

import com.example.orderpay.search.domain.OrderSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SearchRepository extends ElasticsearchRepository<OrderSearchDocument, String> {

}
