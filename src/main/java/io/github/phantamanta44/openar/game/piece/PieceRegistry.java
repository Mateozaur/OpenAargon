package io.github.phantamanta44.openar.game.piece;

import io.github.phantamanta44.openar.Aargon;
import io.github.phantamanta44.openar.game.piece.impl.*;
import io.github.phantamanta44.openar.game.piece.impl.PieceLegacyStub;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PieceRegistry {

	private static final Map<String, IGamePiece> registry = new ConcurrentHashMap<>();
	public static final IGamePiece EMPTY = new PieceAir();

	public static void init() {
		registerPiece(new PieceAir());
		registerPiece(new PieceBorder());
		registerPiece(new PieceCoin());
		registerPiece(new PieceDoubleMirror());
		registerPiece(new PieceDoubleSlit());
		registerPiece(new PieceFilter());
		registerPiece(new PieceMine());
		registerPiece(new PieceMirror());
		registerPiece(new PieceOneWay());
		registerPiece(new PiecePolarizer());
		registerPiece(new PiecePrism());
		registerPiece(new PieceRefractor());
		registerPiece(new PieceSlime());
		registerPiece(new PieceSource());
		registerPiece(new PieceSplitter());
		registerPiece(new PieceTNT());
		registerPiece(new PieceThreeWay());
		registerPiece(new PieceWall());
        registerPiece(new PieceLegacyStub("Light Bender", "@"));
        registerPiece(new PieceLegacyStub("Selector", "I"));
        registerPiece(new PieceLegacyStub("Convert", "J"));
        registerPiece(new PieceLegacyStub("Complex", "L"));
        registerPiece(new PieceLegacyStub("Prismirror", "M"));
        registerPiece(new PieceLegacyStub("Fuel Cell", "c"));
        registerPiece(new PieceLegacyStub("Black Hole", "h"));
        registerPiece(new PieceLegacyStub("Spaceship", "p"));
        registerPiece(new PieceLegacyStub("Water", "w"));
        registerPiece(new PieceLegacyStub("Splicer", "y"));
	}

	public static void registerPiece(IGamePiece piece) {
		registry.put(piece.getToken(), piece);
	}

	public static IGamePiece getPiece(String token) {
		IGamePiece piece = registry.get(token);
		if (piece == null) {
			Aargon.getLogger().warn("Unknown piece '{}'!", token);
			return EMPTY;
		}
		return piece;
	}

}
