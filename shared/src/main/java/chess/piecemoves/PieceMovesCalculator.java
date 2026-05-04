package chess.piecemoves;

import java.util.Collection;
import chess.ChessMove;
import chess.ChessBoard;
import chess.ChessPosition;

public interface PieceMovesCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}
