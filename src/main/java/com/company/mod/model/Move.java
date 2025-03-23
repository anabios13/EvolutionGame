package com.company.mod.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "moves")
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    @JsonIgnore
    private Game game;

    @ManyToOne
    @JoinColumn(name = "player_id")
    @JsonIgnore
    private Player player;

    @Enumerated(EnumType.STRING)
    private MoveType type;

    @ManyToOne
    @JsonIgnore
    private Animal sourceAnimal;

    @ManyToOne
    @JsonIgnore
    private Animal targetAnimal;

    @ManyToOne
    private Card card;

    private String description;
    private LocalDateTime timestamp;

    public Move() {
        timestamp = LocalDateTime.now();
    }

    // Сеттер для установки ссылки на игру
    public void setGame(Game game) {
        this.game = game;
    }

    // Фабричные методы
    public static Move createJoinGameMove(Game game, Player player) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.type = MoveType.JOIN_GAME;
        move.description = player.getName() + " joined the game";
        return move;
    }

    public static Move createDealCardsMove(Game game, Player player) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.type = MoveType.DEAL_CARDS;
        move.description = player.getName() + " received cards";
        return move;
    }

    public static Move createAddPropertyMove(Game game, Player player, Animal animal, Card card) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = animal;
        move.card = card;
        move.type = MoveType.ADD_PROPERTY;
        move.description = player.getName() + " added property " + card.getProperty() + " to animal";
        return move;
    }

    public static Move createFeedAnimalMove(Game game, Player player, Animal animal) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = animal;
        move.type = MoveType.FEED_ANIMAL;
        move.description = player.getName() + " fed an animal";
        return move;
    }

    public static Move createReproduceMove(Game game, Player player, Animal parent, Animal offspring) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = parent;
        move.targetAnimal = offspring;
        move.type = MoveType.REPRODUCE;
        move.description = player.getName() + " reproduced an animal";
        return move;
    }

    public static Move createEndPhaseMove(Game game, Player player, GamePhase phase) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.type = MoveType.END_PHASE;
        move.description = player.getName() + " ended phase " + phase;
        return move;
    }

    public MoveType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
