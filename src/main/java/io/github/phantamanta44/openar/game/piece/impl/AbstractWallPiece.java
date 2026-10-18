package io.github.phantamanta44.openar.game.piece.impl;

import io.github.phantamanta44.openar.game.beam.Beam;
import io.github.phantamanta44.openar.game.map.IGameField;
import io.github.phantamanta44.openar.game.piece.IGamePiece;
import io.github.phantamanta44.openar.util.math.IntVector;

import java.util.Collection;
import java.util.Collections;

/**
 * Common behaviour shared by solid wall-like pieces.
 *
 * This class intentionally contains only mechanics that are currently truly
 * shared by walls and borders. In particular, rendering and beam penetration
 * depth remain the responsibility of the concrete piece types.
 */
public abstract class AbstractWallPiece implements IGamePiece {

    @Override
    public Collection<Beam> getReflections(
            IGameField field,
            IntVector coords,
            int rot,
            int meta,
            Beam in) {
        return Collections.emptyList();
    }

    protected int normalizeVariant(int rot) {
        return Math.floorMod(rot, 8);
    }

}
