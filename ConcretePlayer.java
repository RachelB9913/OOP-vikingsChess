public class ConcretePlayer implements Player{
    private boolean hagana; //true = hagana-player 1 , false = hatkafa-player 2
    private int countWins=0;

    public ConcretePlayer (boolean c){ //constructor that gets a boolean
        this.hagana=c;
        this.countWins=0;
    }

    @Override
    public boolean isPlayerOne() {
        return hagana;
    }

    /** after there is a win - adds 1 to the winning players "team"*/
    public void addWin(){
        countWins++;
    }

    /**gets the number of wins of a certain player ("team")*/
    @Override
    public int getWins() {
        return countWins;
    }
}