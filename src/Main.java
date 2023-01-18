import java.io.*;
import java.util.*;
public class Main {
    public static boolean blackKingMoved = false;
    public static String name = "";
    public static boolean gameOver = false;
    public static String[][] currentBoard;
    public static boolean whiteKingMoved = false;
    public static boolean[][] isWhiteAttacking = new boolean[8][8];
    public static boolean[][] isBlackAttacking = new boolean[8][8];
    public static MyFrame frame;
    public static boolean aiTeam = false;
    public static boolean aiExists = false;
    public static String previousMove = "Start";
    public static boolean[][] rooks = new boolean[2][2];
    public static int movesSinceCapture = 0;
    public static ArrayList<String> boardStates = new ArrayList<>(10);
    public static boolean stalemate(String previousMove, String[][] board, boolean[][] isBlackAttacking, boolean[][] isWhiteAttacking) {
        //no moves available
        boolean mover = true;
        if (!previousMove.equals("Start")) {mover = !(board[previousMove.charAt(5)-'A'][previousMove.charAt(6)-'1'].charAt(0) == 'W');}
        boolean answer;
        ArrayList<String> a = new ArrayList<>(1);
        if (mover) {answer = lookAhead(board, 2, true, previousMove, blackKingMoved, whiteKingMoved, -Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, false, a) < -1000 && !isWhiteInCheck(board, isBlackAttacking);}
        else {answer = lookAhead(board, 2, false, previousMove, blackKingMoved, whiteKingMoved, -Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, false, a) > 1000 && !isBlackInCheck(board, isWhiteAttacking);}
        //repetition
        String x = boardToString(board);
        int repCount = 0;
        for (String i : boardStates) {
            if (i.equals(x)) {repCount++;}
        }
        if (!answer) {answer = repCount > 2;}
        //insufficient material
        if (!answer) {
            ArrayList<Character> whitePieces = new ArrayList<>(16);
            ArrayList<Character> blackPieces = new ArrayList<>(16);
            for (int i = 0; i < 8; i++) {
                for (int k = 0; k < 8; k++) {
                    if (board[i][k].charAt(0) == 'B') {
                        blackPieces.add(board[i][k].charAt(1));
                    }
                    if (board[i][k].charAt(0) == 'W') {
                        whitePieces.add(board[i][k].charAt(1));
                    }
                }
            }
            boolean whiteInsuff = false;
            boolean blackInsuff = false;
            if (whitePieces.size()< 2){
                if (whitePieces.contains('N') || whitePieces.contains('B') || whitePieces.size() == 1) {whiteInsuff = true;}
            }
            if (blackPieces.size()< 2){
                if (blackPieces.contains('N') || blackPieces.contains('B') || blackPieces.size() == 1) {blackInsuff = true;}
            }
            if (whiteInsuff && blackInsuff) {answer = true;}
        }
        //no pieces captured in 50 moves
        if (!answer) {answer = movesSinceCapture >= 50;}
        return answer;
    }
    public static boolean isWhiteInCheck(String[][] board, boolean[][] blackIsAttacking) {
        int x = 0;
        int y = 0;
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 8; k++) {
                if (board[i][k].equals("WK")) {
                    x = i;
                    y = k;
                }
            }
        }
        return blackIsAttacking[x][y];
    }
    public static boolean isBlackInCheck(String[][] board, boolean[][] whiteIsAttacking) {
        int x = 0;
        int y = 0;
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 8; k++) {
                if (board[i][k].equals("BK")) {
                    x = i;
                    y = k;
                }
            }
        }
        return whiteIsAttacking[x][y];
    }
    public static boolean whiteLost(String[][] board, String prev) {
        ArrayList<String> a = new ArrayList<>(1);
        return lookAhead(board, 2, true, prev, blackKingMoved, whiteKingMoved, -Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, false, a) < -1000 && isWhiteInCheck(board, isBlackAttacking);
    }
    public static boolean blackLost(String[][] board, String prev) {
        ArrayList<String> a = new ArrayList<>(1);
        return lookAhead(board, 2, false, prev, blackKingMoved, whiteKingMoved, -Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, false, a) > 1000 && isBlackInCheck(board, isWhiteAttacking);
    }
    public static String boardToString(String[][] board) {
        StringBuilder a = new StringBuilder();
        for (int i = 7; i >= 0; i--) {//columns
            int spaceCounter = 0;
            for (int k = 0; k < 8; k++) {//rows
                char p = board[k][i].charAt(1);
                if (board[k][i].equals("  ")) {
                    spaceCounter++;
                    if (k ==7) {
                        a.append(spaceCounter);
                        spaceCounter = 0;
                        a.append("/");
                    }
                    continue;
                }
                if (board[k][i].charAt(0) == 'B') {
                    p = (char)((int)p+32);
                }
                if (spaceCounter != 0) {
                    a.append(spaceCounter);}
                a.append(p);
                spaceCounter = 0;
                if (k == 7) {
                    a.append("/");
                }
            }
        }
        return a.toString();
    }
    /*
    public static void printAttacked(boolean[][] attacked) {
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 8; k++) {
                if (attacked[k][7-i]){System.out.print(1 + " ");}
                else {System.out.print(0 + " ");}
            }
            System.out.println();
        }
    }

     */
    public static String[][] removePiece(String[][] tBoard, String move) {
        String[][] board = new String[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(tBoard[i], 0, board[i], 0, 8);
        }
        int x = move.charAt(5)-'A';
        int y = move.charAt(6)-'1';
        if (x > 7 || x < 0 || y > 7 || y < 0) {
            return board;
        }
        board[x][y] = "  ";
        return board;
    }
    public static void updateAttackedPieces(String[][] board) {
        for (int i =0; i < 8; i++) {
            for (int k =0; k<8;k++) {
                isBlackAttacking[i][k] = false;
                isWhiteAttacking[i][k] = false;
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 8; k++) {
                boolean mover = board[i][k].charAt(0) == 'W';
                switch (board[i][k].charAt(1)) {
                    case 'P' -> {
                        String[] testMoves = new String[2];
                        if (mover) {
                            testMoves[0] = "P " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k + 2);
                            testMoves[1] = "P " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k + 2);
                        } else {
                            testMoves[0] = "P " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k);
                            testMoves[1] = "P " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k);
                        }
                        for (String a : testMoves) {
                            int x = a.charAt(5)-'A';
                            int y = a.charAt(6)-'1';
                            if (x <= 7 && x >= 0 && y <= 7 && y >= 0) {
                                if (mover) {isWhiteAttacking[x][y] = true;}
                                else {isBlackAttacking[x][y] = true;}
                            }
                        }
                    }
                    case 'K' -> {
                        String[] testMoves = new String[8];
                        testMoves[0] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k + 2);
                        testMoves[1] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k + 2);
                        testMoves[2] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k + 2);
                        testMoves[3] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k + 1);
                        testMoves[4] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k + 1);
                        testMoves[5] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k);
                        testMoves[6] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k);
                        testMoves[7] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k);
                        ArrayList<String> validMoves = new ArrayList<>(4);
                        for (String a : testMoves) {
                            String[][] tBoard = removePiece(board, a);
                            if (determineMove(a.charAt(0), tBoard, a, mover, true, previousMove, blackKingMoved, whiteKingMoved, true)) {
                                validMoves.add(a);
                            }
                        }
                        for (String a : validMoves) {
                            int x = a.charAt(5)-'A';
                            int y = a.charAt(6)-'1';
                            if (mover){isWhiteAttacking[x][y] = true;}
                            else {isBlackAttacking[x][y] = true;}
                        }
                    }
                    case 'N' -> {
                        String[] testMoves = new String[8];
                        testMoves[0] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 2) + (k);
                        testMoves[1] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 2) + (k + 2);
                        testMoves[2] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k + 3);
                        testMoves[3] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k - 1);
                        testMoves[4] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k + 3);
                        testMoves[5] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k - 1);
                        testMoves[6] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 2) + (k);
                        testMoves[7] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 2) + (k + 2);
                        ArrayList<String> validMoves = new ArrayList<>(4);
                        for (String a : testMoves) {
                            String[][] tBoard = removePiece(board, a);
                            if (determineMove(a.charAt(0), tBoard, a, mover, true, previousMove, blackKingMoved, whiteKingMoved, true)) {
                                validMoves.add(a);
                            }
                        }
                        for (String a : validMoves) {
                            int x = a.charAt(5)-'A';
                            int y = a.charAt(6)-'1';
                            if (mover){isWhiteAttacking[x][y] = true;}
                            else {isBlackAttacking[x][y] = true;}
                        }
                    }
                    case 'R' -> {
                        String[] testMoves = new String[28];
                        for (int b = 1; b < 8; b++) {
                            testMoves[b - 1] = "R " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + b) + (k + 1);
                            testMoves[b + 6] = "R " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - b) + (k + 1);
                            testMoves[b + 13] = "R " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k + 1 + b);
                            testMoves[b + 20] = "R " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k + 1 - b);
                        }
                        ArrayList<String> validMoves = new ArrayList<>(4);
                        for (String a : testMoves) {
                            String[][] tBoard = removePiece(board, a);
                            if (determineMove(a.charAt(0), tBoard, a, mover, true, previousMove, blackKingMoved, whiteKingMoved, true)) {
                                validMoves.add(a);
                            }
                        }
                        for (String a : validMoves) {
                            int x = a.charAt(5)-'A';
                            int y = a.charAt(6)-'1';
                            if (mover){isWhiteAttacking[x][y] = true;}
                            else {isBlackAttacking[x][y] = true;}
                        }
                    }
                    case 'B' -> {
                        String[] testMoves = new String[28];
                        for (int b = 1; b < 8; b++) {
                            testMoves[b - 1] = "B " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + b) + (k + 1 + b);
                            testMoves[b + 6] = "B " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - b) + (k + 1 - b);
                            testMoves[b + 13] = "B " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - b) + (k + 1 + b);
                            testMoves[b + 20] = "B " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + b) + (k + 1 - b);
                        }
                        ArrayList<String> validMoves = new ArrayList<>(4);
                        for (String a : testMoves) {
                            String[][] tBoard = removePiece(board, a);
                            if (determineMove(a.charAt(0), tBoard, a, mover, true, previousMove, blackKingMoved, whiteKingMoved, true)) {
                                validMoves.add(a);
                            }
                        }
                        for (String a : validMoves) {
                            int x = a.charAt(5)-'A';
                            int y = a.charAt(6)-'1';
                            if (mover){isWhiteAttacking[x][y] = true;}
                            else {isBlackAttacking[x][y] = true;}
                        }
                    }
                    case 'Q' -> {
                        String[] testMoves = new String[56];
                        for (int b = 1; b < 8; b++) {
                            testMoves[b - 1] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + b) + (k + 1);
                            testMoves[b + 6] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - b) + (k + 1);
                            testMoves[b + 13] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k + 1 + b);
                            testMoves[b + 20] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k + 1 - b);
                            testMoves[b + 27] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + b) + (k + 1 + b);
                            testMoves[b + 34] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - b) + (k + 1 - b);
                            testMoves[b + 41] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - b) + (k + 1 + b);
                            testMoves[b + 48] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + b) + (k + 1 - b);
                        }
                        ArrayList<String> validMoves = new ArrayList<>(4);
                        for (String a : testMoves) {
                            String[][] tBoard = removePiece(board, a);
                            if (determineMove(a.charAt(0), tBoard, a, mover, true, previousMove, blackKingMoved, whiteKingMoved, true)) {
                                validMoves.add(a);
                            }
                        }
                        for (String a : validMoves) {
                            int x = a.charAt(5)-'A';
                            int y = a.charAt(6)-'1';
                            if (mover){isWhiteAttacking[x][y] = true;}
                            else {isBlackAttacking[x][y] = true;}
                        }
                    }
                }
            }
        }
    }
    public static String[][] testBoard(String[][] board, String move, boolean mover, String pMove) {
        String[][] newBoard = new String[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, 8);
        }
        if (move.charAt(0) == 'C') { // Castling
            int y = 0;
            int castleX = 0;
            if (!mover) {y = 7;}
            if (move.charAt(1) == 'H') {castleX = 7;}
            int i;
            int k;
            if (castleX == 7) {
                i = 5;
                k = 6;
            }
            else {
                i = 3;
                k = 2;
            }
            newBoard[4][y] = "  ";
            if (mover) {
                newBoard[i][y] = "W";
                newBoard[k][y] = "W";
            }
            else {
                newBoard[i][y] = "B";
                newBoard[k][y] = "B";
            }
            newBoard[i][y] += "R";
            newBoard[k][y] += "K";
            newBoard[castleX][y] = "  ";
            return newBoard;
        }
        int a = move.charAt(5)-'A';
        int c = move.charAt(6)-'1';
        if (move.charAt(0) == 'P' && newBoard[a][c].equals("  ") && pMove.length() > 5 && pMove.charAt(5) == move.charAt(5) && ((mover && pMove.charAt(6) == move.charAt(6)-1) || (!mover && pMove.charAt(6) == move.charAt(6)+1))) {
            int b = move.charAt(6)-'1' - 1;
            if (!mover) {b += 2;}
            newBoard[move.charAt(5)-'A'][b] = "  ";
        }
        newBoard[move.charAt(2)-'A'][move.charAt(3)-'1'] = "  ";
        if (mover) {
            newBoard[a][c] = "W";
        } else {
            newBoard[a][c] = "B";
        }
        if (move.charAt(0) == 'P' && ((c == 7 && mover) || (c == 0 && !mover))) {
            newBoard[a][c] += "Q";
        }
        else {newBoard[a][c] += move.charAt(0);}
        return newBoard;
    }
    public static boolean determineMove(char p, String[][] board, String move, boolean mover, boolean request, String preMove, boolean bKing, boolean wKing, boolean bypass) {
        boolean madeMove = false;
        if (p == 'C' && ((mover && !wKing) || (!mover && !bKing)) && move.length() == 2 && ((move.charAt(1) == 'A' && (mover && board[0][0].equals("WR") || !mover && board[0][7].equals("BR"))) || (move.charAt(1) == 'H'&& (mover && board[7][0].equals("WR") || !mover && board[7][7].equals("BR"))))) {
            boolean canMove = castle(move, board, mover, true);
            if (request) {return canMove;}
            if (canMove) {
                castle(move, board, mover, false);
                updateAttackedPieces(board);
                return false;
            }
        }
        if (move.length() != 7) {
            return false;
        }
        else if (move.charAt(2) >'H' || move.charAt(2) < 'A' || move.charAt(5) > 'H' || move.charAt(5) < 'A') {p = 'A';}
        else if (move.charAt(4) != '-') {p = 'A';}
        else if ((int)move.charAt(3) -'1'> 7 || (int)move.charAt(3)-'1' < 0) {p = 'A';}
        else if ((int)move.charAt(6) -'1'> 7 || (int)move.charAt(6)-'1' < 0) {p = 'A';}
        int beginX = move.charAt(2) - 'A';
        int beginY = move.charAt(3) - '1';
        int endX = move.charAt(5) - 'A';
        int endY = move.charAt(6) - '1';
        if (endX == beginX && endY == beginY) {p = 'A';}
        if (p != 'A' && ((mover && board[endX][endY].charAt(0) == 'W') || (!mover && board[endX][endY].charAt(0) == 'B'))) {p = 'A';}
        String pieceExpected;
        if (mover) {pieceExpected = "W";}
        else {pieceExpected = "B";}
        pieceExpected += p;
        if (!bypass && p != 'A' && !(aiExists && mover == aiTeam)) {
            String[][] tempBoard = testBoard(board, move, mover, previousMove);
            updateAttackedPieces(tempBoard);
            if (mover && isWhiteInCheck(tempBoard, isBlackAttacking)) {
                p = 'A';
            }
            else if (!mover && isBlackInCheck(tempBoard, isWhiteAttacking)) {
                p = 'A';
            }
            updateAttackedPieces(currentBoard);
        }
        boolean attackingPiece = false;
        if (p != 'A' && !pieceExpected.equals(board[beginX][beginY])) {
            return false;
        }
        else {
            if (p != 'A') {attackingPiece = !board[endX][endY].equals("  ");}
            switch (p) {
                case 'P' -> {
                    boolean canMove = pawn(move, board, mover, true, preMove);
                    if (request) {return canMove;}
                    if (canMove) {
                        pawn(move, board, mover, false, preMove);
                        madeMove = true;
                    }
                }
                case 'R' -> {
                    boolean canMove = rook(move, board, mover, true);
                    if (request) {return canMove;}
                    if (canMove) {
                        rook(move, board, mover, false);
                        madeMove = true;
                    }
                }
                case 'K' -> {
                    boolean canMove = king(move, board, mover, true);
                    if (request) {return canMove;}
                    if (canMove) {
                        king(move, board, mover, false);
                        madeMove = true;
                    }
                }
                case 'N' -> {
                    boolean canMove = knight(move, board, mover, true);
                    if (request) {return canMove;}
                    if (canMove) {
                        knight(move, board, mover, false);
                        madeMove = true;
                    }
                }
                case 'B' -> {
                    boolean canMove = bishop(move, board, mover, true);
                    if (request) {return canMove;}
                    if (canMove) {
                        bishop(move, board, mover, false);
                        madeMove = true;
                    }
                }
                case 'Q' -> {
                    boolean canMove = queen(move, board, mover, true);
                    if (request) {return canMove;}
                    if (canMove) {
                        queen(move, board, mover, false);
                        madeMove = true;
                    }
                }
                default -> {return false;}

            }
        }
        if (madeMove) {
            boardStates.add(boardToString(board));
            if (attackingPiece) {movesSinceCapture = 0;}
            else {movesSinceCapture++;}
            updateAttackedPieces(board);
            boolean b = blackLost(board, previousMove);
            boolean w = whiteLost(board, previousMove);
            boolean s = stalemate(previousMove, board, isBlackAttacking, isWhiteAttacking);
            if (b || w) {
                gameOver = true;
                System.out.println("Checkmate");
                /*
                if (aiExists) {
                    File fileToBeModified = new File("~/IdeaProjects/Chess-Minimax2/Scores.txt");
                    String content = "";
                    BufferedReader reader = new BufferedReader(new FileReader(fileToBeModified));
                    String line = reader.readLine();
                    boolean foundName = false;
                    while (line != null) {
                        if (line.substring(0, name.length()).equals(name)) {
                            foundName = true;
                            line = editScores(line, w, !aiTeam);
                            System.out.println(line);
                        }
                        content = content + line + System.lineSeparator();
                        line = reader.readLine();
                    }
                    if (!foundName) {
                        String l = name + ":0-0";
                        l = editScores(l, w, !aiTeam);
                        System.out.println(l);
                        content = content + l + System.lineSeparator();
                    }
                    FileWriter writer = new FileWriter(fileToBeModified);
                    writer.write(content);
                    reader.close();
                    writer.close();
                }

                 */
            }
            if (s) {
                gameOver = true;
                System.out.println("Stalemate");
            }
        }
        return false;
    }
    public static String editScores(String line, boolean whiteLost, boolean playerTeam) {
        int playerScore = Integer.parseInt(line.substring(name.length() + 1, line.indexOf('-')));
        int aiScore = Integer.parseInt(line.substring(line.indexOf('-')+1));
        if (whiteLost && !playerTeam) {
            return name + ":" + (playerScore+1) + "-" + aiScore;
        }
        else {
            return name + ":" + playerScore + "-" + (aiScore+1);
        }
    }
    public static boolean castle(String move, String[][] board, boolean mover, boolean request) {
        int castleX = (int) move.charAt(1) - 'A';
        boolean makeMove = castleX == 0 || castleX == 7;
        int y = 0;
        if (!mover) {y = 7;}
        if (rooks[castleX/7][y/7]) {
            makeMove = false;
        }
        updateAttackedPieces(board);
        if ((y == 0 && isBlackAttacking[4][0]) || (y == 7 && isWhiteAttacking[4][7])) {
            makeMove = false;
        }
        if (castleX == 0) {
            for (int i = 1; i < 4; i++) {
                if (!board[i][y].equals("  ") || (y == 0 && isBlackAttacking[i][y]) || (y == 7 && isWhiteAttacking[i][y])) {
                    makeMove = false;
                    break;
                }
            }
        }
        else if (castleX == 7) {
            for (int i = 5; i < 7; i++) {
                if (!board[i][y].equals("  ") || (y == 0 && isBlackAttacking[i][y]) || (y == 7 && isWhiteAttacking[i][y])) {
                    makeMove = false;
                    break;
                }
            }
        }
        int i;
        int k;
        if (castleX == 7) {
            i = 5;
            k = 6;
        }
        else {
            i = 3;
            k = 2;
        }
        if (request) {return makeMove;}
        if (makeMove) {
            board[4][y] = "  ";
            if (mover) {
                currentBoard[i][y] = "W";
                currentBoard[k][y] = "W";
                whiteKingMoved = true;
            }
            else {
                currentBoard[i][y] = "B";
                currentBoard[k][y] = "B";
                blackKingMoved = true;
            }
            currentBoard[i][y] += "R";
            currentBoard[k][y] += "K";
            currentBoard[castleX][y] = "  ";
            previousMove = move;
            rooks[castleX/7][y/7] = true;
        }
        else {
            return false;
        }
        return false;
    }
    public static boolean pawn(String move, String[][] board, boolean mover, boolean request, String preMove) {
        Scanner in = new Scanner(System.in);
        move = move.substring(2);
        int beginX = move.charAt(0) - 'A';
        int beginY = move.charAt(1) - '1';
        int endX = move.charAt(3) - 'A';
        int endY = move.charAt(4) - '1';
        boolean makeMove = false;
        if (mover) {
            if (beginX == endX) {
                if (endY - beginY == 1 && board[endX][endY].charAt(0) == ' ') {
                    makeMove = true;
                }
                else if (endY - beginY == 2 && beginY ==1) {
                    makeMove = (board[beginX][endY - 1].equals("  ") && (board[endX][endY].charAt(0) == ' '));
                }
            }
            else {
                makeMove = (Math.abs(beginX - endX) == 1 && endY - beginY == 1 && board[endX][endY].charAt(0) == 'B');
                if (preMove.length() > 2) {
                    char p = preMove.charAt(0);
                    int difference = Math.abs(preMove.charAt(3)-preMove.charAt(6));
                    if (p == 'P' && difference == 2 && (beginX == preMove.charAt(5) - 'A'+1 || beginX == preMove.charAt(5)-'A'-1) && beginY == preMove.charAt(6) - '1') {
                        if (Math.abs(endX-beginX) == 1 && endY-beginY == 1){
                            if (request) {return true;}
                            currentBoard[beginX][beginY] = "  ";
                            currentBoard[endX][endY] = "WP";
                            currentBoard[preMove.charAt(5)-'A'][preMove.charAt(6)-'1'] = "  ";
                            previousMove = "P " + move;
                            return false;
                        }
                    }
                }
            }
            if (request) {return makeMove;}
            if (!makeMove) {
                return false;
            }
            else {
                currentBoard[beginX][beginY] = "  ";
                currentBoard[endX][endY] = "WP";
                previousMove = "P " + move;
                if (endY == 7) {
                    char b;
                    String a;
                    if (aiTeam && aiExists){
                        b = 'Q';
                    }
                    else {
                        System.out.println("Would u like to promote to a Q or a N?");
                        a = in.nextLine().toUpperCase();
                        b = a.charAt(0);
                    }
                    while (b != 'Q' && b != 'N') {
                        System.out.println("Would u like to promote to a Q or a N?");
                        a = in.nextLine().toUpperCase();
                        b = a.charAt(0);
                    }
                    currentBoard[endX][endY] = "W" + b;
                }
            }
        }
        else {
            if (beginX == endX) {
                if (endY - beginY == -1 && (board[endX][endY].charAt(0) == ' ')) {
                    makeMove = true;
                }
                else if (endY - beginY == -2 && beginY == 6) {
                    makeMove = (board[endX][endY + 1].equals("  ") && (board[endX][endY].charAt(0) == ' '));
                }
            }
            else {
                makeMove = (Math.abs(beginX - endX) == 1 && endY - beginY == -1 && board[endX][endY].charAt(0) == 'W');
                if (preMove.length() > 2) {
                    char p = preMove.charAt(0);
                    int difference = Math.abs(preMove.charAt(3)-preMove.charAt(6));
                    if (p == 'P' && difference == 2 && (beginX == preMove.charAt(5) - 'A'+1 || beginX == preMove.charAt(5)-'A'-1) && beginY == preMove.charAt(6) - '1') {
                        if (Math.abs(endX-beginX) == 1 && endY-beginY == -1){
                            if (request){return true;}
                            currentBoard[beginX][beginY] = "  ";
                            currentBoard[endX][endY] = "BP";
                            currentBoard[preMove.charAt(5)-'A'][preMove.charAt(6)-'1'] = "  ";
                            previousMove = "P " + move;
                            return false;
                        }
                    }
                }
            }
            if (request){return makeMove;}
            if (!makeMove) {return false;}
            else {
                currentBoard[beginX][beginY] = "  ";
                currentBoard[endX][endY] = "BP";
                previousMove = "P " + move;
                if (endY == 0) {
                    char b;
                    String a;
                    if (!aiTeam && aiExists){
                        b = 'Q';
                    }
                    else {
                        System.out.println("Would u like to promote to a Q or a N?");
                        a = in.nextLine().toUpperCase();
                        b = a.charAt(0);
                    }
                    while (b != 'Q' && b != 'N') {
                        System.out.println("Would u like to promote to a Q or a N?");
                        a = in.nextLine().toUpperCase();
                        b = a.charAt(0);
                    }
                    currentBoard[endX][endY] = "B" + b;
                }
            }
        }
        return false;
    }
    public static boolean knight(String move, String[][] board, boolean mover, boolean request) {
        move = move.substring(2);
        int beginX = move.charAt(0) - 'A';
        int beginY = move.charAt(1) - '1';
        int endX = move.charAt(3) - 'A';
        int endY = move.charAt(4) - '1';
        boolean makeMove = ((Math.abs(endX-beginX) == 1 && Math.abs(endY-beginY) == 2) || (Math.abs(endX-beginX) == 2 && Math.abs(endY-beginY) == 1));
        if (request) {return makeMove;}
        if (!makeMove) {return false;}
        else {
            board[beginX][beginY] = "  ";
            if (mover) {currentBoard[endX][endY] = "W";}
            else {currentBoard[endX][endY] = "B";}
            currentBoard[endX][endY] += "N";
            previousMove = "N " + move;
        }
        return false;
    }
    public static boolean king(String move, String[][] board, boolean mover, boolean request) {
        move = move.substring(2);
        int beginX = move.charAt(0) - 'A';
        int beginY = move.charAt(1) - '1';
        int endX = move.charAt(3) - 'A';
        int endY = move.charAt(4) - '1';
        boolean makeMove = false;
        if (mover) {
            if (!board[beginX][beginY].equals("WK")) {
                return false;
            }
            if (board[endX][endY].charAt(0) != 'W' && Math.abs(endX-beginX) <=1 && Math.abs(endY-beginY) <= 1){
                makeMove = true;
            }
        }
        else {
            if (!board[beginX][beginY].equals("BK")) {return false;}
            if (board[endX][endY].charAt(0) != 'B' && Math.abs(endX-beginX) <=1 && Math.abs(endY-beginY) <= 1){
                makeMove = true;
            }
        }
        if (request) {return makeMove;}
        if (!makeMove) {
            return false;
        }
        else {
            currentBoard[endX][endY] = currentBoard[beginX][beginY];
            currentBoard[beginX][beginY] = "  ";
            if (mover) {
                whiteKingMoved = true;
            }
            else {
                blackKingMoved = true;
            }
            previousMove = "K " + move;
        }
        return false;
    }
    public static boolean bishop(String move, String[][] board, boolean mover, boolean request) {
        move = move.substring(2);
        int beginX = move.charAt(0) - 'A';
        int beginY = move.charAt(1) - '1';
        int endX = move.charAt(3) - 'A';
        int endY = move.charAt(4) - '1';
        boolean makeMove = Math.abs(endX-beginX) == Math.abs(endY-beginY);
        for (int i = 1; i < Math.abs(endX-beginX); i++) {
            if (makeMove && !board[beginX+(i*(endX-beginX)/Math.abs(endX-beginX))][beginY+(i*(endY-beginY)/Math.abs(endY-beginY))].equals("  ")) {
                makeMove = false;
                break;
            }
        }
        if (request) {return makeMove;}
        if (!makeMove) {
            return false;
        }
        else {
            currentBoard[beginX][beginY] = "  ";
            if (mover) {
                currentBoard[endX][endY] = "W";
            } else {
                currentBoard[endX][endY] = "B";
            }
            currentBoard[endX][endY] += "B";
            previousMove = "B " + move;
        }
        return false;
    }
    public static boolean rook(String move, String[][] board, boolean mover, boolean request) {
        move = move.substring(2);
        int beginX = move.charAt(0) - 'A';
        int beginY = move.charAt(1) - '1';
        int endX = move.charAt(3) - 'A';
        int endY = move.charAt(4) - '1';
        boolean makeMove = true;
        if ((endX != beginX && endY != beginY) || (endX == beginX && endY == beginY)) {return false;}
        if (mover) {
            if (beginX == endX) {
                if (beginY-endY > 0) {
                    for (int i = beginY-1; i > endY; i--) {
                        makeMove = board[beginX][i].equals("  ");
                        if (!makeMove) {break;}
                    }
                }
                else {
                    for (int i = beginY+1; i < endY; i++) {
                        makeMove = board[beginX][i].equals("  ");
                        if (!makeMove) {break;}
                    }
                }
                if (makeMove) {
                    makeMove = (board[endX][endY].charAt(0) != 'W');
                }
            }
            else {
                if (beginX-endX > 0) {
                    for (int i = beginX-1; i > endX; i--) {
                        makeMove = board[i][beginY].equals("  ");
                        if (!makeMove) {break;}
                    }
                }
                if (beginX-endX < 0) {
                    for (int i = beginX+1; i < endX; i++) {
                        makeMove = board[i][beginY].equals("  ");
                        if (!makeMove) {break;}
                    }
                }
                if (makeMove) {
                    makeMove = (board[endX][endY].charAt(0) != 'W');
                }
            }
        }
        else {
            if (beginX == endX) {
                if (beginY-endY > 0) {
                    for (int i = beginY-1; i > endY; i--) {
                        makeMove = board[beginX][i].equals("  ");
                        if (!makeMove) {break;}
                    }
                }
                else {
                    for (int i = beginY+1; i < endY; i++) {
                        makeMove = board[beginX][i].equals("  ");
                        if (!makeMove) {break;}
                    }
                }
                if (makeMove) {
                    makeMove = (board[endX][endY].charAt(0) != 'B');
                }
            }
            else {
                if (beginX-endX > 0) {
                    for (int i = beginX-1; i > endX; i--) {
                        makeMove = board[i][beginY].equals("  ");
                        if (!makeMove) {break;}
                    }
                }
                if (beginX-endX < 0) {
                    for (int i = beginX+1; i < endX; i++) {
                        makeMove = board[i][beginY].equals("  ");
                        if (!makeMove) {break;}
                    }
                }
                if (makeMove) {
                    makeMove = (board[endX][endY].charAt(0) != 'B');
                }
            }
        }
        if (request) {return makeMove;}
        if (makeMove) {
            currentBoard[endX][endY] = currentBoard[beginX][beginY];
            currentBoard[beginX][beginY] = "  ";
            previousMove = "R " + move;
            if ((beginX == 0 || beginX == 7) && (beginY == 0 || beginY == 7)) {rooks[beginX/7][beginY/7] = true;}
        }
        else {
            return false;
        }
        return false;
    }
    public static boolean queen(String move, String[][] board, boolean mover, boolean request) {
        String moveT = move.substring(2);
        int beginX = moveT.charAt(0) - 'A';
        int beginY = moveT.charAt(1) - '1';
        int endX = moveT.charAt(3) - 'A';
        int endY = moveT.charAt(4) - '1';
        boolean makeMove;
        if (beginX == endX || beginY == endY) {makeMove = rook(move,board,mover,true);}
        else {makeMove = bishop(move,board,mover,true);}
        if (mover && board[endX][endY].charAt(0) == 'W' || !mover && board[endX][endY].charAt(0) == 'B') {makeMove = false;}
        if (request) {return makeMove;}
        if (makeMove) {
            currentBoard[beginX][beginY] = "  ";
            if (mover) {
                currentBoard[endX][endY] = "W";
            } else {
                currentBoard[endX][endY] = "B";
            }
            currentBoard[endX][endY] += "Q";
            previousMove = move;
        }
        else {
            return false;
        }
        return false;
    }
    public static double[] score(String[][] board, double AI) {
        double whiteScore = 0;
        double blackScore = 0;
        boolean wKing = false;
        boolean bKing = false;
        for (double x = 0; x < 8; x++) {
            for (double y = 0; y < 8; y++) {
                switch (board[(int)x][(int)y].charAt(1)) {
                    case 'K':
                        if (board[(int)x][(int)y].charAt(0) == 'W') {
                            wKing = true;
                            whiteScore+= (7-y)/10;
                        }
                        else {
                            bKing = true;
                            blackScore += y/10;
                        }
                        break;
                    case 'N':
                    case 'B':
                        if(board[(int)x][(int)y].charAt(0) == 'W') {
                            if ((int)y == 0) {whiteScore--;}
                            whiteScore+= 3;
                            if (x < 4) {
                                whiteScore += x*x/20;
                            }
                            else {
                                whiteScore += (7-x)*(7-x)/20;
                            }
                            if (y < 4) {
                                whiteScore+= y*y/20;
                            }
                            else {
                                whiteScore+= (7-y)*(7-y)/20;
                            }
                        }
                        else {
                            blackScore+= 3;
                            if ((int)y == 7) {blackScore--;}
                            if (x < 4) {
                                blackScore += x*x/20;
                            }
                            else {
                                blackScore += (7-x)*(7-x)/20;
                            }
                            if (y < 4) {
                                blackScore+= y*y/20;
                            }
                            else {
                                blackScore+= (7-y)*(7-y)/20;
                            }
                        }
                        break;
                    case 'R': if(board[(int)x][(int)y].charAt(0) == 'W') {
                        if ((int)y == 0 && ((int)x == 0 || (int)x == 7)) {whiteScore--;}
                        whiteScore+= 5;
                        if (x < 4) {
                            whiteScore += (3-x)*y/20;
                        }
                        else {
                            whiteScore += Math.abs(3-x)*y/20;
                        }
                    }
                    else {
                        if ((int)y == 7 && ((int)x == 0 || (int)x == 7)) {blackScore--;}
                        blackScore += 5;
                        if (x < 4) {
                            blackScore += (4-x)*(7-y)/20;
                        }
                        else {
                            blackScore += Math.abs(4-x)*(7-y)/20;
                        }
                    }
                        break;
                    case 'Q': if(board[(int)x][(int)y].charAt(0) == 'W') {
                        if ((int)y == 0) {whiteScore--;}
                        whiteScore+= 9;
                        if (x < 4) {
                            whiteScore += x*x/20;
                        }
                        else {
                            whiteScore += (7-x)*(7-x)/20;
                        }
                        if (y < 4) {
                            whiteScore+= y*y/20;
                        }
                        else {
                            whiteScore+= (7-y)*(7-y)/20;
                        }
                    }
                    else {
                        if ((int)y == 7) {blackScore--;}
                        blackScore+=9;
                        if (x < 4) {
                            blackScore += x*x/20;
                        }
                        else {
                            blackScore += (7-x)*(7-x)/20;
                        }
                        if (y < 4) {
                            blackScore+= y*y/20;
                        }
                        else {
                            blackScore+= (7-y)*(7-y)/20;
                        }
                    }
                        break;
                    case 'P':
                        if(board[(int)x][(int)y].charAt(0) == 'W') {
                            if ((int)x>1 && (int)x < 6 && (int)y == 1) {
                                whiteScore--;
                            }
                            whiteScore+= 1 + AI;
                            if (x < 4) {
                                whiteScore += x*(x/20);
                                whiteScore += x*y/20;
                            }
                            else {
                                whiteScore += (Math.abs(7-x)*Math.abs(7-x)/20);
                                whiteScore += Math.abs(7-x)*y/20;
                            }
                        }
                        else {
                            blackScore += 1 + AI;
                            if ((int)x>1 && (int)x < 6 && (int)y == 6) {
                                blackScore--;
                            }
                            if (x < 4) {
                                blackScore += Math.abs(7-x)*(x/20);
                                blackScore += x*(7-y)/20;
                            }
                            else {
                                blackScore += Math.abs(7-x)*(Math.abs(7-x)/20);
                                blackScore += Math.abs(7-x)*(7-y)/20;
                            }
                        }
                        break;
                }
            }
        }
        double[] r = new double[3];
        if (!bKing && wKing) {
            r[0] = 1;
        }
        else if (!wKing && bKing) {
            r[0] = -1;
        }
        r[1] =  whiteScore;
        r[2] = blackScore;
        return r;
    }

    public static void main(String[] args) {
        // Move notation [Piece abbreviation] [starting square]-[ending square]
        Scanner input = new Scanner(System.in);
        int people = 0;
        boolean team = true;
        System.out.println("What is your name?");
        name = input.nextLine().toLowerCase();
        while (people == 0) {
            System.out.println("How many people are playing?");
            String a = input.nextLine();
            switch (a) {
                case "2" -> {people = Integer.parseInt(a);
                    aiExists = false;}
                case "1" -> {
                    aiExists = true;
                    System.out.println("Which team are you playing?");
                    String x = input.nextLine();
                    if (x.equalsIgnoreCase("black")) {
                        aiTeam = true;
                        team = false;
                        people = Integer.parseInt(a);

                    }
                    else if (x.equalsIgnoreCase("white")){
                        aiTeam = false;
                        team = true;
                        people = Integer.parseInt(a);
                    }
                }
                case "custom" -> people = -1;
            }
        }
        String[][] chessboard = new String[8][8];
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 8; k++) {
                chessboard[i][k] = "  ";
            }
        }
        if (people != -1) {
            for (int i = 0; i < 8; i++) {
                chessboard[i][1] = "WP";
                chessboard[i][6] = "BP";
                chessboard[i][0] = "W";
                chessboard[i][7] = "B";
            }
            for (int i = 0; i < 2; i++) {
                chessboard[0][7 * i] += "R";
                chessboard[7][7 * i] += "R";
                chessboard[1][7 * i] += "N";
                chessboard[6][7 * i] += "N";
                chessboard[2][7 * i] += "B";
                chessboard[5][7 * i] += "B";
                chessboard[3][7 * i] += "Q";
                chessboard[4][7 * i] += "K";
            }
        }
        else {
            chessboard[0][7] = "BR";
            chessboard[3][4] = "WQ";
            chessboard[3][7] = "BK";
            chessboard[3][0] = "WK";
            chessboard[0][1] = "WP";
            chessboard[7][6] = "WR";
        }
        currentBoard = chessboard;
        if (team) {
            frame = new MyFrame(chessboard, people, true);
        }
        else {
            frame = new MyFrame(chessboard, people, false);
        }
