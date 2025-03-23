package game.entities;

import com.company.mod.model.Game;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;

import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;


@RunWith(MockitoJUnitRunner.class)
public class EvolutionPhaseTest {

    @Spy
    private Deck deck =new Deck();

    @InjectMocks
    Game game;

    String player1 = "pop";
    String player2 = "test";

    @Test
    public void endPhase1() throws IllegalAccessException, NoSuchFieldException {
        game.setCardList(deck.getCards());
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.start();
        Field playerOnMove=game.getClass().getDeclaredField("playerOnMove");
        playerOnMove.setAccessible(true);

        //player test ends phase
        game.makeMove(new Move("test", 0, 0, 0, "END_PHASE", null, null));
        assert (playerOnMove.getInt(game) == 0);
        assert (game.getPlayersOrder().get(0).equals(player1));
        assert (game.getPhase().equals(Phase.EVOLUTION));

        game.makeMove(new Move("pop", 0, 0, 0, "END_PHASE", null, null));
        assert (playerOnMove.getInt(game) == 0);
        assert (game.getPlayersOrder().size()==2);
        assert (game.getPlayersOrder().get(0).equals(player1));
        assert (game.getPhase().equals(Phase.FEED));
    }
}