package io.github.phantamanta44.openar.game.piece;

import java.util.Locale;

/**
 * Stable identifier for a visual style/tileset.
 *
 * The identifier is deliberately independent from atlas layout and from the
 * legacy Collector's Edition tileset enum. A placed piece may optionally carry
 * a style override; when absent, the board-level style is inherited.
 */
public final class VisualStyleId {

    public static final VisualStyleId BASE = new VisualStyleId("base");

    private final String value;

    private VisualStyleId(String value) {
        this.value = value;
    }

    public static VisualStyleId of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Visual style id cannot be null");
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Visual style id cannot be empty");
        }

        if ("base".equals(normalized)) {
            return BASE;
        }
        return new VisualStyleId(normalized);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof VisualStyleId)) return false;
        VisualStyleId that = (VisualStyleId)other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

}
