let stompClient = null;
let selectedAnimal = null;       // выбранное животное (для действий)
let selectedCard = null;         // выбранная карта из руки (как свойство)
let selectedTargetAnimal = null; // выбранное животное-цель
let currentPlayerId = null;
let currentPhase = null;
let isCurrentTurn = false;

// Подключение к WebSocket
function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/game/' + gameId, function (message) {
            const game = JSON.parse(message.body);
            updateUI(game);
        });
        stompClient.send("/app/game/" + gameId + "/state", {}, "");
    }, function (error) {
        console.error('STOMP error:', error);
        setTimeout(connectWebSocket, 5000);
    });
}

// Обновление UI согласно состоянию игры
function updateUI(game) {
    console.log('Updating UI with game state:', game);
    if (!game || !game.players) {
        console.error('Game state is undefined or incomplete');
        return;
    }

    selectedAnimal = null;
    selectedCard = null;
    selectedTargetAnimal = null;
    currentPhase = game.currentPhase;

    const currentPlayer = game.players.find(p => p.name === playerName);
    isCurrentTurn = currentPlayer && game.currentPlayerIndex === game.players.indexOf(currentPlayer);

    const updateElement = (id, value) => {
        const element = document.getElementById(id);
        if (element) element.textContent = value;
    };

    updateElement('roundCounter', game.round || 0);
    updateElement('currentPhase', game.currentPhase || 'WAITING');
    updateElement('foodTokens', game.foodTokens || 0);
    updateElement('blueFoodTokens', game.blueFoodTokens || 0);
    updateElement('yellowFoodTokens', game.yellowFoodTokens || 0);
    updateElement('deck', (game.remainingCards || 0) + ' cards');
    updateElement('boardDeck', (game.remainingCards || 0) + ' cards');

    // отрисовка токенов еды
    const foodTokensArea = document.getElementById('food-tokens-area');
    if (foodTokensArea) {
        foodTokensArea.innerHTML = "";
        for (let i = 1; i <= (game.foodTokens || 0); i++) {
            const token = document.createElement('div');
            token.className = 'food-token ' + (i % 2 === 0 ? 'red' : 'blue');
            foodTokensArea.appendChild(token);
        }
    }

    // Уникальные животные по ID
    function getUniqueAnimals(animals) {
        const seenIds = new Set();
        return (animals || []).filter(animal => {
            if (seenIds.has(animal.id)) return false;
            seenIds.add(animal.id);
            return true;
        });
    }

    // Обновляем область оппонентов
    const opponentsArea = document.getElementById('opponents-area');
    if (opponentsArea) {
        opponentsArea.innerHTML = "";
        game.players.forEach(player => {
            if (player.name !== playerName) {
                const opponentDiv = document.createElement('div');
                opponentDiv.className = 'opponent';
                const uniqueAnimals = getUniqueAnimals(player.animals);
                opponentDiv.innerHTML = `
                    <div class="opponent-name">${player.name}</div>
                    <div class="opponent-animals">
                        ${uniqueAnimals.map(animal => `
                            <div class="animal-card opponent-animal" data-animal-id="${animal.id}" onclick="selectTargetAnimal(this)">
                                <div class="animal-info">
                                    <div class="food-count">Food: ${animal.foodCount || 0}</div>
                                </div>
                                ${(animal.properties || []).map(property => `
                                    <div class="card-property" title="${property.description || ''}">
                                        ${property.property || ''}
                                    </div>
                                `).join('')}
                            </div>
                        `).join('')}
                    </div>
                `;
                opponentsArea.appendChild(opponentDiv);
            }
        });
    }

    // Обновляем область текущего игрока
    const currentPlayerArea = document.getElementById('current-player-area');
    if (currentPlayerArea && currentPlayer) {
        const playerNameElement = currentPlayerArea.querySelector('.player-name');
        if (playerNameElement) playerNameElement.textContent = currentPlayer.name;

        const playerScoreElement = currentPlayerArea.querySelector('.player-score');
        if (playerScoreElement) playerScoreElement.textContent = `Score: ${currentPlayer.score || 0}`;

        const playerCardsDiv = currentPlayerArea.querySelector('.player-cards');
        if (playerCardsDiv) {
            const uniqueAnimals = getUniqueAnimals(currentPlayer.animals);
            playerCardsDiv.innerHTML = uniqueAnimals.map(animal => `
                <div class="animal-card ${selectedAnimal === animal.id ? 'selected' : ''}" 
                     data-animal-id="${animal.id}" onclick="selectAnimal(this)">
                    <div class="animal-info">
                        <div class="food-count">Food: ${animal.foodCount || 0}</div>
                    </div>
                    ${(animal.properties || []).map(property => `
                        <div class="card-property" title="${property.description || ''}">
                            ${property.property || ''}
                        </div>
                    `).join('')}
                </div>
            `).join('');
        }

        const handCardsDiv = currentPlayerArea.querySelector('.hand-cards');
        if (handCardsDiv) {
            handCardsDiv.innerHTML = (currentPlayer.hand || []).map(card => `
                <div class="hand-card ${selectedCard === card.id ? 'selected' : ''}" 
                     data-card-id="${card.id}" onclick="selectCard(this)">
                    <div class="card-info">
                        <div class="card-type">${card.type || ''}</div>
                        <div class="card-property">Property: ${card.property || 'None'}</div>
                        ${card.additionalPoints > 0 ? `<div class="card-points">Points: ${card.additionalPoints}</div>` : ''}
                    </div>
                </div>
            `).join('');
        }
    }

    updateButtonStates(game.currentPhase, isCurrentTurn);
}


