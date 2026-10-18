package io.github.phantamanta44.openar.game.piece;

/**
 * Catalog entry joining the canonical identity of a piece with its current
 * runtime implementation and optional legacy token alias.
 *
 * Keeping this object small is deliberate: rendering/style data can be moved
 * here later without forcing the legacy map format into the engine model.
 */
public final class PieceDefinition {

    private final PieceId id;
    private final String legacyToken;
    private final IGamePiece implementation;

    public PieceDefinition(PieceId id, String legacyToken, IGamePiece implementation) {
        if (id == null) {
            throw new IllegalArgumentException("Piece id cannot be null");
        }
        if (implementation == null) {
            throw new IllegalArgumentException("Piece implementation cannot be null");
        }

        this.id = id;
        this.legacyToken = legacyToken;
        this.implementation = implementation;
    }

    public PieceId getId() {
        return id;
    }

    public String getLegacyToken() {
        return legacyToken;
    }

    public IGamePiece getImplementation() {
        return implementation;
    }

}
