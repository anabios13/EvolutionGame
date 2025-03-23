package game.entities;

import game.controller.Deck;
import game.entities.Animal;
import game.entities.Game;
import game.entities.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class GameTest {

    @Spy
    Deck deck;

    @InjectMocks
    Game first,second;

    @Test
    public void gsonExpose(){
        Game game=new Game();
        game.addPlayer("test");
        Player pl=game.getPlayer("test");
        pl.addAnimal(new Animal(1,pl));
        pl.addAnimal(new Animal(2,pl));
        String gamejson=game.getFullJson("test");
    }

    @Test
    public void testCardHolder(){
        first.setCardList(deck.getCards());
        second.setCardList(deck.getCards());
        //assert (first.getCardList().containsAll(second.getCardList()));
    }

    @Test
    public void winners() throws IllegalAccessException, NoSuchFieldException {

        Map<String,Player> players=new HashMap<>();
        Player one=new Player("one",1);
        Field usedCards=one.getClass().getDeclaredField("usedCards");
        Field points=one.getClass().getDeclaredField("points");
        usedCards.setAccessible(true);
        points.setAccessible(true);
        points.setInt(one,10);
        usedCards.setInt(one,20);
        Player two=new Player("two",2);
        points.setInt(two,10);
        usedCards.setInt(two,22);
        Player three=new Player("three",3);
        points.setInt(three,12);
        usedCards.setInt(three,25);
        players.put("one",one);
        players.put("two",two);
        players.put("three",three);
        List<Player> sorted=new ArrayList<>(players.values());
        sorted.sort(Comparator.comparing(Player::getPoints).thenComparing(Player::getUsedCards).reversed());
        //String winners=sorted.stream().map(x->x.pointsToString()).collect(Collectors.joining("\n"));
        assert (sorted.get(0)==three);
        assert (sorted.get(1)==two);
        assert (sorted.get(2)==one);
    }

    }