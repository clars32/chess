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

    private boolean whiteCanCastleKingside = true;
    private boolean whiteCanCastleQueenside = true;
    private boolean blackCanCastleKingside = true;
    private boolean blackCanCastleQueenside = true;

    private ChessPosition enPassantTargetPosition = null;
    private ChessPosition enPassantCapturedPawnPosition = null;

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
            addEnPassantMoves(pieceMoves, currentPiece, startPosition);
        }

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

        if (currentPiece.getPieceType() == ChessPiece.PieceType.KING) {
            addCastlingMoves(validMoves, currentPiece.getTeamColor(), startPosition);
        }

        return validMoves;

    }

    private void addEnPassantMoves(Collection<ChessMove> moves, ChessPiece pawn, ChessPosition startPosition) {

        if (enPassantTargetPosition == null || enPassantCapturedPawnPosition == null) {
            return;
        }

        ChessPiece capturedPawn = gameBoard.getPiece(enPassantCapturedPawnPosition);
        if (capturedPawn == null ||
            capturedPawn.getPieceType() != ChessPiece.PieceType.PAWN ||
            capturedPawn.getTeamColor() == pawn.getTeamColor()) {
                return;
        }

        int direction = pawn.getTeamColor() == TeamColor.WHITE ? 1 : -1;
        boolean isAdjacent = startPosition.getRow() == enPassantCapturedPawnPosition.getRow() &&
            Math.abs(startPosition.getColumn() - enPassantCapturedPawnPosition.getColumn()) == 1;
        boolean landsOnTarget = startPosition.getRow() + direction == enPassantTargetPosition.getRow() &&
            enPassantCapturedPawnPosition.getColumn() == enPassantTargetPosition.getColumn();

        if (isAdjacent && landsOnTarget && gameBoard.getPiece(enPassantTargetPosition) == null) {
            moves.add(new ChessMove(startPosition, enPassantTargetPosition, null));
        }

    }

    private void applyMoveToBoard(ChessBoard board, ChessMove move, ChessPiece movingPiece) {

        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        if (isCastlingMove(movingPiece, startPosition, endPosition)) {
            applyCastlingMove(board, startPosition, endPosition, movingPiece);
        } else if (isEnPassantMove(board, movingPiece, move)) {
            applyEnPassantMove(board, move, movingPiece);
        } else if (move.getPromotionPiece() == null) {
            board.addPiece(endPosition, board.getPiece(startPosition));
            board.addPiece(startPosition, null);
        } else {
            board.addPiece(endPosition, new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece()));
            board.addPiece(startPosition, null);
        }
    }

    private boolean isEnPassantMove(ChessBoard board, ChessPiece pieceToMove, ChessMove move) {
        
        return pieceToMove.getPieceType() == ChessPiece.PieceType.PAWN &&
            enPassantTargetPosition != null &&
            enPassantTargetPosition.equals(move.getEndPosition()) &&
            move.getStartPosition().getColumn() != move.getEndPosition().getColumn() &&
            board.getPiece(move.getEndPosition()) == null;

    }

    private void applyEnPassantMove(ChessBoard board, ChessMove move, ChessPiece pawn) {
        
        board.addPiece(move.getEndPosition(), pawn);
        board.addPiece(move.getStartPosition(), null);
        board.addPiece(enPassantCapturedPawnPosition, null);

    }

    private void updateEnPassantOpportunity(ChessPiece movedPiece, ChessPosition startPosition, ChessPosition endPosition) {

        enPassantTargetPosition = null;
        enPassantCapturedPawnPosition = null;

        if (movedPiece.getPieceType() == ChessPiece.PieceType.PAWN &&
            Math.abs(endPosition.getRow() - startPosition.getRow()) == 2) {
                enPassantTargetPosition = new ChessPosition(
                    (startPosition.getRow() + endPosition.getRow()) / 2, startPosition.getColumn());
                enPassantCapturedPawnPosition = endPosition;
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

        boolean isEnPassant = isEnPassantMove(gameBoard, pieceToMove, move);
        ChessPiece capturedPiece = isEnPassant
            ? gameBoard.getPiece(enPassantCapturedPawnPosition)
            : gameBoard.getPiece(endPosition);

        applyMoveToBoard(gameBoard, move, pieceToMove);

        updateCastlingRights(pieceToMove, startPosition, endPosition, capturedPiece);
        updateEnPassantOpportunity(pieceToMove, startPosition, endPosition);

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

        if (!hasCastlingRight(teamColor, kingside)) {
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

    private boolean hasCastlingRight(TeamColor teamColor, boolean kingside) {

        if (teamColor == TeamColor.WHITE) {
            return kingside ? whiteCanCastleKingside : whiteCanCastleQueenside;
        } else {
            return kingside ? blackCanCastleKingside : blackCanCastleQueenside;
        }

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

        gameBoard.addPiece(endPosition, king);
        gameBoard.addPiece(startPosition, null);
        gameBoard.addPiece(rookEnd, rook);
        gameBoard.addPiece(rookStart, null);

    }

    private void updateCastlingRights(ChessPiece movedPiece, ChessPosition startPosition, ChessPosition endPosition, ChessPiece capturedPiece) {

        if (movedPiece.getPieceType() == ChessPiece.PieceType.KING) {
            disableKingCastlingRight(movedPiece.getTeamColor());
        }

        if (movedPiece.getPieceType() == ChessPiece.PieceType.ROOK) {
            disableRookCastlingRight(movedPiece.getTeamColor(), startPosition);
        }

        if (capturedPiece != null && capturedPiece.getPieceType() == ChessPiece.PieceType.ROOK) {
            disableRookCastlingRight(capturedPiece.getTeamColor(), endPosition);
        }

    }

    private void disableKingCastlingRight(TeamColor teamColor) {

        if (teamColor == TeamColor.WHITE) {
            whiteCanCastleKingside = false;
            whiteCanCastleQueenside = false;
        } else {
            blackCanCastleKingside = false;
            blackCanCastleQueenside = false;
        }

    }

    private void disableRookCastlingRight(TeamColor teamColor, ChessPosition rookPosition) {

        if (rookPosition.getRow() != homeRow(teamColor)) {
            return;
        }

        if (teamColor == TeamColor.WHITE) {
            if (rookPosition.getColumn() == 1) {
                whiteCanCastleQueenside = false;
            } else if (rookPosition.getColumn() == 8) {
                whiteCanCastleKingside = false;
            }
        } else {
            if (rookPosition.getColumn() == 1) {
                blackCanCastleQueenside = false;
            } else if (rookPosition.getColumn() == 8) {
                blackCanCastleKingside = false;
            }
        }

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
        enPassantTargetPosition = null;
        enPassantCapturedPawnPosition = null;
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
        result = prime * result + (whiteCanCastleKingside ? 1231 : 1237);
        result = prime * result + (whiteCanCastleQueenside ? 1231 : 1237);
        result = prime * result + (blackCanCastleKingside ? 1231 : 1237);
        result = prime * result + (blackCanCastleQueenside ? 1231 : 1237);
        result = prime * result + ((enPassantTargetPosition == null) ? 0 : enPassantTargetPosition.hashCode());
        result = prime * result
                + ((enPassantCapturedPawnPosition == null) ? 0 : enPassantCapturedPawnPosition.hashCode());
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
        if (whiteCanCastleKingside != other.whiteCanCastleKingside) {
            return false;
        }
        if (whiteCanCastleQueenside != other.whiteCanCastleQueenside) {
            return false;
        }
        if (blackCanCastleKingside != other.blackCanCastleKingside) {
            return false;
        }
        if (blackCanCastleQueenside != other.blackCanCastleQueenside) {
            return false;
        }
        if (enPassantTargetPosition == null) {
            if (other.enPassantTargetPosition != null) {
                return false;
            }
        } else if (!enPassantTargetPosition.equals(other.enPassantTargetPosition)) {

        }
        if (enPassantCapturedPawnPosition == null) {
            if (other.enPassantCapturedPawnPosition != null) {
                return false;
            }
        } else if (!enPassantCapturedPawnPosition.equals(other.enPassantCapturedPawnPosition)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ChessGame [teamTurn=" + teamTurn + ", gameBoard=" + gameBoard + ", whiteCanCastleKingside="
                + whiteCanCastleKingside + ", whiteCanCastleQueenside=" + whiteCanCastleQueenside
                + ", blackCanCastleKingside=" + blackCanCastleKingside + ", blackCanCastleQueenside="
                + blackCanCastleQueenside + ", enPassantTargetPosition=" + enPassantTargetPosition
                + ", enPassantCapturedPawnPosition=" + enPassantCapturedPawnPosition + "]";
    }

}