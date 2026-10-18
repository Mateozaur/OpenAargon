package io.github.phantamanta44.openar.game.piece;

import io.github.phantamanta44.openar.Aargon;
import io.github.phantamanta44.openar.game.piece.impl.*;

/**
 * Backwards-compatible access point for game pieces.
 *
 * Legacy maps still resolve one-character tokens here, while new code can use
 * stable PieceId values through the same canonical PieceCatalog.
 */
public class PieceRegistry {

    private static final PieceCatalog CATALOG = new PieceCatalog();
    private static boolean initialized = false;

    public static final IGamePiece EMPTY = new PieceAir();

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        registerPiece("air", new PieceAir());
        registerPiece("border", new PieceBorder());
        registerPiece("coin", new PieceCoin());
        registerPiece("double_mirror", new PieceDoubleMirror());
        registerPiece("double_slit", new PieceDoubleSlit());
        registerPiece("filter", new PieceFilter());
        registerPiece("mine", new PieceMine());
        registerPiece("mirror", new PieceMirror());
        registerPiece("one_way", new PieceOneWay());
        registerPiece("polarizer", new PiecePolarizer());
        registerPiece("prism", new PiecePrism());
        registerPiece("refractor", new PieceRefractor());
        registerPiece("slime", new PieceSlime());
        registerPiece("source", new PieceSource());
        registerPiece("splitter", new PieceSplitter());
        registerPiece("tnt", new PieceTNT());
        registerPiece("three_way", new PieceThreeWay());
        registerPiece("wall", new PieceWall());

        registerPiece("light_bender", new PieceLegacyStub("Light Bender", "@"));
        registerPiece("selector", new PieceLegacyStub("Selector", "I"));
        registerPiece("convert", new PieceLegacyStub("Convert", "J"));
        registerPiece("complex", new PieceLegacyStub("Complex", "L"));
        registerPiece("prismirror", new PieceLegacyStub("Prismirror", "M"));
        registerPiece("fuel_cell", new PieceLegacyStub("Fuel Cell", "c"));
        registerPiece("black_hole", new PieceLegacyStub("Black Hole", "h"));
        registerPiece("spaceship", new PieceLegacyStub("Spaceship", "p"));
        registerPiece("water", new PieceLegacyStub("Water", "w"));
        registerPiece("splicer", new PieceLegacyStub("Splicer", "y"));

        initialized = true;
    }

    public static void registerPiece(String id, IGamePiece piece) {
        CATALOG.register(new PieceDefinition(PieceId.of(id), piece.getToken(), piece));
    }

    /**
     * Compatibility overload for callers that have not adopted canonical IDs.
     * New registrations should always provide an explicit stable id.
     */
    @Deprecated
    public static void registerPiece(IGamePiece piece) {
        String fallbackId = "legacy." + piece.getName()
                .trim()
                .toLowerCase()
                .replace(' ', '_');
        registerPiece(fallbackId, piece);
    }

    public static PieceCatalog getCatalog() {
        return CATALOG;
    }

    public static PieceDefinition getDefinition(PieceId id) {
        return CATALOG.get(id);
    }

    /**
     * Resolves a legacy one-character map token into the canonical catalog.
     * Unknown tokens retain the historical behaviour of becoming air.
     */
    public static PieceDefinition getDefinition(String legacyToken) {
        PieceDefinition definition = CATALOG.getByLegacyToken(legacyToken);
        if (definition == null) {
            Aargon.getLogger().warn("Unknown piece '{}'!", legacyToken);
            return CATALOG.get(PieceId.of("air"));
        }
        return definition;
    }

    public static IGamePiece getPiece(PieceId id) {
        PieceDefinition definition = CATALOG.get(id);
        if (definition == null) {
            Aargon.getLogger().warn("Unknown piece id '{}'!", id);
            return EMPTY;
        }
        return definition.getImplementation();
    }

    public static IGamePiece getPiece(String token) {
        return getDefinition(token).getImplementation();
    }

}
