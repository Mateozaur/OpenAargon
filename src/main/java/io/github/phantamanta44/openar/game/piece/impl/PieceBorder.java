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
             * border.png:
             *
             * row 1, column 4 = west + east + south
             * row 1, column 5 = west + east + north
             */
            if (west && east && south && !north) {
                return new IntVector(4 * 32, 0);
            }

            if (west && east && north && !south) {
                return new IntVector(5 * 32, 0);
            }
        }

        /*
         * Original OpenAargon behaviour for every other border shape.
         */
        return new IntVector(rot * 32, (meta == 6) ? 0 : 32);
    }

    private boolean isBorder(IGameField field, int x, int y) {
        if (x < 0 || y < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT) {
            return false;
        }

        return field.getPiece(new IntVector(x, y)) instanceof PieceBorder;
    }

}
