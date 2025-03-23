package com.company.mod.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
@Entity
@Table(name = "animals")
public class Animal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    @JsonBackReference
    private Player player;

    @ManyToOne
    @JoinColumn(name = "game_id")
    @JsonIgnore
    private Game game;

    @ElementCollection
    @CollectionTable(name = "animal_properties")
    private List<Card> properties = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isFed = false;

    @Column(nullable = false, columnDefinition = "integer default 0", name = "food_count")
    private int foodCount = 0;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int fatReserve = 0;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isHibernating = false;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean canBeAttacked = true;

    @Column(nullable = false, columnDefinition = "integer default 1", name = "max_food_count")
    private int maxFoodCount = 1;

    public Animal() {
        this.active = true;
        this.isFed = false;
        this.foodCount = 0;
        this.fatReserve = 0;
        this.isHibernating = false;
        this.canBeAttacked = true;
        this.maxFoodCount = 1;
    }

    public void addProperty(Card property) {
        if (!hasProperty(property.getProperty())) {
            properties.add(property);
        }
    }

    public void removeProperty(Card property) {
        properties.remove(property);
    }

    public boolean hasProperty(Card.CardProperty property) {
        return properties.stream()
                .anyMatch(card -> card.getProperty() == property);
    }

    public void feed() {
        if (!isHibernating) {
            isFed = true;
            foodCount++;
        }
    }

    public void addFatReserve() {
        if (isFed && hasProperty(Card.CardProperty.FAT_RESERVE)) {
            fatReserve++;
        }
    }

    public void useFatReserve() {
        if (fatReserve > 0) {
            fatReserve--;
            foodCount++;
        }
    }

    public void hibernate() {
        if (!isHibernating) {
            isHibernating = true;
            isFed = true;
        }
    }

    public void wakeUp() {
        isHibernating = false;
    }

    public boolean canBeAttackedBy(Animal attacker) {
        if (!active) {
            return false;
        }
        if (hasProperty(Card.CardProperty.WATERFOWL) &&
                !attacker.hasProperty(Card.CardProperty.WATERFOWL)) {
            return false;
        }
        if (hasProperty(Card.CardProperty.BIG) &&
                !attacker.hasProperty(Card.CardProperty.BIG)) {
            return false;
        }
        if (hasProperty(Card.CardProperty.CAMOUFLAGE) &&
                !attacker.hasProperty(Card.CardProperty.SHARP_VISION)) {
            return false;
        }
        if (hasProperty(Card.CardProperty.BURROWING) && isFed) {
            return false;
        }
        return true;
    }

    public boolean canAttack(Animal target) {
        if (!active || !hasProperty(Card.CardProperty.PREDATOR)) {
            return false;
        }
        if (isFed && fatReserve == 0) {
            return false;
        }
        return target.canBeAttackedBy(this);
    }

    public void attack(Animal target) {
        if (canAttack(target)) {
            target.setCanBeAttacked(false);
            foodCount += 2;
            isFed = true;
        }
    }

    public void defend() {
        if (hasProperty(Card.CardProperty.FAST)) {
            Random random = new Random();
            if (random.nextInt(6) + 1 >= 4) {
                canBeAttacked = false;
            }
        }
    }

    public void useMimicry(Animal newTarget) {
        if (hasProperty(Card.CardProperty.MIMICRY)) {
            newTarget.setCanBeAttacked(true);
        }
    }

    public void useTailDrop() {
        if (hasProperty(Card.CardProperty.TAIL_DROP)) {
            properties.remove(0); // Remove first property
            isFed = true;
            foodCount++;
        }
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public Animal reproduce() {
        if (!isFed) {
            throw new RuntimeException("Animal must be fed to reproduce");
        }

        Animal offspring = new Animal();
        offspring.setGame(this.game);
        offspring.setPlayer(this.player);
        offspring.setFoodCount(1);
        offspring.setProperties(new ArrayList<>(this.properties));

        return offspring;
    }
}
