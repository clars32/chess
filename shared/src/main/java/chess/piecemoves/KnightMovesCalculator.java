package chess.piecemoves;

import java.util.ArrayList;
import java.util.Collection;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;

public class KnightMovesCalculator implements PieceMovesCalculator{

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {

        ArrayList<ChessMove> validMoves = new ArrayList<ChessMove>();

        ChessPiece currentPiece = board.getPiece(position);

        // Check all possible moves
        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        ArrayList<ChessPosition> surroundingSquares = new ArrayList<>();
        surroundingSquares.add(new ChessPosition(currentRow + 1, currentCol + 2));
        surroundingSquares.add(new ChessPosition(currentRow + 1, currentCol - 2));
        surroundingSquares.add(new ChessPosition(currentRow + 2, currentCol + 1));
        surroundingSquares.add(new ChessPosition(currentRow + 2, currentCol - 1));
        surroundingSquares.add(new ChessPosition(currentRow - 1, currentCol + 2));
        surroundingSquares.add(new ChessPosition(currentRow - 1, currentCol - 2));
        surroundingSquares.add(new ChessPosition(currentRow - 2, currentCol + 1));
        surroundingSquares.add(new ChessPosition(currentRow - 2, currentCol - 1));

        for (ChessPosition nextPosition : surroundingSquares) {
            if (nextPosition.getColumn() > 8 || nextPosition.getColumn() < 1 ||
            nextPosition.getRow() > 8 || nextPosition.getRow() < 1) {
                continue;
            }
            ChessPiece pieceAtNext = board.getPiece(nextPosition);
            if (pieceAtNext == null) {
                validMoves.add(new ChessMove(position, nextPosition, null));
            } else if (pieceAtNext.getTeamColor() != currentPiece.getTeamColor()) { // Capture
                validMoves.add(new ChessMove(position, nextPosition, null));
            } else{
                continue;
            }
        }

        return validMoves;

    }
    
}
