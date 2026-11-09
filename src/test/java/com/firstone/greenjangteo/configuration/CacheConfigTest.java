package com.firstone.greenjangteo.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CacheConfigTest {
    @DisplayName("공용 Upstash DB에서 다른 프로젝트와 겹치지 않도록 캐시 키에 프로젝트 접두사를 붙인다.")
    @Test
    void prefixesCacheKeysWithProjectName() {
        // given
        CacheManager cacheManager = new CacheConfig().redisCacheManager(mock(RedisConnectionFactory.class));

        // when
        RedisCache cache = (RedisCache) cacheManager.getCache("post");

        // then
        assertThat(cache.getCacheConfiguration().getKeyPrefixFor("post")).isEqualTo("green-jangteo:post::");
    }

    @DisplayName("Redis 장애가 요청 실패로 번지지 않도록 캐시 오류는 로그만 남기고 넘어간다.")
    @Test
    void continuesOnCacheError() {
        // when, then
        assertThat(new CacheConfig().errorHandler()).isInstanceOf(LoggingCacheErrorHandler.class);
    }
}
