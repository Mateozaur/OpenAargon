package io.github.phantamanta44.openar.game.piece;

/**
 * One placed piece together with the per-cell state supplied by a level.
 *
 * PieceDefinition describes what the piece is. PieceInstance describes one
 * occurrence of it on a board. Keeping these values together prevents level
 * loaders and the runtime field from maintaining several parallel grids for
 * one tile.
 */
public final class PieceInstance {

    private final PieceDefinition definition;
    private final int rotation;
    private final int meta;
    private final int mutability;
    private final VisualStyleId visualStyle;

    /**
     * Creates a piece that inherits the board-level visual style.
     */
    public PieceInstance(
            PieceDefinition definition,
            int rotation,
            int meta,
            int mutability) {
        this(definition, rotation, meta, mutability, null);
    }

    /**
     * Creates a piece with an optional per-cell visual style override.
     * A null style means "inherit the board style".
     */
    public PieceInstance(
            PieceDefinition definition,
            int rotation,
            int meta,
            int mutability,
            VisualStyleId visualStyle) {
        if (definition == null) {
            throw new IllegalArgumentException("Piece definition cannot be null");
        }

        this.definition = definition;
        this.rotation = rotation;
        this.meta = meta;
        this.mutability = mutability;
        this.visualStyle = visualStyle;
    }

    public PieceDefinition getDefinition() {
        return definition;
    }

    public PieceId getId() {
        return definition.getId();
    }

    public IGamePiece getImplementation() {
        return definition.getImplementation();
    }

    public int getRotation() {
        return rotation;
    }

    public int getMeta() {
        return meta;
    }

    public int getMutability() {
        return mutability;
    }

    /**
     * Returns the explicit style for this tile, or null when it inherits the
     * board-level style.
     */
    public VisualStyleId getVisualStyle() {
        return visualStyle;
    }

    public VisualStyleId resolveVisualStyle(VisualStyleId boardStyle) {
        return visualStyle != null ? visualStyle : boardStyle;
    }

    public PieceInstance withRotation(int value) {
        return new PieceInstance(definition, value, meta, mutability, visualStyle);
    }

    public PieceInstance withMeta(int value) {
        return new PieceInstance(definition, rotation, value, mutability, visualStyle);
    }

    public PieceInstance withMutability(int value) {
        return new PieceInstance(definition, rotation, meta, value, visualStyle);
    }

    public PieceInstance withVisualStyle(VisualStyleId value) {
        return new PieceInstance(definition, rotation, meta, mutability, value);
    }

}
