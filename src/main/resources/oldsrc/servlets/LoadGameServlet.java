package servlets;

import game.controller.GameManager;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = "/load")
public class LoadGameServlet extends HttpServlet {
    @Inject
    private GameManager gameManager;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String login = (String) session.getAttribute("player");
        Integer gameId = Integer.valueOf(req.getParameter("gameId"));

        try {
            gameManager.loadGame(gameId, login);
            session.setAttribute("gameId", gameId);
            resp.sendRedirect("views/socket.html");
        } catch (IllegalArgumentException e) {
            req.setAttribute("loadError", "Wrong game id");
            req.getRequestDispatcher("/views/cabinet.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("loadError", "System error, try again.");
            req.getRequestDispatcher("/views/cabinet.jsp").forward(req, resp);
        }

    }
}
