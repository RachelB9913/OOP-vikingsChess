import javax.management.loading.ClassLoaderRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Stack;

public class GameLogic implements PlayableLogic {
    private final ConcretePlayer player1 = new ConcretePlayer(true);
    private final ConcretePlayer player2 = new ConcretePlayer(false);
    private boolean isGameFinished = false;
    private boolean player2Turn; //false=player1Turn
    private ConcretePiece[][] board;
    private ArrayList<ConcretePiece> aPiece = new ArrayList<ConcretePiece>();
    ConcretePlayer winner;
    private Stack<String> theMove;
    private String str="";
    public ArrayList<ConcretePiece> positions= new ArrayList<ConcretePiece>();
    public ArrayList<Position> allPos;

    public GameLogic() {
        board = new ConcretePiece[11][11];
        this.player2Turn = true;
        reset();
    }

    /**
     * this function defines all the rules of the move function of the players according to the rules of the game
     * the pieces can move only to an empty position, in a straight line, without passing over different pieces and the pieces
     * that are not a king cannot go to the corners
     * the function gets 2 positions and after checking all the conditions, if all the conditions are met-
     * it moves the piece from position a to position b
     * it starts with saving the data of the move (both of the positions) in a string - will be used in the undo function.
     * in addition: after moving the piece there are few more functions are activated (will be detailed later in this class)
     */
    @Override
    public boolean move(Position a, Position b) {
        str=  "!"+a.getX()+","+a.getY()+"!" + "@"+b.getX()+","+b.getY()+"@"; //remembers the move that was taken
        boolean flag = true;
        if (board[b.getX()][b.getY()] == null && !isGameFinished) {  //b is an empty position
            if ((player2Turn && getPieceAtPosition(a).getOwner() == player2) || (!player2Turn && getPieceAtPosition(a).getOwner() == player1)) { //if it is the right player's turn
                if (a.getX() != b.getX() && a.getY() != b.getY()) {   //can't walk diagonally
                    return false;
                } else {
                    if ((board[a.getX()][a.getY()].getType() != "♔") && ((b.getX() == 0 && b.getY() == 0) || (b.getX() == 10 && b.getY() == 10) || (b.getX() == 0 && b.getY() == 10) || (b.getX() == 10 && b.getY() == 0))) {   //b is not a corner
                        return false;
                    } else {
                        if (a.getY() == b.getY()) {  //the path is empty
                            for (int i = Math.min(a.getX(), b.getX()) + 1; i < Math.max(a.getX(), b.getX()); i++) {
                                if (board[i][a.getY()] != null) {
                                    flag = false;
                                }
                            }
                        }
                        if (a.getX() == b.getX()) {
                            for (int j = Math.min(a.getY(), b.getY()) + 1; j < Math.max(a.getY(), b.getY()); j++) {
                                if (board[a.getX()][j] != null) {
                                    flag = false;
                                }
                            }
                        }
                        if (flag) {
                            ConcretePiece current = board[a.getX()][a.getY()];
                            board[a.getX()][a.getY()] = null;
                            board[b.getX()][b.getY()] = current;
                            for(int i=0;i<allPos.size();i++){
                                if(allPos.get(i).getX()==b.getX() &&allPos.get(i).getY()==b.getY()){
                                    allPos.get(i).addpiece(board[b.getX()][b.getY()]);
                                }
                            }
                            current.addStep(b.getX(), b.getY()); //adding the position to the history of the piece only if the piece didn't step there before
                            player2Turn = !player2Turn; //change the player who is playing
                            current.setDist(current.getDist() + computeDist(a, b)); //setting the new progress in squeres of this piece
                            isDead(current, b);
                            theMove.add(str);
                            win();
                            if (isGameFinished) {
                                printWin();
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * computes the distance passed (in squares) between to positions
     * because the step has to be in a straight line - just subtract the x values or the y values as needed.
     * this function is used after every move - in order to update the distance variable of each piece
     * (used in part 2 of the assignment)
     */
    private int computeDist(Position a, Position b) {
        int dis = 0;
        if (a.getX() == b.getX()) {
            dis = Math.abs(a.getY() - b.getY());
        } else if (a.getY() == b.getY()) {
            dis = Math.abs(a.getX() - b.getX());
        }
        return dis;
    }

    /**
     * a getter - gets a position and returns the number that appears in the map of numbers in the relevant position.
     * the map is build by the addToPos function.
     * this function is used mostly in the numOfPieces comparator
     */
    public int getNumPiecesPassed(Position p) {
        //return numOfPiece[p.getX()][p.getY()];
        return p.getAllPieces();
    }

    /**
     * this function is responsible to the killing of the pieces that need to be killed according to the rules of the game.
     * it gets the piece that did the move and the position it moves to.
     * there are a few types of killings - with 2 players, with a player and a corner ore with a player and a wall.
     * the king can't kill and a player cannot be killed if it has put itself in the "deadly" position.
     * the kill will happen only if the owner of the killed piece is not the owner of the piece that did the move.
     * after the kill add the data of the dead piece to the str string (will be used in the undo function)
     * if a piece is killed, it's position becomes null.
     * after killing a piece - we add a kill (+1) to the specific piece that did the move using the addKill function.
     */
    public void isDead(ConcretePiece current, Position b) {  // "♔"  "♙"  "♟︎"
        int x=b.getX();
        int y=b.getY();
        if (board[b.getX()][b.getY()].getType() != "♔") {
            if (b.getX() > 0 && board[b.getX() - 1][b.getY()] != null && board[b.getX() - 1][b.getY()].getType() != "♔" && board[b.getX()][b.getY()].getOwner() != board[b.getX() - 1][b.getY()].getOwner()) {
                if (b.getX() - 1 == 0) {  //killing with a border
                    str = str+ "%"+ (x-1) +","+y+","+ board[x-1][y].getSN() +","+ board[x-1][y].getType()+","+"#"+current.getKills()+"#";
                    board[b.getX() - 1][b.getY()] = null;
                    current.addKill();
                } else if (board[b.getX() - 2][b.getY()] != null && board[b.getX() - 2][b.getY()].getType() != "♔" && board[b.getX() - 2][b.getY()].getOwner() == board[b.getX()][b.getY()].getOwner()) { // a regular kill (sandwich)
                    str = str+ "%"+ (x-1) +","+y+","+ board[x-1][y].getSN() +","+ board[x-1][y].getType()+","+"#"+current.getKills()+"#";
                    board[b.getX() - 1][b.getY()] = null;
                    current.addKill();
                } else if (b.getX() - 2 == 0 && (b.getY() == 0 || b.getY() == 10)) { //killing with a corner
                    str = str+ "%"+ (x-1) +","+y+","+ board[x-1][y].getSN() +","+ board[x-1][y].getType()+","+"#"+current.getKills()+"#";
                    board[b.getX() - 1][b.getY()] = null;
                    current.addKill();
                }
            }
            if (b.getX() < 10 && board[b.getX() + 1][b.getY()] != null && board[b.getX() + 1][b.getY()].getType() != "♔" && board[b.getX()][b.getY()].getOwner() != board[b.getX() + 1][b.getY()].getOwner()) {
                if (b.getX() + 1 == 10) {
                    str = str+ "%"+ (x+1) +","+y+","+ board[x+1][y].getSN() +","+ board[x+1][y].getType()+","+"#"+current.getKills()+"#";
                    board[b.getX() + 1][b.getY()] = null;
                    current.addKill();
                } else if (board[b.getX() + 2][b.getY()] != null && board[b.getX() + 2][b.getY()].getType() != "♔" && board[b.getX() + 2][b.getY()].getOwner() == board[b.getX()][b.getY()].getOwner()) {
                    str = str+ "%"+ (x+1) +","+y+","+ board[x+1][y].getSN() +","+ board[x+1][y].getType()+","+"#"+current.getKills()+"#";
                    board[b.getX() + 1][b.getY()] = null;
                    current.addKill();
                } else if (b.getX() + 2 == 10 && (b.getY() == 0 || b.getY() == 10)) {
                    str = str+ "%"+ (x+1) +","+y+","+ board[x+1][y].getSN() +","+ board[x+1][y].getType()+","+"#"+current.getKills()+"#";
                    board[b.getX() + 1][b.getY()] = null;
                    current.addKill();
                }
            }
            if (b.getY() < 10 && board[b.getX()][b.getY() + 1] != null && board[b.getX()][b.getY() + 1].getType() != "♔" && board[b.getX()][b.getY()].getOwner() != board[b.getX()][b.getY() + 1].getOwner()) {
                if (b.getY() + 1 == 10) {
                    str = str+ "%"+ x+","+(y+1)+","+ board[x][y+1].getSN() +","+ board[x][y+1].getType()+","+"#"+current.getKills()+"#";
                    board[b.getX()][b.getY() + 1] = null;
                    current.addKill();
                } else if (board[b.getX()][b.getY() + 2] != null && board[b.getX()][b.getY() + 2].getType() != "♔" && board[b.getX()][b.getY() + 2].getOwner() == board[b.getX()][b.getY()].getOwner()) {
                    str = str+ "%"+ x+","+(y+1)+","+ board[x][y+1].getSN() +","+ board[x][y+1].getType()+","+"#"+current.getKills()+"#";
                    board[b.getX()][b.getY() + 1] = null;
                    current.addKill();
                } else if (b.getY() + 2 == 10 && (b.getX() == 0 || b.getX() == 10)) {
                    str = str+ "%"+ x+","+(y+1)+","+ board[x][y+1].getSN() +","+ board[x][y+1].getType()+","+"#"+current.getKills()+"#";
                    board[b.getX()][b.getY() + 1] = null;
                    current.addKill();
                }
            }
            if (b.getY() > 0 && board[b.getX()][b.getY() - 1] != null && board[b.getX()][b.getY() - 1].getType() != "♔" && board[b.getX()][b.getY()].getOwner() != board[b.getX()][b.getY() - 1].getOwner()) {
                if (b.getY() - 1 == 0) {
                    str = str+ "%"+ x+","+(y-1)+","+ board[x][y-1].getSN() +","+ board[x][y-1].getType()+","+"#"+current.getKills()+"#";
                    board[b.getX()][b.getY() - 1] = null;
                    current.addKill();
                } else if (board[b.getX()][b.getY() - 2] != null && board[b.getX()][b.getY() - 2].getType() != "♔" && board[b.getX()][b.getY() - 2].getOwner() == board[b.getX()][b.getY()].getOwner()) {
                    str = str+ "%"+ x+","+(y-1)+","+ board[x][y-1].getSN() +","+ board[x][y-1].getType()+","+"#"+current.getKills()+"#";
                    board[b.getX()][b.getY() - 1] = null;
                    current.addKill();
                } else if (b.getY() - 2 == 0 && (b.getX() == 0 || b.getX() == 10)) {
                    str = str+ "%"+ x+","+(y-1)+","+ board[x][y-1].getSN() +","+ board[x][y-1].getType()+","+"#"+current.getKills()+"#";
                    board[b.getX()][b.getY() - 1] = null;
                    current.addKill();
                }
            }
        }
    }

    /** getter - returns a piece at a given position by getting the piece that appears in the board of pieces at a current time */
    @Override
    public Piece getPieceAtPosition(Position position) {
        return board[position.getX()][position.getY()];
    }

    @Override
    public Player getFirstPlayer() {
        return player1;
    }

    @Override
    public Player getSecondPlayer() {
        return player2;
    }

    @Override
    public boolean isGameFinished() {
        return isGameFinished;
    }

    /**
     * this function defines if and when there is a win.
     * the player 1 team wins if the king got to one of the corners or if they managed to kill all of the other player's pieces
     * and the player 2 team wins if they succeeded to "kill" the king.
     * if there is a win we update a few variables - we add a win to the relevant team and define it as the winner.
     * at the end we change the boolean isGameFinished to true in order to define that the game IS finished.
     */
    public void win() {
        if (board[0][0] != null && board[0][0].getType() == "♔") {
            player1.addWin();
            winner = player1;
            isGameFinished = true;
        }
        if (board[10][0] != null && board[10][0].getType() == "♔") {
            player1.addWin();
            winner = player1;
            isGameFinished = true;
        }
        if (board[0][10] != null && board[0][10].getType() == "♔") {
            player1.addWin();
            winner = player1;
            isGameFinished = true;
        }
        if (board[10][10] != null && board[10][10].getType() == "♔") {
            player1.addWin();
            winner = player1;
            isGameFinished = true;
        }
        if (isKingDead()) {
            player2.addWin();
            winner = player2;
            isGameFinished = true;
        }
        boolean only1 = true;
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                if (board[i][j] != null && board[i][j].getOwner() == player2) {
                    only1 = false;
                    break;
                }
            }
        }
        if (only1) {
            player1.addWin();
            winner = player1;
            isGameFinished = true;
        }
    }

    /**part 2 of the assignment:
     * after a win this function is activated. each section prints a different data about the game:
     * section 1- prints the step history of each piece that made a move (didn't stay in place) after sorting the aPiece arrayList with the "stepHistComp".
     * first it prints the histories of the pieces that belong to the winning team and then of the losing team.
     * section 2- prints the number of kills of each piece that killed at least 1 piece, after sorting the aPiece arrayList with the "killCountComp".
     * at first defines the type of piece - K/A/D and then prints.
     * section 3- prints the distance passed by each piece that moved during the game, after sorting the aPiece arrayList with the "distComp".
     * here too it first defines the type of piece and then prints.
     * section 4- with the help of listNumOfPieces function - prints the positions that were stepped in at least twice, after sorting with the "numOf PiecesComp"
     * prints the position and the number of pieces stepped in this position.
     * the format of the printing in all the sections is as asked in the assignment.
     * the sections are devided with stars. */
    public void printWin() {
        //section 1//
        aPiece.sort(stepHistComp);
        if (winner == player1) {
            for (ConcretePiece piece : aPiece) {
                if (piece.getOwner() == player1) {
                    if (piece.pHist.size() > 1) {
                        String result = toStringHis(piece);
                        if (piece.getType() == "♔") {
                            System.out.println("K" + piece.getSN() + ": " + result);
                        } else {
                            System.out.println("D" + piece.getSN() + ": " + result);
                        }
                    }
                }
            }
            for (ConcretePiece piece : aPiece) {
                if (piece.getOwner() == player2) {
                    if (piece.pHist.size() > 1) {
                        String result = toStringHis(piece);
                        System.out.println("A" + piece.getSN() + ": " + result);
                    }
                }
            }
        }
        if (winner == player2) {
            for (ConcretePiece piece : aPiece) {
                if (piece.getOwner() == player2) {
                    if (piece.pHist.size() > 1) {
                        String result = toStringHis(piece);
                        System.out.println("A" + piece.getSN() + ": " + result);
                    }
                }
            }
            for (ConcretePiece piece : aPiece) {
                if (piece.getOwner() == player1) {
                    if (piece.pHist.size() > 1) {
                        String result = toStringHis(piece);
                        if (piece.getType() == "♔") {
                            System.out.println("K" + piece.getSN() + ": " + result);
                        } else {
                            System.out.println("D" + piece.getSN() + ": " + result);
                        }
                    }
                }
            }
        }
        stars();
        //section 2//
        aPiece.sort(killCountComp); //sorted by the number of kills
        for (int i = 0; i < aPiece.size(); i++) {
            String ans = "";
            if (aPiece.get(i).getKills() > 0) {
                if (aPiece.get(i).getOwner() == player1) {
                    if (aPiece.get(i).getType() == "♔") {
                        ans = ans + "K";
                    } else {
                        ans = ans + "D";
                    }
                } else if (aPiece.get(i).getOwner() == player2) {
                    ans = ans + "A";
                }
                System.out.println(ans + aPiece.get(i).getSN() + ": " + aPiece.get(i).getKills() + " kills");
            }
        }
        stars();
        //section 3//
        aPiece.sort(distComp);
        for (int i = 0; i < aPiece.size(); i++) {
            String ans = "";
            if (aPiece.get(i).getDist() > 0) {
                if (aPiece.get(i).getOwner() == player1) {
                    if (aPiece.get(i).getType() == "♔") {
                        ans = ans + "K";
                    } else {
                        ans = ans + "D";
                    }
                } else if (aPiece.get(i).getOwner() == player2) {
                    ans = ans + "A";
                }
                System.out.println(ans + aPiece.get(i).getSN() + ": " + aPiece.get(i).getDist() + " squares");
            }
        }
        stars();

        for(int i=0;i<11;i++){
            for(int j=0;j<11;j++){
                Position position = new Position(i,j);
                //if(numOfPiece[i][j]>=2){
                if(position.getAllPieces()>=2){
                    System.out.println(toStringPos(position) + getNumPiecesPassed(position) + " pieces");
                }
            }
        }
        stars();
    }

    /**
     * this function turns a given position to a string
     */
    public String toStringPos(Position p) {
        String ans = "";
        ans = "(" + p.getX() + ", " + p.getY() + ")";
        return ans;
    }

    /**
     * this function gets a piece and returns a string that shows the piece's moves history
     * the string format is: [(,),(,)...] when each (,) uses the toStringPos function
     */
    public String toStringHis(ConcretePiece piece) {
        String ans = "[" + toStringPos(piece.pHist.getFirst());
        for (int i = 1; i < piece.pHist.size(); i++) {
            ans = ans + ", " + toStringPos(piece.pHist.get(i));
        }
        ans = ans + "]";
        return ans;
    }

    /**
     * this function checks if the king is dead -
     * in order to kill the king it has to be surrounded by the other player's pieces from all 4 sides or 3 sides and a wall.
     * when the king is dead turn its position to null
     * we use this function in order to understand if the player's 2 team won the game.
     */
    public boolean isKingDead() {
        boolean flag = false;
        for (int i = 0; i < getBoardSize(); i++) {
            for (int j = 0; j < getBoardSize(); j++) {
                if (board[i][j] != null && board[i][j].getType() == "♔") {
                    //checks for each wall if the king is surrounded from the other 3 sides sides
                    if (i == 0 && j > 0 && j < 10) {
                        if (board[i][j + 1] != null && board[i][j - 1] != null && board[i + 1][j] != null) {
                            if (board[i][j + 1].getOwner() == player2 && board[i][j - 1].getOwner() == player2 && board[i + 1][j].getOwner() == player2) {
                                flag = true;
                            }
                        }
                    } else if (i == 10 && j > 0 && j < 10) {
                        if (board[i][j + 1] != null && board[i][j - 1] != null && board[i - 1][j] != null) {
                            if (board[i][j + 1].getOwner() == player2 && board[i][j - 1].getOwner() == player2 && board[i - 1][j].getOwner() == player2) {
                                flag = true;
                            }
                        }
                    } else if (j == 0 && i > 0 && i < 10) {
                        if (board[i + 1][j] != null && board[i - 1][j] != null && board[i][j + 1] != null) {
                            if (board[i + 1][j].getOwner() == player2 && board[i - 1][j].getOwner() == player2 && board[i][j + 1].getOwner() == player2) {
                                flag = true;
                            }
                        }
                    } else if (j == 10 && i > 0 && i < 10) {
                        if (board[i + 1][j] != null && board[i - 1][j] != null && board[i][j - 1] != null) {
                            if (board[i + 1][j].getOwner() == player2 && board[i - 1][j].getOwner() == player2 && board[i][j - 1].getOwner() == player2) {
                                flag = true;
                            }
                        }
                        //checks the option that the king is surrounded  from all 4 sides
                    } else if (i != 0 && i != 10 && j != 0 && j != 10) {
                        if (board[i][j + 1] != null && board[i][j - 1] != null && board[i + 1][j] != null && board[i - 1][j] != null) {
                            if (board[i][j + 1].getOwner() == player2 && board[i][j - 1].getOwner() == player2 && board[i + 1][j].getOwner() == player2 && board[i - 1][j].getOwner() == player2) {
                                flag = true;
                            }
                        }
                    }
                    if (flag) {
                        board[i][j] = null;
                    }
                }
            }
        }
        return flag;
    }

    @Override
    public boolean isSecondPlayerTurn() {
        return player2Turn;
    }

    /**
     * this function defines what to do when the user press' reset or when there is a win
     * creates: a new board of null that will be filled with pieces, a new map of int that will be used during the game
     * a new arrayList that will contain the pieces added to the board, defines the game as unfinished and sets the turn to be
     * player 2 turn.
     * the following things happen to all the pieces: creates for each piece a new ConcretePiece and defines the player that's playing,
     * puts the piece in it's right place on the board and adds 1 (+1) to the map in the same place. it defines each piece its own
     * serial number according to the picture in the assignment, adds the piece to the arrayList created above
     * and adds to each pieces' arrayList of moves the initial position.
     */
    @Override
    public void reset() {
        positions = new ArrayList<ConcretePiece>();
        allPos = new ArrayList<Position>();
        board = new ConcretePiece[11][11];
        aPiece = new ArrayList<ConcretePiece>();
        theMove = new Stack<String>();
        isGameFinished = false;
        player2Turn = true;
        ConcretePiece k = new King(player1); board[5][5] = k;
        k.setSN(7); aPiece.add(k); k.addStep(5, 5);
        ConcretePiece D1 = new Pawn(player1); board[5][3] = D1;
        D1.setSN(1); aPiece.add(D1); D1.addStep(5, 3);
        ConcretePiece D2 = new Pawn(player1); board[4][4] = D2;
        D2.setSN(2); aPiece.add(D2); D2.addStep(4, 4);
        ConcretePiece D3 = new Pawn(player1); board[5][4] = D3;
        D3.setSN(3); aPiece.add(D3); D3.addStep(5, 4);
        ConcretePiece D4 = new Pawn(player1); board[6][4] = D4;
        D4.setSN(4); aPiece.add(D4); D4.addStep(6, 4);
        ConcretePiece D5 = new Pawn(player1); board[3][5] = D5;
        D5.setSN(5); aPiece.add(D5); D5.addStep(3, 5);
        ConcretePiece D6 = new Pawn(player1); board[4][5] = D6;
        D6.setSN(6); aPiece.add(D6); D6.addStep(4, 5);
        ConcretePiece D8 = new Pawn(player1); board[6][5] = D8;
        D8.setSN(8); aPiece.add(D8); D8.addStep(6, 5);
        ConcretePiece D9 = new Pawn(player1); board[7][5] = D9;
        D9.setSN(9); aPiece.add(D9); D9.addStep(7, 5);
        ConcretePiece D10 = new Pawn(player1); board[4][6] = D10;
        D10.setSN(10); aPiece.add(D10); D10.addStep(4, 6);
        ConcretePiece D11 = new Pawn(player1); board[5][6] = D11;
        D11.setSN(11); aPiece.add(D11); D11.addStep(5, 6);
        ConcretePiece D12 = new Pawn(player1); board[6][6] = D12;
        D12.setSN(12); aPiece.add(D12); D12.addStep(6, 6);
        ConcretePiece D13 = new Pawn(player1); board[5][7] = D13;
        D13.setSN(13); aPiece.add(D13); D13.addStep(5, 7);

        ConcretePiece A1 = new Pawn(player2); board[3][0] = A1; A1.setSN(1); aPiece.add(A1); A1.addStep(3, 0);
        ConcretePiece A2 = new Pawn(player2); board[4][0] = A2; A2.setSN(2); aPiece.add(A2); A2.addStep(4, 0);
        ConcretePiece A3 = new Pawn(player2); board[5][0] = A3; A3.setSN(3); aPiece.add(A3); A3.addStep(5, 0);
        ConcretePiece A4 = new Pawn(player2); board[6][0] = A4; A4.setSN(4); aPiece.add(A4); A4.addStep(6, 0);
        ConcretePiece A5 = new Pawn(player2); board[7][0] = A5; A5.setSN(5); aPiece.add(A5); A5.addStep(7, 0);
        ConcretePiece A6 = new Pawn(player2); board[5][1] = A6; A6.setSN(6); aPiece.add(A6); A6.addStep(5, 1);
        ConcretePiece A19 = new Pawn(player2); board[5][9] = A19; A19.setSN(19); aPiece.add(A19); A19.addStep(5, 9);
        ConcretePiece A20 = new Pawn(player2); board[3][10] = A20; A20.setSN(20); aPiece.add(A20); A20.addStep(3, 10);
        ConcretePiece A21 = new Pawn(player2); board[4][10] = A21; A21.setSN(21); aPiece.add(A21); A21.addStep(4, 10);
        ConcretePiece A22 = new Pawn(player2); board[5][10] = A22; A22.setSN(22); aPiece.add(A22); A22.addStep(5, 10);
        ConcretePiece A23 = new Pawn(player2); board[6][10] = A23; A23.setSN(23); aPiece.add(A23); A23.addStep(6, 10);
        ConcretePiece A24 = new Pawn(player2); board[7][10] = A24; A24.setSN(24); aPiece.add(A24); A24.addStep(7, 10);
        ConcretePiece A7 = new Pawn(player2); board[0][3] = A7; A7.setSN(7); aPiece.add(A7); A7.addStep(0, 3);
        ConcretePiece A9 = new Pawn(player2); board[0][4] = A9; A9.setSN(9); aPiece.add(A9); A9.addStep(0, 4);
        ConcretePiece A11 = new Pawn(player2); board[0][5] = A11; A11.setSN(11); aPiece.add(A11); A11.addStep(0, 5);
        ConcretePiece A15 = new Pawn(player2); board[0][6] = A15; A15.setSN(15); aPiece.add(A15); A15.addStep(0, 6);
        ConcretePiece A17 = new Pawn(player2); board[0][7] = A17; A17.setSN(17); aPiece.add(A17); A17.addStep(0, 7);
        ConcretePiece A12 = new Pawn(player2); board[1][5] = A12; A12.setSN(12); aPiece.add(A12); A12.addStep(1, 5);
        ConcretePiece A8 = new Pawn(player2); board[10][3] = A8; A8.setSN(8); aPiece.add(A8); A8.addStep(10, 3);
        ConcretePiece A10 = new Pawn(player2); board[10][4] = A10; A10.setSN(10); aPiece.add(A10); A10.addStep(10, 4);
        ConcretePiece A14 = new Pawn(player2); board[10][5] = A14; A14.setSN(14); aPiece.add(A14); A14.addStep(10, 5);
        ConcretePiece A16 = new Pawn(player2); board[10][6] = A16; A16.setSN(16); aPiece.add(A16); A16.addStep(10, 6);
        ConcretePiece A18 = new Pawn(player2); board[10][7] = A18; A18.setSN(18); aPiece.add(A18); A18.addStep(10, 7);
        ConcretePiece A13 = new Pawn(player2); board[9][5] = A13; A13.setSN(13); aPiece.add(A13); A13.addStep(9, 5);

        for(int i=0;i<11;i++){
            for(int j=0;j<11;j++) {
                Position pos = new Position(i,j);
                if(board[i][j]!=null){
                    pos.addpiece(board[i][j]);
                }
            }
        }

        for(int i=0;i<11;i++){
            for(int j=0;j<11;j++){
                if(board[i][j]!=null){
                    Position pos = new Position(i,j);
                    pos.addpiece(board[i][j]);
                }
            }
        }
    }

    /** the function defines what steps need to happen if the user clicked on "back" button.
     * during the move function and the "isDead" function, data about the piece that moved and the dying pieces is collected in a string.
     * each part of the data separated with a symbol - ! / @ / % / # / ,
     * in this function using the split function on the string, the data is used to restore the pieces and theirs positions.
     */
    @Override
    public void undoLastMove() {
        player2Turn=!player2Turn; //return the turn to be the one who was before
        String lastMove = theMove.pop();
        if (lastMove != null) {
            //finding position a of the move
            String[] previous;
            previous = lastMove.split("!");
            String pPos = previous[1];
            String[] pxy = pPos.split(",");
            int previousX = Integer.parseInt(pxy[0]);
            int previousY = Integer.parseInt(pxy[1]);
            Position p = new Position(previousX,previousY);
            //finding position b of the move
            String[] now;
            now= lastMove.split("@");
            String nPos = now[1];
            String[] nxy = nPos.split(",");
            int nowX = Integer.parseInt(nxy[0]);
            int nowY = Integer.parseInt(nxy[1]);
            Position n = new Position(nowX,nowY);
            //moving the piece to the previous position - from b to a
            ConcretePiece curr = board[nowX][nowY];
            board[nowX][nowY] = null;
            board[previousX][previousY] = curr;
            //removing the last position in the step history of the piece
            curr.pHist.removeLast();
            int dist = computeDist(n,p);
            curr.setDist(curr.getDist()-dist); //subtract the distance the piece made in its last move
            //returning the pieces that died to their previous position
            String[] killed = lastMove.split("%"); //an array of all the pieces that died this turn
            for(int i=1;i<killed.length;i++){
                String k = killed[i];
                String[] kxy = k.split(","); //array of the data of the relevant piece that died
                int kX = Integer.parseInt(kxy[0]); //builds back the position of the dead piece
                int kY = Integer.parseInt(kxy[1]);
                int kSN = Integer.parseInt(kxy[2]);  //gets the serial number of the dead piece
                String kPlayer = kxy[3]; //gets the player whose piece died
                for(int j=0;j<aPiece.size();j++){
                    if(Objects.equals(aPiece.get(j).getType(), kPlayer) && aPiece.get(j).getSN()==kSN){
                        ConcretePiece live = new Pawn(aPiece.get(j));
                        live.setSN(kSN);
                        board[kX][kY]=live;
                    }
                }
                //sets the number of kills of the killing piece to be the number it was before this move
                String cKills = kxy[4];
                String[] numKills = cKills.split("#");
                curr.setKills(Integer.parseInt(numKills[1]));
            }
        }
    }

    @Override
    public int getBoardSize() {
        return 11;
    }

    /** this function prints 75 stars - a buffer between each two parts at part 2 of the assignment */
    public void stars() {
        for (int i = 0; i <= 73; i++) {
            System.out.print("*");
        }
        System.out.println("*");
    }

    //comparators for part two//
    /** part 1 of part 2:
     * this comparator compares between 2 pieces according to theirs history of steps in ascending order.
     * each piece has its own arrayList that contains every step the piece did during the game (every position it stepped in). */
    Comparator<ConcretePiece> stepHistComp = new Comparator<ConcretePiece>() {
        public int compare(ConcretePiece p1, ConcretePiece p2) {
            //compares according to the number of steps taken by the piece
            if (p1.pHist.size() < p2.pHist.size()) {
                return -1;
            } else if (p1.pHist.size() > p2.pHist.size()) {
                return 1;
            }
            //if the number of steps is the same compares by the serial number of the piece- smaller comes first
            else {
                if (p1.getSN() < p2.getSN()) {
                    return -1;
                } else if (p1.getSN() > p2.getSN()) {
                    return 1;
                }
            }
            return 0;
        }
    };
    /** part 2 of part 2:
     * this comparator compares between 2 pieces according to the number of kills the pieces had during the game, in descending order.
     * each piece has its own arrayList that contains every step the piece did during the game (every position it stepped in). */
    Comparator<ConcretePiece> killCountComp = new Comparator<ConcretePiece>() {
        public int compare(ConcretePiece p1, ConcretePiece p2) {
            //compares according to the number of kills
            if (p1.getKills() > p2.getKills()) {
                return -1;
            } else if (p1.getKills() < p2.getKills()) {
                return 1;
            }
            //if the number of kills is the same compares by the serial number of the piece- smaller comes first
            else {
                if (p1.getSN() < p2.getSN()) {
                    return -1;
                } else if (p1.getSN() > p2.getSN()) {
                    return 1;
                }
                //if the serial number is the same as well puts the piece of the winning team first
                else {
                    if ((winner == player1 && p1.getOwner() == player1 && p2.getOwner() == player2) || (winner == player2 && p1.getOwner() == player2 && p2.getOwner() == player1)) {
                        return -1;
                    } else if ((winner == player1 && p1.getOwner() == player2 && p2.getOwner() == player1) || (winner == player2 && p1.getOwner() == player1 && p2.getOwner() == player2)) {
                        return 1;
                    }
                }
            }
            return 0;
        }
    };
    /** part 3 of part 2:
     * this comparator compares between 2 pieces according to the distance the pieces moved during the game, in descending order.
     * each piece has its own variable that updates after every move it makes during the game (counting squares) */
    Comparator<ConcretePiece> distComp = new Comparator<ConcretePiece>() {
        public int compare(ConcretePiece p1, ConcretePiece p2) {
            if (p1.getDist() < p2.getDist()) {
                return 1;
            } else if (p1.getDist() > p2.getDist()) {
                return -1;
            } else {
                if (p1.getSN() < p2.getSN()) {
                    return -1;
                } else if (p1.getSN() > p2.getSN()) {
                    return 1;
                }
                //if the serial number is the same as well puts the piece of the winning team first
                else {
                    if ((winner == player1 && p1.getOwner() == player1 && p2.getOwner() == player2) || (winner == player2 && p1.getOwner() == player2 && p2.getOwner() == player1)) {
                        return -1;
                    } else if ((winner == player1 && p1.getOwner() == player2 && p2.getOwner() == player1) || (winner == player2 && p1.getOwner() == player1 && p2.getOwner() == player2)) {
                        return 1;
                    }
                }
            }
            return 0;
        }
    };
    /** part 4 of part 2:
     * this comparator compares between 2 positions according to how many pieces stepped there during the game in descending order.
     * if the number of pieces at the positions is the same it compares by the X values and the Y values of the positions */
    Comparator<Position> numOfPiecesComp = new Comparator<Position>() {
        public int compare(Position p1, Position p2) {
            if (p1.getAllPieces() < p2.getAllPieces()) {
               return 1; //p1 is bigger
            } else if (p1.getAllPieces() > p2.getAllPieces()) {
                return -1; //p2 is bigger
            } else if (p1.getAllPieces() == p2.getAllPieces()) { //they are the same - check the position itself
                if (p1.getX() < p2.getX()) {
                    return -1;
                } else if (p1.getX() > p2.getX()) {
                    return 1;
                } else { //if the X are equal
                    if (p1.getY() < p2.getY()) {
                        return -1;
                    } else if (p1.getY() > p2.getY()) {
                        return 1;
                    }
                }
            }
            return 0;
        }
    };
}
//the end - enjoy :)