package io.github.phantamanta44.openar.game.piece.impl;

import io.github.phantamanta44.openar.game.map.IGameField;
import io.github.phantamanta44.openar.util.math.IntVector;

public class PieceWall extends AbstractWallPiece {

    /*
     * OpenAargon's BeamColor ordinal order:
     *
     * 0 RED
     * 1 GREEN
     * 2 BLUE
     * 3 CYAN
     * 4 MAGENTA
     * 5 YELLOW
     * 6 WHITE
     * 7 BLACK
     *
     * Aargon .PCE color order:
     *
     * 0 BLACK
     * 1 BLUE
     * 2 GREEN
     * 3 CYAN
     * 4 RED
     * 5 MAGENTA
     * 6 YELLOW
     * 7 WHITE
     */
    private static final int[] PCE_COLOR_BY_META = {
            4, // RED
            2, // GREEN
            1, // BLUE
            3, // CYAN
            5, // MAGENTA
            6, // YELLOW
            7, // WHITE
            0  // BLACK
    };

    @Override
    public String getName() {
        return "Wall";
    }

    @Override
    public String getToken() {
        return "#";
    }

    @Override
    public String getTexturePath(
            IGameField field,
            IntVector coords,
            int rot,
            int meta) {
        return "texture/wall_pce.png";
    }

    @Override
    public IntVector getTextureOffset(
            IGameField field,
            IntVector coords,
            int rot,
            int meta) {

        int safeRot = normalizeVariant(rot);

        int safeMeta = meta;
        if (safeMeta < 0 || safeMeta >= PCE_COLOR_BY_META.length) {
            safeMeta = 7; // BLACK fallback
        }

        int pceColor = PCE_COLOR_BY_META[safeMeta];

        /*
         * wall_pce.png is stored top-to-bottom in PCE color order.
         * TextureManager uses PNGDecoder.decodeFlipped(), therefore
         * vertical texture coordinates are reversed.
         */
        int x = safeRot * 32;
        int y = (7 - pceColor) * 32;

        return new IntVector(x, y);
    }

}
