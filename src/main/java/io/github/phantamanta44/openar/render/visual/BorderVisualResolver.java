package io.github.phantamanta44.openar.render.visual;

import io.github.phantamanta44.openar.game.piece.VisualStyleId;
import io.github.phantamanta44.openar.util.math.IntVector;

/**
 * Resolves semantic border roles against style-specific visual resources.
 *
 * The public contract is BorderVisualVariant -> SpriteDefinition. Atlas tile
 * numbers used by a concrete style are private resource-layout details here;
 * callers never need to know PCE picture indices or legacy rotations.
 */
public final class BorderVisualResolver {

    private static final int TILE_SIZE = 32;

    private static final VisualStyleId LP1 = VisualStyleId.of("lp1");
    private static final String LP1_TEXTURE = "texture/legacy/lp1.png";
    private static final int LP1_COLUMNS = 20;
    private static final int LP1_ROWS = 15;

    private BorderVisualResolver() { }

    /**
     * Returns a style-specific border sprite, or null when the style should use
     * the canonical normalized border resource.
     */
    public static SpriteDefinition resolve(
            VisualStyleId style,
            BorderVisualVariant variant) {
        if (style == null || variant == null) return null;

        if (LP1.equals(style)) {
            return resolveLp1(variant);
        }

        return null;
    }

    /**
     * LP1's ordinary black_tiles sheet contains an orientation-complete frame
     * family clustered in the same resource region. These values are treated as
     * layout coordinates of the LP1 style resource, not as runtime PCE lookup
     * semantics.
     *
     * The T-junction aliases deliberately mirror OpenAargon's historical native
     * border normalization: south/north junctions use the semantic equivalents
     * of variants 4/5. This is important for SELECTOR's two X/WHITE separator
     * cells, where topology overrides the stored legacy rotation.
     */
    private static SpriteDefinition resolveLp1(BorderVisualVariant variant) {
        final int tile;
        switch (variant) {
            case BOTTOM_EDGE:
                tile = 79;
                break;
            case BOTTOM_LEFT_CORNER:
                tile = 59;
                break;
            case LEFT_EDGE:
                tile = 97;
                break;
            case TOP_LEFT_CORNER:
                tile = 117;
                break;
            case TOP_EDGE:
                tile = 118;
                break;
            case TOP_RIGHT_CORNER:
                tile = 58;
                break;
            case RIGHT_EDGE:
                tile = 98;
                break;
            case BOTTOM_RIGHT_CORNER:
                tile = 78;
                break;
            case TEE_SOUTH:
                tile = 118;
                break;
            case TEE_NORTH:
                tile = 58;
                break;
            default:
                return null;
        }

        int col = tile % LP1_COLUMNS;
        int row = tile / LP1_COLUMNS;
        if (row < 0 || row >= LP1_ROWS) return null;

        // TextureManager decodes PNGs flipped vertically, matching legacy atlas
        // addressing used elsewhere in the renderer.
        IntVector offset = new IntVector(
                col * TILE_SIZE,
                (LP1_ROWS - 1 - row) * TILE_SIZE);
        return new SpriteDefinition(LP1_TEXTURE, offset, false);
    }

}
