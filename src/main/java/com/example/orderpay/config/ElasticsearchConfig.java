package com.example.orderpay.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 1️⃣ RestClient 생성
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ).build();

        // 2️⃣ ObjectMapper에 JavaTimeModule 등록
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 3️⃣ JacksonJsonpMapper 생성
        JacksonJsonpMapper mapper = new JacksonJsonpMapper(objectMapper);

        // 4️⃣ RestClientTransport 생성
        RestClientTransport transport = new RestClientTransport(restClient, mapper);

        // 5️⃣ ElasticsearchClient 생성 및 반환
        return new ElasticsearchClient(transport);
    }
}

