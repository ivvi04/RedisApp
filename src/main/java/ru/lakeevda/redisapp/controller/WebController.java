package ru.lakeevda.redisapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("active", "index");
        return "web/index";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("active", "users");
        return "web/users";
    }

    @GetMapping("/chats")
    public String chats(Model model) {
        model.addAttribute("active", "chats");
        return "web/chats";
    }

    @GetMapping("/chats/{chatId}")
    public String chat(@PathVariable Long chatId, Model model) {
        model.addAttribute("active", "chats");
        model.addAttribute("chatId", chatId);
        return "web/chat";
    }
}
