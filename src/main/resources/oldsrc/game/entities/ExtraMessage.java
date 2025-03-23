package game.entities;

import game.constants.Property;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Embeddable
class ExtraMessage implements Serializable {
    private String playerOnAttack;
    private int predator;
    private String playerUnderAttack;
    private Property type;
    private List<Integer> victims;

    public ExtraMessage(){}

    ExtraMessage(String name, int id, String name1, Property type) {
        playerOnAttack = name;
        predator = id;
        playerUnderAttack = name1;
        this.type = type;
    }

    void setVictims(List<Integer> victims){
        this.victims=victims;
    }

    public void setPlayerOnAttack(String playerOnAttack) {
        this.playerOnAttack = playerOnAttack;
    }

    public void setPredator(int predator) {
        this.predator = predator;
    }

    public String getPlayerUnderAttack() {
        return playerUnderAttack;
    }

    public void setPlayerUnderAttack(String playerUnderAttack) {
        this.playerUnderAttack = playerUnderAttack;
    }

    public void setType(Property type) {
        this.type = type;
    }

    public int getPredator() {
        return predator;
    }

    public String getPlayerOnAttack() {
        return playerOnAttack;
    }

    public Property getType() {
        return type;
    }

}
