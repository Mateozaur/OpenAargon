package io.github.phantamanta44.openar.render.visual;

/**
 * Canonical, renderer-facing border roles.
 *
 * The names describe the visual role of a frame segment rather than the
 * Collector's Edition numeric rotation that happened to encode it. This keeps
 * style code independent from PCE/black_tiles layout details.
 *
 * The default sprite column is intentionally only the mapping for OpenAargon's
 * normalized texture/border.png resource. Other styles are free to map the same
 * semantic variant to any resource or layout.
 */
public enum BorderVisualVariant {

    BOTTOM_EDGE(0),
    BOTTOM_LEFT_CORNER(1),
    LEFT_EDGE(2),
    TOP_LEFT_CORNER(3),
    TOP_EDGE(4),
    TOP_RIGHT_CORNER(5),
    RIGHT_EDGE(6),
    BOTTOM_RIGHT_CORNER(7),

    /** Horizontal border junction with a branch towards the board interior. */
    TEE_SOUTH(4),

    /** Horizontal border junction with a branch towards the board interior. */
    TEE_NORTH(5);

    private final int defaultSpriteColumn;

    BorderVisualVariant(int defaultSpriteColumn) {
        this.defaultSpriteColumn = defaultSpriteColumn;
    }

    public int getDefaultSpriteColumn() {
        return defaultSpriteColumn;
    }

    /**
     * Compatibility translation for legacy map rotation values.
     *
     * Keep this numeric mapping at the legacy boundary. Callers that already
     * know the semantic variant should not traffic in these rotation numbers.
     */
    public static BorderVisualVariant fromLegacyRotation(int rotation) {
        switch (Math.floorMod(rotation, 8)) {
            case 0:
                return BOTTOM_EDGE;
            case 1:
                return BOTTOM_LEFT_CORNER;
            case 2:
                return LEFT_EDGE;
            case 3:
                return TOP_LEFT_CORNER;
            case 4:
                return TOP_EDGE;
            case 5:
                return TOP_RIGHT_CORNER;
            case 6:
                return RIGHT_EDGE;
            case 7:
                return BOTTOM_RIGHT_CORNER;
            default:
                throw new IllegalStateException("Unreachable normalized border rotation");
        }
    }
}
