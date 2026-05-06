package chess.piecemoves;

import java.util.ArrayList;
import java.util.Collection;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

public class RookMovesCalculator implements PieceMovesCalculator {
    
     public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {

        ArrayList<ChessMove> validMoves = new ArrayList<ChessMove>();

        ChessPiece currentPiece = board.getPiece(position);

        // Check moves up
        int newRow = position.getRow() + 1;
        int newCol = position.getColumn();

        while (newRow <= 8) {

            ChessPosition nextPosition = new ChessPosition(newRow, newCol);
            ChessPiece pieceAtNext = board.getPiece(nextPosition);
            if (pieceAtNext == null) {
                validMoves.add(new ChessMove(position, nextPosition, null));
            } else if (pieceAtNext.getTeamColor() != currentPiece.getTeamColor()) { // Capture
                validMoves.add(new ChessMove(position, nextPosition, null));
                break;
            } else {
                break;
            }

            newRow++;

        }

        // Check moves down
        newRow = position.getRow() - 1;
        newCol = position.getColumn();

        while (newRow >= 1) {
            
            ChessPosition nextPosition = new ChessPosition(newRow, newCol);
            ChessPiece pieceAtNext = board.getPiece(nextPosition);
            if (pieceAtNext == null) {
                validMoves.add(new ChessMove(position, nextPosition, null));
            } else if (pieceAtNext.getTeamColor() != currentPiece.getTeamColor()) { // Capture
                validMoves.add(new ChessMove(position, nextPosition, null));
                break;
            } else {
                break;
            }

            newRow--;

        }

        // Check moves to the left
        newRow = position.getRow();
        newCol = position.getColumn() - 1;

        while (newCol >= 1) {
            
            ChessPosition nextPosition = new ChessPosition(newRow, newCol);
            ChessPiece pieceAtNext = board.getPiece(nextPosition);
            if (pieceAtNext == null) {
                validMoves.add(new ChessMove(position, nextPosition, null));
            } else if (pieceAtNext.getTeamColor() != currentPiece.getTeamColor()) { // Capture
                validMoves.add(new ChessMove(position, nextPosition, null));
                break;
            } else {
                break;
            }

            newCol--;

        }

        // Check moves to the right
        newRow = position.getRow();
        newCol = position.getColumn() + 1;

        while (newCol <= 8) {
            
            ChessPosition nextPosition = new ChessPosition(newRow, newCol);
            ChessPiece pieceAtNext = board.getPiece(nextPosition);
            if (pieceAtNext == null) {
                validMoves.add(new ChessMove(position, nextPosition, null));
            } else if (pieceAtNext.getTeamColor() != currentPiece.getTeamColor()) { // Capture
                validMoves.add(new ChessMove(position, nextPosition, null));
                break;
            } else {
                break;
            }

            newCol++;

        }

        return validMoves;

     }

}
