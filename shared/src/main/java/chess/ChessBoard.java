package chess;

import java.util.Arrays;
import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        

        squares = new ChessPiece[8][8];

        // Set the pawns
        for (int i = 0; i <= 7; i++) {
            squares[1][i] = new ChessPiece(TeamColor.WHITE, PieceType.PAWN);
            squares[6][i] = new ChessPiece(TeamColor.BLACK, PieceType.PAWN);
        }

        // Set the rooks
        squares[0][0] = new ChessPiece(TeamColor.WHITE, PieceType.ROOK);
        squares[0][7] = new ChessPiece(TeamColor.WHITE, PieceType.ROOK);
        squares[7][0] = new ChessPiece(TeamColor.BLACK, PieceType.ROOK);
        squares[7][7] = new ChessPiece(TeamColor.BLACK, PieceType.ROOK);

        // Set the knights
        squares[0][1] = new ChessPiece(TeamColor.WHITE, PieceType.KNIGHT);
        squares[0][6] = new ChessPiece(TeamColor.WHITE, PieceType.KNIGHT);
        squares[7][1] = new ChessPiece(TeamColor.BLACK, PieceType.KNIGHT);
        squares[7][6] = new ChessPiece(TeamColor.BLACK, PieceType.KNIGHT);

        // Set the bishops
        squares[0][2] = new ChessPiece(TeamColor.WHITE, PieceType.BISHOP);
        squares[0][5] = new ChessPiece(TeamColor.WHITE, PieceType.BISHOP);
        squares[7][2] = new ChessPiece(TeamColor.BLACK, PieceType.BISHOP);
        squares[7][5] = new ChessPiece(TeamColor.BLACK, PieceType.BISHOP);

        // Set the queens
        squares[0][3] = new ChessPiece(TeamColor.WHITE, PieceType.QUEEN);
        squares[7][3] = new ChessPiece(TeamColor.BLACK, PieceType.QUEEN);

        // Set the kings
        squares[0][4] = new ChessPiece(TeamColor.WHITE, PieceType.KING);
        squares[7][4] = new ChessPiece(TeamColor.BLACK, PieceType.KING);

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(squares);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChessBoard other = (ChessBoard) obj;
        if (!Arrays.deepEquals(squares, other.squares))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChessBoard [squares=" + Arrays.toString(squares) + "]";
    }

}
