package game.controller;

import services.dataBaseService.GameDAO;
import services.dataBaseService.IdDAO;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class IdGenerator {
    private AtomicInteger nextGameId;

    @Inject
    private GameDAO gameDAO;

    public IdGenerator(){}

    @PostConstruct
    private void setIds(){
        nextGameId =new AtomicInteger(gameDAO.getGameLastId());
    }

    int getGame_next_id(){
        return nextGameId.incrementAndGet();
    }
}
