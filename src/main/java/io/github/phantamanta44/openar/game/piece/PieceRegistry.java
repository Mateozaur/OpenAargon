package io.github.phantamanta44.openar.game.piece;

import io.github.phantamanta44.openar.Aargon;
import io.github.phantamanta44.openar.game.piece.impl.*;

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
