package ru.lakeevda.redisapp.presentation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.lakeevda.redisapp.application.command.CreateChatCommand;
import ru.lakeevda.redisapp.application.command.ManageChatMembersCommand;
import ru.lakeevda.redisapp.application.port.in.ChatUseCase;
import ru.lakeevda.redisapp.domain.enums.ChatMemberAction;
import ru.lakeevda.redisapp.infrastructure.security.SecurityUtils;
import ru.lakeevda.redisapp.presentation.api.dto.chat.ChatCreateRequest;
import ru.lakeevda.redisapp.presentation.api.dto.chat.ChatManageMembersRequest;
import ru.lakeevda.redisapp.presentation.api.dto.chat.ChatResponse;
import ru.lakeevda.redisapp.presentation.api.mapper.ChatResponseMapper;

import java.util.List;

/**
 * REST-контроллер для работы с чатами.
 *
 * @author lakeev-da
 */
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;

    /**
     * Возвращает чаты текущего пользователя или все чаты для администратора.
     *
     * @param authentication данные аутентификации
     * @return список доступных чатов
     */
    @GetMapping
    public List<ChatResponse> getMyChats(Authentication authentication) {
        return SecurityUtils.isAdmin(authentication)
                ? chatUseCase.getAllChats().stream()
                .map(ChatResponseMapper::fromDomain)
                .toList()
                : chatUseCase.getUserChats(Long.valueOf(authentication.getName())).stream()
                .map(ChatResponseMapper::fromDomain)
                .toList();
    }

    /**
     * Создаёт новый чат.
     *
     * @param request данные нового чата
     * @return созданный чат
     */
    @PostMapping
    public ChatResponse create(@Valid @RequestBody ChatCreateRequest request) {
        return ChatResponseMapper.fromDomain(chatUseCase.create(new CreateChatCommand(
                request.name(),
                request.memberIds(),
                request.createdById())));
    }

    /**
     * Возвращает чат по идентификатору.
     *
     * @param chatId идентификатор чата
     * @return данные чата
     */
    @GetMapping("/{chatId}")
    public ChatResponse getById(@PathVariable Long chatId) {
        return ChatResponseMapper.fromDomain(chatUseCase.getById(chatId));
    }

    /**
     * Ищет чаты по названию или идентификатору.
     *
     * @param query  строка поиска
     * @param chatId идентификатор чата, может быть не указан
     * @return найденные чаты
     */
    @GetMapping("/search")
    public List<ChatResponse> searchChats(@RequestParam String query,
                                          @RequestParam(required = false) Long chatId) {
        return chatUseCase.searchChats(query, chatId).stream()
                .map(ChatResponseMapper::fromDomain)
                .toList();
    }

    /**
     * Добавляет или удаляет участника чата.
     *
     * @param request данные операции с участником
     */
    @PutMapping("/members")
    public void manageMembers(@Valid @RequestBody ChatManageMembersRequest request) {
        chatUseCase.manageMember(new ManageChatMembersCommand(
                request.chatId(),
                request.userId(),
                ChatMemberAction.fromValue(request.action())));
    }

    /**
     * Помечает чат как удалённый.
     *
     * @param chatId идентификатор чата
     */
    @DeleteMapping("/{chatId}")
    public void delete(@PathVariable Long chatId) {
        chatUseCase.delete(chatId);
    }
}
