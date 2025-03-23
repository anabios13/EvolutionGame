package com.company.mod.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "games")
public class Game {
    @Id
    private String id;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 20)
    private List<Player> players = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private GamePhase currentPhase;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    private List<Move> moves = new ArrayList<>();

    private int foodTokens;

    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    @Column(name = "current_player_index", nullable = false, columnDefinition = "integer default 0")
    private int currentPlayerIndex = 0;

    @Column(name = "round", nullable = false, columnDefinition = "integer default 0")
    private int round = 0;

    @Transient
    private int remainingCards;

    @Transient
    private boolean phaseLocked = false;

    public Game() {
        this.id = UUID.randomUUID().toString();
        this.currentPhase = GamePhase.WAITING;
        this.active = true;
        this.currentPlayerIndex = 0;
        this.round = 0;
        this.remainingCards = 50;
        this.foodTokens = 0;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public List<Player> getPlayers() { return players; }
    public GamePhase getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(GamePhase phase) { this.currentPhase = phase; }
    public int getFoodTokens() { return foodTokens; }
    public void setFoodTokens(int foodTokens) { this.foodTokens = foodTokens; }
    public boolean isActive() { return active; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public int getRound() { return round; }
    public int getRemainingCards() { return remainingCards; }
    public void decreaseRemainingCards(int count) { this.remainingCards = Math.max(0, remainingCards - count); }

    public void addPlayer(Player player) {
        players.add(player);
        player.setGame(this);
    }

    public void addMove(Move move) {
        moves.add(move);
        move.setGame(this);
    }

    public Player getCurrentPlayer() {
        if (players == null || players.isEmpty()) { return null; }
        if (currentPlayerIndex >= players.size()) { currentPlayerIndex = 0; }
        return players.get(currentPlayerIndex);
    }

    public void nextPlayer() {
        if (players != null && !players.isEmpty()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    // Бросок кубиков (1-6)
    private int rollDice(int numberOfDice) {
        int sum = 0;
        for (int i = 0; i < numberOfDice; i++) {
            sum += ThreadLocalRandom.current().nextInt(1, 7);
        }
        return sum;
    }

    // Расчет еды согласно ТЗ (формулы зависят от количества игроков)
    private int calculateFoodTokens(int numPlayers) {
        int tokens = 0;
        switch (numPlayers) {
            case 2: tokens = rollDice(1) + 2; break;
            case 3: tokens = rollDice(2); break;
            case 4: tokens = rollDice(2) + 2; break;
            case 5: tokens = rollDice(3) + 2; break;
            case 6: tokens = rollDice(3) + 4; break;
            case 7: tokens = rollDice(4) + 2; break;
            case 8: tokens = rollDice(4) + 4; break;
            default: tokens = rollDice(1) + 2; break;
        }
        return tokens;
    }

    /**
     * Метод переключения фаз, сохраняя логику из ТЗ:
     * - WAITING: ожидание игроков.
     * - DEALING: фаза развития (раздача стартовых карт/животных).
     * - FEEDING: фаза питания (расчет и раздача фишек еды, кормление животных).
     * - REPRODUCTION: фаза вымирания и получения новых карт.
     * - ENDED: игра окончена.
     */
    public void nextPhase() {
        if (phaseLocked) { return; }
        phaseLocked = true;

        Player currentPlayer = getCurrentPlayer();

        switch (currentPhase) {
            case WAITING:
                if (players.size() >= 2) {
                    currentPhase = GamePhase.DEALING;
                    // Раздаем каждому игроку по 6 животных (как в ТЗ)
                    for (Player player : players) {
                        for (int i = 0; i < 6; i++) {
                            Animal animal = new Animal();
                            animal.setGame(this);
                            animal.setPlayer(player);
                            player.addAnimal(animal);
                        }
                    }
                }
                break;
            case DEALING:
                this.foodTokens = calculateFoodTokens(players.size());
                currentPhase = GamePhase.FEEDING;
                break;
            case FEEDING:
                boolean allFed = players.stream()
                        .flatMap(p -> p.getAnimals().stream())
                        .allMatch(Animal::isFed);
                boolean noFood = (foodTokens <= 0);
                if (allFed || noFood) {
                    currentPhase = GamePhase.REPRODUCTION;
                }
                break;
            case REPRODUCTION:
                for (Player player : players) {
                    player.processEndOfRound();
                }
                if (remainingCards > 0) {
                    currentPhase = GamePhase.DEALING;
                    round++;
                } else {
                    currentPhase = GamePhase.ENDED;
                    active = false;
                }
                currentPlayerIndex = round % players.size();
                break;
            default:
                break;
        }

        if (currentPlayer != null) {
            addMove(Move.createEndPhaseMove(this, currentPlayer, currentPhase));
        }
        phaseLocked = false;
    }

    @Transactional
    public void endPhase() {
        if (currentPhase == GamePhase.WAITING) {
            throw new IllegalStateException("Нельзя завершить фазу WAITING");
        }
        if (currentPhase == GamePhase.ENDED) {
            throw new IllegalStateException("Игра уже завершена");
        }
        if (phaseLocked) { return; }
        Player currentPlayer = getCurrentPlayer();
        if (currentPlayer != null) {
            addMove(Move.createEndPhaseMove(this, currentPlayer, currentPhase));
        }
        nextPhase();
    }
}
