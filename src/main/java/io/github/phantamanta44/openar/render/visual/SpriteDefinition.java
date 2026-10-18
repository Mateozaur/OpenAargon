package io.github.phantamanta44.openar.render.visual;

import io.github.phantamanta44.openar.util.math.IntVector;

/**
 * Renderer-neutral description of one sprite inside a texture.
 *
 * This deliberately contains no legacy token, tileset or piece-class logic.
 * Those concerns belong to visual catalogs/adapters that select a sprite.
 */
public final class SpriteDefinition {

    private final String texturePath;
    private final IntVector textureOffset;
    private final boolean animated;

    public SpriteDefinition(String texturePath, IntVector textureOffset, boolean animated) {
        if (texturePath == null || texturePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Texture path cannot be empty");
        }
        if (textureOffset == null) {
            throw new IllegalArgumentException("Texture offset cannot be null");
        }

        this.texturePath = texturePath;
        this.textureOffset = textureOffset;
        this.animated = animated;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public IntVector getTextureOffset() {
        return textureOffset;
    }

    public boolean isAnimated() {
        return animated;
    }

}
