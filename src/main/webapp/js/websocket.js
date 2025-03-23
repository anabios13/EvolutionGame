//window.onload = init;
const tcp = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
const host = window.location.host;
const path = window.location.pathname.substring(0, window.location.pathname.lastIndexOf(".")); //url without .html
//var path="/evo/socket";
const socket = new WebSocket(tcp + host + path);
socket.onmessage = onMessage;

let playerName, draggedProperty, playedCardId, mimicryVictims;
var move = null;
var firstAnimalId = null;
var secondAnimalId = null;
var tailLoss = false;
var mimicry = false;
var doEat = false;

function eatFood() {
    if (doEat) {
        alert("You can't eat/attack twice during one move");
        return;
    }
    move = "EAT_FOOD";
    document.getElementById("doing").innerText = "Feed ";// set firstAnimalId
}

function endMove() {
    move = "END_MOVE";
    document.getElementById("doing").innerText = "end move";
    let json = buildMessage();
    clearFields();
    socket.send(json);
}

function endPhase() {
    move = "END_PHASE";
    document.getElementById("doing").innerText = "end " + document.getElementById("phase").innerText;
    let json = buildMessage();
    clearFields(); //clear fields after message is built!
    socket.send(json);
}

function saveGame() {
    move = "SAVE_GAME";
    document.getElementById("doing").innerText = "saved game";
    let json = buildMessage();
    clearFields();
    socket.send(json);
}

function onMessage(event) {
    clearFields();
    let game = JSON.parse(event.data);
    if (game.hasOwnProperty("error")) {
        alert(game.error);
        document.getElementById("wrapper").style.pointerEvents = "auto";
        return;
    }

    //build always
    document.getElementById("phase").innerText = game.phase;
    document.getElementById("log").innerText += game.log;

    //build occasionally
    if (game.hasOwnProperty("players")) {
        let common = document.getElementById("common");
        //common.innerText = "";

        for (let name in game.players) {
            let player = game.players[name];
            if (document.getElementById(name)==null) common.appendChild(buildPlayerBlock(player));
        }
    }

    if (game.hasOwnProperty("changedAnimal")) {
        for (let k=0;k<game.changedAnimal.length;k++){
            let animal=game.changedAnimal[k];
            let id = animal.id;
            let owner = animal.ownerName;
            let flag=false;
            if (owner == playerName) flag=true;
            let playerBlock=document.getElementById(owner);
            if (document.getElementById(id)==null) playerBlock.appendChild(buildAnimal(animal,flag));
            else
                playerBlock.replaceChild(buildAnimal(animal, flag),document.getElementById(id));
        }
    }

    if (game.hasOwnProperty("deleteAnimal")) {
        for (let k=0;k<game.deleteAnimal.length;k++){
            let id=game.deleteAnimal[k];
            document.getElementById(id).remove();
        }
    }

    if (game.hasOwnProperty("player")) {
        playerName = game.player;
        document.getElementById("player").innerText = playerName;
    }
    if (game.hasOwnProperty("id")) document.getElementById("gameId").innerText = game.id;
    if (game.hasOwnProperty("playersList")) document.getElementById("players").innerText = game.playersList;
    if (game.hasOwnProperty("cards")) buildCards(game.cards);
    if (game.hasOwnProperty("newCards")) buildNewCards(game.newCards);
    if (game.hasOwnProperty("deletedCard")) deleteCard(game.deletedCard);

    if (game.phase == "FEED") {
        document.getElementById("End move").style.display = 'inline-block'; //show button
        document.getElementById("feedPanel").style.display = 'block'; //show panel
        setFood(game);
    } else if (game.phase == "EVOLUTION") {
        document.getElementById("movePanel").style.display = 'block'; //evolution phase
        document.getElementById("feedPanel").style.display = 'none'; //hide panel
        document.getElementById("End move").style.display = 'none'; //hide button
    }

    if (game.status == true) {
        document.getElementById("status").innerText = "It's your turn!";
        document.getElementById("wrapper").style.pointerEvents = "auto"; //clickable whole page
    } else {
        document.getElementById("status").innerText = "Please, wait...";
        document.getElementById("wrapper").style.pointerEvents = "none"; //disable whole page
    }

    if (game.hasOwnProperty("TAIL_LOSS")) {
        let message = game.TAIL_LOSS;
        if (message.playerOnAttack === playerName) wait();
        else if (message.playerUnderAttack === playerName) {
            alert("Animal #" + message.predator + " attack your animal #" + message.victim + " with tail loss property. Choose property to loose or click animal to die");
            tailLoss = true;
            let animals = Array.from(document.getElementsByClassName("animal"));
            let victim_id = message.victims[0];
            let animal = animals.find(x => x.id == victim_id);
            animal.style.pointerEvents = "auto"; //clickable only animals
            document.getElementById("Make move").style.pointerEvents = "auto";
            document.getElementById("Clear").style.pointerEvents = 'auto';
        }
    }

    if (game.hasOwnProperty("MIMICRY")) {
        let message = game.MIMICRY;
        if (message.playerOnAttack === playerName) wait();
        else if (message.playerUnderAttack === playerName) {
            alert("Animal #" + message.predator + " attack your animal #" + message.victim + " with mimicry property. Choose animal to redirect attack or click animal to die");
            mimicry = true;
            document.getElementById("common").style.pointerEvents = "none";
            document.getElementById(playerName).style.pointerEvents = 'auto';
            mimicryVictims = message.victims;
            document.getElementById("Make move").style.pointerEvents = "auto";
            document.getElementById("Clear").style.pointerEvents = 'auto';
        }
    }

    if (game.hasOwnProperty("last")) document.getElementById("last").style.display = "block";
    if (game.hasOwnProperty("winners")) {
        alert(game.winners); //show panel
        location.assign("/evo/signIn");
    }
}

function makeMove() {
    if (document.getElementById("phase").innerText == "EVOLUTION") {
        if (move == null) {
            alert("You haven't made any move");
            return;
        }
    }
    else if (document.getElementById("phase").innerText == "FEED") {

        if (move == null) {
            if (secondAnimalId == null) {
                alert("You haven't made any move");
                return;
            }
            move = "ATTACK";
            if (doEat) {
                alert("You can't eat/attack twice during one move");
                return;
            }
        }
    }
    let json = buildMessage();
    clearFields();//clear fields after message is built!
    socket.send(json);
}

function clearFields() {
    move = null;
    draggedProperty = null;
    firstAnimalId = null;
    secondAnimalId = null;
    playedCardId = null;
    tailLoss = false;
    document.getElementById("doing").innerText = "";
    document.getElementById("wrapper").style.pointerEvents = "none";
}


function wait() {
    document.getElementById("status").innerText = "Please, wait for victim answer...";
    document.getElementById("wrapper").style.pointerEvents = "none"; //disable whole page
}


//
//
// function getCookie(player) {
//     match = document.cookie.match(new RegExp(player + '=([^;]+)'));
//     if (match) return match[1];
// }
//
//
// function init() {
//     // playerName=getCookie("player");
//     // Object.freeze(player);
// }