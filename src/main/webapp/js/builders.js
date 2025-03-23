function setFood(game) {
    let food = document.getElementById("food");
    while (food.firstChild)
        food.removeChild(food.firstChild);
    for (let i = 0; i < game.food; i++) food.appendChild(buildFood())
}

function buildFood() {
    let img = document.createElement('IMG');
    img.setAttribute('src', '../images/food.png');
    img.setAttribute('class', 'food active');
    return img;
}

function buildFat() {
    let img = document.createElement('IMG');
    img.setAttribute('src', '../images/fat.png');
    img.setAttribute("class", "active");
    return img;
}

function buildCards(cards){
    let personal = document.getElementById("personal");
    personal.innerHTML = "";

    for (let k = 0; k < cards.length; k++) {
        let card = cards[k];
        personal.appendChild(buildCard(card));
    }
}

function buildNewCards(cards) {
    let personal=document.getElementById("personal");
    for(let k=0;k<cards.length;k++){
        let card=cards[k];
        personal.appendChild(buildCard(card));
    }
}

function deleteCard(id) {
        let cardId="card"+id;
        document.getElementById(cardId).remove();
}

function buildPlayerBlock(player) {
    let playerBlock = document.createElement("div");
    playerBlock.id = player.name;

    let playerName = document.createElement("div");
    playerBlock.appendChild(playerName);

    if (player.name == this.playerName) {
        doEat = player.doEat;
        playerName.innerText = 'Your animals:';

        for (let id in player.animals) {
            let animal = player.animals[id];
            playerBlock.appendChild(buildAnimal(animal, true));
        }

    } else {
        playerName.innerText = player.name + "'s animals:";

        for (let id in player.animals) {
            let animal = player.animals[id];
            playerBlock.appendChild(buildAnimal(animal, false));
        }
    }
    return playerBlock;
}

function buildAnimal(animal, flag) {
    let animDiv = document.createElement("div");
    animDiv.setAttribute("class", "animal active");
    animDiv.setAttribute("id", animal.id);
    for (let key in animal) {
        if (key == "propertyList") {
            for (let m in animal.propertyList) {
                let span = document.createElement("span");
                span.setAttribute("class", "property");

                if (flag) {
                    let property = animal.propertyList[m];
                    span.className += " active";
                    buttonOnAnimal(span, property, animal.id);
                } else
                    span.appendChild(document.createTextNode(animal.propertyList[m]));
                animDiv.appendChild(span);
            }
        }
        else if (key=="ownerName") continue;
        else if (key == "currentFatSupply" && animal[key]>0) {
            let span = document.createElement("span");
            span.setAttribute("class", "fat");
            for (let i = 0; i < animal[key]; i++) {
                span.appendChild(buildFat());
            }
            span.addEventListener("click", function () {
                move = "EAT_FAT";
                firstAnimalId = animal.id;
                document.getElementById("doing").innerText = "animal #" + firstAnimalId + " eats fat supply";
            });
            animDiv.appendChild(span);

        } else if (key == "hungry" || key == "id") {
            let span = document.createElement("span");
            span.setAttribute("class", "parameter");
            span.innerText = key + ": " + animal[key];
            animDiv.appendChild(span);
        }
        else {
            let span = document.createElement("span");
            span.setAttribute("class", "parameter");
            let arr = animal[key];
            if (arr.length > 0) {
                span.innerText = key + ": " + arr.toString();
                animDiv.appendChild(span);
            }
        }
    }

    animDiv.addEventListener("click", function () {
        if (mimicry) {
            move = "PLAY_MIMICRY";
            if (mimicryVictims.includes(animal.id)) {
                document.getElementById("doing").innerText = "Redirect predator to animal #" + animal.id;
                firstAnimalId = animal.id;
            }
            else (alert("You can't redirect the predator to this animal"));
        }

        else if (firstAnimalId == null) {
            firstAnimalId = animal.id;
            document.getElementById("doing").innerText += " animal #" + firstAnimalId;
        }
        else {
            secondAnimalId = animal.id;
            let text;
            if (document.getElementById("phase").innerText == "EVOLUTION") text = " and animal #";
            else if (document.getElementById("phase").innerText == "FEED") text = " attack animal #";
            document.getElementById("doing").innerText += text + secondAnimalId;
        }
    });

    return animDiv;
}

function buttonOnAnimal(span, property, id) {
    span.addEventListener("click", function (event) {
        event.stopPropagation();
        if (document.getElementById("phase").innerText == "FEED" || tailLoss) { //active only in feed phase
            playAnimalProperty(property, id);
        }
    });
    span.innerText = property;
}

function playAnimalProperty(property, animalId) {
    draggedProperty = property;
    firstAnimalId = animalId;
    if (tailLoss) {
        move = "DELETE_PROPERTY";
        document.getElementById("doing").innerText = "Delete property  ";
    }
    else {
        move = "PLAY_ANIMAL_PROPERTY";
        document.getElementById("doing").innerText = "Play property "
    }
    document.getElementById("doing").innerText += draggedProperty + " from animal #" + animalId;
}

function playProperty(property, cardId) {
    if (document.getElementById("phase").innerText == "EVOLUTION") {
        playedCardId = cardId;
        if (property === "MakeAnimal") {
            move = "MAKE_ANIMAL";
            document.getElementById("doing").innerText = "Make animal from card # " + cardId;
        } else if (property === "DeleteProperty") {
            tailLoss = true;
            move = "DELETE_PROPERTY";
        }
        else {
            move = "PLAY_PROPERTY";
            draggedProperty = property;
            document.getElementById("doing").innerText = "play property " + draggedProperty + " from card #" + cardId;
        }
    }
}

function buildButton(name, cardId) {
    let property = document.createElement("button");
    property.addEventListener("click", function () {
        playProperty(name, cardId);
    });
    property.innerText = name;
    return property;
}

function buildCard(card) {
    let cardDiv = document.createElement("div");
    cardDiv.setAttribute("class", "card");
    cardDiv.setAttribute("id","card"+card.id);
    let number = document.createElement("span");
    number.innerText = card.id;
    cardDiv.appendChild(number);
    cardDiv.appendChild(buildButton(card.property, card.id));

    if (card.hasOwnProperty("extraProperty")) {
        if (card.extraProperty == "DELETE_PROPERTY")
            cardDiv.appendChild(buildButton("DeleteProperty", card.id));
        else
            cardDiv.appendChild(buildButton(card.extraProperty, card.id));
    }

    cardDiv.appendChild(buildButton("MakeAnimal", card.id));
    return cardDiv;
}

function buildMessage() {
    return JSON.stringify({
        "player": playerName,
        "cardId": playedCardId,
        "animalId": firstAnimalId,
        "secondAnimalId": secondAnimalId,
        "move": move,
        "property": draggedProperty,
        "log": document.getElementById("doing").innerText
    });
}