package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn = TeamColor.WHITE;

    private ChessBoard gameBoard = new ChessBoard();

    public ChessGame() {
        gameBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    private ChessBoard duplicateBoard(ChessBoard currentBoard) {
        ChessBoard newBoard = new ChessBoard();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece pieceAtPosition = currentBoard.getPiece(new ChessPosition(i, j));
                if (pieceAtPosition != null) {
                    newBoard.addPiece(new ChessPosition(i, j), new ChessPiece(pieceAtPosition.getTeamColor(), pieceAtPosition.getPieceType()));
                }
            }
        }
        return newBoard;
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        
        ChessPiece currentPiece = gameBoard.getPiece(startPosition);

        if (currentPiece == null) {
            return null;
        }

        Collection<ChessMove> pieceMoves = currentPiece.pieceMoves(gameBoard, startPosition);

        ArrayList<ChessMove> validMoves = new ArrayList<ChessMove>();

        for (ChessMove move : pieceMoves) { // Only add moves that do not put the king in danger

            ChessBoard nextMoveBoard = duplicateBoard(gameBoard);
            ChessGame nextMoveGame = new ChessGame();
            nextMoveGame.setBoard(nextMoveBoard);

            // Make the move on the board, but skip validation to avoid circular dependency
            if (move.getPromotionPiece() == null) {
                nextMoveBoard.addPiece(move.getEndPosition(), nextMoveBoard.getPiece(startPosition));
            } else {
                nextMoveBoard.addPiece(move.getEndPosition(), new ChessPiece(currentPiece.getTeamColor(), move.getPromotionPiece()));
            }
            nextMoveBoard.addPiece(startPosition, null);

            if (!nextMoveGame.isInCheck(currentPiece.getTeamColor())) {
                validMoves.add(move);
            }
            
        }

        return validMoves;

    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();

        ChessPiece pieceToMove = gameBoard.getPiece(startPosition);

        if ((validMoves(startPosition) == null) || // There are no valid moves
        (!validMoves(startPosition).contains(move)) || // Requested move is invalid
        (pieceToMove.getTeamColor() != teamTurn)) { // Not your turn
            throw new InvalidMoveException();
        }

        if (promotionPiece == null) {
            gameBoard.addPiece(endPosition, pieceToMove);
        } else {
            gameBoard.addPiece(endPosition, new ChessPiece(teamTurn, promotionPiece));
        }
        gameBoard.addPiece(startPosition, null);

        TeamColor nextTurn = getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
        setTeamTurn(nextTurn);

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        
        ChessPosition kingPosition = findKingPosition(teamColor);
        return kingPosition != null & isSquareUnderAttack(kingPosition, teamColor);
    }

    private ChessPosition findKingPosition(TeamColor teamColor) {
        
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition positionToCheck = new ChessPosition(row, col);
                ChessPiece pieceToCheck = gameBoard.getPiece(positionToCheck);
                if (pieceToCheck != null
                    && pieceToCheck.getPieceType() == ChessPiece.PieceType.KING
                    && pieceToCheck.getTeamColor() == teamColor) {
                        return positionToCheck;
                }
            }
        }

        return null;

    }

    private boolean isSquareUnderAttack(ChessPosition targetSquare, TeamColor defendingTeam) {

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition positionToCheck = new ChessPosition(row, col);
                ChessPiece pieceToCheck = gameBoard.getPiece(positionToCheck);
                if (pieceToCheck != null
                    && pieceToCheck.getTeamColor() != defendingTeam
                    && pieceAttacksSquare(positionToCheck, targetSquare)) {
                        return true;
                }
            }
        }

        return false;

    }

    private boolean pieceAttacksSquare(ChessPosition startPosition, ChessPosition targetSquare) {

        ChessPiece pieceToCheck = gameBoard.getPiece(startPosition);

        if (pieceToCheck == null) {
            return false;
        }

        Collection<ChessMove> possibleMoves = pieceToCheck.pieceMoves(gameBoard, startPosition);
        for (ChessMove move : possibleMoves) {
            if (move.getEndPosition().equals(targetSquare)) {
                return true;
            }
        }

        return false;

    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((teamTurn == null) ? 0 : teamTurn.hashCode());
        result = prime * result + ((gameBoard == null) ? 0 : gameBoard.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChessGame other = (ChessGame) obj;
        if (teamTurn != other.teamTurn) {
            return false;
        }
        if (gameBoard == null) {
            if (other.gameBoard != null) {
                return false;
            }
        } else if (!gameBoard.equals(other.gameBoard)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ChessGame [teamTurn=" + teamTurn + ", gameBoard=" + gameBoard + "]";
    }

}
