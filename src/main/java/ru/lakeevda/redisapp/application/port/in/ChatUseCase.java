package ru.lakeevda.redisapp.application.port.in;

import ru.lakeevda.redisapp.application.command.CreateChatCommand;
import ru.lakeevda.redisapp.application.command.ManageChatMembersCommand;
import ru.lakeevda.redisapp.domain.model.Chat;

import java.util.List;

/**
 * Входящий порт сценариев работы с чатами.
 *
 * @author lakeev-da
 */
public interface ChatUseCase {

    /**
     * Возвращает чат по идентификатору.
     *
     * @param id идентификатор чата
     * @return чат
     */
    Chat getById(Long id);

    /**
     * Возвращает чаты пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список чатов
     */
    List<Chat> getUserChats(Long userId);

    /**
     * Возвращает все неудалённые чаты.
     *
     * @return список чатов
     */
    List<Chat> getAllChats();

    /**
     * Создаёт чат.
     *
     * @param command команда создания
     * @return созданный чат
     */
    Chat create(CreateChatCommand command);

    /**
     * Добавляет или удаляет участника чата.
     *
     * @param command команда изменения состава
     */
    void manageMember(ManageChatMembersCommand command);

    /**
     * Помечает чат как удалённый.
     *
     * @param id идентификатор чата
     */
    void delete(Long id);

    /**
     * Ищет чаты по названию или идентификатору.
     *
     * @param query  строка поиска
     * @param chatId идентификатор чата
     * @return найденные чаты
     */
    List<Chat> searchChats(String query, Long chatId);
}
