package io.github.phantamanta44.openar.game.piece.impl;

import io.github.phantamanta44.openar.game.beam.Beam;
import io.github.phantamanta44.openar.game.map.IGameField;
import io.github.phantamanta44.openar.game.piece.IGamePiece;
import io.github.phantamanta44.openar.util.math.IntVector;

import java.util.Collection;
import java.util.Collections;

/**
 * Temporary logic stub for official v10 pieces that OpenAargon does not implement yet.
 * Rendering is supplied centrally by LegacyRenderResolver.
 */
public class PieceLegacyStub implements IGamePiece {
    private final String name;
    private final String token;

    public PieceLegacyStub(String name, String token) {
        this.name = name;
        this.token = token;
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getToken() { return token; }

    @Override
    public Collection<Beam> getReflections(IGameField field, IntVector coords, int rot, int meta, Beam in) {
        return Collections.emptyList();
    }

    @Override
    public String getTexturePath(IGameField field, IntVector coords, int rot, int meta) {
        return "texture/empty.png";
    }

    @Override
    public IntVector getTextureOffset(IGameField field, IntVector coords, int rot, int meta) {
        return IntVector.ZERO;
    }
}
