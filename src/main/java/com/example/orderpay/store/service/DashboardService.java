package com.example.orderpay.store.service;

import com.example.orderpay.member.User;
import com.example.orderpay.order.domain.OrderStatus;
import com.example.orderpay.order.repository.OrderRepository;
import com.example.orderpay.store.dto.StoreDashboardResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    // 캐시 TTL (초)
    private static final long CACHE_TTL_SECONDS = 30L;

    public StoreDashboardResponse getDashboard(User loginUser, Long storeId, String range) {
        if (loginUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        // 1) range 문자열 파싱: 예) "10m", "1h", "1d"
        Duration duration = parseRangeToDuration(range);
        OffsetDateTime since = OffsetDateTime.now().minus(duration);

        String key = "dashboard:" + storeId + ":" + (range == null ? "default" : range);

        // 2) Redis에서 캐시 확인
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, StoreDashboardResponse.class);
            } catch (JsonProcessingException e) {
                // 파싱 실패 시 무시하고 새로 계산
            }
        }

        // 3) DB에서 기간 필터 적용 집계 계산
        Map<String, Long> counts = Arrays.stream(OrderStatus.values())
                .collect(Collectors.toMap(Enum::name,
                        s -> orderRepository.countByStoreIdAndStatusAndCreatedAtAfter(storeId, s, since)));

        long totalOrders = orderRepository.countByStoreIdAndCreatedAtAfter(storeId, since);
        var totalAmount = orderRepository.sumTotalAmountByStoreIdSince(storeId, since);

        List<Long> recentOrderIds = orderRepository.findTop10ByStoreIdAndCreatedAtAfterOrderByCreatedAtDesc(storeId, since)
                .stream().map(o -> o.getId()).collect(Collectors.toList());

        StoreDashboardResponse.Metrics metrics = StoreDashboardResponse.Metrics.builder()
                .totalOrders(totalOrders)
                .totalAmount(totalAmount)
                .build();

        StoreDashboardResponse resp = StoreDashboardResponse.builder()
                .storeId(storeId)
                .counts(counts)
                .metrics(metrics)
                .recentOrderIds(recentOrderIds)
                .build();

        // 4) Redis에 JSON 캐시
        try {
            String json = objectMapper.writeValueAsString(resp);
            redis.opsForValue().set(key, json, Duration.ofSeconds(CACHE_TTL_SECONDS));
        } catch (JsonProcessingException e) {
            // 캐시 실패 로깅 (선택 사항)
        }

        return resp;
    }

    /** range 문자열을 Duration으로 변환 */
    private Duration parseRangeToDuration(String range) {
        if (range == null || range.isEmpty()) return Duration.ofDays(1); // 기본 1일
        try {
            if (range.endsWith("m")) {
                int minutes = Integer.parseInt(range.replace("m", ""));
                return Duration.ofMinutes(minutes);
            } else if (range.endsWith("h")) {
                int hours = Integer.parseInt(range.replace("h", ""));
                return Duration.ofHours(hours);
            } else if (range.endsWith("d")) {
                int days = Integer.parseInt(range.replace("d", ""));
                return Duration.ofDays(days);
            }
        } catch (NumberFormatException e) {
            // 잘못된 포맷이면 기본 1일
        }
        return Duration.ofDays(1);
    }

}
