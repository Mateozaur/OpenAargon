package io.github.phantamanta44.openar.game.piece.impl;

import io.github.phantamanta44.openar.game.map.GameField;
import io.github.phantamanta44.openar.game.map.IGameField;
import io.github.phantamanta44.openar.game.piece.WallTopology;
import io.github.phantamanta44.openar.render.visual.BorderVisualVariant;
import io.github.phantamanta44.openar.util.math.IntVector;

public class PieceBorder extends AbstractWallPiece {

    private static final int BOARD_WIDTH = 20;
    private static final int BOARD_HEIGHT = 13;

    @Override
    public String getName() {
        return "Border";
    }

    @Override
    public String getToken() {
        return "=";
    }

    @Override
    public String getTexturePath(IGameField field, IntVector coords, int rot, int meta) {
        return "texture/border.png";
    }

    @Override
    public IntVector getTextureOffset(IGameField field, IntVector coords, int rot, int meta) {
        BorderVisualVariant variant = getVisualVariant(field, coords, rot);
        return new IntVector(
                variant.getDefaultSpriteColumn() * 32,
                (meta == 6) ? 0 : 32);
    }

    /**
     * Resolves legacy map state into a semantic border role.
     *
     * Most CE border cells use the stored rotation as an oriented frame hint;
     * that numeric encoding is translated here and does not need to leak into
     * style/render code. Board topology wins for the two T-junction cases that
     * cannot be represented reliably by the raw map rotation alone.
     */
    public BorderVisualVariant getVisualVariant(
            IGameField field,
            IntVector coords,
            int legacyRotation) {
        BorderVisualVariant fallback =
                BorderVisualVariant.fromLegacyRotation(legacyRotation);

        /*
         * Automatic topology detection is only used for an actual board.
         * Other IGameField implementations, such as the wrapper used while
         * dragging a piece, retain the translated legacy orientation.
         */
        if (field instanceof GameField && coords != null) {
            WallTopology topology = getBorderTopology(field, coords);

            if (topology.hasWest()
                    && topology.hasEast()
                    && topology.hasSouth()
                    && !topology.hasNorth()) {
                return BorderVisualVariant.TEE_SOUTH;
            }

            if (topology.hasWest()
                    && topology.hasEast()
                    && topology.hasNorth()
                    && !topology.hasSouth()) {
                return BorderVisualVariant.TEE_NORTH;
            }
        }

        return fallback;
    }

    /**
     * Compatibility accessor for older callers that still expect the native
     * border.png column as an integer.
     */
    public int getRenderRotation(IGameField field, IntVector coords, int rot) {
        return getVisualVariant(field, coords, rot).getDefaultSpriteColumn();
    }

    /**
     * Derives board connectivity independently from the legacy rotation value.
     * Native border rendering currently connects only to other PieceBorder
     * cells. Future semantic style resolvers may broaden that rule without
     * changing the map format.
     */
    public WallTopology getBorderTopology(IGameField field, IntVector coords) {
        return WallTopology.of(
                isBorder(field, coords.getX(), coords.getY() - 1),
                isBorder(field, coords.getX() + 1, coords.getY()),
                isBorder(field, coords.getX(), coords.getY() + 1),
                isBorder(field, coords.getX() - 1, coords.getY()));
    }

    private boolean isBorder(IGameField field, int x, int y) {
        if (x < 0 || y < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT) {
            return false;
        }

        return field.getPiece(new IntVector(x, y)) instanceof PieceBorder;
    }

}
