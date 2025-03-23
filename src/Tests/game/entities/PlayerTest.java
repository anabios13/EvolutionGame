package game.entities;


import com.company.mod.model.Animal;
import com.company.mod.model.Player;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlayerTest {
    Player player=new Player("test",1);
    Animal an1=new Animal(1,player);
    Animal an2=new Animal(2,player);
    Animal an3=new Animal(3,player);

    @Test
    public void connectAnimal() throws GameException {
        player.addAnimal(an1);
        player.addAnimal(an2);
        player.connectAnimal(1,2,Property.COOPERATION);

        assertThrows(GameException.class, ()->{
            player.connectAnimal(1,2,Property.COOPERATION);
        }, "These animals are already cooperating");

        assertThrows(GameException.class,()->{
            player.connectAnimal(1,3,Property.COMMUNICATION); //not throw any
        },"It's not your animal(s)");

        player.addAnimal(an3);
        player.connectAnimal(1,3,Property.COMMUNICATION);

        assertThrows(GameException.class, ()->{
            player.connectAnimal(1,3,Property.COMMUNICATION);
        }, "These animals are already communicating");

        player.animalsDie();
    }



}