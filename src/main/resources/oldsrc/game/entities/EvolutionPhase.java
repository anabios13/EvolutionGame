package game.entities;

import game.constants.Phase;
import game.constants.Property;
import game.controller.Deck;
import game.controller.GameException;

class EvolutionPhase {

    static void processMove(Game game,Move move) throws GameException {

        switch (move.getMove()) {
            case MAKE_ANIMAL:
                game.makeAnimal(move);
                break;
            case DELETE_PROPERTY:
                Player player=game.getPlayer(move.getPlayer());
                Animal animal=player.getAnimal(move.getAnimalId());
                animal.removeProperty(move.getProperty());
                player.deleteCard(move.getCardId());
                break;
            case PLAY_PROPERTY:
                if (Deck.isPropertyDouble(move.getProperty()))
                    processDoubleProperty(game,move);
                else {
                    processSimpleProperty(game,move);
                }
        }
        if (game.getPhase().equals(Phase.EVOLUTION)) game.switchPlayerOnMove(); //if new phase, do not switch player, because playersTurn is update
    }

    private static void processDoubleProperty(Game game,Move move) throws GameException {
        Player player = game.getPlayer(move.getPlayer());
        int id1=move.getAnimalId();
        int id2=move.getSecondAnimalId();
        if (id1==0 || id2==0) throw new GameException("You must pick two animals to play property "+move.getProperty());
        player.connectAnimal(id1,id2,move.getProperty());
        player.deleteCard(move.getCardId());

    }

    private static void processSimpleProperty(Game game,Move move) throws GameException {
        if (move.getAnimalId()==0) throw new GameException("You forgot pick an animal");

        Player player = game.getPlayer(move.getPlayer());
        Animal animal = player.getAnimal(move.getAnimalId());
        Property property=move.getProperty();

        if (property.equals(Property.PARASITE)) {
            if (animal!=null) throw new GameException("You can't play Parasite on your own animal");
            Animal attackedAnimal=game.getAnimal(move.getAnimalId());
            attackedAnimal.addProperty(property);

        } else {
            if (animal==null) throw new GameException("It's not your animal");
            animal.addProperty(property);
        }
        player.deleteCard(move.getCardId());
    }
}
