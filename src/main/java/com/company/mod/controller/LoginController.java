package com.company.mod.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        // Если сессия уже содержит игрока, перенаправляем в кабинет
        if (session.getAttribute("player") != null) {
            return "redirect:/cabinet";
        }
        return "login"; // шаблон login.html
    }
}
