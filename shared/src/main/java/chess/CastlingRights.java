package chess;

import chess.ChessGame.TeamColor;

class CastlingRights {
    
    private boolean whiteKingside = true;
    private boolean whiteQueenside = true;
    private boolean blackKingside = true;
    private boolean blackQueenside = true;

    boolean hasRight(TeamColor teamColor, boolean kingside) {

        if (teamColor == TeamColor.WHITE) {
            return kingside ? whiteKingside : whiteQueenside;
        } else {
            return kingside ? blackKingside : blackQueenside;
        }

    }

    void update(ChessPiece movedPiece, ChessPosition startPosition, ChessPosition endPosition, ChessPiece capturedPiece) {

        if (movedPiece.getPieceType() == ChessPiece.PieceType.KING) {
            disableKingRights(movedPiece.getTeamColor());
        }

        if (movedPiece.getPieceType() == ChessPiece.PieceType.ROOK) {
            disableRookRights(movedPiece.getTeamColor(), startPosition);
        }

        if (capturedPiece != null && capturedPiece.getPieceType() == ChessPiece.PieceType.ROOK) {
            disableRookRights(capturedPiece.getTeamColor(), endPosition);
        }

    }

    private void disableKingRights(TeamColor teamColor) {

        if (teamColor == TeamColor.WHITE) {
            whiteKingside = false;
            whiteQueenside = false;
        } else {
            blackKingside = false;
            blackQueenside = false;
        }

    }

    private void disableRookRights(TeamColor teamColor, ChessPosition rookPosition) {

        if (rookPosition.getRow() != homeRow(teamColor)) {
            return;
        }

        if (teamColor == TeamColor.WHITE) {
            if (rookPosition.getColumn() == 1) {
                whiteQueenside = false;
            } else if (rookPosition.getColumn() == 8) {
                whiteKingside = false;
            }
        } else {
            if (rookPosition.getColumn() == 1) {
                blackQueenside = false;
            } else if (rookPosition.getColumn() == 8) {
                blackKingside = false;
            }
        }

    }

    private int homeRow(TeamColor teamColor) {
        return teamColor == TeamColor.WHITE ? 1 : 8;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (whiteKingside ? 1231 : 1237);
        result = prime * result + (whiteQueenside ? 1231 : 1237);
        result = prime * result + (blackKingside ? 1231 : 1237);
        result = prime * result + (blackQueenside ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CastlingRights other)) {
            return false;
        }
        return whiteKingside == other.whiteKingside &&
            whiteQueenside == other.whiteQueenside &&
            blackKingside == other.blackKingside &&
            blackQueenside == other.blackQueenside;
    }

    @Override
    public String toString() {
        return "CastlingRights [whiteKingside=" + whiteKingside + ", whiteQueenside=" + whiteQueenside
                + ", blackKingside=" + blackKingside + ", blackQueenside=" + blackQueenside + "]";
    }

}
