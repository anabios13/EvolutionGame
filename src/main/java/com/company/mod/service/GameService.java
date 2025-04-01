package com.company.mod.service;

import com.company.mod.model.*;
import com.company.mod.repository.GameRepository;
import com.company.mod.repository.CardRepository;
import com.company.mod.repository.PlayerRepository;
import com.company.mod.controller.WebSocketController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    @Lazy
    private WebSocketController gameWebSocketController;

    @Transactional
    public Game createGame() {
        Game game = new Game();
        game = gameRepository.save(game);
        gameWebSocketController.broadcastGameState(game.getId());
        return game;
    }

    @Transactional
    public Game joinGame(String gameId, String playerName) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        if (game.getCurrentPhase() != GamePhase.WAITING) {
            throw new RuntimeException("Game already started");
        }
        boolean exists = game.getPlayers().stream()
                .anyMatch(p -> p.getName().equals(playerName));
        if (!exists) {
            Player player = new Player(playerName);
            player.setGame(game);
            game.addPlayer(player);
            player = playerRepository.save(player);
            game.addMove(Move.createJoinGameMove(game, player));
        }
        game = gameRepository.save(game);
        gameWebSocketController.broadcastGameState(game.getId());
        return game;
    }

    @Transactional
    public Game startGame(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        if (game.getPlayers().size() < 2) {
            throw new RuntimeException("Not enough players");
        }
        if (game.getCurrentPhase() != GamePhase.WAITING) {
            return game;
        }
        log.info("Attempting to start game " + gameId);

        game.setCurrentPhase(GamePhase.DEVELOPMENT);

        Card.CardProperty[] properties = Card.CardProperty.values();
        Random random = new Random();

        for (Player player : game.getPlayers()) {
            for (int i = 0; i < 6; i++) {
                Card.CardProperty property = properties[random.nextInt(properties.length)];
                Card card = new Card(Card.CardType.ANIMAL, property);
                card = cardRepository.save(card);
                player.addCard(card);
                game.decreaseRemainingCards(1);
            }
            player = playerRepository.save(player);
        }

        game = gameRepository.save(game);
        gameWebSocketController.broadcastGameState(game.getId());
        return game;
    }

    @Transactional
    public Game makeMove(String gameId, String playerName, Map<String, Object> moveData) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        Player player = game.getPlayers().stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Player not found in game"));

        MoveType moveType = MoveType.valueOf((String) moveData.get("type"));
        Move move;

        switch (moveType) {
            case JOIN_GAME:
                move = Move.createJoinGameMove(game, player);
                break;
            case ADD_PROPERTY:
                move = handleAddProperty(game, player, moveData);
                break;
            case FEED_ANIMAL:
                move = handleFeedAnimal(game, player, moveData);
                break;
            case REPRODUCE:
                move = handleReproduce(game, player, moveData);
                break;
            case ATTACK:
                move = handleAttack(game, player, moveData);
                break;
            case USE_PROPERTY:
                move = handleUseProperty(game, player, moveData);
                break;
            case PASS:
                move = handlePass(game, player);
                break;
            case STOMP_FOOD:
                move = handleStompFood(game, player, moveData);
                break;
            case HIBERNATE:
                move = handleHibernate(game, player, moveData);
                break;
            case USE_FAT_RESERVE:
                move = handleUseFatReserve(game, player, moveData);
                break;
            case USE_MIMICRY:
                move = handleUseMimicry(game, player, moveData);
                break;
            case USE_TAIL_DROP:
                move = handleUseTailDrop(game, player, moveData);
                break;
            case USE_PIRACY:
                move = handleUsePiracy(game, player, moveData);
                break;
            case USE_SYMBIOSIS:
                move = handleUseSymbiosis(game, player, moveData);
                break;
            case USE_COOPERATION:
                move = handleUseCooperation(game, player, moveData);
                break;
            case USE_INTERACTION:
                move = handleUseInteraction(game, player, moveData);
                break;
            case PLACE_ANIMAL:
                move = handlePlaceAnimal(game, player, moveData);
                break;
            case PLACE_PROPERTY:
                move = handlePlaceProperty(game, player, moveData);
                break;
            case END_EXTINCTION:
                move = handleEndExtinction(game, player);
                break;
            default:
                throw new RuntimeException("Unknown move type: " + moveType);
        }

        game.addMove(move);
        game = gameRepository.save(game);
        gameWebSocketController.broadcastGameState(game.getId());
        return game;
    }

    private Move handleFeedAnimal(Game game, Player player, Map<String, Object> moveData) {
        Long animalId = Long.valueOf((String) moveData.get("animalId"));
        Animal animal = findAnimal(game, animalId);

        if (game.getCurrentPhase() != GamePhase.FOOD_DETERMINATION) {
            throw new RuntimeException("Кормление возможно только в фазе FEEDING");
        }
        if (!game.isCurrentPlayer(player)) {
            throw new RuntimeException("Не ваш ход");
        }
        if (animal.isFed()) {
            throw new RuntimeException("Животное уже накормлено");
        }
        if (game.getFoodTokens() <= 0) {
            throw new RuntimeException("Нет доступных фишек еды");
        }
        if (player.hasPassedFeeding()) {
            throw new RuntimeException("Вы уже завершили ход кормления");
        }

        // Кормим животное
        animal.feed();
        game.setFoodTokens(game.getFoodTokens() - 1);
        if (animal.getFoodCount() >= animal.getRequiredFood()) {
            animal.setFed(true);
        }

        // Переключаем ход на следующего игрока
        game.nextPlayer();

        // Если все игроки завершили ход или фишки еды закончились, завершаем фазу
        // кормления
        boolean allPlayersPassed = game.getPlayers().stream().allMatch(Player::hasPassedFeeding);
        if (allPlayersPassed || game.getFoodTokens() <= 0) {
            game.endFeedingPhase();
        }

        return Move.createFeedAnimalMove(game, player, animal);
    }

    // Реализация метода для репродукции животного
    private Move handleReproduce(Game game, Player player, Map<String, Object> moveData) {
        Long parentId = Long.valueOf((String) moveData.get("parentId"));
        Animal parent = findAnimal(game, parentId);
        if (!parent.isFed()) {
            throw new RuntimeException("Животное должно быть накормлено для репродукции");
        }
        Animal offspring = parent.reproduce();
        player.addAnimal(offspring);
        return Move.createReproduceMove(game, player, parent, offspring);
    }

    // Реализация метода для атаки
    private Move handleAttack(Game game, Player player, Map<String, Object> moveData) {
        Long attackerId = Long.valueOf((String) moveData.get("attackerId"));
        Long targetId = Long.valueOf((String) moveData.get("targetId"));
        Animal attacker = findAnimal(game, attackerId);
        Animal target = findAnimal(game, targetId);
        if (!attacker.canAttack(target)) {
            throw new RuntimeException("Неверная атака");
        }
        attacker.attack(target);
        target.defend();
        return Move.createAttackMove(game, player, attacker, target);
    }

    // Реализация метода для использования свойства
    private Move handleUseProperty(Game game, Player player, Map<String, Object> moveData) {
        Long animalId = Long.valueOf((String) moveData.get("animalId"));
        String propertyName = (String) moveData.get("property");
        Animal animal = findAnimal(game, animalId);
        Card.CardProperty property = Card.CardProperty.valueOf(propertyName);
        if (!animal.hasProperty(property)) {
            throw new RuntimeException("У животного нет данного свойства");
        }
        return Move.createUsePropertyMove(game, player, animal, property);
    }

    // Реализация метода для передачи хода (Pass)
    // Внутри класса GameService
    private Move handlePass(Game game, Player player) {
        // Сохраняем текущую фазу до изменений
        GamePhase currentPhase = game.getCurrentPhase();

        // Помечаем, что игрок сдал ход в зависимости от фазы
        switch (currentPhase) {
            case DEVELOPMENT:
                player.passDevelopment();
                break;
            case FOOD_DETERMINATION:
                player.passFeeding();
                break;
            default:
                throw new RuntimeException("Передача хода невозможна в текущей фазе: " + currentPhase);
        }

        // Проверяем, сдали ли ход все игроки в данной фазе
        boolean allPassed = false;
        if (currentPhase == GamePhase.DEVELOPMENT) {
            allPassed = game.getPlayers().stream().allMatch(Player::hasPassedDevelopment);
        } else if (currentPhase == GamePhase.FOOD_DETERMINATION) {
            allPassed = game.getPlayers().stream().allMatch(Player::hasPassedFeeding);
        }

        if (allPassed) {
            // Если все игроки сдали ход, завершаем фазу
            if (currentPhase == GamePhase.DEVELOPMENT) {
                game.endDevelopmentPhase();
            } else if (currentPhase == GamePhase.FOOD_DETERMINATION) {
                game.endFeedingPhase();
            }
            // Переход к следующей фазе (в случае EXTINCTION этот блок не вызывается,
            // т.к. Pass должен работать только в DEVELOPMENT или FOOD_DETERMINATION)
            game.nextPhase();
        } else {
            // Если не все сдали ход, просто переключаем текущего игрока на следующего
            game.nextPlayer();
        }

        return Move.createPassMove(game, player);
    }

    // Реализация метода для stomp food
    private Move handleStompFood(Game game, Player player, Map<String, Object> moveData) {
        Long animalId = Long.valueOf((String) moveData.get("animalId"));
        Animal animal = findAnimal(game, animalId);
        if (!animal.hasProperty(Card.CardProperty.STOMPER)) {
            throw new RuntimeException("Животное не имеет свойства STOMPER");
        }
        if (game.getYellowFoodTokens() <= 0) {
            throw new RuntimeException("Нет доступных желтых фишек еды");
        }
        game.setYellowFoodTokens(game.getYellowFoodTokens() - 1);
        game.setBlueFoodTokens(game.getBlueFoodTokens() + 1);
        return Move.createStompFoodMove(game, player, animal);
    }

    // Реализация метода для хибернации
    private Move handleHibernate(Game game, Player player, Map<String, Object> moveData) {
        Long animalId = Long.valueOf((String) moveData.get("animalId"));
        Animal animal = findAnimal(game, animalId);
        if (!animal.hasProperty(Card.CardProperty.HIBERNATION)) {
            throw new RuntimeException("Животное не может переходить в хибернацию");
        }
        animal.hibernate();
        return Move.createHibernateMove(game, player, animal);
    }

    // Реализация метода для использования жирового запаса
    private Move handleUseFatReserve(Game game, Player player, Map<String, Object> moveData) {
        Long animalId = Long.valueOf((String) moveData.get("animalId"));
        Animal animal = findAnimal(game, animalId);
        if (!animal.hasProperty(Card.CardProperty.FAT_RESERVE)) {
            throw new RuntimeException("Животное не имеет жирового запаса");
        }
        animal.useFatReserve();
        return Move.createUseFatReserveMove(game, player, animal);
    }

    // Реализация метода для использования mimicry
    private Move handleUseMimicry(Game game, Player player, Map<String, Object> moveData) {
        Long animalId = Long.valueOf((String) moveData.get("animalId"));
        Long newTargetId = Long.valueOf((String) moveData.get("newTargetId"));
        Animal animal = findAnimal(game, animalId);
        Animal newTarget = findAnimal(game, newTargetId);
        if (!animal.hasProperty(Card.CardProperty.MIMICRY)) {
            throw new RuntimeException("Животное не может использовать mimicry");
        }
        animal.useMimicry(newTarget);
        return Move.createUseMimicryMove(game, player, animal, newTarget);
    }

    // Реализация метода для использования tail drop
    private Move handleUseTailDrop(Game game, Player player, Map<String, Object> moveData) {
        Long animalId = Long.valueOf((String) moveData.get("animalId"));
        Animal animal = findAnimal(game, animalId);
        if (!animal.hasProperty(Card.CardProperty.TAIL_DROP)) {
            throw new RuntimeException("Животное не может использовать tail drop");
        }
        animal.useTailDrop();
        return Move.createUseTailDropMove(game, player, animal);
    }

    // Реализация метода для использования piracy
    private Move handleUsePiracy(Game game, Player player, Map<String, Object> moveData) {
        Long pirateId = Long.valueOf((String) moveData.get("pirateId"));
        Long targetId = Long.valueOf((String) moveData.get("targetId"));
        Animal pirate = findAnimal(game, pirateId);
        Animal target = findAnimal(game, targetId);
        if (!pirate.hasProperty(Card.CardProperty.PIRACY)) {
            throw new RuntimeException("Животное не может использовать piracy");
        }
        if (!target.isFed()) {
            throw new RuntimeException("Целевое животное не накормлено");
        }
        target.setFoodCount(target.getFoodCount() - 1);
        pirate.feed();
        return Move.createUsePiracyMove(game, player, pirate, target);
    }

    // Реализация метода для использования symbiosis
    private Move handleUseSymbiosis(Game game, Player player, Map<String, Object> moveData) {
        Long hostId = Long.valueOf((String) moveData.get("hostId"));
        Long symbiotId = Long.valueOf((String) moveData.get("symbiotId"));
        Animal host = findAnimal(game, hostId);
        Animal symbiot = findAnimal(game, symbiotId);
        if (!host.hasProperty(Card.CardProperty.SYMBIOSIS)) {
            throw new RuntimeException("Хозяин не может использовать symbiosis");
        }
        host.setCanBeAttacked(false);
        symbiot.setCanBeAttacked(false);
        return Move.createUseSymbiosisMove(game, player, host, symbiot);
    }

    // Реализация метода для использования cooperation
    private Move handleUseCooperation(Game game, Player player, Map<String, Object> moveData) {
        Long animalId = Long.valueOf((String) moveData.get("animalId"));
        Long partnerId = Long.valueOf((String) moveData.get("partnerId"));
        Animal animal = findAnimal(game, animalId);
        Animal partner = findAnimal(game, partnerId);
        if (!animal.hasProperty(Card.CardProperty.COOPERATION)) {
            throw new RuntimeException("Животное не может использовать cooperation");
        }
        if (animal.isFed() && !partner.isFed()) {
            partner.feed();
        }
        return Move.createUseCooperationMove(game, player, animal, partner);
    }

    // Реализация метода для использования interaction
    private Move handleUseInteraction(Game game, Player player, Map<String, Object> moveData) {
        Long animalId = Long.valueOf((String) moveData.get("animalId"));
        Long partnerId = Long.valueOf((String) moveData.get("partnerId"));
        Animal animal = findAnimal(game, animalId);
        Animal partner = findAnimal(game, partnerId);
        if (!animal.hasProperty(Card.CardProperty.INTERACTION)) {
            throw new RuntimeException("Животное не может использовать interaction");
        }
        if (animal.isFed() && !partner.isFed()) {
            partner.feed();
        }
        return Move.createUseInteractionMove(game, player, animal, partner);
    }

    // Реализация метода для размещения карты как животного
    private Move handlePlaceAnimal(Game game, Player player, Map<String, Object> moveData) {
        Long cardId = Long.valueOf((String) moveData.get("cardId"));
        Card card = findCard(player, cardId);
        if (card == null) {
            throw new RuntimeException("Карта не найдена в руке игрока");
        }
        Animal animal = new Animal();
        animal.setCard(card);
        animal.setPlayer(player);
        animal.setName(card.getType() == Card.CardType.ANIMAL ? "Animal" : "Property");
        player.addAnimal(animal);
        player.removeCard(card);
        return Move.createPlaceAnimalMove(game, player, animal);
    }

    // Реализация метода для размещения карты как свойства
    private Move handlePlaceProperty(Game game, Player player, Map<String, Object> moveData) {
        Long cardId = Long.valueOf((String) moveData.get("cardId"));
        Long animalId = Long.valueOf((String) moveData.get("animalId"));
        Card card = findCard(player, cardId);
        Animal animal = findAnimal(game, animalId);
        if (card == null) {
            throw new RuntimeException("Карта не найдена в руке игрока");
        }
        if (animal == null) {
            throw new RuntimeException("Животное не найдено");
        }
        if (!animal.getPlayer().equals(player)) {
            throw new RuntimeException("Животное не принадлежит игроку");
        }
        animal.addProperty(card);
        player.removeCard(card);
        return Move.createPlacePropertyMove(game, player, animal, card);
    }

    private Animal findAnimal(Game game, Long animalId) {
        return game.getPlayers().stream()
                .flatMap(p -> p.getAnimals().stream())
                .filter(a -> a.getId().equals(animalId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Animal not found"));
    }

    private Card findCard(Player player, Long cardId) {
        return player.getHand().stream()
                .filter(c -> c.getId().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Card not found"));
    }

    @Transactional
    public Game endPhase(String gameId, String playerName) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        if (!game.getCurrentPlayer().getName().equals(playerName)) {
            throw new RuntimeException("Не ваш ход для завершения фазы");
        }

        // Обработка в зависимости от текущей фазы
        switch (game.getCurrentPhase()) {
            case DEVELOPMENT:
                game.endDevelopmentPhase();
                break;
            case FOOD_DETERMINATION:
                game.endFeedingPhase();
                // После Feeding-фазы добавляем новые карты для каждого игрока
                dealNewCards(game);
                break;
            case EXTINCTION:
                // Дополнительных действий не требуется – переход просто в DEVELOPMENT
                break;
            default:
                throw new RuntimeException("Нельзя завершить фазу: " + game.getCurrentPhase());
        }

        // Переход к следующей фазе:
        // В случае EXTINCTION метод nextPhase() переводит игру в DEVELOPMENT, увеличивает счетчик раунда
        game.nextPhase();
        game = gameRepository.save(game);
        gameWebSocketController.broadcastGameState(game.getId());
        return game;
    }

    /**
     * Метод раздает новые карты игрокам.
     * Для каждого игрока количество карт равно количеству его животных + 1.
     * Каждая создаваемая карта сохраняется через cardRepository.
     */
    private void dealNewCards(Game game) {
        Card.CardProperty[] properties = Card.CardProperty.values();
        Random random = new Random();
        for (Player player : game.getPlayers()) {
            int numCards = player.getAnimals().size() + 1;
            for (int i = 0; i < numCards; i++) {
                if (game.getRemainingCards() > 0) {
                    Card card = new Card(Card.CardType.ANIMAL, properties[random.nextInt(properties.length)]);
                    // Сохраняем карту – переводим её в persistent-состояние
                    card = cardRepository.save(card);
                    // Добавляем сохраненную карту в руку игрока
                    player.addCard(card);
                    game.decreaseRemainingCards(1);
                }
            }
        }
    }

    public Game getGameState(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
    }

    private Move handleAddProperty(Game game, Player player, Map<String, Object> moveData) {
        Long animalId = Long.valueOf((String) moveData.get("animalId"));
        Long cardId = Long.valueOf((String) moveData.get("cardId"));
        Animal animal = findAnimal(game, animalId);
        Card card = findCard(player, cardId);

        if (game.getCurrentPhase() != GamePhase.DEVELOPMENT) {
            throw new RuntimeException("Можно добавлять свойства только в фазе DEVELOPMENT");
        }

        animal.addProperty(card);
        player.removeCard(card);

        return Move.createAddPropertyMove(game, player, animal, card);
    }

    private Move handleEndExtinction(Game game, Player player) {
        if (game.getCurrentPhase() != GamePhase.EXTINCTION) {
            throw new RuntimeException("Можно завершить фазу вымирания только в фазе EXTINCTION");
        }
        if (!game.isCurrentPlayer(player)) {
            throw new RuntimeException("Не ваш ход");
        }

        // Переключаем ход на следующего игрока
        game.nextPlayer();

        // Если все игроки завершили фазу вымирания, переходим к следующей фазе
        if (game.getCurrentPlayerIndex() == 0) {
            game.nextPhase();
        }

        return Move.createEndExtinctionMove(game, player);
    }

}
