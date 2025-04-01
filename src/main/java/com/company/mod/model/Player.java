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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "player_hand", joinColumns = @JoinColumn(name = "player_id"), inverseJoinColumns = @JoinColumn(name = "card_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<Card> hand = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "player_discard", joinColumns = @JoinColumn(name = "player_id"), inverseJoinColumns = @JoinColumn(name = "card_id"))
    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    private List<Card> discard = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean hasPassedDevelopment = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean hasPassedFeeding = false;

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

    public void addCard(Card card) {
        if (!hand.contains(card)) {
            hand.add(card);
        }
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public void discardCard(Card card) {
        if (hand.remove(card)) {
            discard.add(card);
        }
    }

    public void passDevelopment() {
        this.hasPassedDevelopment = true;
    }

    public void passFeeding() {
        this.hasPassedFeeding = true;
    }

    public boolean hasPassedDevelopment() {
        return hasPassedDevelopment;
    }

    public boolean hasPassedFeeding() {
        return hasPassedFeeding;
    }

    public void resetPhaseState() {
        this.hasPassedDevelopment = false;
        this.hasPassedFeeding = false;
        for (Animal animal : animals) {
            animal.resetPhaseState();
        }
    }

    public void processEndOfRound() {
        // Remove unfed animals
        animals.removeIf(animal -> !animal.isFed());

        // Reset phase states
        resetPhaseState();
    }

    public int calculateScore() {
        int totalScore = 0;
        for (Animal animal : animals) {
            // Base points for surviving animals
            totalScore += 2;

            // Points for properties
            totalScore += animal.getProperties().size();

            // Additional points for special properties
            for (Card property : animal.getProperties()) {
                switch (property.getProperty()) {
                    case PREDATOR:
                    case BIG:
                        totalScore += 1;
                        break;
                    case PARASITE:
                        totalScore += 2;
                        break;
                }
            }
        }
        return totalScore;
    }
}
