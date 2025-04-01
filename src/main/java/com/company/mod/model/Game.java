package com.company.mod.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;

@Entity
@Table(name = "games")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Game {
    @Id
    private String id;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 20)
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    private List<Move> moves = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private GamePhase currentPhase;

    @Column(name = "food_tokens", nullable = false, columnDefinition = "integer default 0")
    private int foodTokens;

    @Column(name = "blue_food_tokens", nullable = false, columnDefinition = "integer default 0")
    private int blueFoodTokens;

    @Column(name = "yellow_food_tokens", nullable = false, columnDefinition = "integer default 0")
    private int yellowFoodTokens;

    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    @Column(name = "current_player_index", nullable = false, columnDefinition = "integer default 0")
    private int currentPlayerIndex = 0;

    @Column(name = "round", nullable = false, columnDefinition = "integer default 0")
    private int round = 0;

    @Column(nullable = false)
    private int remainingCards;

    @Column(nullable = false)
    private boolean phaseLocked;

    @Column(nullable = false)
    private boolean developmentPhaseEnded;

    @Column(nullable = false)
    private boolean feedingPhaseEnded;

    @Column(nullable = false)
    private boolean isGameOver = false;

    @Transient
    private Random random = new Random();

    public Game() {
        this.id = UUID.randomUUID().toString();
        this.currentPhase = GamePhase.WAITING;
        this.active = true;
        this.currentPlayerIndex = 0;
        this.round = 0;
        this.remainingCards = 84; // 84 карты в стандартной колоде
        this.foodTokens = 0;
        this.blueFoodTokens = 0;
        this.yellowFoodTokens = 0;
        this.phaseLocked = false;
        this.developmentPhaseEnded = false;
        this.feedingPhaseEnded = false;
    }

    // Геттеры и сеттеры

    public String getId() {
        return id;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(GamePhase phase) {
        this.currentPhase = phase;
    }

    public int getFoodTokens() {
        return foodTokens;
    }

    public void setFoodTokens(int foodTokens) {
        this.foodTokens = foodTokens;
    }

    public int getBlueFoodTokens() {
        return blueFoodTokens;
    }

    public void setBlueFoodTokens(int blueFoodTokens) {
        this.blueFoodTokens = blueFoodTokens;
    }

    public int getYellowFoodTokens() {
        return yellowFoodTokens;
    }

    public void setYellowFoodTokens(int yellowFoodTokens) {
        this.yellowFoodTokens = yellowFoodTokens;
    }

    public boolean isActive() {
        return active;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public int getRound() {
        return round;
    }

    public int getRemainingCards() {
        return remainingCards;
    }

    public void decreaseRemainingCards(int count) {
        this.remainingCards = Math.max(0, remainingCards - count);
    }

    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
            player.setGame(this);
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);
        player.setGame(null);
    }

    public void addMove(Move move) {
        moves.add(move);
        move.setGame(this);
    }

    public Player getCurrentPlayer() {
        if (players == null || players.isEmpty()) {
            return null;
        }
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
        return players.get(currentPlayerIndex);
    }

    public void nextPlayer() {
        if (players != null && !players.isEmpty()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    // Генерация еды согласно числу игроков (метод rollDice() не изменялся)
    private int rollDice(int numberOfDice) {
        int sum = 0;
        for (int i = 0; i < numberOfDice; i++) {
            sum += ThreadLocalRandom.current().nextInt(1, 7);
        }
        return sum;
    }

    private int calculateFoodTokens(int numPlayers) {
        int tokens = 0;
        switch (numPlayers) {
            case 2:
                tokens = rollDice(1) + 2;
                break;
            case 3:
                tokens = rollDice(2);
                break;
            case 4:
                tokens = rollDice(2) + 2;
                break;
            case 5:
                tokens = rollDice(3) + 2;
                break;
            case 6:
                tokens = rollDice(3) + 4;
                break;
            case 7:
                tokens = rollDice(4) + 2;
                break;
            case 8:
                tokens = rollDice(4) + 4;
                break;
            default:
                tokens = rollDice(1) + 2;
                break;
        }
        return tokens;
    }

    /**
     * Переход между фазами:
     * WAITING, DEVELOPMENT, FOOD_DETERMINATION, FEEDING, EXTINCTION, ENDED
     */
    public void nextPhase() {
        if (phaseLocked) {
            return;
        }
        phaseLocked = true;
        try {
            Player currentPlayer = getCurrentPlayer();
            switch (currentPhase) {
                case WAITING:
                    if (players.size() >= 2) {
                        currentPhase = GamePhase.DEVELOPMENT;
                        // Раздача 6 карт каждому игроку
                        for (Player player : players) {
                            for (int i = 0; i < 6; i++) {
                                if (remainingCards > 0) {
                                    Card card = new Card();
                                    card.setType(Card.CardType.ANIMAL);
                                    player.addCard(card);
                                    decreaseRemainingCards(1);
                                }
                            }
                        }
                        for (Player player : players) {
                            player.resetPhaseState();
                        }
                    }
                    break;
                case DEVELOPMENT:
                    if (developmentPhaseEnded) {
                        currentPhase = GamePhase.FOOD_DETERMINATION;
                        this.foodTokens = calculateFoodTokens(players.size());
                        developmentPhaseEnded = false;
                        for (Player player : players) {
                            player.resetPhaseState();
                        }
                    }
                    break;
                case FOOD_DETERMINATION:
                    if (feedingPhaseEnded) {
                        currentPhase = GamePhase.EXTINCTION;
                        processExtinction();
                        //dealNewCards();
                        feedingPhaseEnded = false;
                        for (Player player : players) {
                            player.resetPhaseState();
                        }
                        if (remainingCards <= 0) {
                            currentPhase = GamePhase.ENDED;
                            calculateFinalScores();
                            isGameOver = true;
                        }
                    }
                    break;
                case EXTINCTION:
                    currentPhase = GamePhase.DEVELOPMENT;
                    round++;
                    for (Player player : players) {
                        player.resetPhaseState();
                    }
                    break;
                case ENDED:
                    // Игра завершена – переходов больше нет
                    break;
            }
            if (!isGameOver) {
                // Обновляем индекс текущего игрока, переходя к следующему
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
        } finally {
            phaseLocked = false;
        }
    }

    private void processExtinction() {
        // Вместо удаления ненакормленных животных просто помечаем их как неактивные
        for (Player player : players) {
            for (Animal animal : player.getAnimals()) {
                if (!animal.isFed()) {
                    animal.deactivate(); // помечаем как неактивное
                }
            }
            // Дополнительная логика для ядовитых животных остается без изменений
            for (Animal animal : player.getAnimals()) {
                if (animal.hasProperty(Card.CardProperty.POISONOUS)) {
                    for (Player otherPlayer : players) {
                        for (Animal predator : otherPlayer.getAnimals()) {
                            if (predator.hasProperty(Card.CardProperty.PREDATOR) &&
                                    predator.getLastAttackedAnimal() == animal) {
                                predator.deactivate();
                            }
                        }
                    }
                }
            }
        }
    }

    private void calculateFinalScores() {
        for (Player player : players) {
            int score = 0;
            // Считаем очки только для активных животных
            for (Animal animal : player.getAnimals()) {
                if (!animal.isActive()) {
                    continue;
                }
                score += 2; // 2 очка за каждое выжившее животное
                score += animal.getProperties().size(); // 1 очко за каждое свойство
                for (Card property : animal.getProperties()) {
                    switch (property.getProperty()) {
                        case PREDATOR:
                        case BIG:
                            score += 1;
                            break;
                        case PARASITE:
                            score += 2;
                            break;
                    }
                }
            }
            player.setScore(score);
        }
    }


    public void endDevelopmentPhase() {
        if (currentPhase == GamePhase.DEVELOPMENT) {
            developmentPhaseEnded = true;
        }
    }

    public void endFeedingPhase() {
        if (currentPhase == GamePhase.FOOD_DETERMINATION) {
            feedingPhaseEnded = true;
        }
    }

    public boolean isDevelopmentPhaseEnded() {
        return developmentPhaseEnded;
    }

    public boolean isFeedingPhaseEnded() {
        return feedingPhaseEnded;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public boolean isCurrentPlayer(Player player) {
        if (player == null || players == null || players.isEmpty()) {
            return false;
        }
        return player.equals(getCurrentPlayer());
    }

    public List<Player> getWinners() {
        if (!isGameOver) {
            return null;
        }
        List<Player> winners = new ArrayList<>();
        int maxScore = 0;
        for (Player player : players) {
            int score = player.calculateScore();
            if (score > maxScore) {
                maxScore = score;
                winners.clear();
                winners.add(player);
            } else if (score == maxScore) {
                winners.add(player);
            }
        }
        if (winners.size() > 1) {
            int maxDiscardSize = winners.stream()
                    .mapToInt(Player::getDiscardSize)
                    .max()
                    .orElse(0);
            winners.removeIf(player -> player.getDiscardSize() < maxDiscardSize);
        }
        return winners;
    }
}
