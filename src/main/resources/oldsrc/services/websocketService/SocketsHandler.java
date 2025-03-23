package services.websocketService;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class SocketsHandler {
    private Map<Session, String> sessions = new HashMap<>(); //session and name
    private Map<Session, Integer> gamesId = new HashMap<>();//session and gameId
    private Map<Integer, Set<Session>> gamesSessions = new HashMap<>();//sessions in game

    void addSession(Session session, String name, Integer gameId) {
        sessions.put(session, name);
        gamesId.put(session, gameId);
        if (!gamesSessions.containsKey(gameId)) gamesSessions.put(gameId, new HashSet<>());
        gamesSessions.get(gameId).add(session);

    }

    void removeSession(Session session) {
        sessions.remove(session);
        Integer gameId = gamesId.get(session);
        gamesId.remove(session);
        gamesSessions.get(gameId).remove(session);
    }

    Set<Session> getGameSessions(Integer gameId) {
        return gamesSessions.get(gameId);
    }

    String getName(Session session) {
        return sessions.get(session);
    }

    Integer getGameId(Session session) {
        return gamesId.get(session);
    }


}
