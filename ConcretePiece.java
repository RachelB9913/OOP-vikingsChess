import java.util.ArrayList;

public abstract class ConcretePiece implements Piece {

    protected ConcretePlayer owner;
    private Position pos;
    private int serialN;
    public ArrayList<Position> pHist=new ArrayList<Position>();
    private int killCount=0;
    private int dist=0;
    public ConcretePiece() {
    }
    public void addStep(int x, int y){
        Position p= new Position(x,y);
        this.pHist.add(p);
    }

    /**used in the "isDead" function in "gameLogic" class - if the piece killed another one it adds 1 to its count*/
    public void addKill(){
        killCount++;
    }

    /**used in the "gameLogic" class for collecting data and for the "printWin" function
     * returns the kills counter of the relevant piece*/
    public int getKills(){
        return this.killCount;
    }

    /**used in the "undo" function in "gameLogic" class - sets the number of kills to be the given number*/
    public void setKills(int k){ this.killCount=k; }

    /**used in the "gameLogic" class - gets the calculated distance the relevant piece passed till this point of the game */
    public int getDist(){
        return this.dist;
    }

    /**used in the "gameLogic" class in the "move" and "undo" functions - updates its value here*/
    public void setDist(int dis){
        this.dist=dis;
    }

    /**used in the "gameLogic" class mostly in the "reset" function - sets a serial number to the pieces(according to the assignment)*/
    public void setSN(int n){
        this.serialN=n;
    }

    /**gets the serial number of a piece*/
    public int getSN(){
        return this.serialN;
    }


    @Override
    public Player getOwner() {
        return owner;
    }
    @Override
    public String getType() {
        return this.getType();
    }
}