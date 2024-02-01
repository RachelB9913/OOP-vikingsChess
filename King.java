import java.util.ArrayList;

public class King extends ConcretePiece{
    private Position place;

    public King(ConcretePlayer c){ //constructor that gets a player
        this.owner=c;
    }

    public String getType(){
        return "♔";
    }
}