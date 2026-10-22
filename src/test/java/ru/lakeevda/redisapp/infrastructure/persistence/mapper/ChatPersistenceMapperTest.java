package ru.lakeevda.redisapp.infrastructure.persistence.mapper;

import org.junit.jupiter.api.Test;
import ru.lakeevda.redisapp.domain.enums.ChatType;
import ru.lakeevda.redisapp.domain.model.Chat;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.ChatEntity;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.UserEntity;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты маппера чатов.
 *
 * @author lakeev-da
 */
class ChatPersistenceMapperTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    private final ChatPersistenceMapper mapper = new ChatPersistenceMapper();

    /**
     * Проверяет преобразование сущности в доменную модель.
     */
    @Test
    void toDomainMapsAllFields() {
        ChatEntity entity = new ChatEntity();
        entity.setId(10L);
        entity.setName("Team");
        entity.setType("group");
        entity.setCreatedAt(CREATED_AT);
        entity.setCreatedById(1L);
        entity.getMembers().add(userEntity(1L));
        entity.getMembers().add(userEntity(2L));

        Chat chat = mapper.toDomain(entity);

        assertThat(chat.id()).isEqualTo(10L);
        assertThat(chat.name()).isEqualTo("Team");
        assertThat(chat.type()).isEqualTo(ChatType.GROUP);
        assertThat(chat.createdAt()).isEqualTo(CREATED_AT);
        assertThat(chat.createdById()).isEqualTo(1L);
        assertThat(chat.memberIds()).containsExactlyInAnyOrder(1L, 2L);
    }

    /**
     * Проверяет преобразование приватного чата.
     */
    @Test
    void toDomainMapsPrivateType() {
        ChatEntity entity = new ChatEntity();
        entity.setType("private");

        assertThat(mapper.toDomain(entity).type()).isEqualTo(ChatType.PRIVATE);
    }

    /**
     * Проверяет перенос доменной модели в сущность и замену участников.
     */
    @Test
    void applyMapsAllFieldsAndReplacesMembers() {
        ChatEntity entity = new ChatEntity();
        entity.getMembers().add(userEntity(99L));
        Set<UserEntity> members = new LinkedHashSet<>();
        members.add(userEntity(1L));
        members.add(userEntity(2L));

        Chat chat = new Chat(10L, "Team", ChatType.GROUP, CREATED_AT, 1L, Set.of(1L, 2L));
        mapper.apply(chat, entity, members);

        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getName()).isEqualTo("Team");
        assertThat(entity.getType()).isEqualTo("group");
        assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(entity.getCreatedById()).isEqualTo(1L);
        assertThat(entity.getMembers()).extracting(UserEntity::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    /**
     * Создаёт JPA-сущность пользователя.
     *
     * @param id идентификатор пользователя
     * @return сущность пользователя
     */
    private UserEntity userEntity(Long id) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        return entity;
    }
}
