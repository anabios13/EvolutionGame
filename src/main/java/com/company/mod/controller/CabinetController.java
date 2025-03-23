package com.company.mod.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.security.Principal;


@Controller
public class CabinetController {
    @GetMapping("/cabinet")
    public String cabinet(Principal principal, Model model) {
        // Если пользователь не аутентифицирован, Spring Security не даст попасть сюда
        model.addAttribute("player", principal.getName());
        return "cabinet";
    }
}

