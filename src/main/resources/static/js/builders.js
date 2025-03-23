function buildFoodToken(token) {
    // Пример создания элемента еды
    let img = document.createElement("img");
    img.setAttribute("src", "/images/food.png");
    img.setAttribute("class", "food-token");
    return img.outerHTML;
}

function buildCard(card) {
    // Пример создания карточки
    let div = document.createElement("div");
    div.className = "card";
    div.innerHTML = "<span>Card #" + card.id + "</span><br/><span>" + card.property + "</span>";
    return div.outerHTML;
}

function buildAnimalCard(animal) {
    // Пример создания карточки животного
    let div = document.createElement("div");
    div.className = "animal-card";
    div.innerHTML = "<span>Animal #" + animal.id + "</span><br/><span>Properties: " + animal.propertyList.join(", ") + "</span><br/><span>Food: " + animal.foodCount + "/" + animal.maxFoodCount + "</span>";
    return div.outerHTML;
}

function buildOpponentArea(opponent) {
    // Пример отображения информации о противнике
    let div = document.createElement("div");
    div.className = "opponent";
    div.innerHTML = "<span>" + opponent.name + "</span><br/><span>Animals: " + opponent.animals.length + "</span>";
    return div.outerHTML;
}

fetch('/api/games/' + gameId + '/start', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    }
})
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        console.log('Game started successfully:', data);
        window.location.href = '/game/' + data.id;
    })
    .catch(error => {
        console.error('Error starting game:', error);
        alert('Error: ' + error.message);
    });
