package chess.piecemoves;

import java.util.ArrayList;
import java.util.Collection;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

public class KingMovesCalculator implements PieceMovesCalculator {

    private static final int[][] MOVE_OFFSETS = {
        {1, 0},
        {0, 1},
        {-1, 0},
        {0, -1},
        {1, 1},
        {1, -1},
        {-1, -1},
        {-1, 1}
    };
    
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {

        return MoveGenerationUtils.collectOffsetMoves(board, position, MOVE_OFFSETS);

    }

}
