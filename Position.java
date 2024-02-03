import java.util.ArrayList;
import java.util.Objects;

public class Position {

    private int x;
    private int y;
    private ArrayList<ConcretePiece> steppedIn;

    public Position(int x, int y) { //constructor that gets x and y and build a position
        this.x = x;
        this.y = y;
        steppedIn= new ArrayList<ConcretePiece>();
    }
    public int getAllPieces (){
        return steppedIn.size();
    }
    public void addpiece(ConcretePiece p){
        if(steppedIn.isEmpty()){
            this.steppedIn.add(p);
            //System.out.println("add one" + x + y);
        }
        else {
            boolean flag = false;
            for (int j = 0; j < steppedIn.size(); j++) {
                if (steppedIn.get(j).getSN() == p.getSN() && steppedIn.get(j).getOwner() == p.getOwner()) {
                    flag = true;
                }
            }
            if (!flag) {
                this.steppedIn.add(p);
                //System.out.println("add one" + x + y);
            }
//        this.steppedIn.add(p);
//        System.out.println("add one" + x + y);
//        if(!steppedIn.contains(p)){
//            this.steppedIn.add(p);
//            System.out.println("add one" +x+y);
//        }
        }
        }
    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}