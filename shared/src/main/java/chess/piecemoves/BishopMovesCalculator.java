package chess.piecemoves;

import java.util.ArrayList;
import java.util.Collection;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

public class BishopMovesCalculator implements PieceMovesCalculator {
    
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {

        ArrayList<ChessMove> valid_moves = new ArrayList<ChessMove>();

        // Check moves up and to the right
        int new_row = position.getRow();
        int new_col = position.getColumn();
        while (new_row <= 7 && new_col <= 7) {
            new_row += 1;
            new_col += 1;
            valid_moves.add(new ChessMove(position, new ChessPosition(new_row, new_col), null));
        }

        // Check moves up and to the left
        new_row = position.getRow();
        new_col = position.getColumn();
        while (new_row <= 7 && new_col >= 2) {
            new_row += 1;
            new_col -= 1;
            valid_moves.add(new ChessMove(position, new ChessPosition(new_row, new_col), null));
        }

        // Check moves down and to the left
        new_row = position.getRow();
        new_col = position.getColumn();
        while (new_row >= 2 && new_col >= 2) {
            new_row -= 1;
            new_col -= 1;
            valid_moves.add(new ChessMove(position, new ChessPosition(new_row, new_col), null));
        }

        // Check moves down and to the right
        new_row = position.getRow();
        new_col = position.getColumn();
        while (new_row >= 2 && new_col <= 7) {
            new_row -= 1;
            new_col += 1;
            valid_moves.add(new ChessMove(position, new ChessPosition(new_row, new_col), null));
        }

        return valid_moves;

    }

}