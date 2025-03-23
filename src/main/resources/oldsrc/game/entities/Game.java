package game.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import game.constants.Constants;
import game.constants.Phase;
import game.constants.Property;
import game.controller.*;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static javax.persistence.FetchType.EAGER;

@Entity
public class Game{

    private transient String winners; //game has winners=>game end=>never saved
    private transient String lastLogMessage;
    private transient Set<Animal> changedAnimals=new HashSet<>();
    private transient Set<Integer> deletedAnimalsId=new HashSet<>();
    private transient JsonElement element;

    private int numberOfPlayers = 2;
    private int animalID;
    private int round;
    private int playerOnMove;
    private String error;
    private StringBuilder log = new StringBuilder();

    // fields included in json
    @Id
    private int id; //set in GameManager with IdGenerator

    @ManyToMany(cascade = CascadeType.PERSIST)
    private List<Card> cardList;

    @OneToMany(cascade = CascadeType.ALL) //no game - no players //orphanremoval=true
    private Map<String, Player> players = new HashMap<>();

    @ElementCollection(fetch = EAGER)
    @OrderColumn(name = "order_index")
    private List<String> playersOrder = new ArrayList<>();

    @Embedded
    private ExtraMessage extraMessage;

    @Enumerated(EnumType.STRING)
    private Phase phase = Phase.START;

    private int food;

    public Game() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNumberOfPlayers(int n) throws IllegalArgumentException {
        if (n<Constants.MIN_NUMBER_OF_PLAYERS.getValue() || n>Constants.MAX_NUMBER_PF_PLAYERS.getValue()) throw new IllegalArgumentException();
        numberOfPlayers = n;
    }

    public void addPlayer(String login) throws IllegalArgumentException{
        if (login==null) throw new IllegalArgumentException();

        Player player = new Player(login, players.size());
        players.put(login, player);
        lastLogMessage = login + " joined game at " + new Date() + "\n";
        log.append(lastLogMessage);
    }

    public boolean isFull() {
        return players.size() == numberOfPlayers;
    }

    public void start() {
        if (!phase.equals(Phase.START)) return;
        animalID = Constants.START_CARD_INDEX.getValue();
        resetPlayersOrder();
        players.forEach((k, v) -> addCardsOnStart(v));
        playerOnMove = 0;
        phase = Phase.EVOLUTION;
    }

    public void refresh() {
        error = null;
        changedAnimals.clear();
        deletedAnimalsId.clear();
        element=null;
    }

    public String getFullJson(String name) {
        Gson gsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeHierarchyAdapter(List.class,new JsonAdapter()).create();
        Gson gson = new Gson();
        if (error != null) {
            return errorToJson(name, element, gson);
        }

        if (element==null){
            element=new JsonObject();
            element.getAsJsonObject().add("phase", gson.toJsonTree(phase));//add object
            element.getAsJsonObject().add("players", gsonExpose.toJsonTree(players));
            element.getAsJsonObject().addProperty("food", food); //add primitive
            element.getAsJsonObject().addProperty("id", id);

            element.getAsJsonObject().addProperty("playersList", new ArrayList<>(players.keySet()).toString());
            element.getAsJsonObject().addProperty("log", log.toString());

            if (extraMessage != null)
                element.getAsJsonObject().add(extraMessage.getType().toString(), gson.toJsonTree(extraMessage));

            if (phase.equals(Phase.END)) element.getAsJsonObject().addProperty("winners", winners);
            if (round == -1) element.getAsJsonObject().addProperty("last", 0);}

        //these json fields are player name sensitive
        element.getAsJsonObject().addProperty("player", name);
        element.getAsJsonObject().add("cards", gson.toJsonTree(players.get(name).getCards()));
        if (playersOrder.size() > 0 && playersOrder.get(playerOnMove).equals(name))
            element.getAsJsonObject().addProperty("status", true);
        else element.getAsJsonObject().addProperty("status", false);

        return gson.toJson(element);
    }