function updateButtonStates(phase, isCurrentTurn) {
    const buttons = {
        startGameButton: phase === 'WAITING' && isCurrentTurn,
        endPhaseButton: (phase === 'DEVELOPMENT' || phase === 'FOOD_DETERMINATION' || phase === 'EXTINCTION') && isCurrentTurn,
        addPropertyButton: (phase === 'DEVELOPMENT' || phase === 'EXTINCTION') && isCurrentTurn && (selectedCard !== null && selectedAnimal !== null),
        feedAnimalButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null,
        reproduceButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null,
        attackButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null && selectedTargetAnimal !== null,
        usePropertyButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null,
        passButton: (phase === 'DEVELOPMENT' || phase === 'FOOD_DETERMINATION') && isCurrentTurn,
        stompFoodButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null,
        hibernateButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null,
        useFatReserveButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null,
        useMimicryButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null && selectedTargetAnimal !== null,
        useTailDropButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null,
        usePiracyButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null && selectedTargetAnimal !== null,
        useSymbiosisButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null && selectedTargetAnimal !== null,
        useCooperationButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null && selectedTargetAnimal !== null,
        useInteractionButton: phase === 'FOOD_DETERMINATION' && isCurrentTurn && selectedAnimal !== null && selectedTargetAnimal !== null,
        placeAsAnimalButton: (phase === 'DEVELOPMENT' || phase === 'EXTINCTION') && isCurrentTurn && selectedCard !== null,
        placeAsPropertyButton: (phase === 'DEVELOPMENT' || phase === 'EXTINCTION') && isCurrentTurn && (selectedCard !== null && selectedAnimal !== null),
    };

    Object.entries(buttons).forEach(([buttonId, isEnabled]) => {
        const button = document.getElementById(buttonId);
        if (button) {
            button.disabled = !isEnabled;
            button.style.opacity = isEnabled ? '1' : '0.5';
            button.style.cursor = isEnabled ? 'pointer' : 'not-allowed';
        }
    });
}

