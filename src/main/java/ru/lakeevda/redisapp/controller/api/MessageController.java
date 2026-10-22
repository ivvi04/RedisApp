package ru.lakeevda.redisapp.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.lakeevda.redisapp.dto.message.MessageResponse;
import ru.lakeevda.redisapp.dto.message.MessageSendRequest;
import ru.lakeevda.redisapp.service.MessageService;

import java.util.List;

/**
 * REST-контроллер для работы с сообщениями.
 *
 * @author lakeev-da
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * Отправляет сообщение в чат.
     *
     * @param request данные сообщения
     * @return сохранённое сообщение
     */
    @PostMapping
    public MessageResponse send(@Valid @RequestBody MessageSendRequest request) {
        return messageService.send(request.chatId(), request.senderId(), request.content());
    }

    /**
     * Возвращает сообщения чата.
     *
     * @param chatId идентификатор чата
     * @return список сообщений
     */
    @GetMapping("/chat/{chatId}")
    public List<MessageResponse> getByChatId(@PathVariable Long chatId) {
        return messageService.getByChatId(chatId);
    }

    /**
     * Ищет сообщения внутри чата.
     *
     * @param chatId идентификатор чата
     * @param query  строка поиска
     * @return найденные сообщения
     */
    @GetMapping("/chat/{chatId}/search")
    public List<MessageResponse> searchInChat(@PathVariable Long chatId, @RequestParam String query) {
        return messageService.searchInChat(chatId, query);
    }

    /**
     * Отмечает сообщение как прочитанное.
     *
     * @param messageId идентификатор сообщения
     * @param userId    идентификатор пользователя
     * @return пустой успешный ответ
     */
    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId, @RequestParam Long userId) {
        messageService.markAsRead(messageId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Удаляет сообщение.
     *
     * @param messageId идентификатор сообщения
     * @return пустой успешный ответ
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(@PathVariable Long messageId) {
        messageService.delete(messageId);
        return ResponseEntity.ok().build();
    }
}