    public String getLightWeightJson(String name) {
        Gson gsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeHierarchyAdapter(List.class,new JsonAdapter()).create();
        Gson gson = new Gson();
        element = new JsonObject();
        if (error != null) {
            return errorToJson(name, element, gson);
        }
        element.getAsJsonObject().add("phase", gson.toJsonTree(phase));//add object
        if (!deletedAnimalsId.isEmpty()) element.getAsJsonObject().add("deleteAnimal",gsonExpose.toJsonTree(deletedAnimalsId));
        if (!changedAnimals.isEmpty()) element.getAsJsonObject().add("changedAnimal",gsonExpose.toJsonTree(changedAnimals));
        element.getAsJsonObject().addProperty("log", lastLogMessage);

        if (phase.equals(Phase.EVOLUTION)){
            if (players.get(name).hasNewCards())
                element.getAsJsonObject().add("newCards", gson.toJsonTree(players.get(name).getNewCards()));
            else if (players.get(name).hasDeletedCard())
                element.getAsJsonObject().addProperty("deletedCard",players.get(name).getDeletedCard());
        }

        if (phase.equals(Phase.FEED)) element.getAsJsonObject().addProperty("food", food); //add primitive

        if (playersOrder.size() > 0 && playersOrder.get(playerOnMove).equals(name))
            element.getAsJsonObject().addProperty("status", true);
        else element.getAsJsonObject().addProperty("status", false);

        if (extraMessage != null)
            element.getAsJsonObject().add(extraMessage.getType().toString(), gson.toJsonTree(extraMessage));

        if (phase.equals(Phase.END)) element.getAsJsonObject().addProperty("winners", winners);
        if (round == -1) element.getAsJsonObject().addProperty("last", 0);
        return gson.toJson(element);
    }

    public void playerBack(String name) {
        players.get(name).backToGame();
    }

    public boolean isEnd() {
        return phase.equals(Phase.END);
    }

    public boolean onProgress() {
        return !phase.equals(Phase.START);
    }

    public boolean hasPlayer(String name) {
        return players.containsKey(name);
    }

    public void setCardList(List<Card> cardList) {
        if (this.cardList==null) this.cardList = cardList;
    }

    public boolean isLeft() {
        for (Player player : players.values()) {
            if (!player.doLeaveGame()) return false;
        }
        return true;
    }

    public void addLogMessage(String message){
        lastLogMessage="\n"+message;
        log.append(lastLogMessage);
    }

    public void makeMove(Move move) {

        lastLogMessage = "\n" + move.getPlayer() + " " + move.getLog() + " at " + new Date();
        log.append(lastLogMessage);
        switch (move.getMove()) {
            case SAVE_GAME:
                return;
            case END_PHASE:
                playerEndsPhase(move.getPlayer());
                return;
            case LEAVE_GAME: //only log updates
                players.get(move.getPlayer()).leaveGame();
                return;
        }
        try {
            switch (phase) {
                case EVOLUTION:
                    EvolutionPhase.processMove(this,move);
                    break;
                case FEED:
                    FeedPhase.processMove(this,move);
                    break;
                case END:
                    break;
            }
        } catch (GameException e) {
            error = e.getMessage();
        }
    }

    void deleteFood() {
        food--;
    }

    void playTailLoss(Animal predator, Animal victim) {
        List<Integer> victims = new ArrayList<>(victim.getId());
        extraMessage = new ExtraMessage(predator.getOwner().getName(), predator.getId(), victim.getOwner().getName(), Property.TAIL_LOSS);
        extraMessage.setVictims(victims);
    }

    void afterTailLoss() {
        String pl = extraMessage.getPlayerOnAttack();
        playerOnMove = playersOrder.indexOf(pl);
        extraMessage = null;
    }

    void playMimicry(Animal predator, Animal victim, List<Integer> list) {
        extraMessage = new ExtraMessage(predator.getOwner().getName(), predator.getId(), victim.getOwner().getName(), Property.MIMICRY);
        extraMessage.setVictims(list);
    }

    void afterMimicry() {
        String pl = extraMessage.getPlayerOnAttack();
        playerOnMove = playersOrder.indexOf(pl);
        extraMessage = null;
    }

    void switchPlayerOnMove() {
        playerOnMove = (playerOnMove + 1) % playersOrder.size(); // circular array;
    }

    int getFood() {
        return food;
    }

