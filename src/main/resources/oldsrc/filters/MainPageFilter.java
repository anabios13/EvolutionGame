package filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = "/index.jsp")
public class MainPageFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("filter go");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession(false); //if there is no session - doesn't create a new one
//        boolean loggedIn = session != null && session.getAttribute("player") != null;
//        System.out.println("filter go main");
//        if (loggedIn) {
//            req.getRequestDispatcher("/views/cabinet.jsp").forward(req, resp);
//        } else {
//            req.getRequestDispatcher("/index.jsp").forward(req, resp);
//        }
        req.getRequestDispatcher("/views/cabinet.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {

    }
}
