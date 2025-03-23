package servlets;

import services.dataBaseService.UsersDAO;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@WebServlet(urlPatterns = "/signIn")
public class SignInServlet extends HttpServlet {

    @Inject
    private UsersDAO usersDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getSession().getAttribute("player")==null) resp.sendRedirect("/index.jsp");
        req.getRequestDispatcher("/views/cabinet.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String login = req.getParameter("login").toLowerCase();
        String password = req.getParameter("password");
        System.out.println("/signIn");
        if (login.isEmpty() || password.isEmpty()) {
            req.setAttribute("signInError", "Put both login and password");
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        }

        HttpSession session = req.getSession();

        try {
            if (usersDAO.isPasswordValid(login, password)) {
                Cookie cookie = new Cookie("player", login);
                resp.addCookie(cookie);
                session.setAttribute("player", login);
                req.getRequestDispatcher("/views/cabinet.jsp").forward(req, resp);
            } else {
                req.setAttribute("signInError", "Sorry,invalid login or password");
                req.getRequestDispatcher("/index.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            req.setAttribute("signInError", "System error, try again");
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        }
    }
}
