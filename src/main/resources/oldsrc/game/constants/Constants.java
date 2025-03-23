package game.constants;

public enum Constants {
    START_NUMBER_OF_CARDS (6),
    START_CARD_INDEX (1),
    NUMBER_OF_EXTRA_CARD(1),
    FOOD(0),
    TOTAL_CARD_NUMBER(84),
    MIN_NUMBER_OF_PLAYERS(2),
    MAX_NUMBER_PF_PLAYERS(4);

    private final int id;

    Constants(int id) {
        this.id=id;
    }

    public int getValue() {
        return id;
    }

    public int maxFoodFor(int i){
        if (i==2) return 8;
        if (i==3) return 12;
        if (i==4) return 14;
        else return 0;
    }

    public int minFoodFor(int i){
        if (i==2) return 3;
        if (i==3) return 2;
        if (i==4) return 4;
        else return 0;
    }
}
