package dataaccess;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameGsonTests {
    
    @Test
    void serializedGamesDoNotIncludeMoveCalculators() {

        ChessGame game = new ChessGame();

        String json = GameGson.create().toJson(game);

        assertFalse(json.contains("movesCalculator"));

    }

    @Test
    void deserializesLegacyGameJsonThatIncludesMoveCalculatorData() {

        ChessGame game = new ChessGame();
        String cleanJson = GameGson.create().toJson(game);
        String legacyJson = cleanJson.replaceFirst(
            "\"type\":\"ROOK\"",
            "\"type\":\"ROOK\",\"movesCalculator\":{\"legacy\":true}"
        );

        ChessGame restoredGame = GameGson.create().fromJson(legacyJson, ChessGame.class);
        ChessPosition rookPosition = new ChessPosition(1, 1);
        ChessPiece rook = restoredGame.getBoard().getPiece(rookPosition);

        assertNotNull(rook);
        assertEquals(ChessGame.TeamColor.WHITE, rook.getTeamColor());
        assertEquals(ChessPiece.PieceType.ROOK, rook.getPieceType());
        assertDoesNotThrow(() -> rook.pieceMoves(restoredGame.getBoard(), rookPosition));

    }
}
