package ru.lakeevda.redisapp.domain.model;

import org.junit.jupiter.api.Test;
import ru.lakeevda.redisapp.domain.enums.ChatType;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты доменной логики чата.
 *
 * @author lakeev-da
 */
class ChatTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    /**
     * Проверяет, что создатель автоматически становится участником приватного чата.
     */
    @Test
    void createAddsCreatorAndResolvesPrivateType() {
        Chat chat = Chat.create("Dialog", 1L, Set.of(2L));

        assertThat(chat.id()).isNull();
        assertThat(chat.name()).isEqualTo("Dialog");
        assertThat(chat.createdById()).isEqualTo(1L);
        assertThat(chat.memberIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(chat.type()).isEqualTo(ChatType.PRIVATE);
        assertThat(chat.createdAt()).isNotNull();
    }

    /**
     * Проверяет, что чат с более чем двумя участниками становится групповым.
     */
    @Test
    void createResolvesGroupType() {
        Chat chat = Chat.create("Team", 1L, Set.of(2L, 3L));

        assertThat(chat.type()).isEqualTo(ChatType.GROUP);
        assertThat(chat.memberIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    /**
     * Проверяет создание чата без переданных участников.
     */
    @Test
    void createWithNullMembersContainsOnlyCreator() {
        Chat chat = Chat.create("Solo", 1L, null);

        assertThat(chat.memberIds()).containsExactly(1L);
    }

    /**
     * Проверяет добавление и удаление участника.
     */
    @Test
    void addAndRemoveMemberChangeMembership() {
        Chat chat = chat(ChatType.PRIVATE, Set.of(1L, 2L));

        Chat withNewMember = chat.addMember(3L);
        Chat withoutMember = withNewMember.removeMember(2L);

        assertThat(withNewMember.memberIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
        assertThat(withoutMember.memberIds()).containsExactlyInAnyOrder(1L, 3L);
        assertThat(chat.memberIds()).containsExactlyInAnyOrder(1L, 2L);
    }

    /**
     * Проверяет повышение типа приватного чата до группового.
     */
    @Test
    void withActualTypeUpgradesPrivateChatToGroup() {
        Chat chat = chat(ChatType.PRIVATE, Set.of(1L, 2L, 3L));

        Chat updated = chat.withActualType();

        assertThat(updated.type()).isEqualTo(ChatType.GROUP);
    }

    /**
     * Проверяет, что приватный чат с двумя участниками не меняет тип.
     */
    @Test
    void withActualTypeKeepsPrivateChat() {
        Chat chat = chat(ChatType.PRIVATE, Set.of(1L, 2L));

        Chat updated = chat.withActualType();

        assertThat(updated).isSameAs(chat);
    }

    /**
     * Проверяет, что удалённый чат не меняет тип при пересчёте.
     */
    @Test
    void withActualTypeKeepsDeletedType() {
        Chat chat = chat(ChatType.DELETED, Set.of(1L, 2L, 3L));

        Chat updated = chat.withActualType();

        assertThat(updated).isSameAs(chat);
        assertThat(updated.isDeleted()).isTrue();
    }

    /**
     * Проверяет мягкое удаление чата.
     */
    @Test
    void markDeletedChangesType() {
        Chat chat = chat(ChatType.GROUP, Set.of(1L, 2L, 3L));

        Chat deleted = chat.markDeleted();

        assertThat(deleted.type()).isEqualTo(ChatType.DELETED);
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.memberIds()).isEqualTo(chat.memberIds());
    }

    /**
     * Проверяет определение наличия участника в чате.
     */
    @Test
    void hasMemberChecksMembership() {
        Chat chat = chat(ChatType.PRIVATE, Set.of(1L, 2L));

        assertThat(chat.hasMember(1L)).isTrue();
        assertThat(chat.hasMember(3L)).isFalse();
    }

    /**
     * Проверяет определение типа по количеству участников.
     */
    @Test
    void resolveTypeDependsOnMembersCount() {
        assertThat(Chat.resolveType(Set.of(1L))).isEqualTo(ChatType.PRIVATE);
        assertThat(Chat.resolveType(Set.of(1L, 2L))).isEqualTo(ChatType.PRIVATE);
        assertThat(Chat.resolveType(Set.of(1L, 2L, 3L))).isEqualTo(ChatType.GROUP);
    }

    /**
     * Создаёт тестовую доменную модель чата.
     *
     * @param type      тип чата
     * @param memberIds идентификаторы участников
     * @return чат
     */
    private Chat chat(ChatType type, Set<Long> memberIds) {
        return new Chat(10L, "Chat", type, CREATED_AT, 1L, memberIds);
    }
}
