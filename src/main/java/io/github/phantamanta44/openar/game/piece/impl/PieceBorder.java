package io.github.phantamanta44.openar.game.piece.impl;

import io.github.phantamanta44.openar.game.map.GameField;
import io.github.phantamanta44.openar.game.map.IGameField;
import io.github.phantamanta44.openar.game.piece.WallTopology;
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
        int renderRotation = getRenderRotation(field, coords, rot);
        return new IntVector(renderRotation * 32, (meta == 6) ? 0 : 32);
    }

    /**
     * Returns the visual border variant that should be rendered for this cell.
     * This remains specific to the native border sheet; legacy atlas variants
     * are resolved independently by the legacy renderer.
     */
    public int getRenderRotation(IGameField field, IntVector coords, int rot) {
        int safeRot = normalizeVariant(rot);

        /*
         * Automatic topology detection is only used for an actual board.
         * Other IGameField implementations, such as the wrapper used while
         * dragging a piece, fall back to the original map encoding.
         */
        if (field instanceof GameField) {
            WallTopology topology = getBorderTopology(field, coords);

            /*
             * Native border.png variant 4 = west + east + south.
             * Native border.png variant 5 = west + east + north.
             */
            if (topology.hasWest()
                    && topology.hasEast()
                    && topology.hasSouth()
                    && !topology.hasNorth()) {
                return 4;
            }

            if (topology.hasWest()
                    && topology.hasEast()
                    && topology.hasNorth()
                    && !topology.hasSouth()) {
                return 5;
            }
        }

        return safeRot;
    }

    /**
     * Derives board connectivity independently from the legacy rotation value.
     * For now native border rendering only connects to other PieceBorder cells,
     * preserving the existing behaviour exactly. Future style resolvers may
     * choose a broader wall-like connectivity rule without changing this data
     * model.
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