function selectAnimal(element) {
    const animalId = element.getAttribute('data-animal-id');
    if (selectedAnimal === animalId) {
        element.classList.remove('selected');
        selectedAnimal = null;
        updateButtonStates(currentPhase, isCurrentTurn);
        return;
    }
    const previouslySelected = document.querySelector('.animal-card.selected');
    if (previouslySelected) previouslySelected.classList.remove('selected');
    element.classList.add('selected');
    selectedAnimal = animalId;
    updateButtonStates(currentPhase, isCurrentTurn);
}

function selectCard(element) {
    const cardId = element.getAttribute('data-card-id');
    if (selectedCard === cardId) {
        element.classList.remove('selected');
        selectedCard = null;
        updateButtonStates(currentPhase, isCurrentTurn);
        return;
    }
    const previouslySelected = document.querySelector('.hand-card.selected');
    if (previouslySelected) previouslySelected.classList.remove('selected');
    element.classList.add('selected');
    selectedCard = cardId;
    updateButtonStates(currentPhase, isCurrentTurn);
}

function selectTargetAnimal(element) {
    const targetId = element.getAttribute('data-animal-id');
    if (selectedTargetAnimal === targetId) {
        element.classList.remove('selected');
        selectedTargetAnimal = null;
        updateButtonStates(currentPhase, isCurrentTurn);
        return;
    }
    const previouslySelected = document.querySelector('.opponent-animal.selected');
    if (previouslySelected) previouslySelected.classList.remove('selected');
    element.classList.add('selected');
    selectedTargetAnimal = targetId;
    updateButtonStates(currentPhase, isCurrentTurn);
}

function makeMove(type, data = {}) {
    console.log('Making move:', type, data);
    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }
    const moveData = { type, ...data };
    fetch('/api/games/' + gameId + '/move', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(moveData)
    })
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(data => {
            console.log('Move response:', data);
            updateUI(data);
        })
        .catch(error => {
            console.error('Error making move:', error);
            alert('Error: ' + error.message);
        });
}

function addProperty() {
    if (selectedAnimal && selectedCard) {
        makeMove('ADD_PROPERTY', { animalId: selectedAnimal, cardId: selectedCard });
    } else {
        alert('Выберите животное и карту свойства.');
    }
}

function feedAnimal() {
    if (selectedAnimal) {
        makeMove('FEED_ANIMAL', { animalId: selectedAnimal });
    } else {
        alert('Выберите животное для кормления.');
    }
}

function reproduce() {
    if (selectedAnimal) {
        makeMove('REPRODUCE', { animalId: selectedAnimal });
    } else {
        alert('Выберите животное для репродукции.');
    }
}

function attack() {
    if (selectedAnimal && selectedTargetAnimal) {
        makeMove('ATTACK', { attackerId: selectedAnimal, targetId: selectedTargetAnimal });
    } else {
        alert('Выберите своё животное и животное-цель для атаки.');
    }
}

function useProperty() {
    if (selectedAnimal && selectedCard) {
        makeMove('USE_PROPERTY', { animalId: selectedAnimal, property: selectedCard });
    } else {
        alert('Выберите животное и карту свойства.');
    }
}

function pass() {
    makeMove('PASS');
}

function stompFood() {
    if (selectedAnimal) {
        makeMove('STOMP_FOOD', { animalId: selectedAnimal });
    } else {
        alert('Выберите животное для stomp food.');
    }
}

function hibernate() {
    if (selectedAnimal) {
        makeMove('HIBERNATE', { animalId: selectedAnimal });
    } else {
        alert('Выберите животное, которое может спать.');
    }
}

function useFatReserve() {
    if (selectedAnimal) {
        makeMove('USE_FAT_RESERVE', { animalId: selectedAnimal });
    } else {
        alert('Выберите животное с жировым запасом.');
    }
}

function useMimicry() {
    if (selectedAnimal && selectedTargetAnimal) {
        makeMove('USE_MIMICRY', { animalId: selectedAnimal, targetId: selectedTargetAnimal });
    } else {
        alert('Выберите своё животное и новую цель для mimicry.');
    }
}

