package io.github.phantamanta44.openar.render.visual;

import io.github.phantamanta44.openar.game.piece.IGamePiece;
import io.github.phantamanta44.openar.game.piece.PieceInstance;
import io.github.phantamanta44.openar.game.piece.VisualStyleId;
import io.github.phantamanta44.openar.game.piece.impl.PieceBorder;
import io.github.phantamanta44.openar.game.piece.impl.PieceLegacyStub;
import io.github.phantamanta44.openar.game.piece.impl.PieceWall;
import io.github.phantamanta44.openar.render.legacy.LegacyRenderResolver;
import io.github.phantamanta44.openar.render.legacy.LegacyTileSet;

/**
 * Canonical visual lookup entry point for placed pieces.
 *
 * For now the catalog delegates legacy-backed pieces to the Collector's Edition
 * resolver. Keeping that dependency here is intentional: callers should ask for
 * a visual definition, not know how an old atlas encodes it. Native pieces fall
 * through by returning null and continue to use their existing renderer.
 */
public final class PieceVisualCatalog {

    private PieceVisualCatalog() { }

    public static SpriteDefinition resolve(
            PieceInstance instance,
            VisualStyleId boardStyle) {
        if (instance == null) return null;

        IGamePiece piece = instance.getImplementation();
        VisualStyleId style = instance.resolveVisualStyle(
                boardStyle != null ? boardStyle : VisualStyleId.BASE);

        boolean useLegacy = piece instanceof PieceWall
                || piece instanceof PieceLegacyStub
                || (piece instanceof PieceBorder && !VisualStyleId.BASE.equals(style));

        if (!useLegacy) return null;

        LegacyTileSet legacyTileSet = LegacyTileSet.fromId(style.getValue());
        LegacyRenderResolver.Sprite legacy = LegacyRenderResolver.resolve(
                piece.getToken(),
                instance.getRotation(),
                instance.getMeta(),
                legacyTileSet);

        if (legacy == null) return null;

        return new SpriteDefinition(
                legacy.getTexturePath(),
                legacy.getTextureOffset(),
                legacy.isAnimated());
    }

}
