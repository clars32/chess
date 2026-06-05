package dataaccess;

import chess.ChessGame;
import chess.ChessPiece;
import com.google.gson.*;

import java.lang.reflect.Type;

public class GameGson {
    
    private GameGson() {
    }

    static Gson create() {
        return new GsonBuilder()
            .registerTypeAdapter(ChessPiece.class, new ChessPieceAdapter())
            .create();
    }

    private static final class ChessPieceAdapter implements JsonSerializer<ChessPiece>, JsonDeserializer<ChessPiece> {

        @Override
        public JsonElement serialize(ChessPiece piece, Type type, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.add("pieceColor", context.serialize(piece.getTeamColor()));
            result.add("type", context.serialize(piece.getPieceType()));
            return result;
        }

        @Override
        public ChessPiece deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {

            JsonObject pieceObject = json.getAsJsonObject();
            ChessGame.TeamColor pieceColor = 
                context.deserialize(pieceObject.get("pieceColor"), ChessGame.TeamColor.class);
            ChessPiece.PieceType pieceType =
                context.deserialize(pieceObject.get("type"), ChessPiece.PieceType.class);
            
            if (pieceColor == null || pieceType == null) {
                throw new JsonParseException("ChessPiece JSON must include pieceColor and type");
            }

            return new ChessPiece(pieceColor, pieceType);
            
        }

    }

}