/*
        boolean gameOver = false;
        int moveCount = 0;
        String[] openings = {"P D7-D5", "P E7-E5", "N B8-C6", "N G8-F6"};
        String[] whiteOpenings = {"P D2-D4", "P E2-E4", "N B1-C3", "N G1-F3"};
        String move1;
        int x = (int)(Math.random()*4);
        while (!gameOver) {
            if (moveCount%2 == 0) { //White
                if ((people == 1 || people == -1) && !team) {
                    if (moveCount == 0 && people != -1) {
                        move1 = whiteOpenings[x];
                        determineMove(move1.charAt(0), currentBoard, move1, true, false, previousMove, blackKingMoved, whiteKingMoved);
                    }
                    else {
                        String[][] v = currentBoard;
                        lookAhead(v, 0, true, previousMove, blackKingMoved, whiteKingMoved, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                    }
                }
                else {
                    System.out.println("White, enter your move: ");
                    String white = input.nextLine();
                    white = white.toUpperCase();
                    if (white.contains("CUSTOM") && people != 1) {
                        if (white.contains("PERMANENT")) {
                            people = 1;
                            team = false;
                        }
                        String[][] v = currentBoard;
                        lookAhead(v, 0, true, previousMove, blackKingMoved, whiteKingMoved, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                    } else {
                        char piece = 'A';
                        if (white.length() > 0) {
                            piece = white.charAt(0);
                        }
                        determineMove(piece, currentBoard, white, true, false, previousMove, blackKingMoved, whiteKingMoved);
                    }
                }
            }
            else {
                if ((people == 1 || people == -1) && team) {
                    if (moveCount == 1 && people != -1) {
                        move1 = openings[x];
                        determineMove(move1.charAt(0), currentBoard, move1, false, false, previousMove, blackKingMoved, whiteKingMoved);
                    }
                    else {
                        String[][] v = currentBoard;
                        lookAhead(v, 0, false, previousMove, blackKingMoved, whiteKingMoved, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                    }
                }
                else {
                    System.out.println("Black, enter your move: ");
                    String black = input.nextLine();
                    black = black.toUpperCase();
                    if (black.contains("CUSTOM") && people != 1) {
                        if (black.contains("PERMANENT")) {
                            people = 1;
                            team = true;
                        }
                        String[][] v = currentBoard;
                        lookAhead(v, 0, false, previousMove, blackKingMoved, whiteKingMoved, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                    }
                    else {
                        char piece = black.charAt(0);
                        determineMove(piece, currentBoard, black, false, false, previousMove, blackKingMoved, whiteKingMoved);
                    }
                }
            }
            String u = previousMove;
            if (previousMove.length() < 7) {
                if (moveCount%2==0){previousMove = 'W'+previousMove;}
                else {previousMove = 'B'+previousMove;}
            }
            frame.paint(currentBoard, previousMove, moveCount%2==1, whiteKingMoved, blackKingMoved, previousMove);
            previousMove = u;
            double[] s = score(currentBoard, 0);
            if (s[0] == 1) {gameOver = true;
                System.out.println("White Wins");
            }
            if (s[0] == -1) {
                gameOver = true;
                System.out.println("Black Wins");
            }
            moveCount++;
        }
*/
    }
    //AI
    public static double lookAhead(String[][] board, int times, boolean mover, String prevMove, boolean bKing, boolean wKing, double alpha, double beta, boolean dontMove, boolean bypassKing, boolean checkForStalemate, ArrayList<String> boardStates2) {
        times--;
        long startTime = 0;
        if (times == 4) {
            startTime = System.currentTimeMillis();
        }
        ArrayList<String> moves = new ArrayList<>(40);
        boolean needsBreak = false;
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 8; k++) {
                if ((mover && board[i][k].charAt(0) == 'W') || (!mover && board[i][k].charAt(0) == 'B')) {
                    switch (board[i][k].charAt(1)) {
                        case 'P' -> {
                            String[] testMoves = new String[4];
                            if (mover) {
                                testMoves[0] = "P " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k + 2);
                                testMoves[1] = "P " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k + 3);
                                testMoves[2] = "P " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k + 2);
                                testMoves[3] = "P " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k + 2);
                            } else {
                                testMoves[0] = "P " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k);
                                testMoves[1] = "P " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k - 1);
                                testMoves[2] = "P " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k);
                                testMoves[3] = "P " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k);
                            }
                            for (String a : testMoves) {
                                if (determineMove('P', board, a, mover, true, prevMove, bKing, wKing, bypassKing)) {
                                    if (board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("  ")) {
                                        moves.add(a);
                                    }
                                    else {
                                        moves.add(0,a);
                                    }
                                    if ((mover && board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("BK")) || (!mover && board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("WK"))) {
                                        needsBreak = true;
                                        break;
                                    }
                                }
                            }
                        }
                        case 'K' -> {
                            String[] testMovesK = new String[8];
                            testMovesK[0] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k + 2);
                            testMovesK[1] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k + 2);
                            testMovesK[2] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k + 2);
                            testMovesK[3] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k + 1);
                            testMovesK[4] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k + 1);
                            testMovesK[5] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k);
                            testMovesK[6] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k);
                            testMovesK[7] = "K " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k);
                            for (String a : testMovesK) {
                                if (determineMove('K', board, a, mover, true, prevMove, bKing, wKing, bypassKing)) {
                                    if (board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("  ")) {
                                        moves.add(a);
                                    }
                                    else {
                                        moves.add(0,a);
                                    }
                                    if ((mover && board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("BK")) || (!mover && board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("WK"))) {
                                        needsBreak = true;
                                        break;
                                    }
                                }
                            }
                        }
                        case 'N' -> {
                            String[] testMovesN = new String[8];
                            testMovesN[0] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 2) + (k);
                            testMovesN[1] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 2) + (k + 2);
                            testMovesN[2] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k + 3);
                            testMovesN[3] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + 1) + (k - 1);
                            testMovesN[4] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k + 3);
                            testMovesN[5] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 1) + (k - 1);
                            testMovesN[6] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 2) + (k);
                            testMovesN[7] = "N " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - 2) + (k + 2);
                            for (String a : testMovesN) {
                                if (determineMove('N', board, a, mover, true, prevMove, bKing, wKing, bypassKing)) {
                                    if (board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("  ")) {
                                        moves.add(a);
                                    }
                                    else {
                                        moves.add(0,a);
                                    }
                                    if ((mover && board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("BK")) || (!mover && board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("WK"))) {
                                        needsBreak = true;
                                        break;
                                    }
                                }
                            }
                        }
                        case 'R' -> {
                            String[] testMovesR = new String[28];
                            for (int b = 1; b < 8; b++) {
                                testMovesR[b - 1] = "R " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + b) + (k + 1);
                                testMovesR[b + 6] = "R " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - b) + (k + 1);
                                testMovesR[b + 13] = "R " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k + 1 + b);
                                testMovesR[b + 20] = "R " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k + 1 - b);
                            }
                            for (String a : testMovesR) {
                                if (determineMove('R', board, a, mover, true, prevMove, bKing, wKing, bypassKing)) {
                                    if (board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("  ")) {
                                        moves.add(a);
                                    }
                                    else {
                                        moves.add(0,a);
                                    }
                                    if ((mover && board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("BK")) || (!mover && board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("WK"))) {
                                        needsBreak = true;
                                        break;
                                    }
                                }
                            }
                        }
                        case 'B' -> {
                            String[] testMovesB = new String[28];
                            for (int b = 1; b < 8; b++) {
                                testMovesB[b - 1] = "B " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + b) + (k + 1 + b);
                                testMovesB[b + 6] = "B " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - b) + (k + 1 - b);
                                testMovesB[b + 13] = "B " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - b) + (k + 1 + b);
                                testMovesB[b + 20] = "B " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + b) + (k + 1 - b);
                            }
                            for (String a : testMovesB) {
                                if (determineMove('B', board, a, mover, true, prevMove, bKing, wKing, bypassKing)) {
                                    if (board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("  ")) {
                                        moves.add(a);
                                    }
                                    else {
                                        moves.add(0,a);
                                    }
                                    if ((mover && board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("BK")) || (!mover && board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("WK"))) {
                                        needsBreak = true;
                                        break;
                                    }
                                }
                            }
                        }
                        case 'Q' -> {
                            String[] testMovesQ = new String[56];
                            for (int b = 1; b < 8; b++) {
                                testMovesQ[b - 1] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + b) + (k + 1);
                                testMovesQ[b + 6] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - b) + (k + 1);
                                testMovesQ[b + 13] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k + 1 + b);
                                testMovesQ[b + 20] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A') + (k + 1 - b);
                                testMovesQ[b + 27] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + b) + (k + 1 + b);
                                testMovesQ[b + 34] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - b) + (k + 1 - b);
                                testMovesQ[b + 41] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' - b) + (k + 1 + b);
                                testMovesQ[b + 48] = "Q " + (char) (i + 'A') + (k + 1) + "-" + (char) (i + 'A' + b) + (k + 1 - b);
                            }
                            for (String a : testMovesQ) {
                                if (determineMove('Q', board, a, mover, true, prevMove, bKing, wKing, bypassKing)) {
                                    if (board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("  ")) {
                                        moves.add(a);
                                    }
                                    else {
                                        moves.add(0,a);
                                    }
                                    if ((mover && board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("BK")) || (!mover && board[a.charAt(5) - 'A'][a.charAt(6) - '1'].equals("WK"))) {
                                        needsBreak = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (needsBreak) {break;}
                }
                if (needsBreak) {break;}
            }
        }
        if (determineMove('C', board, "CH", mover, true, prevMove, bKing, wKing, bypassKing) && !needsBreak) {
            moves.add("CH");
        }
        if (determineMove('C', board, "CA", mover, true, prevMove, bKing, wKing, bypassKing) && !needsBreak) {
            moves.add("CA");
        }
        double[] scores = new double[moves.size()];
        for (int i = 0; i < moves.size() && moves.get(i).length() > 0; i++) {
            if (times == 0 || needsBreak) {
                double a = 0;
                double f = 0.1;
                if ((times % 2 == 1 && !mover) || (times % 2 == 0 && mover)) {
                    f -= 0.2;
                }
                String[][] b = testBoard(board, moves.get(i), mover, prevMove);
                double[] d = score(b, f);
                a += d[1] - d[2];
                a += 99999 * d[0];
                if (checkForStalemate) {
                    //no moves available
                    boolean answer = false;
                    //HELP

                    //repetition
                    String x = boardToString(board);
                    int repCount = 0;
                    for (String y : boardStates) {
                        if (y.equals(x)) {repCount++;}
                    }
                    for (String y : boardStates2) {
                        if (y.equals(x)) {repCount++;}
                    }
                    if (!answer) {answer = repCount > 2;}
                    //insufficient material
                    if (!answer) {
                        ArrayList<Character> whitePieces = new ArrayList<>(16);
                        ArrayList<Character> blackPieces = new ArrayList<>(16);
                        for (int j = 0; j < 8; j++) {
                            for (int k = 0; k < 8; k++) {
                                if (board[j][k].charAt(0) == 'B') {
                                    blackPieces.add(board[j][k].charAt(1));
                                }
                                if (board[j][k].charAt(0) == 'W') {
                                    whitePieces.add(board[j][k].charAt(1));
                                }
                            }
                        }
                        boolean whiteInsuff = false;
                        boolean blackInsuff = false;
                        if (whitePieces.size()< 2){
                            if (whitePieces.contains('N') || whitePieces.contains('B') || whitePieces.size() == 1) {whiteInsuff = true;}
                        }
                        if (blackPieces.size()< 2){
                            if (blackPieces.contains('N') || blackPieces.contains('B') || blackPieces.size() == 1) {blackInsuff = true;}
                        }
                        if (whiteInsuff && blackInsuff) {answer = true;}
                    }
                    //no pieces captured in 50 moves
                    if (!answer) {answer = movesSinceCapture + (5-times) >= 50;}
                    if (answer) {a = 0;}
                }
                scores[i] = a;
            } else if (times > 0) {
                boardStates2.add(moves.get(i));
                scores[i] = lookAhead(testBoard(board, moves.get(i), mover, prevMove), times, !mover, moves.get(i), bKing, wKing, alpha, beta, dontMove, bypassKing, checkForStalemate, boardStates2);
                boardStates2.remove(boardStates2.size()-1);
                if (times == 4) {
                    System.out.print("\b\b\b");
                    System.out.print((int) (((double) (i + 1) / (double) moves.size()) * 100) + "%");
                }
            }
            if (mover) {
                alpha = Double.max(alpha, scores[i]);
            } else {
                beta = Double.min(beta, scores[i]);
            }
            if (beta <= alpha) {
                break;
            }
        }
        if (times == 4) {System.out.print("\b\b\b\b");}
        double k = 0;
        int index = 0;
        if (moves.size() > 0){
            k = scores[0];
            for (int i = 0; i < moves.size(); i++) {
                if ((scores[i] > k && mover) || (scores[i] < k && !mover)) {

                    k = scores[i];
                    index = i;
                }
            }
        }
        if (times == 4 && !dontMove) {
            double f = 0.1;
            if (mover) {f = -0.1;}
            double[] a = score(testBoard(board, moves.get(index), mover, prevMove), 0);
            System.out.println("Current eval: " + (double)((int)((a[1]-a[2])*100))/100);
            System.out.println("Future eval: " + ((double)((int)((k+f)*100))/100));
            determineMove(moves.get(index).charAt(0), currentBoard, moves.get(index), mover, false, previousMove, bKing, wKing, true);
            frame.paint(board, moves.get(index), mover, prevMove);
            System.out.println("AI move: " + moves.get(index));
            long elapsedTime = System.currentTimeMillis() - startTime;
            long elapsedSeconds = elapsedTime / 1000;
            System.out.println(elapsedSeconds + "s");
        }
        return k;
    }
}
/*
AVG EVALUATIONS WITH PRUNING AT DEPTH = 4: 24608
AVG EVALUATIONS WITHOUT PRUNING AT DEPTH = 4: 1078503
PRUNING DECREASES EVALUATIONS BY 97.25%
Add stalemate func
 */