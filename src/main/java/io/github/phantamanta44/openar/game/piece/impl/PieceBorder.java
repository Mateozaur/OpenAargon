package io.github.phantamanta44.openar.game.piece.impl;

import io.github.phantamanta44.openar.game.beam.Beam;
import io.github.phantamanta44.openar.game.map.GameField;
import io.github.phantamanta44.openar.game.map.IGameField;
import io.github.phantamanta44.openar.game.piece.IGamePiece;
import io.github.phantamanta44.openar.util.math.IntVector;

import java.util.Collection;
import java.util.Collections;

public class PieceBorder implements IGamePiece {

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
    public Collection<Beam> getReflections(IGameField field, IntVector coords, int rot, int meta, Beam in) {
        return Collections.emptyList();
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
     * This is intentionally independent from the texture source: the base
     * border sheet and alternate legacy tilesets must use the same normalized
     * border topology.
     */
    public int getRenderRotation(IGameField field, IntVector coords, int rot) {
        int safeRot = Math.floorMod(rot, 8);

        /*
         * Automatic topology detection is only used for an actual board.
         * Other IGameField implementations, such as the wrapper used while
         * dragging a piece, fall back to the original map encoding.
         */
        if (field instanceof GameField) {
            boolean north = isBorder(field, coords.getX(), coords.getY() - 1);
            boolean east  = isBorder(field, coords.getX() + 1, coords.getY());
            boolean south = isBorder(field, coords.getX(), coords.getY() + 1);
            boolean west  = isBorder(field, coords.getX() - 1, coords.getY());

            /*
             * Border variant 4 = west + east + south
             * Border variant 5 = west + east + north
             */
            if (west && east && south && !north) {
                return 4;
            }

            if (west && east && north && !south) {
                return 5;
            }
        }

        return safeRot;
    }

    private boolean isBorder(IGameField field, int x, int y) {
        if (x < 0 || y < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT) {
            return false;
        }

        return field.getPiece(new IntVector(x, y)) instanceof PieceBorder;
    }

}
