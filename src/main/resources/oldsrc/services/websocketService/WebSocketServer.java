package services.websocketService;

import game.constants.MoveType;
import game.controller.GameManager;
import game.entities.Game;
import game.entities.Move;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ApplicationScoped
@ServerEndpoint(value = "/views/socket", configurator = SocketConfigurator.class, decoders = {Decoder.class})
public class WebSocketServer {

    @Inject
    private GameManager gameManager;

    @Inject
    private SocketsHandler socketsHandler;

    @OnOpen
    public void open(Session session, EndpointConfig config) {
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        String player = (String) httpSession.getAttribute("player");
        Integer gameId = (Integer) httpSession.getAttribute("gameId");
        socketsHandler.addSession(session, player, gameId);
        sendToAll(session, true);
    }

    private void sendToAll(Session session, boolean sendFullGame) {
        Integer gameId = socketsHandler.getGameId(session);
        Game game = gameManager.getGame(gameId);
        for (Session s : socketsHandler.getGameSessions(gameId)) {
            try {
                String name = socketsHandler.getName(s);
                String message;
                if (sendFullGame) message = game.getFullJson(name);
                else message = game.getLightWeightJson(name);
                if (message != null) s.getBasicRemote().sendText(message); //null means error for one of the players
            } catch (IOException e) {
                socketsHandler.removeSession(session);
                e.printStackTrace();
            }
        }
        game.refresh();
        if (game.isEnd()) gameManager.remove(gameId);
    }

    private void notifyAboutLeaving(String name, Integer gameId) {
        Game game = gameManager.getGame(gameId);
        for (Session s : socketsHandler.getGameSessions(gameId)) {
            try {
                String message = game.getLightWeightJson(name);
                s.getBasicRemote().sendText(message);
            } catch (IOException e) {
                socketsHandler.removeSession(s);
                e.printStackTrace();
            }
        }
        game.refresh();
    }

    @OnMessage
    public void handleMessage(Move message, Session session) {
        Integer gameId = socketsHandler.getGameId(session);

        if (message.getMove().equals(MoveType.SAVE_GAME))
            try {
                gameManager.save(gameId);
                gameManager.getGame(gameId).makeMove(message);
            } catch (Exception e) {
                gameManager.getGame(gameId).addLogMessage("System error while saving game. Game not saved");
            }

        gameManager.getGame(gameId).makeMove(message);
        sendToAll(session, false);
    }

    @OnClose
    public void close(Session session) {
        Integer gameId = socketsHandler.getGameId(session);
        String name = socketsHandler.getName(session);

        Move message = new Move(socketsHandler.getName(session), 0, 0, 0, "LEAVE_GAME", null, " leave game");
        gameManager.getGame(gameId).makeMove(message);

        socketsHandler.removeSession(session);
        notifyAboutLeaving(name, gameId);
    }

    @OnError
    public void onError(Throwable error) {
        //Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, error);
    }


}