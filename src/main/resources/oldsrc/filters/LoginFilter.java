package filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = "/views/*")
public class LoginFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
        //System.out.println("filter go");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
//        HttpSession session = request.getSession(false); //if there is no session - doesn't create a new one
//        String loginURI = request.getContextPath();
//
//        boolean loggedIn = session != null && session.getAttribute("player") != null;
//        boolean loginRequest = request.getRequestURI().equals(loginURI);
//        System.out.println("filter go");
//        if (loggedIn || loginRequest) {
//            chain.doFilter(request, response);
//        } else {
//            response.sendRedirect(loginURI);
//        }

        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {

    }
}
