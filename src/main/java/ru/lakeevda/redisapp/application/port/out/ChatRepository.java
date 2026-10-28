package ru.lakeevda.redisapp.application.port.out;

import ru.lakeevda.redisapp.domain.enums.ChatType;
import ru.lakeevda.redisapp.domain.model.Chat;

import java.util.List;
import java.util.Optional;

/**
 * Исходящий порт хранения чатов.
 *
 * @author lakeev-da
 */
public interface ChatRepository {

    /**
     * Находит чат по идентификатору.
     *
     * @param id идентификатор чата
     * @return найденный чат
     */
    Optional<Chat> findById(Long id);

    /**
     * Проверяет, что пользователь состоит в неудалённом чате.
     *
     * @param chatId идентификатор чата
     * @param userId идентификатор пользователя
     * @return {@code true}, если пользователь является участником активного чата
     */
    boolean isActiveMember(Long chatId, Long userId);

    /**
     * Возвращает чаты участника, исключая указанный тип.
     *
     * @param userId       идентификатор пользователя
     * @param excludedType исключаемый тип
     * @return список чатов
     */
    List<Chat> findByMemberIdAndTypeNot(Long userId, ChatType excludedType);

    /**
     * Возвращает чаты, кроме указанного типа, от новых к старым.
     *
     * @param excludedType исключаемый тип
     * @return список чатов
     */
    List<Chat> findAllByTypeNotOrderByCreatedAtDesc(ChatType excludedType);

    /**
     * Ищет чаты по названию или идентификатору.
     *
     * @param query  строка поиска
     * @param chatId идентификатор чата
     * @return найденные чаты
     */
    List<Chat> search(String query, Long chatId);

    /**
     * Сохраняет чат.
     *
     * @param chat чат
     * @return сохранённый чат
     */
    Chat save(Chat chat);
}
