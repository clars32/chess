package chess.piecemoves;

import java.util.ArrayList;
import java.util.Collection;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;

public class BishopMovesCalculator implements PieceMovesCalculator {
    
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {

        ArrayList<ChessMove> validMoves = new ArrayList<ChessMove>();

        ChessPiece currentPiece = board.getPiece(position);

        // Check moves up and to the right
        int newRow = position.getRow() + 1;
        int newCol = position.getColumn() + 1;

        while (newRow <= 8 && newCol <= 8) {

            ChessPosition nextPosition = new ChessPosition(newRow, newCol);
            ChessPiece pieceAtNext = board.getPiece(nextPosition);

            if (pieceAtNext == null) {
                validMoves.add(new ChessMove(position, nextPosition, null));
            } else if (pieceAtNext.getTeamColor() != currentPiece.getTeamColor()) { // Capture
                validMoves.add(new ChessMove(position, nextPosition, null));
                break;
            }
            else {
                break;
            }

            newRow++;
            newCol++;

        }

        // Check moves up and to the left
        newRow = position.getRow() + 1;
        newCol = position.getColumn() - 1;

        while (newRow <= 8 && newCol >= 1) {

            ChessPosition nextPosition = new ChessPosition(newRow, newCol);
            ChessPiece pieceAtNext = board.getPiece(nextPosition);
            
            if (pieceAtNext == null) {
                validMoves.add(new ChessMove(position, nextPosition, null));
            } else if (pieceAtNext.getTeamColor() != currentPiece.getTeamColor()) { // Capture
                validMoves.add(new ChessMove(position, nextPosition, null));
                break;
            }
            else {
                break;
            }

            newRow++;
            newCol--;

        }

        // Check moves down and to the left
        newRow = position.getRow() - 1;
        newCol = position.getColumn() - 1;

        while (newRow >= 1 && newCol >= 1) {

            ChessPosition nextPosition = new ChessPosition(newRow, newCol);
            ChessPiece pieceAtNext = board.getPiece(nextPosition);
            
            if (pieceAtNext == null) {
                validMoves.add(new ChessMove(position, nextPosition, null));
            } else if (pieceAtNext.getTeamColor() != currentPiece.getTeamColor()) { // Capture
                validMoves.add(new ChessMove(position, nextPosition, null));
                break;
            }
            else {
                break;
            }

            newRow--;
            newCol--;

        }

        // Check moves down and to the right
        newRow = position.getRow() - 1;
        newCol = position.getColumn() + 1;

        while (newRow >= 1 && newCol <= 8) {

            ChessPosition nextPosition = new ChessPosition(newRow, newCol);
            ChessPiece pieceAtNext = board.getPiece(nextPosition);
            
            if (pieceAtNext == null) {
                validMoves.add(new ChessMove(position, nextPosition, null));
            } else if (pieceAtNext.getTeamColor() != currentPiece.getTeamColor()) { // Capture
                validMoves.add(new ChessMove(position, nextPosition, null));
                break;
            }
            else {
                break;
            }

            newRow--;
            newCol++;

        }
        
        return validMoves;

    }

}