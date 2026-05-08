package chess.piecemoves;

import java.util.ArrayList;
import java.util.Collection;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

final class MoveGenerationUtils {

    private MoveGenerationUtils() {
    }

    static boolean isOnBoard(ChessPosition position) {
        return isOnBoard(position.getRow(), position.getColumn());
    }

    static boolean isOnBoard(int row, int col) {
       return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private static void addRayMoves(ChessBoard board, ChessPosition startPosition, ChessPiece currentPiece, 
        Collection<ChessMove> validMoves, int rowDelta, int colDelta) {

        int nextRow = startPosition.getRow() + rowDelta;
        int nextCol = startPosition.getColumn() + colDelta;

        while (isOnBoard(nextRow, nextCol)) {
            
            ChessPosition nextPosition = new ChessPosition(nextRow, nextCol);
            ChessPiece pieceAtNext = board.getPiece(nextPosition);

            if (pieceAtNext == null) {
                validMoves.add(new ChessMove(startPosition, nextPosition, null));
            } else {
                if (pieceAtNext.getTeamColor() != currentPiece.getTeamColor()) {
                    validMoves.add(new ChessMove(startPosition, nextPosition, null));
                }
                break;
            }

            nextRow += rowDelta;
            nextCol += colDelta;

        }
        
    }

    private static void addOffsetMove(ChessBoard board, ChessPosition startPosition, ChessPiece currentPiece,
        Collection<ChessMove> validMoves, int rowOffset, int colOffset) {
        
        ChessPosition nextPosition = new ChessPosition(startPosition.getRow() + rowOffset, startPosition.getColumn() + colOffset);

        if (!isOnBoard(nextPosition)) {
            return;
        }

        ChessPiece pieceAtNext = board.getPiece(nextPosition);
        if (pieceAtNext == null || pieceAtNext.getTeamColor() != currentPiece.getTeamColor()) {
            validMoves.add(new ChessMove(startPosition, nextPosition, null));
        }

    }

    static Collection<ChessMove> collectSlidingMoves(ChessBoard board, ChessPosition startPosition, int[][] directions) {

        ArrayList<ChessMove> validMoves = new ArrayList<>();
        ChessPiece currentPiece = board.getPiece(startPosition);

        if (currentPiece == null) {
            return validMoves;
        }

        for (int[] direction : directions) {
            addRayMoves(board, startPosition, currentPiece, validMoves, direction[0], direction[1]);
        }

        return validMoves;

    }

    static Collection<ChessMove> collectOffsetMoves(ChessBoard board, ChessPosition startPosition, int[][] offsets) {

        ArrayList<ChessMove> validMoves = new ArrayList<>();
        ChessPiece currentPiece = board.getPiece(startPosition);

        if (currentPiece == null) {
            return validMoves;
        }

        for (int[] offset : offsets) {
            addOffsetMove(board, startPosition, currentPiece, validMoves, offset[0], offset[1]);
        }

        return validMoves;

    }
    
}