package ru.lakeevda.redisapp.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.io.Serializable;
import java.time.Duration;

/**
 * Конфигурация кеширования и сериализации данных в Redis.
 *
 * @author lakeev-da
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Создаёт шаблон Redis с JSON-сериализацией значений.
     *
     * @param redisConnectionFactory фабрика подключений к Redis
     * @return шаблон Redis
     */
    @Bean
    public RedisTemplate<String, Serializable> redisCacheTemplate(
            LettuceConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, Serializable> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jacksonJsonRedisSerializer());
        return template;
    }

    /**
     * Создаёт менеджер кешей Redis с TTL по умолчанию.
     *
     * @param connectionFactory фабрика подключений к Redis
     * @return менеджер кешей
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jacksonJsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * Создаёт JSON-сериализатор с поддержкой полиморфных типов.
     *
     * @return JSON-сериализатор Redis
     */
    private GenericJacksonJsonRedisSerializer jacksonJsonRedisSerializer() {
        return GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(Object.class)
                        .build())
                .build();
    }
}
