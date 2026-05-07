package chess;

import java.util.Collection;
import java.util.Objects;

import chess.ChessGame.TeamColor;

class EnPassantState {
    
    private ChessPosition targetPosition = null;
    private ChessPosition capturedPawnPosition = null;

    void addMoves(Collection<ChessMove> moves, ChessBoard board, ChessPiece pawn, ChessPosition startPosition) {
        
        if (targetPosition == null || capturedPawnPosition == null) {
            return;
        }

        ChessPiece capturedPawn = board.getPiece(capturedPawnPosition);
        if (capturedPawn == null ||
            capturedPawn.getPieceType() != ChessPiece.PieceType.PAWN ||
            capturedPawn.getTeamColor() == pawn.getTeamColor()) {
                return;
        }

        int direction = pawn.getTeamColor() == TeamColor.WHITE ? 1 : -1;
        boolean isAdjacent = startPosition.getRow() == capturedPawnPosition.getRow() &&
            Math.abs(startPosition.getColumn() - capturedPawnPosition.getColumn()) == 1;
        boolean landsOnTarget = startPosition.getRow() + direction == targetPosition.getRow() &&
            capturedPawnPosition.getColumn() == targetPosition.getColumn();

        if (isAdjacent && landsOnTarget && board.getPiece(targetPosition) == null) {
            moves.add(new ChessMove(startPosition, targetPosition, null));
        }

    }

    boolean isMove(ChessBoard board, ChessPiece pieceToMove, ChessMove move) {
        
        return pieceToMove.getPieceType() == ChessPiece.PieceType.PAWN &&
            targetPosition != null &&
            targetPosition.equals(move.getEndPosition()) &&
            move.getStartPosition().getColumn() != move.getEndPosition().getColumn() &&
            board.getPiece(move.getEndPosition()) == null;

    }

    ChessPiece capturedPiece(ChessBoard board) {
        return capturedPawnPosition == null ? null : board.getPiece(capturedPawnPosition);
    }

    void apply(ChessBoard board, ChessMove move, ChessPiece pawn) {

        board.addPiece(move.getEndPosition(), pawn);
        board.addPiece(move.getStartPosition(), null);
        board.addPiece(capturedPawnPosition, null);

    }

    void update(ChessPiece movedPiece, ChessPosition startPosition, ChessPosition endPosition) {

        clear();

        if (movedPiece.getPieceType() == ChessPiece.PieceType.PAWN &&
            Math.abs(endPosition.getRow() - startPosition.getRow()) == 2) {
                targetPosition = new ChessPosition(
                    (startPosition.getRow() + endPosition.getRow()) / 2, startPosition.getColumn());
                capturedPawnPosition = endPosition;
        }

    }

    void clear() {

        targetPosition = null;
        capturedPawnPosition = null;

    }

    @Override
    public int hashCode() {
        return Objects.hash(targetPosition, capturedPawnPosition);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EnPassantState other)) {
            return false;
        }
        return Objects.equals(targetPosition, other.targetPosition) &&
            Objects.equals(capturedPawnPosition, other.capturedPawnPosition);
    }

    @Override
    public String toString() {
        return "EnPassantState [targetPosition=" + targetPosition + 
            ", capturedPawnPosition=" + capturedPawnPosition + "]";
    }
    
}
