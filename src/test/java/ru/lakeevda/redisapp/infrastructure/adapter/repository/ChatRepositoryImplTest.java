package ru.lakeevda.redisapp.infrastructure.adapter.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lakeevda.redisapp.domain.enums.ChatType;
import ru.lakeevda.redisapp.domain.model.Chat;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.ChatEntity;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.UserEntity;
import ru.lakeevda.redisapp.infrastructure.persistence.mapper.ChatPersistenceMapper;
import ru.lakeevda.redisapp.infrastructure.persistence.repository.ChatJpaRepository;
import ru.lakeevda.redisapp.infrastructure.persistence.repository.UserJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты адаптера хранения чатов.
 *
 * @author lakeev-da
 */
@ExtendWith(MockitoExtension.class)
class ChatRepositoryImplTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private ChatJpaRepository chatRepository;

    @Mock
    private UserJpaRepository userRepository;

    private ChatRepositoryImpl repository;

    /**
     * Создаёт тестируемый адаптер с реальным маппером.
     */
    @BeforeEach
    void setUp() {
        repository = new ChatRepositoryImpl(chatRepository, userRepository, new ChatPersistenceMapper());
    }

    /**
     * Проверяет поиск чата по идентификатору с маппингом участников.
     */
    @Test
    void findByIdMapsEntityToDomain() {
        when(chatRepository.findById(10L)).thenReturn(Optional.of(chatEntity(10L)));

        Optional<Chat> result = repository.findById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(10L);
        assertThat(result.get().name()).isEqualTo("Team");
        assertThat(result.get().type()).isEqualTo(ChatType.GROUP);
        assertThat(result.get().memberIds()).containsExactlyInAnyOrder(1L, 2L);
    }

    /**
     * Проверяет пустой результат поиска по идентификатору.
     */
    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(chatRepository.findById(10L)).thenReturn(Optional.empty());

        assertThat(repository.findById(10L)).isEmpty();
    }

    /**
     * Проверяет поиск чатов участника с исключением типа.
     */
    @Test
    void findByMemberIdAndTypeNotMapsEntities() {
        when(chatRepository.findByMemberIdAndTypeNot(1L, ChatType.DELETED.getValue()))
                .thenReturn(List.of(chatEntity(10L)));

        List<Chat> result = repository.findByMemberIdAndTypeNot(1L, ChatType.DELETED);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(10L);
        verify(chatRepository).findByMemberIdAndTypeNot(1L, "deleted");
    }

    /**
     * Проверяет выборку чатов без указанного типа.
     */
    @Test
    void findAllByTypeNotOrderByCreatedAtDescMapsEntities() {
        when(chatRepository.findByTypeNotOrderByCreatedAtDesc(ChatType.DELETED.getValue()))
                .thenReturn(List.of(chatEntity(10L)));

        List<Chat> result = repository.findAllByTypeNotOrderByCreatedAtDesc(ChatType.DELETED);

        assertThat(result).hasSize(1);
        verify(chatRepository).findByTypeNotOrderByCreatedAtDesc("deleted");
    }

    /**
     * Проверяет поиск чатов по названию или идентификатору.
     */
    @Test
    void searchDelegatesToRepository() {
        when(chatRepository.searchChats("Tea", 10L)).thenReturn(List.of(chatEntity(10L)));

        List<Chat> result = repository.search("Tea", 10L);

        assertThat(result).hasSize(1);
        verify(chatRepository).searchChats("Tea", 10L);
    }

    /**
     * Проверяет сохранение нового чата с подстановкой участников.
     */
    @Test
    void saveCreatesNewEntityWithMembers() {
        UserEntity first = userEntity(1L);
        UserEntity second = userEntity(2L);
        when(userRepository.getReferenceById(1L)).thenReturn(first);
        when(userRepository.getReferenceById(2L)).thenReturn(second);
        when(chatRepository.save(any(ChatEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Chat chat = new Chat(null, "Team", ChatType.GROUP, CREATED_AT, 1L, Set.of(1L, 2L));
        Chat result = repository.save(chat);

        ArgumentCaptor<ChatEntity> captor = ArgumentCaptor.forClass(ChatEntity.class);
        verify(chatRepository).save(captor.capture());
        ChatEntity saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Team");
        assertThat(saved.getType()).isEqualTo("group");
        assertThat(saved.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(saved.getCreatedById()).isEqualTo(1L);
        assertThat(saved.getMembers()).containsExactlyInAnyOrder(first, second);
        assertThat(result.memberIds()).containsExactlyInAnyOrder(1L, 2L);
        verify(chatRepository, never()).findById(any(Long.class));
    }

    /**
     * Проверяет обновление существующего чата.
     */
    @Test
    void saveUpdatesExistingEntity() {
        ChatEntity existing = chatEntity(10L);
        when(chatRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userRepository.getReferenceById(1L)).thenReturn(userEntity(1L));
        when(chatRepository.save(existing)).thenReturn(existing);

        Chat chat = new Chat(10L, "Renamed", ChatType.PRIVATE, CREATED_AT, 1L, Set.of(1L));
        Chat result = repository.save(chat);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(existing.getName()).isEqualTo("Renamed");
        assertThat(existing.getType()).isEqualTo("private");
        assertThat(existing.getMembers()).extracting(UserEntity::getId).containsExactly(1L);
        verify(chatRepository).findById(10L);
        verify(chatRepository).save(existing);
    }

    /**
     * Создаёт JPA-сущность чата.
     *
     * @param id идентификатор чата
     * @return сущность чата
     */
    private ChatEntity chatEntity(Long id) {
        ChatEntity entity = new ChatEntity();
        entity.setId(id);
        entity.setName("Team");
        entity.setType("group");
        entity.setCreatedAt(CREATED_AT);
        entity.setCreatedById(1L);
        entity.getMembers().add(userEntity(1L));
        entity.getMembers().add(userEntity(2L));
        return entity;
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
        entity.setEmail("user" + id + "@example.com");
        entity.setUsername("User" + id);
        entity.setPassword("hash");
        entity.setRole("user");
        return entity;
    }
}
