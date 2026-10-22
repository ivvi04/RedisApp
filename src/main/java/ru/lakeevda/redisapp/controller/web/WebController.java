package ru.lakeevda.redisapp.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Web-контроллер: только выбор view и подготовка атрибутов страницы.
 *
 * @author lakeev-da
 */
@Controller
public class WebController {

    /**
     * Показывает главную страницу администратора.
     *
     * @param model модель представления
     * @return имя представления
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("active", "index");
        return "web/index";
    }

    /**
     * Показывает страницу управления пользователями.
     *
     * @param model модель представления
     * @return имя представления
     */
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("active", "users");
        return "web/users";
    }

    /**
     * Показывает страницу чатов.
     *
     * @param model модель представления
     * @return имя представления
     */
    @GetMapping("/chats")
    public String chats(Model model) {
        model.addAttribute("active", "chats");
        return "web/chats";
    }

    /**
     * Показывает страницу конкретного чата.
     *
     * @param chatId идентификатор чата
     * @param model  модель представления
     * @return имя представления
     */
    @GetMapping("/chats/{chatId}")
    public String chat(@PathVariable Long chatId, Model model) {
        model.addAttribute("active", "chats");
        model.addAttribute("chatId", chatId);
        return "web/chat";
    }
}
