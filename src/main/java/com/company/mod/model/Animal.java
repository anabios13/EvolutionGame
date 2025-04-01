package com.company.mod.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "animals")
public class Animal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    @JsonBackReference
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    @JsonManagedReference
    private Card card;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "animal_properties", joinColumns = @JoinColumn(name = "animal_id"), inverseJoinColumns = @JoinColumn(name = "card_id"))
    @JsonManagedReference
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 20)
    private List<Card> properties = new ArrayList<>();

    @Column(nullable = false)
    private int foodCount = 0;

    @Column(nullable = false)
    private int requiredFood = 1;

    @Column(nullable = false)
    private boolean isFed = false;

    @Column(name = "active", nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private boolean isHibernating = false;

    @Column(nullable = false)
    private boolean canBeAttacked = true;

    @Column(nullable = true)
    private boolean hasUsedFatReserve = false;

    @Column(nullable = true)
    private boolean hasUsedMimicry = false;

    @Column(nullable = true)
    private boolean hasUsedTailDrop = false;

    @Column(nullable = true)
    private boolean hasUsedPiracy = false;

    @Column(nullable = true)
    private boolean hasUsedSymbiosis = false;

    @Column(nullable = true)
    private boolean hasUsedCooperation = false;

    @Column(nullable = true)
    private boolean hasUsedInteraction = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_attacked_animal_id")
    private Animal lastAttackedAnimal;

    public Animal getLastAttackedAnimal() {
        return lastAttackedAnimal;
    }

    public void setLastAttackedAnimal(Animal lastAttackedAnimal) {
        this.lastAttackedAnimal = lastAttackedAnimal;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public boolean isActive() {
        return isActive;
    }

    public void addProperty(Card card) {
        if (!properties.contains(card)) {
            properties.add(card);
            updateRequiredFood();
        }
    }

    public void removeProperty(Card card) {
        properties.remove(card);
        updateRequiredFood();
    }

    public boolean hasProperty(Card.CardProperty property) {
        return properties.stream()
                .anyMatch(card -> card.getProperty() == property);
    }

    public void feed() {
        if (!isHibernating) {
            foodCount++;
            if (foodCount >= requiredFood) {
                isFed = true;
            }
        }
    }

    public void hibernate() {
        if (hasProperty(Card.CardProperty.HIBERNATION) && !isHibernating) {
            isHibernating = true;
            isFed = true;
        }
    }

    public void useFatReserve() {
        if (hasProperty(Card.CardProperty.FAT_RESERVE) && !hasUsedFatReserve) {
            hasUsedFatReserve = true;
            feed();
        }
    }

    public void useMimicry(Animal newTarget) {
        if (hasProperty(Card.CardProperty.MIMICRY) && !hasUsedMimicry) {
            hasUsedMimicry = true;
            // Логика перенаправления атаки будет реализована в GameService
        }
    }

    public void useTailDrop() {
        if (hasProperty(Card.CardProperty.TAIL_DROP) && !hasUsedTailDrop) {
            hasUsedTailDrop = true;
            // Логика отбрасывания свойства будет реализована в GameService
        }
    }

    public void usePiracy(Animal target) {
        if (hasProperty(Card.CardProperty.PIRACY) && !hasUsedPiracy) {
            hasUsedPiracy = true;
            if (!target.isFed()) {
                target.setFoodCount(target.getFoodCount() - 1);
                feed();
            }
        }
    }

    public void useSymbiosis(Animal symbiot) {
        if (hasProperty(Card.CardProperty.SYMBIOSIS) && !hasUsedSymbiosis) {
            hasUsedSymbiosis = true;
            setCanBeAttacked(false);
            symbiot.setCanBeAttacked(false);
        }
    }

    public void useCooperation(Animal partner) {
        if (hasProperty(Card.CardProperty.COOPERATION) && !hasUsedCooperation) {
            hasUsedCooperation = true;
            if (isFed() && !partner.isFed()) {
                partner.feed();
            }
        }
    }

    public void useInteraction(Animal partner) {
        if (hasProperty(Card.CardProperty.INTERACTION) && !hasUsedInteraction) {
            hasUsedInteraction = true;
            if (isFed() && !partner.isFed()) {
                partner.feed();
            }
        }
    }

    public Animal reproduce() {
        Animal offspring = new Animal();
        offspring.setPlayer(player);
        offspring.setRequiredFood(requiredFood);
        // Копируем свойства родителя
        for (Card property : properties) {
            offspring.addProperty(property);
        }
        return offspring;
    }

    public void attack(Animal target) {
        if (hasProperty(Card.CardProperty.PREDATOR)) {
            if (target.canBeAttacked) {
                target.setLastAttackedAnimal(this);
                target.setCanBeAttacked(false);
                // Хищник получает 2 синие фишки еды
                foodCount += 2;
                isFed = true;
            }
        }
    }

    public void defend() {
        if (hasProperty(Card.CardProperty.FAST)) {
            // Логика защиты будет реализована в GameService
        }
    }

    public boolean canAttack(Animal target) {
        if (!hasProperty(Card.CardProperty.PREDATOR)) {
            return false;
        }

        // Проверяем специальные условия атаки
        if (target.hasProperty(Card.CardProperty.WATERFOWL) && !hasProperty(Card.CardProperty.WATERFOWL)) {
            return false;
        }

        if (target.hasProperty(Card.CardProperty.BIG) && !hasProperty(Card.CardProperty.BIG)) {
            return false;
        }

        if (target.hasProperty(Card.CardProperty.CAMOUFLAGE) && !hasProperty(Card.CardProperty.SHARP_VISION)) {
            return false;
        }

        if (target.hasProperty(Card.CardProperty.BURROWING) && target.isFed()) {
            return false;
        }

        return target.isCanBeAttacked();
    }

    private void updateRequiredFood() {
        requiredFood = 1;
        for (Card property : properties) {
            if (property.getProperty() == Card.CardProperty.PREDATOR ||
                    property.getProperty() == Card.CardProperty.BIG) {
                requiredFood++;
            }
        }
    }

    public void resetPhaseState() {
        isHibernating = false;
        hasUsedFatReserve = false;
        hasUsedMimicry = false;
        hasUsedTailDrop = false;
        hasUsedPiracy = false;
        hasUsedSymbiosis = false;
        hasUsedCooperation = false;
        hasUsedInteraction = false;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
        if (card != null) {
            this.requiredFood = card.getType() == Card.CardType.ANIMAL ? 1 : 0;
            this.name = card.getType() == Card.CardType.ANIMAL ? "Animal" : "Property";
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
