package com.company.mod.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "score", nullable = false, columnDefinition = "integer default 0")
    private int score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    @JsonBackReference
    private Game game;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 20)
    private List<Animal> animals = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_hand")
    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    private List<Card> hand = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_discard")
    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    private List<Card> discard = new ArrayList<>();

    // Количество заработанных фишек еды (например, для подсчёта очков)
    private int foodTokens;

    public Player() {
        this.score = 0;
    }

    public Player(String name) {
        this.name = name;
        this.score = 0;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public List<Animal> getAnimals() {
        return animals;
    }

    public void addAnimal(Animal animal) {
        animals.add(animal);
        animal.setPlayer(this);
    }

    public void removeAnimal(Animal animal) {
        animals.remove(animal);
        animal.setPlayer(null);
    }

    public boolean hasAnimal(Animal animal) {
        return animals.contains(animal);
    }

    public boolean hasCard(Card card) {
        return hand.contains(card);
    }

    public int getAnimalCount() {
        return animals.size();
    }

    public int getHandSize() {
        return hand.size();
    }

    public int getDiscardSize() {
        return discard.size();
    }

    // В конце раунда удаляем ненакормленных животных и начисляем фишки
    public void processEndOfRound() {
        animals.removeIf(animal -> !animal.isFed());
        foodTokens += animals.size();
        score += foodTokens; // Обновляем счет игрока
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public void discardCard(Card card) {
        hand.remove(card);
        discard.add(card);
    }
}
