package chess.piecemoves;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

public class PawnMovesCalculator implements PieceMovesCalculator {
    
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {

        ArrayList<ChessMove> validMoves = new ArrayList<ChessMove>();

        ChessPiece currentPiece = board.getPiece(position);
        TeamColor pawnColor = currentPiece.getTeamColor();

        int currentRow = position.getRow();
        int currentCol = position.getColumn();

        int moveDirection = pawnColor == TeamColor.WHITE ? 1 : -1;

        List<ChessPiece.PieceType> possiblePromotions = List.of(PieceType.BISHOP, PieceType.ROOK, PieceType.QUEEN, PieceType.KNIGHT);

        // Can the pawn move forward one?

        int newRow = currentRow + (moveDirection * 1);
        ChessPosition nextPosition = new ChessPosition(newRow, currentCol);
        ChessPiece pieceAtNext = board.getPiece(nextPosition);
        if (pieceAtNext == null) {
            if ((pawnColor == TeamColor.WHITE && newRow == 8) || (pawnColor == TeamColor.BLACK && newRow == 1)) { // Promotion
                for (PieceType promotionPiece : possiblePromotions) {
                    validMoves.add(new ChessMove(position, nextPosition, promotionPiece));
                }
            } else {
                validMoves.add(new ChessMove(position, nextPosition, null));
            }
        }

        // Can the pawn move forward two?

        if ((pawnColor == TeamColor.WHITE && currentRow == 2) || (pawnColor == TeamColor.BLACK && currentRow == 7)) {

            ChessPosition interveningPosition = new ChessPosition(currentRow + (moveDirection * 1), currentCol);
            ChessPiece pieceInBetween = board.getPiece(interveningPosition);

            nextPosition = new ChessPosition(currentRow + (moveDirection * 2), currentCol);
            pieceAtNext = board.getPiece(nextPosition);

            if (pieceInBetween == null && pieceAtNext == null) {
                validMoves.add(new ChessMove(position, nextPosition, null));
            }

        }

        // Can the pawn capture?

        ArrayList<ChessPosition> captureSquares = new ArrayList<>();
        newRow = currentRow + (1 * moveDirection);
        captureSquares.add(new ChessPosition(newRow, currentCol + 1));
        captureSquares.add(new ChessPosition(newRow, currentCol - 1));

        for (ChessPosition capturePosition : captureSquares) {
            if (capturePosition.getColumn() > 8 || capturePosition.getColumn() < 1 ||
            capturePosition.getRow() > 8 || capturePosition.getRow() < 1) {
                continue;
            }
            pieceAtNext = board.getPiece(capturePosition);
            if (pieceAtNext != null && pieceAtNext.getTeamColor() != currentPiece.getTeamColor()) { // Capture
                if ((pawnColor == TeamColor.WHITE && newRow == 8) || (pawnColor == TeamColor.BLACK && newRow == 1)) { // Promotion
                    for (PieceType promotionPiece : possiblePromotions) {
                        validMoves.add(new ChessMove(position, capturePosition, promotionPiece));
                    }
                } else {
                    validMoves.add(new ChessMove(position, capturePosition, null));
                }
            } else {
                continue;
            }
        }
        
        return validMoves;

    }

}
