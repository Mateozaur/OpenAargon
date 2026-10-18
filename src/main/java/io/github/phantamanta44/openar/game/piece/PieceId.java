package io.github.phantamanta44.openar.game.piece;

import java.util.Objects;

/**
 * Stable, renderer- and file-format-independent identifier of a logical game
 * piece. Legacy map tokens are aliases that are resolved by PieceCatalog and
 * must not become the canonical identity of a piece.
 */
public final class PieceId {

    private final String value;

    private PieceId(String value) {
        this.value = value;
    }

    public static PieceId of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Piece id cannot be null");
        }

        String normalized = value.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Piece id cannot be empty");
        }

        return new PieceId(normalized);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PieceId)) return false;
        PieceId other = (PieceId) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

}
