package com.company.mod.model;

import lombok.Data;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
@Table(name = "cards")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private CardType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    @JsonManagedReference
    private CardProperty property;

    private int additionalPoints;
    private boolean isPaired;
    private String description;

    public enum CardType {
        ANIMAL,
        PROPERTY
    }

    public enum CardProperty {
        // Basic properties
        WATERFOWL("Can only be attacked by waterfowl predators"),
        FAST("Roll 4-6 to avoid attack"),
        MIMICRY("Redirect attack to another animal once per turn"),
        BIG("Can only be attacked by big predators"),
        STOMPER("Can destroy one red food token per turn"),
        POISONOUS("Kills predator that eats it"),
        TAIL_DROP("Discard property to survive attack"),
        INTERACTION("Share food with another animal"),
        HIBERNATION("Skip feeding for one turn"),
        SCAVENGER("Get blue food when another animal is eaten"),
        SYMBIOSIS("Protect another animal from attacks"),
        PIRACY("Steal food from another animal"),
        COOPERATION("Get blue food when another animal gets food"),
        BURROWING("Cannot be attacked when fed"),
        CAMOUFLAGE("Can only be attacked by sharp vision"),
        SHARP_VISION("Can attack camouflaged animals"),
        PARASITE("Can only be played on opponent's animal"),
        FAT_RESERVE("Store food for later use"),
        PREDATOR("Can attack other animals");

        private final String description;

        CardProperty(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isPaired() {
            return this == INTERACTION || this == SYMBIOSIS || this == COOPERATION;
        }

        public int getAdditionalPoints() {
            switch (this) {
                case PREDATOR:
                case BIG:
                    return 1;
                case PARASITE:
                    return 2;
                default:
                    return 0;
            }
        }
    }

    public Card() {
        this.additionalPoints = 0;
        this.isPaired = false;
    }

    public Card(CardType type, CardProperty property) {
        this.type = type;
        this.property = property;
        this.additionalPoints = property != null ? property.getAdditionalPoints() : 0;
        this.isPaired = property != null && property.isPaired();
        this.description = property != null ? property.getDescription() : null;
        this.name = type == CardType.ANIMAL ? "Animal Card" : (property != null ? property.name() : "Property Card");
    }

    public boolean isAnimal() {
        return type == CardType.ANIMAL;
    }

    public boolean isProperty() {
        return type == CardType.PROPERTY;
    }

    public boolean hasProperty(CardProperty property) {
        return this.property == property;
    }

    public boolean isPairedProperty() {
        return property != null && property.isPaired();
    }

    public int getPoints() {
        return additionalPoints;
    }
}