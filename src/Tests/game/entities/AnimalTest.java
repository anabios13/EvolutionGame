package game.entities;

import game.constants.Property;
import game.controller.GameException;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AnimalTest {

    @Test
   public void addProperty() throws GameException {
        Player owner1=new Player("test",1);
        int index=1;
        Animal scavenger=new Animal(index++,owner1);
        Animal predator=new Animal(index++,owner1);
        scavenger.addProperty(Property.SCAVENGER);

        assertThrows(GameException.class,()->{
            scavenger.addProperty(Property.PREDATOR);
        },"Scavenger cannot be a predator");

        predator.addProperty(Property.PREDATOR);

        assertThrows(GameException.class,()->{
            predator.addProperty(Property.SCAVENGER);
        },"Predator cannot be a scavenger");

        assertThrows(GameException.class,()->{
            predator.addProperty(Property.PREDATOR);
        },"This animal already has property: predator");

        predator.addProperty(Property.FAT);
        predator.addProperty(Property.FAT);
        assert (predator.totalFatSupply==2);
        assert (predator.hungry ==2);
    }

    @Test
    public void eatMeet() throws GameException, NoSuchFieldException, IllegalAccessException {
        //set up player with 5 connected animals
        Game game=new Game();
        Field food=game.getClass().getDeclaredField("food");
        food.setAccessible(true);
        food.setInt(game,8);
        Player player=new Player("test",1);
        int index=1;
        Animal[] animals={new Animal(index++,player),new Animal(index++,player),new Animal(index++,player),new Animal(index++,player),new Animal(index++,player)};

        for (Animal an:animals
             ) {
            player.addAnimal(an);
        }
        player.connectAnimal(1,2,Property.COOPERATION);
        player.connectAnimal(2,3,Property.COOPERATION);
        player.connectAnimal(3,4, Property.COMMUNICATION);
        player.connectAnimal(4,5,Property.COMMUNICATION);

        //feed animals 1 first
        animals[0].eatMeet(player,game);
        assertThrows (GameException.class, ()->{animals[0].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[1].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[2].eatMeet(player,game);},"This animal is fed!");
        assert (animals[3].hungry ==1);
        assert (animals[4].hungry ==1);

        //feed animal 3 first
        player.resetFedFlag();
        player.resetFields();
        food.setInt(game,8);
        animals[2].eatMeet(player,game);
        assertThrows (GameException.class, ()->{animals[0].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[1].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[2].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[3].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[4].eatMeet(player,game);},"This animal is fed!");
        assert (game.getFood()==5); //5= MAX.FOOD-3

        //feed animal 5 first
        player.resetFedFlag();
        player.resetFields();
        food.setInt(game,8);
        animals[4].eatMeet(player,game);
        assertThrows (GameException.class, ()->{animals[0].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[1].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[2].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[3].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[4].eatMeet(player,game);},"This animal is fed!");
        assert (game.getFood()==5); //max 8-3

        //feed animal 5 first, game has less food
        player.resetFedFlag();
        player.resetFields();
        food.setInt(game,2);
        animals[4].eatMeet(player,game);
        assertThrows (GameException.class, ()->{animals[3].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[4].eatMeet(player,game);},"This animal is fed!");
        assert (game.getFood()==0);
        assert (animals[0].hungry ==1);
        assert (animals[1].hungry ==1);
        assert (animals[2].hungry ==1);

        //feed animal 3 first, game has less food
        player.resetFedFlag();
        player.resetFields();
        food.setInt(game,2);
        animals[2].eatMeet(player,game);
        assertThrows (GameException.class, ()->{animals[0].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[1].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[2].eatMeet(player,game);},"This animal is fed!");
        assertThrows (GameException.class, ()->{animals[3].eatMeet(player,game);},"This animal is fed!");
        assert (animals[4].hungry ==1);
        assert (game.getFood()==0);

        //feed big animal
        player.resetFedFlag();
        player.resetFields();
        food.setInt(game,8);
        animals[0].addProperty(Property.BIG);
        animals[2].addProperty(Property.BIG);
        player.resetFields();
        animals[0].eatMeet(player,game);
        assertThrows (GameException.class, ()->{animals[1].eatMeet(player,game);},"This animal is fed!");
        assert (animals[0].hungry ==1);
        assert (animals[1].hungry ==0);
        assert (animals[2].hungry ==1);

        //feed big animal again
        animals[0].eatMeet(player,game);
        assert (animals[0].hungry ==0);
        assert (animals[1].hungry ==0);
        assert (animals[2].hungry ==1);
    }

    @Test
    public void attack() throws GameException {
        //build animals for players;
        Player test=new Player("test",1);
        Player pop=new Player("pop",2);

        //create predator
        final Animal predator=new Animal(1,test);
        predator.addProperty(Property.PREDATOR);
        test.addAnimal(predator);

        //swim animal
        Animal swim=new Animal(2,pop);
        swim.addProperty(Property.SWIMMING);
        pop.addAnimal(swim);

        //animal in symbiosisWith with swim
        Animal small=new Animal(3,pop);
        pop.addAnimal(small);
        pop.connectAnimal(3,2,Property.SYMBIOSIS);

        //simple animal connect with small
        Animal simple=new Animal(4,pop);
        pop.addAnimal(simple);
        pop.connectAnimal(3,4,Property.COOPERATION);

        //Big animal connect with small
        Animal big=new Animal(5,pop);
        big.addProperty(Property.BIG);
        pop.addAnimal(big);
        pop.connectAnimal(3,5,Property.COOPERATION);

        //non-swim predator attack swim animal
        assertThrows(GameException.class,()->{predator.attack(swim);},"Not-swimming predator can't eat swimming animal");

        //predator attack animal under symbiont defence
        assertThrows(GameException.class,()->{predator.attack(small);},"You can't eat this animal while its symbiont is alive");

        //predator become swimming
        predator.addProperty(Property.SWIMMING);
        predator.attack(swim);
        swim.die();
        predator.eatFish(2);
        assert (predator.hungry ==0);
        assertThrows(GameException.class,()->{predator.attack(small);},"This predator has been used");

        //new predator eat non-defend small
        Animal predator1=new Animal(6,test);
        predator1.addProperty(Property.PREDATOR);
        pop.addAnimal(predator1);
        predator1.attack(small);
        small.die();
        predator1.eatFish(2);
        assert (big.cooperateTo.isEmpty());
        assert (simple.cooperateTo.isEmpty());
    }

    @Test
    public void playAnimalProperty() throws GameException {
        Animal animal=new Animal(1,new Player("test",1));
        animal.addProperty(Property.HIBERNATION);
        animal.hibernate(0);
        assert(animal.notHungry());
        assertThrows(GameException.class,()->{
            animal.hibernate(1);
        },"You can't hibernate 2 rounds in a row");
        animal.hibernate(2);
    }

    @Test
    public void removeProperty() throws GameException {
        Player owner=new Player("test",2);
        Property communication=Property.COMMUNICATION;
        Animal first=new Animal(1,owner);
        Animal second=new Animal(2,owner);
        Animal third=new Animal(3,owner);
        owner.addAnimal(first);
        owner.addAnimal(second);
        owner.addAnimal(third);
        owner.connectAnimal(1,2,communication);
        owner.connectAnimal(2,3,communication);
        second.removeProperty(communication);
        assert (first.propertyList.isEmpty());
        assert (!second.propertyList.isEmpty());
        assert (!third.propertyList.isEmpty());
    }
}