    void feedScavenger(String name) {
        List<String> scavengerOwners = new ArrayList<>(players.keySet());
        int start = 0;
        for (int i = 0; i < scavengerOwners.size(); i++) {
            if (scavengerOwners.get(i).equals(name)) {
                start = i;
                break;
            }
        }
        for (int i = start; i < scavengerOwners.size() + start; i++) {
            int k = i % scavengerOwners.size(); //circular array
            Player player = players.get(scavengerOwners.get(k));
            if (player.feedScavenger())
                break;
        }
    }

    void addLogMessage(String... s) {
        StringBuilder sb = new StringBuilder(log);
        for (String str : s)
            sb.append(str);

        lastLogMessage = sb.toString();
        log.append(lastLogMessage);
    }

    void makeAnimal(Move move) {
        Player player = players.get(move.getPlayer());
        Animal animal = new Animal(animalID++, player);
        animal.setObserver(this);
        changedAnimals.add(animal);
        player.deleteCard(move.getCardId());
        player.addAnimal(animal);
    }

    void updateChanges(Animal animal,String type){
        if ("change".equals(type)) changedAnimals.add(animal);
        else if("delete".equals(type)) {
            animal.deleteObserver();
            deletedAnimalsId.add(animal.getId());
        }
    }

    Animal getAnimal(int id) {
        for (Player player : players.values())
            if (player.hasAnimal(id))
                return player.getAnimal(id);
        return null;
    }

    Player getPlayer(String name) {
        return players.get(name);
    }

    List<String> getPlayersOrder() {
        return playersOrder;
    }

    Phase getPhase() {
        return phase;
    }

    int getRound() {
        return round;
    }

    ExtraMessage getExtraMessage() {
        return extraMessage;
    }

    private String errorToJson(String name, JsonElement element, Gson gson) {
        if (playersOrder.get(playerOnMove).equals(name)) {
            element.getAsJsonObject().addProperty("error", error);
            return gson.toJson(element);
        } else return null;
    }

    private void resetPlayersOrder() {
        playersOrder = new ArrayList<>(players.keySet());
        playersOrder.sort(Comparator.comparingInt(s -> players.get(s).getOrder())); //important to keep order of players
    }

    private void playerEndsPhase(String name) {
        playersOrder.remove(name);
        if (playersOrder.isEmpty())
            goToNextPhase();
        else switchPlayerOnMove();
    }

    private void goToNextPhase() {
        switch (phase) {
            case EVOLUTION:
                food = ThreadLocalRandom.current().nextInt(Constants.FOOD.minFoodFor(numberOfPlayers), Constants.FOOD.maxFoodFor(numberOfPlayers) + 1);
                phase = Phase.FEED;
                break;
            case FEED:
                if (cardList.isEmpty()) endGame();
                else {
                    for (Player pl : players.values()
                            ) {
                        pl.animalsDie();
                        pl.resetFields();
                        pl.setRequiredCards();
                    }
                    giveCards();
                    phase = Phase.EVOLUTION;
                    round++;

                }
                if (cardList.isEmpty()) round = -1;//last round
                break;
        }
        resetPlayersOrder();
        playerOnMove = round % players.size(); //circular array; each round starts by next player
    }

    private void endGame() {
        phase = Phase.END;
        List<Player> sorted = new ArrayList<>(players.values());
        sorted.sort(Comparator.comparing(Player::getPoints).thenComparing(Player::getUsedCards).reversed());
        winners = sorted.stream().map(Player::pointsToString).collect(Collectors.joining("\n"));
    }

    private void giveCards() {

        while (!cardList.isEmpty()) {
            int flag = players.size();
            for (Player player : players.values()) {
                if (player.getRequiredCards() == 0) flag--;
                else
                    player.addCard(cardList.remove(cardList.size() - 1));
                if (cardList.isEmpty()) break;
            }
            if (flag == 0) break;
        }
    }


    private void addCardsOnStart(Player player) {
        for (int i = 0; i < Constants.START_NUMBER_OF_CARDS.getValue(); i++)
            player.addCard(cardList.remove(cardList.size() - 1));

    }

    @Override
    public String toString() {
        return "#" + id + ", players: " + players.values().stream().map(Player::getName).collect(Collectors.joining(", "));
    }
}
