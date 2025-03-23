package servlets;

import game.controller.GameManager;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ContextConfig implements ServletContextListener {
    @Inject
    private GameManager gameManager;

//    @Inject
//    DBService dbService;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        servletContext.setAttribute("gameManager", gameManager);
        //dbService.createTable();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
