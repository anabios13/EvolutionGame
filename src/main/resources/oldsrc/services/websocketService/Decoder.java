package services.websocketService;

import com.google.gson.Gson;
import game.entities.Move;

import javax.websocket.EndpointConfig;

public class Decoder implements javax.websocket.Decoder.Text<Move> {

    @Override
    public Move decode(String s) {
        return new Gson().fromJson(s, Move.class);
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
    }

    @Override
    public void destroy() {
    }
}
