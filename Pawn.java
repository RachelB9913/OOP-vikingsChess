import java.util.ArrayList;

public class Pawn extends ConcretePiece{
    private Position place;

    public Pawn(ConcretePlayer p){ //constructor that gets a player
        super();
        this.owner=p;
    }
    public Pawn(ConcretePiece p){ //constructor that gets a piece
        super();
        this.owner=p.owner;
    }

    public String getType(){
        if(owner.isPlayerOne()){
            return "♙";
        }
        else {
            return "♟︎";
        }
    }
}