function useTailDrop() {
    if (selectedAnimal) {
        makeMove('USE_TAIL_DROP', { animalId: selectedAnimal });
    } else {
        alert('Выберите животное для использования tail drop.');
    }
}

function usePiracy() {
    if (selectedAnimal && selectedTargetAnimal) {
        makeMove('USE_PIRACY', { pirateId: selectedAnimal, targetId: selectedTargetAnimal });
    } else {
        alert('Выберите своё животное-пират и животное-цель.');
    }
}

function useSymbiosis() {
    if (selectedAnimal && selectedTargetAnimal) {
        makeMove('USE_SYMBIOSIS', { hostId: selectedAnimal, symbiotId: selectedTargetAnimal });
    } else {
        alert('Выберите животное-хозяина и симбиота.');
    }
}

function useCooperation() {
    if (selectedAnimal && selectedTargetAnimal) {
        makeMove('USE_COOPERATION', { animalId: selectedAnimal, partnerId: selectedTargetAnimal });
    } else {
        alert('Выберите два животного для cooperation.');
    }
}

function useInteraction() {
    if (selectedAnimal && selectedTargetAnimal) {
        makeMove('USE_INTERACTION', { animalId: selectedAnimal, partnerId: selectedTargetAnimal });
    } else {
        alert('Выберите два животного для interaction.');
    }
}

function endPhase() {
    fetch(`/api/games/${gameId}/end-phase`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...(csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {}) }
    })
        .then(response => response.json())
        .then(game => {
            selectedAnimal = null;
            selectedCard = null;
            selectedTargetAnimal = null;
            updateUI(game);
        })
        .catch(error => {
            console.error('Error ending phase:', error);
            alert('Error ending phase: ' + error.message);
        });
}

function startGame() {
    if (!gameId) {
        console.error('Game ID is not set');
        return;
    }
    fetch(`/api/games/${gameId}/start`, { method: 'POST' })
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(game => {
            // alert("Game started: " + game.id);
            currentPlayerId = game.players.find(p => p.name === playerName)?.id;
            if (!currentPlayerId) {
                console.error('Could not find current player ID');
                return;
            }
            updateUI(game);
        })
        .catch(error => {
            console.error('Error starting game:', error);
        });
}

function leaveGame() {
    if (confirm('Вы уверены, что хотите покинуть игру?')) {
        window.location.href = '/cabinet';
    }
}

function placeCardAsAnimal() {
    if (selectedCard) {
        makeMove('PLACE_ANIMAL', { cardId: selectedCard });
        selectedCard = null;
        updatePlacementButtons();
        const handCards = document.querySelectorAll('.hand-card');
        handCards.forEach(card => card.classList.remove('selected'));
    } else {
        alert('Выберите карту для размещения как животного.');
    }
}

function placeCardAsProperty() {
    if (selectedCard && selectedAnimal) {
        makeMove('PLACE_PROPERTY', { cardId: selectedCard, animalId: selectedAnimal });
        selectedCard = null;
        selectedAnimal = null;
        updatePlacementButtons();
        const handCards = document.querySelectorAll('.hand-card');
        const animalCards = document.querySelectorAll('.animal-card');
        handCards.forEach(card => card.classList.remove('selected'));
        animalCards.forEach(card => card.classList.remove('selected'));
    } else {
        alert('Выберите карту и животное для размещения свойства.');
    }
}

function updatePlacementButtons() {
    const placeAsAnimalButton = document.getElementById('placeAsAnimalButton');
    const placeAsPropertyButton = document.getElementById('placeAsPropertyButton');
    if (placeAsAnimalButton) {
        placeAsAnimalButton.disabled = selectedCard === null;
    }
    if (placeAsPropertyButton) {
        placeAsPropertyButton.disabled = selectedCard === null || selectedAnimal === null;
    }
}

document.addEventListener('DOMContentLoaded', function () {
    connectWebSocket();
});
