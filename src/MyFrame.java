import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class MyFrame extends JFrame implements ActionListener {
    DrawPanel draw_panel;
    String[] openings = {"P D7-D5", "P E7-E5", "N B8-C6", "N G8-F6"};
    String[] whiteOpenings = {"P D2-D4", "P E2-E4", "N B1-C3", "N G1-F3"};
    JButton[][] button;

    String selectedButton = "88";
    String[][]board;
    public boolean canMove;
    boolean isWhiteMoving = true;
    private String p = "Start";
    private final int peep;
    private int moves = 0;
    MyFrame(String[][] pBoard, int peep, boolean canMove) throws IOException {
        this.canMove = canMove;
        board = pBoard;
        this.peep = peep;
        button = new JButton[8][8];
        for (int i = 0; i < 8; i++){
            for (int k = 0; k < 8; k++) {
                button[i][k] = new JButton();
                button[i][k].setBounds(i*100,k*100,100,100);
                button[i][k].addActionListener(this);
                button[i][k].setOpaque(false);
                button[i][k].setContentAreaFilled(false);
                button[i][k].setBorderPainted(false);
                this.add(button[i][k]);
            }
        }
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Chess AI GUI");
        draw_panel = new DrawPanel(pBoard);
        this.add(draw_panel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        if (!canMove){
            String move = whiteOpenings[(int)(Math.random()*4)];
            Main.determineMove(move.charAt(0), board, move, true, false, "Start", Main.blackKingMoved, Main.whiteKingMoved, false);
            paint(board,move,true,p);
            this.canMove = true;
            isWhiteMoving = false;
            moves++;
        }
    }
    public void actionPerformed(ActionEvent e){
        if (Main.gameOver) {canMove = false;}
        String willMove = "";
        String sB = selectedButton;
        if (canMove && !selectedButton.equals("88") &&
                ((board[selectedButton.charAt(0)-'0'][selectedButton.charAt(1)-'0'].charAt(0) == 'W' && isWhiteMoving) ||
                        (board[selectedButton.charAt(0)-'0'][selectedButton.charAt(1)-'0'].charAt(0) == 'B' && !isWhiteMoving))){
            willMove = board[selectedButton.charAt(0)-'0'][selectedButton.charAt(1)-'0'].charAt(1) + " " +(char)(selectedButton.charAt(0)-'0'+'A')+(selectedButton.charAt(1)-'0'+1);
        }
        boolean shouldBreak = false;
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 8; k++){
                if (e.getSource() == button[i][k]) {
                    selectedButton = String.valueOf((button[i][k].getX()/100)) + (7-(button[i][k].getY()/100));
                    shouldBreak = true;
                    break;
                }
            }
            if (shouldBreak) {break;}
        }
        if (selectedButton.equals(sB)) {selectedButton = "88";}
        draw_panel.setBoard(board, p, selectedButton);
        if (willMove.length() > 0 && !selectedButton.equals("88")){
            willMove += "-" + (char)(selectedButton.charAt(0)-'0'+'A')+(selectedButton.charAt(1)-'0'+1);
            boolean a = false;
            boolean c = false;
            if (willMove.charAt(0) == 'K' && Math.abs((int)willMove.charAt(5)-(int)willMove.charAt(2)) == 2) {
                char j = willMove.charAt(5);
                if (j == 'G') {j = 'H';}
                else if (j == 'C') {j = 'A';}
                willMove = "C" + j;
                try {
                    a = Main.determineMove('C', board, "C" + j, isWhiteMoving, true, p, Main.blackKingMoved, Main.whiteKingMoved, false);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                c = a;
            }
            else if (willMove.length() == 7){
                try {
                    a = Main.determineMove(willMove.charAt(0), board, willMove, isWhiteMoving, true, p, Main.blackKingMoved, Main.whiteKingMoved, false);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (a) {
                moves++;
                if (!c && board[willMove.charAt(5)-'A'][willMove.charAt(6)-'1'].charAt(1) == 'K') {canMove = false;}
                try {
                    Main.determineMove(willMove.charAt(0), board, willMove, isWhiteMoving, false, p, Main.blackKingMoved, Main.whiteKingMoved, false);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                selectedButton = "88";
                this.p = willMove;
                paint(board,willMove,isWhiteMoving,p);
                isWhiteMoving = !isWhiteMoving;
                if (peep == 1 && canMove) {
                    canMove = false;
                    try {
                        AIMove(willMove);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }
    public void AIMove(String willMove) throws IOException {
        if (!Main.gameOver) {
            String move;
            if (moves == 1) {
                move = openings[(int) (Math.random() * 4)];
                Main.determineMove(move.charAt(0), board, move, isWhiteMoving, false, "Start", Main.blackKingMoved, Main.whiteKingMoved, false);
                paint(board, move, false, p);
            } else {
                ArrayList<String> a = new ArrayList<>(1);
                Main.lookAhead(board, Main.depth, isWhiteMoving, willMove, Main.blackKingMoved, Main.whiteKingMoved, -Integer.MAX_VALUE, Integer.MAX_VALUE, false, true, false, a);
            }
            boolean breaker = false;
            for (int i = 0; i < 8; i++) {
                for (int k = 0; k < 8; k++) {
                    if ((!isWhiteMoving && board[i][k].equals("WK")) || (isWhiteMoving && board[i][k].equals("BK"))) {
                        breaker = true;
                        break;
                    }
                }
                if (breaker) {
                    break;
                }
            }
            canMove = breaker;
            p = Main.previousMove;
            isWhiteMoving = !isWhiteMoving;
            selectedButton = "88";
            moves++;
        }
    }
    public void paint(String[][] pBoard, String move, boolean mover, String p) {
        this.p = p;
        isWhiteMoving = mover;
        board = pBoard;
        draw_panel.setBoard(pBoard, move, selectedButton);
    }
}
