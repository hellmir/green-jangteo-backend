package com.firstone.greenjangteo.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.firstone.greenjangteo.hibernate.HibernateCollectionMixIn;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Collection;

import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig extends CachingConfigurerSupport {
    private static final String CACHE_KEY_PREFIX = "green-jangteo:";

    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new Hibernate5Module());
        mapper.registerModule(new JavaTimeModule());

        mapper.addMixIn(Collection.class, HibernateCollectionMixIn.class);

        return mapper;
    }


    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }

    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper())))
                .entryTtl(defaultCacheConfig().entryTtl(Duration.ofMinutes(30)).getTtl())
                .prefixCacheNameWith(CACHE_KEY_PREFIX);

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }
}
