package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame.TeamColor;

import static ui.EscapeSequences.*;

public class BoardRenderer {
    
    // Light tan and dark brown for the board squares
    private static final String SET_BG_COLOR_TAN = "\u001b[48;5;222m";
    private static final String SET_BG_COLOR_BROWN = "\u001b[48;5;94m";

    public static void drawBoard(ChessGame game, ChessGame.TeamColor perspective) {

        ChessBoard board = game.getBoard();
        System.out.println();

        // White perspective: rows 8 down to 1, cols a-h (1-8)
        // Black perspective: rows 1 up to 8, cols h-a (8-1)
        boolean whiteView = (perspective == TeamColor.WHITE);
        int rowStart = whiteView ? 8 : 1;
        int rowEnd   = whiteView ? 1 : 8;
        int rowStep  = whiteView ? -1 : 1;
        int colStart = whiteView ? 1 : 8;
        int colEnd   = whiteView ? 8 : 1;
        int colStep  = whiteView ? 1 : -1;

        drawColumnLabels(colStart, colEnd, colStep);

        for (int row = rowStart; row != rowEnd + rowStep; row += rowStep) {

            // Left border with row number
            System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + " " + row + " ");

            for (int col = colStart; col != colEnd + colStep; col += colStep) {
                boolean lightSquare = (row + col) % 2 == 1;
                String bgColor = lightSquare ? SET_BG_COLOR_TAN : SET_BG_COLOR_BROWN;

                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                String pieceStr = getPieceString(piece);

                System.out.print(bgColor + pieceStr);

            }

            // Right border with row number
            System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + " " + row + " ");
            System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR);

        }

        drawColumnLabels(colStart, colEnd, colStep);
        System.out.println();

    }

    private static void drawColumnLabels(int colStart, int colEnd, int colStep) {
        
        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "  ");

        for (int col = colStart; col != colEnd + colStep; col += colStep) {
            char letter = (char) ('a' + col - 1);
            System.out.print(" " + letter + " ");
        }

        System.out.println("  " + RESET_BG_COLOR + RESET_TEXT_COLOR);

    }

    private static String getPieceString(ChessPiece piece) {

        if (piece == null) {
            return "  ";
        }

        String textColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
            ? SET_TEXT_COLOR_WHITE
            : SET_TEXT_COLOR_BLACK;

        String letter = switch (piece.getPieceType()) {
            case KING   -> "K";
            case QUEEN  -> "Q";
            case ROOK   -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN   -> "P";
        };

        return textColor + SET_TEXT_BOLD + " " + letter + " " + RESET_TEXT_BOLD_FAINT;

    }
    
}
