package game.controller;

import game.entities.Game;
import services.dataBaseService.GameDAO;

import javax.ejb.ApplicationException;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//https://docs.oracle.com/cd/E19798-01/821-1841/gipsz/index.html - singleton concurrency
@Singleton
@ApplicationScoped
public class GameManager {

    @Inject
    private IdGenerator generator;

    @Inject
    private GameDAO gameDAO;

    @Inject
    private Deck deck;

    private Map<Integer, Game> games = new HashMap<>();

    @Schedule(hour = "*") //every hour
    public void removeGames() {
        games.values().removeIf(Game::isLeft); //safety removing from map
    }

    public String loadSavedGames(String login) {
        List<Integer> savedGameIds;
        try {
            savedGameIds = gameDAO.getSavedGames(login);
        } catch (Exception e) {
            return " no games";
        }
        return savedGameIds.isEmpty()? " no games":savedGameIds.toString();
    }

    public String getNewGames(String login) {
        //get all the games those are not full or are full but contain this player
        return games.values().stream().filter(game -> !game.onProgress() || game.hasPlayer(login)).map(Game::toString).collect(Collectors.joining("\n"));
    }

    public int createGame(String name, int numberOfPlayers) throws IllegalArgumentException {
        if (name==null || numberOfPlayers<2 || numberOfPlayers>4) throw new IllegalArgumentException();
        Game game = new Game();
        game.setId(generator.getGame_next_id());
        game.setNumberOfPlayers(numberOfPlayers);
        game.addPlayer(name);
        games.put(game.getId(), game);  //if (games.containsKey(game.getId())) throw new ????
        return game.getId();
    }

    public void joinPlayer(Integer gameId, String login) throws IllegalArgumentException {
        if (!games.containsKey(gameId) || login==null) throw new IllegalArgumentException();

        Game game = games.get(gameId);
        if (game.hasPlayer(login)) game.playerBack(login);

        else {
            game.addPlayer(login);

            if (game.isFull()) {
                game.setCardList(deck.getCards());
                game.start();
            }
        }
    }

    public void loadGame(Integer gameId, String login) throws IllegalArgumentException,PersistenceException {
        if (login==null || gameId==null) throw new IllegalArgumentException();
        if (games.containsKey(gameId)) return; //do not load again if game is already loaded by another user
        Game game = gameDAO.load(gameId, login);
        if (game == null) throw new IllegalArgumentException();
        games.put(gameId, game);
    }

    public void remove(Integer gameId) throws IllegalArgumentException{
        if (gameId==null) return;
        games.remove(gameId);
        try {
            gameDAO.remove(gameId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Game getGame(Integer id) {
        return games.get(id);
    }

    public void save(Integer gameId) {
        Game game = gameDAO.save(games.get(gameId));
        games.put(gameId, game);
    }

}
