package com.company.mod.controller;

import com.company.mod.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    // @GetMapping({"/", "/login"})
    // public String login(HttpSession session) {
    // // Если пользователь уже авторизован, перенаправляем в кабинет.
    // if (session.getAttribute("player") != null) {
    // return "redirect:/cabinet";
    // }
    // return "login"; // Создайте шаблон login.html для страницы входа.
    // }
    @GetMapping("/signUp")
    public String signUpPage(HttpSession session) {
        // Если пользователь уже авторизован, перенаправляем в кабинет
        if (session.getAttribute("player") != null) {
            return "redirect:/cabinet";
        }
        return "signUp"; // имя шаблона регистрации, например, signUp.html
    }

    @PostMapping("/signIn")
    public String signIn(@RequestParam String login,
            @RequestParam String password,
            HttpSession session,
            HttpServletResponse response,
            Model model) {
        login = login.toLowerCase();
        logger.info("Attempting sign in for user: {}", login);

        if (login.isEmpty() || password.isEmpty()) {
            logger.warn("Empty login or password provided");
            model.addAttribute("signInError", "Put both login and password");
            return "login";
        }

        try {
            if (userService.isPasswordValid(login, password)) {
                List authorityList = new ArrayList();
                authorityList.add(new SimpleGrantedAuthority("ROLE_USER"));
                // Создаем токен аутентификации с ролью ROLE_USER (при необходимости добавьте
                // нужные роли)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        login,
                        null,
                        authorityList);
                // Устанавливаем аутентификацию в SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
                // Также можно сохранить информацию в сессии, если требуется
                session.setAttribute("player", login);
                Cookie cookie = new Cookie("player", login);
                response.addCookie(cookie);
                logger.info("Successful sign in for user: {}", login);
                return "redirect:/cabinet";
            } else {
                logger.warn("Invalid credentials for user: {}", login);
                model.addAttribute("signInError", "Sorry, invalid login or password");
                return "login";
            }
        } catch (Exception e) {
            logger.error("Error during sign in for user: {}", login, e);
            model.addAttribute("signInError", "System error, try again");
            return "login";
        }
    }

    @PostMapping("/signUp")
    public String signUp(@RequestParam String login,
            @RequestParam String password,
            HttpSession session,
            HttpServletResponse response,
            Model model) {
        login = login.toLowerCase();
        try {
            if (userService.userExists(login)) {
                model.addAttribute("signUpError", "Login already in use");
                return "login";
            }
            userService.createUser(login, password);
            Cookie cookie = new Cookie("player", login);
            response.addCookie(cookie);
            session.setAttribute("player", login);
            model.addAttribute("signUpMessage", "Success");
            return "login";
        } catch (Exception e) {
            model.addAttribute("signUpError", "System error, try again");
            return "login";
        }
    }

    @GetMapping("/signOut")
    public String signOut(HttpSession session) {
        session.invalidate();
        return "redirect:/index";
    }
}