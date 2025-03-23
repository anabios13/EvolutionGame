package game.entities;
import com.google.gson.Gson;
import game.constants.MoveType;
import game.constants.Property;

public class Move {
    private final String player;
    private MoveType move;
    private Property property;
    private final int cardId;
    private final int animalId;
    private final int secondAnimalId;
    private final String log;


    public Move(String player, int cardId, int animalId, int secondAnimalId, String move, String property, String log){
        this.player = player;
        this.cardId = cardId;
        this.animalId=animalId;
        this.secondAnimalId=secondAnimalId;
        if (move!=null) this.move = MoveType.valueOf(move);
        if (property!=null) this.property=Property.valueOf(property);
        this.log=log;
    }

    public MoveType getMove() {
        return move;
    }

    public String getPlayer() {
        return player;
    }

    public Property getProperty(){return property;}

    String getLog(){
        return log;
    }

    int getCardId(){return cardId;}

    int getAnimalId(){return animalId;}

    int getSecondAnimalId(){return secondAnimalId;}

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
