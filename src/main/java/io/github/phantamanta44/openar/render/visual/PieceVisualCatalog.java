package io.github.phantamanta44.openar.render.visual;

import io.github.phantamanta44.openar.game.beam.BeamColor;
import io.github.phantamanta44.openar.game.map.IGameField;
import io.github.phantamanta44.openar.game.piece.IGamePiece;
import io.github.phantamanta44.openar.game.piece.PieceInstance;
import io.github.phantamanta44.openar.game.piece.VisualStyleId;
import io.github.phantamanta44.openar.game.piece.impl.PieceBorder;
import io.github.phantamanta44.openar.game.piece.impl.PieceLegacyStub;
import io.github.phantamanta44.openar.game.piece.impl.PieceWall;
import io.github.phantamanta44.openar.render.legacy.LegacyRenderResolver;
import io.github.phantamanta44.openar.render.legacy.LegacyTileSet;
import io.github.phantamanta44.openar.util.math.IntVector;

/**
 * Canonical visual lookup entry point for placed pieces.
 *
 * Callers ask for a visual definition rather than depending on a particular
 * legacy atlas layout. Some piece families still use the CE compatibility
 * resolver while they are migrated. Borders are the first family resolved from
 * a semantic visual role instead of a PCE/black_tiles slot.
 */
public final class PieceVisualCatalog {

    private static final int TILE_SIZE = 32;
    private static final String DEFAULT_BORDER_TEXTURE = "texture/border.png";

    private PieceVisualCatalog() { }

    /**
     * Compatibility overload for callers without board context.
     */
    public static SpriteDefinition resolve(
            PieceInstance instance,
            VisualStyleId boardStyle) {
        return resolve(instance, boardStyle, null, null);
    }

    /**
     * Resolves a piece visual with optional board context.
     *
     * Board context lets semantic piece families such as borders derive their
     * visual role from topology instead of trusting a raw legacy rotation.
     */
    public static SpriteDefinition resolve(
            PieceInstance instance,
            VisualStyleId boardStyle,
            IGameField field,
            IntVector coords) {
        if (instance == null) return null;

        IGamePiece piece = instance.getImplementation();
        VisualStyleId style = instance.resolveVisualStyle(
                boardStyle != null ? boardStyle : VisualStyleId.BASE);

        if (piece instanceof PieceBorder) {
            return resolveBorder(
                    (PieceBorder)piece,
                    instance,
                    field,
                    coords,
                    style);
        }

        boolean useLegacy = piece instanceof PieceWall
                || piece instanceof PieceLegacyStub;

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

    private static SpriteDefinition resolveBorder(
            PieceBorder border,
            PieceInstance instance,
            IGameField field,
            IntVector coords,
            VisualStyleId style) {
        BorderVisualVariant variant = border.getVisualVariant(
                field,
                coords,
                instance.getRotation());

        /*
         * border.png is OpenAargon's normalized 8x2 border resource. Row zero
         * is the WHITE/X map variant; row one is the ordinary/default variant.
         *
         * The style argument is intentionally not used yet. This is the
         * canonical baseline that removes PieceBorder from black_tiles. Future
         * style-specific border art should override semantic variants here (or
         * through a data-driven registry), not reintroduce PCE tile numbers.
         */
        int row = instance.getMeta() == BeamColor.WHITE.ordinal() ? 0 : 1;
        IntVector offset = new IntVector(
                variant.getDefaultSpriteColumn() * TILE_SIZE,
                row * TILE_SIZE);

        return new SpriteDefinition(
                DEFAULT_BORDER_TEXTURE,
                offset,
                false);
    }

}
