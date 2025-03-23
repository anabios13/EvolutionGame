package game.entities;

import com.google.gson.annotations.Expose;
import game.constants.Constants;
import game.constants.Property;
import game.controller.GameException;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
public class Player{

    private transient int requiredCards = Constants.START_NUMBER_OF_CARDS.getValue();
    private transient int points;
    private transient boolean leftGame;
    private final transient List<Card> newCards=new ArrayList<>();
    private transient int deletedCard;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int orderInMove;
    private int usedCards;

    //include json
    @Expose
    private String name;
    //@Expose
    @ManyToMany(cascade = CascadeType.PERSIST)
    private final List<Card> cards = new ArrayList<>();

    @Expose
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner") //no player - no animals //orphanRemoval=true
    private final Map<Integer, Animal> animals = new HashMap<>();
    @Expose
    private boolean doEat;

    public Player() {}

    public Player(String login, int order) {
        this.name = login;
        this.orderInMove = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    int getOrder() {
        return orderInMove;
    }

    void setDoEat(boolean bool) {
        doEat = bool;
    }

    void resetGrazing() {
        animals.forEach((k, v) -> v.setDoGrazing(false));
    }

    void leaveGame() {
        leftGame = true;
    }

    void backToGame() {
        leftGame = false;
    }

    boolean doLeaveGame() {
        return leftGame;
    }

    boolean doEat() {
        return doEat;
    }

    void addCard(Card card) {
        cards.add(card);
        requiredCards--;
        newCards.add(card);
    }

    void deleteCard(int id) {
        for (Card card : cards) {
            if (card.getId() == id) {
                cards.remove(card);
                deletedCard=id;
                break;
            }
        }
    }

    List<Card> getNewCards(){
        List<Card> result=new ArrayList<>(newCards);
        newCards.clear();
        return new ArrayList<>(result);
    }

    int getDeletedCard(){
        int result=deletedCard;
        deletedCard=0;
        return result;
    }

    boolean hasNewCards(){
        return !newCards.isEmpty();
    }

    boolean hasDeletedCard(){
        return deletedCard!=0;
    }

    int getPoints() {
        animals.forEach((k, v) -> points += v.hungry); //how to calculate double cards?
        return points;
    }

    String pointsToString() {
        return name + ": " + points + " points, " + usedCards + " cards used.";
    }

    void addAnimal(Animal animal) {
        animals.put(animal.getId(), animal);
    }

    //return true if player has any scavenger that can be fed; feed only one of them. Player should choose which one, actually
    boolean feedScavenger() {
        for (Animal animal : animals.values()) {
            if (animal.hasProperty(Property.SCAVENGER) && animal.hungry > 0) {
                animal.eatFish(1);
                return true;
            }
        }
        return false;
    }

    void connectAnimal(int id1, int id2, Property property) throws GameException {

        if (animals.size() < 2) throw new GameException("You don't have enough animals");

        if (!animals.containsKey(id1) || !animals.containsKey(id2))
            throw new GameException("It's not your animal(s)");

        if (id1 == 0 || id2 == 0)
            throw new GameException("You must pick two animals to play this card");

        else if (id1 == id2)
            throw new GameException("You must play this property on two different animals");

        Animal animal = animals.get(id1);
        Animal animal2 = animals.get(id2);

        switch (property) {
            case COMMUNICATION:
                if (animal.isCommunicate(id2) || animal.isCooperate(id2))
                    throw new GameException("These animals are already helping each other");
                else {
                    animal.setCommunicateTo(id2);
                    animal2.setCommunicateTo(id1);
                    animal.addProperty(property);
                    animal2.addProperty(property);
                }
                break;
            case COOPERATION:
                if (animal.isCommunicate(id2) || animal.isCooperate(id2))
                    throw new GameException("These animals are already helping each other");
                else {
                    animal.setCooperateTo(id2);
                    animal2.setCooperateTo(id1);
                    animal.addProperty(property);
                    animal2.addProperty(property);
                }
                break;
            case SYMBIOSIS:
                if (animal.isInSymbiosis(id2) || animal.isSymbiontFor(id2))
                    throw new GameException("Animal #" + id1 + " is already in symbiosisWith with animal #" + id2);
                else {
                    animal.setSymbiosisWith(id2);
                    animal2.setSymbiontFor(id1);
                    animal.addProperty(property);
                    animal2.addProperty(property);
                }
        }
        animal.notifyObserver("change");
        animal2.notifyObserver("change");
    }

    void animalsDie() {
        Collection<Animal> all = animals.values();
        Iterator<Animal> it = all.iterator(); //to remove animal safety;
        while (it.hasNext()) {
            Animal animal = it.next();
            if (animal.hungry > 0 || animal.poisoned) {
                animal.die();
                usedCards += animal.hungry;
                it.remove();
            }
        }
    }

    void resetFedFlag() {
        animals.forEach((k, v) -> v.fed = false);
    }

    void resetFields() {
        for (Animal an : animals.values()) {
            an.setHungry();
            an.setAttack(false);
            an.fed = false;
            an.setDoPiracy(false);
            an.setDoGrazing(false);
            doEat = false;
        }
    }

    List<Integer> canRedirectAttack(Animal predator, int victim) {
        ArrayList<Integer> canAttack = new ArrayList<>();

        for (Animal an : animals.values()) {
            if (an.getId() == victim || an.hasProperty(Property.MIMICRY)) continue;
            try {
                predator.attack(an);
                canAttack.add(an.getId());
            } catch (GameException e) {
                //if catch ex, canAttack won't be increase
            }
        }
        return canAttack;
    }

    void increaseUsedCards() {
        usedCards++;
    }

    Animal getAnimal(int id) {
        return animals.get(id);
    }

    boolean hasAnimal(int id){return animals.containsKey(id);}

    void deleteAnimal(int id) {
        animals.remove(id);
    }

    int getUsedCards() {
        return usedCards;
    }

    int getRequiredCards() {
        return requiredCards;
    }

    void setRequiredCards() {
        if (!hasAnimals() && !hasCards())
            requiredCards = Constants.START_NUMBER_OF_CARDS.getValue();
        else requiredCards = animals.size() + Constants.NUMBER_OF_EXTRA_CARD.getValue();
    }

    List<Card> getCards() {
        newCards.clear();
        deletedCard=0;
        return cards;
    }

    private boolean hasCards() {
        return !cards.isEmpty();
    }

    private boolean hasAnimals() {
        return !animals.isEmpty();
    }
}
