package chess.piecemoves;

import java.util.ArrayList;
import java.util.Collection;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;

public class KnightMovesCalculator implements PieceMovesCalculator{

    private static final int[][] MOVE_OFFSETS = {
        {2, 1},
        {1, 2},
        {-1, 2},
        {2, -1},
        {1, -2},
        {-1, -2},
        {-2, -1},
        {-2, 1}
    };
    
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {

        return MoveGenerationUtils.collectOffsetMoves(board, position, MOVE_OFFSETS);

    }
    
}
