package chess.piecemoves;

import java.util.Collection;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

public class RookMovesCalculator implements PieceMovesCalculator {
    
    private static final int[][] DIRECTIONS = {
        {1, 0},
        {-1, 0},
        {0, 1},
        {0, -1}
    };
    
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {

        return MoveGenerationUtils.collectSlidingMoves(board, position, DIRECTIONS);

    }

}
