import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class DrawPanel extends JPanel {
    private boolean isCastle = false;
    ImageIcon bKing = new ImageIcon(Objects.requireNonNull(getClass().getResource("Black King.png")));
    ImageIcon bQueen = new ImageIcon(Objects.requireNonNull(getClass().getResource("Black Queen.png")));
    ImageIcon bKnight = new ImageIcon(Objects.requireNonNull(getClass().getResource("Black Knight.png")));
    ImageIcon bRook = new ImageIcon(Objects.requireNonNull(getClass().getResource("Black Rook.png")));
    ImageIcon bPawn = new ImageIcon(Objects.requireNonNull(getClass().getResource("Black Pawn.png")));
    ImageIcon bBishop = new ImageIcon(Objects.requireNonNull(getClass().getResource("Black Bishop.png")));
    ImageIcon wKing = new ImageIcon(Objects.requireNonNull(getClass().getResource("White King.png")));
    ImageIcon wQueen = new ImageIcon(Objects.requireNonNull(getClass().getResource("White Queen.png")));
    ImageIcon wKnight = new ImageIcon(Objects.requireNonNull(getClass().getResource("White Knight.png")));
    ImageIcon wRook = new ImageIcon(Objects.requireNonNull(getClass().getResource("White Rook.png")));
    ImageIcon wPawn = new ImageIcon(Objects.requireNonNull(getClass().getResource("White Pawn.png")));
    ImageIcon wBishop = new ImageIcon(Objects.requireNonNull(getClass().getResource("White Bishop.png")));
    String[][] pBoard = new String[8][8];
    int[] coordinates = new int[4];
    int[] coords = new int[8];
    int[] c = new int[2];
    public DrawPanel(String[][] pBoard) {
        this.setPreferredSize(new Dimension(2560, 1600)); //2560 x 1600 mac, 1920 x 1080 pc
        for (int i = 0; i < 8; i++) {
            System.arraycopy(pBoard[i], 0, this.pBoard[i], 0, 8);
        }
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 4; k++) {
                String a = this.pBoard[i][k];
                this.pBoard[i][k] = this.pBoard[i][7-k];
                this.pBoard[i][7-k] = a;
            }
        }
        for (int i = 0; i < 4; i++) {coordinates[i] = -1;}
        c[0] = 8;
        c[1] = 8;
    }
    public void setBoard(String[][] pBoard, String move, String sB) {
        c[0] = sB.charAt(0)-'0';
        c[1] = 7-(sB.charAt(1)-'0');
        for (int i = 0; i < 8; i++) {
            System.arraycopy(pBoard[i], 0, this.pBoard[i], 0, 8);
        }
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 4; k++) {
                String a = this.pBoard[i][k];
                this.pBoard[i][k] = this.pBoard[i][7-k];
                this.pBoard[i][7-k] = a;
            }
        }
        if (move.length() == 3) {
            isCastle = true;
            coords[0] = move.charAt(2)-'A';
            coords[2] = 4;
            coords[4] = 4 + ((move.charAt(2)-'A'+1)/2)-2;
            coords[6] = 4 + ((move.charAt(2)-'A'+1)/4)-1;
            if (move.charAt(0) == 'W') {
                for (int i = 1; i < 8; i+=2) {
                    coords[i] = 7;
                }
            }
            else {
                for (int i = 1; i < 8; i+=2) {
                    coords[i] = 0;
                }
            }
        }
        else if (move.length() == 7){
            isCastle = false;
            coordinates[0] = move.charAt(2)-'A';
            coordinates[1] = 7-(move.charAt(3)-'1');
            coordinates[2] = move.charAt(5)-'A';
            coordinates[3] = 7-(move.charAt(6)-'1');
        }
        else {
            for (int i = 0; i < 4; i++){coordinates[i] = -1;}
        }
        this.repaint();
    }
    public void paint(Graphics g) {
        Graphics2D graphic_2d = (Graphics2D) g;
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 8; k++) {
                if ((i+k)%2 == 0) {graphic_2d.setPaint(Color.lightGray);}
                else {graphic_2d.setPaint(Color.gray);}
                float h = (float)0.25;
                float s = (float)0.64;
                float b = (float)0.55;
                if (i == c[0] && k==c[1]){
                    h = (float)0.5;
                    if ((i+k)%2 != 0) {b = (float)0.45;}
                    graphic_2d.setPaint(Color.getHSBColor(h, s, b));
                }
                else if (!isCastle && ((i == coordinates[0] && k == coordinates[1]) || (i == coordinates[2] && k == coordinates[3]))) {
                    if ((i+k)%2 != 0) {b = (float)0.45;}
                    graphic_2d.setColor(Color.getHSBColor(h, s, b));
                }
                else if (isCastle && ((i == coords[0] && k == coords[1]) || (i == coords[2] && k == coords[3]) || (i == coords[4] && k == coords[5]) || (i == coords[6] && k == coords[7]))){
                    if ((i+k)%2 != 0) {b = (float)0.45;}
                    graphic_2d.setPaint(Color.getHSBColor(h, s, b));
                }
                graphic_2d.fillRect(i*100,k*100, 100, 100);
                switch (pBoard[i][k]) {
                    case "BK" -> bKing.paintIcon(this, g, i * 100, k * 100);
                    case "BQ" -> bQueen.paintIcon(this, g, i*100, k*100);
                    case "BP" -> bPawn.paintIcon(this, g, i*100, k*100);
                    case "BR" -> bRook.paintIcon(this, g, i*100, k*100);
                    case "BN" -> bKnight.paintIcon(this, g, i*100, k*100);
                    case "BB" -> bBishop.paintIcon(this, g, i*100, k*100);
                    case "WK" -> wKing.paintIcon(this, g, i*100, k*100);
                    case "WQ" -> wQueen.paintIcon(this, g, i*100, k*100);
                    case "WP" -> wPawn.paintIcon(this, g, i*100, k*100);
                    case "WR" -> wRook.paintIcon(this, g, i*100, k*100);
                    case "WN" -> wKnight.paintIcon(this, g, i*100, k*100);
                    case "WB" -> wBishop.paintIcon(this, g, i*100, k*100);
                }
            }
        }
    }
}
