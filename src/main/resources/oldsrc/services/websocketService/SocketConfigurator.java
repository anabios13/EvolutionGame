package services.websocketService;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class SocketConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig conf, HandshakeRequest request, HandshakeResponse response) {

        HttpSession httpSession = (HttpSession)request.getHttpSession();
        conf.getUserProperties().put(HttpSession.class.getName(), httpSession);

    }
}