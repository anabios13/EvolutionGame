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
    @Column(name = "move_type")
    private MoveType type;

    @ManyToOne
    @JoinColumn(name = "source_animal_id")
    @JsonIgnore
    private Animal sourceAnimal;

    @ManyToOne
    @JoinColumn(name = "target_animal_id")
    @JsonIgnore
    private Animal targetAnimal;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;

    private String description;

    @Column(name = "created_at")
    private LocalDateTime timestamp;

    public Move() {
        timestamp = LocalDateTime.now();
    }

    // Сеттер для установки ссылки на игру
    public void setGame(Game game) {
        this.game = game;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setType(MoveType type) {
        this.type = type;
    }

    public void setSourceAnimal(Animal sourceAnimal) {
        this.sourceAnimal = sourceAnimal;
    }

    public void setTargetAnimal(Animal targetAnimal) {
        this.targetAnimal = targetAnimal;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public static Move createAttackMove(Game game, Player player, Animal attacker, Animal target) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = attacker;
        move.targetAnimal = target;
        move.type = MoveType.ATTACK;
        move.description = player.getName() + "'s " + attacker.getId() + " attacked " + target.getId();
        return move;
    }

    public static Move createUsePropertyMove(Game game, Player player, Animal animal, Card.CardProperty property) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = animal;
        move.type = MoveType.USE_PROPERTY;
        move.description = player.getName() + " used property " + property + " on animal " + animal.getId();
        return move;
    }

    public static Move createPassMove(Game game, Player player) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.type = MoveType.PASS;
        move.description = player.getName() + " passed";
        return move;
    }

    public static Move createStompFoodMove(Game game, Player player, Animal animal) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = animal;
        move.type = MoveType.STOMP_FOOD;
        move.description = player.getName() + "'s animal " + animal.getId() + " stomped food";
        return move;
    }

    public static Move createHibernateMove(Game game, Player player, Animal animal) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = animal;
        move.type = MoveType.HIBERNATE;
        move.description = player.getName() + "'s animal " + animal.getId() + " went into hibernation";
        return move;
    }

    public static Move createUseFatReserveMove(Game game, Player player, Animal animal) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = animal;
        move.type = MoveType.USE_FAT_RESERVE;
        move.description = player.getName() + "'s animal " + animal.getId() + " used fat reserve";
        return move;
    }

    public static Move createUseMimicryMove(Game game, Player player, Animal animal, Animal newTarget) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = animal;
        move.targetAnimal = newTarget;
        move.type = MoveType.USE_MIMICRY;
        move.description = player.getName() + "'s animal " + animal.getId() + " redirected attack to "
                + newTarget.getId();
        return move;
    }

    public static Move createUseTailDropMove(Game game, Player player, Animal animal) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = animal;
        move.type = MoveType.USE_TAIL_DROP;
        move.description = player.getName() + "'s animal " + animal.getId() + " used tail drop";
        return move;
    }

    public static Move createUsePiracyMove(Game game, Player player, Animal pirate, Animal target) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = pirate;
        move.targetAnimal = target;
        move.type = MoveType.USE_PIRACY;
        move.description = player.getName() + "'s animal " + pirate.getId() + " stole food from " + target.getId();
        return move;
    }

    public static Move createUseSymbiosisMove(Game game, Player player, Animal host, Animal symbiot) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = host;
        move.targetAnimal = symbiot;
        move.type = MoveType.USE_SYMBIOSIS;
        move.description = player.getName() + "'s animals " + host.getId() + " and " + symbiot.getId()
                + " formed symbiosis";
        return move;
    }

    public static Move createUseCooperationMove(Game game, Player player, Animal animal, Animal partner) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = animal;
        move.targetAnimal = partner;
        move.type = MoveType.USE_COOPERATION;
        move.description = player.getName() + "'s animals " + animal.getId() + " and " + partner.getId()
                + " cooperated";
        return move;
    }

    public static Move createUseInteractionMove(Game game, Player player, Animal animal, Animal partner) {
        Move move = new Move();
        move.setGame(game);
        move.setPlayer(player);
        move.setType(MoveType.USE_INTERACTION);
        move.setSourceAnimal(animal);
        move.setTargetAnimal(partner);
        move.setDescription(String.format("%s used interaction between %s and %s",
                player.getName(), animal.getCard().getType(), partner.getCard().getType()));
        return move;
    }

    public static Move createPlaceAnimalMove(Game game, Player player, Animal animal) {
        Move move = new Move();
        move.setGame(game);
        move.setPlayer(player);
        move.setType(MoveType.PLACE_ANIMAL);
        move.setSourceAnimal(animal);
        move.setCard(animal.getCard());
        move.setDescription(String.format("%s placed a new animal: %s",
                player.getName(), animal.getCard().getType()));
        return move;
    }

    public static Move createPlacePropertyMove(Game game, Player player, Animal animal, Card property) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.sourceAnimal = animal;
        move.card = property;
        move.type = MoveType.PLACE_PROPERTY;
        move.description = player.getName() + " placed property " + property.getProperty() + " on animal "
                + animal.getId();
        return move;
    }

    public static Move createEndExtinctionMove(Game game, Player player) {
        Move move = new Move();
        move.setGame(game);
        move.player = player;
        move.type = MoveType.END_EXTINCTION;
        move.description = player.getName() + " ended extinction phase";
        return move;
    }

    public MoveType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
