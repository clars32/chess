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

    private final CastlingRights castlingRights = new CastlingRights();
    private final EnPassantState enPassantState = new EnPassantState();

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

        Collection<ChessMove> pieceMoves = new ArrayList<>(currentPiece.pieceMoves(gameBoard, startPosition));

        if (currentPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
            enPassantState.addMoves(pieceMoves, gameBoard, currentPiece, startPosition);
        }

        ArrayList<ChessMove> validMoves = new ArrayList<ChessMove>();

        for (ChessMove move : pieceMoves) { // Only add moves that do not put the king in danger

            ChessBoard nextMoveBoard = duplicateBoard(gameBoard);
            ChessGame nextMoveGame = new ChessGame();
            nextMoveGame.setBoard(nextMoveBoard);

            applyMoveToBoard(nextMoveBoard, move, currentPiece);

            if (!nextMoveGame.isInCheck(currentPiece.getTeamColor())) {
                validMoves.add(move);
            }

        }

        if (currentPiece.getPieceType() == ChessPiece.PieceType.KING) {
            addCastlingMoves(validMoves, currentPiece.getTeamColor(), startPosition);
        }

        return validMoves;

    }

    private void applyMoveToBoard(ChessBoard board, ChessMove move, ChessPiece movingPiece) {

        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        if (isCastlingMove(movingPiece, startPosition, endPosition)) {
            applyCastlingMove(board, startPosition, endPosition, movingPiece);
        } else if (enPassantState.isMove(board, movingPiece, move)) {
            enPassantState.apply(board, move, movingPiece);
        } else if (move.getPromotionPiece() == null) {
            board.addPiece(endPosition, board.getPiece(startPosition));
            board.addPiece(startPosition, null);
        } else {
            board.addPiece(endPosition, new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece()));
            board.addPiece(startPosition, null);
        }
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

        ChessPiece pieceToMove = gameBoard.getPiece(startPosition);
        Collection<ChessMove> legalMoves = validMoves(startPosition);

        if (pieceToMove == null || legalMoves == null || 
            !legalMoves.contains(move) || pieceToMove.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        boolean isEnPassant = enPassantState.isMove(gameBoard, pieceToMove, move);
        ChessPiece capturedPiece = isEnPassant
            ? enPassantState.capturedPiece(gameBoard)
            : gameBoard.getPiece(endPosition);

        applyMoveToBoard(gameBoard, move, pieceToMove);

        castlingRights.update(pieceToMove, startPosition, endPosition, capturedPiece);
        enPassantState.update(pieceToMove, startPosition, endPosition);

        TeamColor nextTurn = getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
        setTeamTurn(nextTurn);

    }

    private int homeRow(TeamColor teamColor) {
        return teamColor == TeamColor.WHITE ? 1 : 8;
    }

    private void addCastlingMoves(Collection<ChessMove> validMoves, TeamColor teamColor, ChessPosition kingPosition) {

        int homeRow = homeRow(teamColor);

        if (kingPosition.getRow() != homeRow || kingPosition.getColumn() != 5) {
            return;
        }

        if (canCastle(teamColor, false)) {
            validMoves.add(new ChessMove(kingPosition, new ChessPosition(homeRow, 3), null));
        }

        if (canCastle(teamColor, true)) {
            validMoves.add(new ChessMove(kingPosition, new ChessPosition(homeRow, 7), null));
        }

    }

    private boolean canCastle(TeamColor teamColor, boolean kingside) {

        int row = homeRow(teamColor);
        int rookColumn = kingside ? 8 : 1;

        if (!castlingRights.hasRight(teamColor, kingside)) {
            return false;
        }

        if (!hasPiece(new ChessPosition(row, 5), teamColor, ChessPiece.PieceType.KING) ||
            !hasPiece(new ChessPosition(row, rookColumn), teamColor, ChessPiece.PieceType.ROOK)) {
                return false;
        }

        int firstEmptyColumn = kingside ? 6 : 2;
        int lastEmptyColumn = kingside ? 7 : 4;
        for (int col = firstEmptyColumn; col <= lastEmptyColumn; col++) {
            if (gameBoard.getPiece(new ChessPosition(row, col)) != null) {
                return false;
            }
        }

        int direction = kingside ? 1 : -1;
        for (int col = 5; col != 5 + (3 * direction); col += direction) {
            if (isSquareUnderAttack(new ChessPosition(row, col), teamColor)) {
                return false;
            }
        }

        return true;

    }

    private boolean hasPiece(ChessPosition position, TeamColor teamColor, ChessPiece.PieceType pieceType) {
        
        ChessPiece piece = gameBoard.getPiece(position);
        return piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == pieceType;
    
    }

    private boolean isCastlingMove(ChessPiece pieceToMove, ChessPosition startPosition, ChessPosition endPosition) {
        
        return pieceToMove.getPieceType() == ChessPiece.PieceType.KING &&
            startPosition.getRow() == endPosition.getRow() &&
            startPosition.getColumn() == 5 &&
            Math.abs(endPosition.getColumn() - startPosition.getColumn()) == 2;

    }

    private void applyCastlingMove(ChessBoard board, ChessPosition startPosition, ChessPosition endPosition, ChessPiece king) {

        int row = startPosition.getRow();
        boolean kingside = endPosition.getColumn() > startPosition.getColumn();

        ChessPosition rookStart = new ChessPosition(row, kingside ? 8 : 1);
        ChessPosition rookEnd = new ChessPosition(row, kingside ? 6 : 4);
        ChessPiece rook = board.getPiece(rookStart);

        board.addPiece(endPosition, king);
        board.addPiece(startPosition, null);
        board.addPiece(rookEnd, rook);
        board.addPiece(rookStart, null);

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        
        ChessPosition kingPosition = findKingPosition(teamColor);
        return kingPosition != null && isSquareUnderAttack(kingPosition, teamColor);
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

        if (pieceToCheck.getPieceType() == ChessPiece.PieceType.PAWN) { // Make sure to check for pawn attacks!

            int direction = pieceToCheck.getTeamColor() == TeamColor.WHITE ? 1 : -1;
            return targetSquare.getRow() == startPosition.getRow() + direction &&
                Math.abs(targetSquare.getColumn() - startPosition.getColumn()) == 1;

        }

        Collection<ChessMove> possibleMoves = pieceToCheck.pieceMoves(gameBoard, startPosition);
        for (ChessMove move : possibleMoves) {
            if (move.getEndPosition().equals(targetSquare)) {
                return true;
            }
        }

        return false;

    }

    private Collection<ChessMove> getAllValidMoves(TeamColor teamColor) {

        ArrayList<ChessMove> allValidMoves = new ArrayList<>();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition positionToCheck = new ChessPosition(row, col);
                ChessPiece pieceToCheck = gameBoard.getPiece(positionToCheck);
                if (pieceToCheck != null && pieceToCheck.getTeamColor() == teamColor) {
                    allValidMoves.addAll(validMoves(positionToCheck));
                }
            }
        }

        return allValidMoves;

    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && getAllValidMoves(teamColor).size() == 0;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && getAllValidMoves(teamColor).size() == 0;
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
        enPassantState.clear();
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
        result = prime * result + castlingRights.hashCode();
        result = prime * result + enPassantState.hashCode();
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
        if (!castlingRights.equals(other.castlingRights)) {
            return false;
        }
        if (!enPassantState.equals(other.enPassantState)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ChessGame [teamTurn=" + teamTurn + ", gameBoard=" + gameBoard + ", castlingRights=" + castlingRights
                + ", enPassantState=" + enPassantState + "]";
    }

}