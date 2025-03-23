package com.company.mod.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CardType type;

    @Enumerated(EnumType.STRING)
    private CardProperty property;

    private int additionalPoints;
    private boolean isPaired;
    private String description;

    public enum CardType {
        ANIMAL,
        PROPERTY
    }

    public enum CardProperty {
        WATERFOWL,
        FAST,
        MIMICRY,
        BIG,
        STOMPER,
        POISONOUS,
        TAIL_DROP,
        INTERACTION,
        HIBERNATION,
        SCAVENGER,
        SYMBIOSIS,
        PIRACY,
        COOPERATION,
        BURROWING,
        CAMOUFLAGE,
        SHARP_VISION,
        PARASITE,
        FAT_RESERVE,
        PREDATOR
    }
}