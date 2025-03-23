package servlets;

import services.dataBaseService.UsersDAO;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = "/signUp")
public class SignUpServlet extends HttpServlet {

//    @Inject
//    DBService dbService;
    @Inject
    private UsersDAO usersDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String login = req.getParameter("login").toLowerCase();
        String password = req.getParameter("password");
        HttpSession session = req.getSession();

        if (login.isEmpty() || password.isEmpty()) {
            req.setAttribute("signUpError", "Put both login and password");
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        }

        try {
            if (usersDAO.addUser(login, password)) {
                Cookie cookie = new Cookie("player", login);
                resp.addCookie(cookie);
                session.setAttribute("player", login);
                RequestDispatcher r=req.getRequestDispatcher("/views/cabinet.jsp");
                r.forward(req, resp);
            } else {
                req.setAttribute("signUpError", "Sorry, this login is already in use.");
                req.getRequestDispatcher("/index.jsp").forward(req, resp);
            }
        }
        catch (Exception e) {
            req.setAttribute("signUpError", "System error, try again");
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
            e.printStackTrace();
        }
    }
}
