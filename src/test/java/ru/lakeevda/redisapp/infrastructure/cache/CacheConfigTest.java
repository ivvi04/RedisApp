package ru.lakeevda.redisapp.infrastructure.cache;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Тесты конфигурации кеширования Redis.
 *
 * @author lakeev-da
 */
class CacheConfigTest {

    private final CacheConfig config = new CacheConfig();

    /**
     * Проверяет настройку сериализаторов шаблона Redis.
     */
    @Test
    void redisCacheTemplateConfiguresSerializers() {
        LettuceConnectionFactory connectionFactory = mock(LettuceConnectionFactory.class);

        RedisTemplate<String, Serializable> template = config.redisCacheTemplate(connectionFactory);

        assertThat(template.getConnectionFactory()).isSameAs(connectionFactory);
        assertThat(template.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(template.getValueSerializer())
                .isInstanceOf(GenericJacksonJsonRedisSerializer.class);
    }

    /**
     * Проверяет настройку TTL, сериализаторов и запрета null в менеджере кешей.
     */
    @Test
    void cacheManagerConfiguresDefaults() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);

        RedisCacheManager manager = config.cacheManager(connectionFactory);
        RedisCache cache = (RedisCache) manager.getCache("chats");

        assertThat(cache).isNotNull();
        RedisCacheConfiguration cacheConfig = cache.getCacheConfiguration();
        assertThat(cacheConfig.getTtlFunction().getTimeToLive("key", "value"))
                .isEqualTo(Duration.ofMinutes(10));
        assertThat(cacheConfig.getAllowCacheNullValues()).isFalse();
        assertThat(cacheConfig.getKeySerializationPair().getReader())
                .isNotNull();
        assertThat(cacheConfig.getValueSerializationPair().getReader())
                .isNotNull();
    }
}
