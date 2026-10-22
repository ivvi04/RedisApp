package ru.lakeevda.redisapp.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.lakeevda.redisapp.dto.chat.ChatCreateRequest;
import ru.lakeevda.redisapp.dto.chat.ChatManageMembersRequest;
import ru.lakeevda.redisapp.dto.chat.ChatResponse;
import ru.lakeevda.redisapp.security.SecurityUtils;
import ru.lakeevda.redisapp.service.ChatService;

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

    private final ChatService chatService;

    /**
     * Возвращает чаты текущего пользователя или все чаты для администратора.
     *
     * @param authentication данные аутентификации
     * @return список доступных чатов
     */
    @GetMapping
    public List<ChatResponse> getMyChats(Authentication authentication) {
        if (SecurityUtils.isAdmin(authentication)) {
            return chatService.getAllChats();
        }
        return chatService.getUserChats(Long.valueOf(authentication.getName()));
    }

    /**
     * Создаёт новый чат.
     *
     * @param request данные нового чата
     * @return созданный чат
     */
    @PostMapping
    public ChatResponse create(@Valid @RequestBody ChatCreateRequest request) {
        return chatService.create(request);
    }

    /**
     * Возвращает чат по идентификатору.
     *
     * @param chatId идентификатор чата
     * @return данные чата
     */
    @GetMapping("/{chatId}")
    public ChatResponse getById(@PathVariable Long chatId) {
        return chatService.getById(chatId);
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
        return chatService.searchChats(query, chatId);
    }

    /**
     * Добавляет или удаляет участника чата.
     *
     * @param request данные операции с участником
     */
    @PutMapping("/members")
    public void manageMembers(@Valid @RequestBody ChatManageMembersRequest request) {
        chatService.manageMember(request.chatId(), request.userId(), request.action());
    }

    /**
     * Помечает чат как удалённый.
     *
     * @param chatId идентификатор чата
     */
    @DeleteMapping("/{chatId}")
    public void delete(@PathVariable Long chatId) {
        chatService.delete(chatId);
    }
}
