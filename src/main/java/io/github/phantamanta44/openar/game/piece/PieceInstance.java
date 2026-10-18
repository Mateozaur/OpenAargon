package io.github.phantamanta44.openar.game.piece;

/**
 * One placed piece together with the per-cell state supplied by a level.
 *
 * PieceDefinition describes what the piece is. PieceInstance describes one
 * occurrence of it on a board. Keeping these values together prevents level
 * loaders from having to maintain several parallel grids for one tile.
 */
public final class PieceInstance {

    private final PieceDefinition definition;
    private final int rotation;
    private final int meta;
    private final int mutability;

    public PieceInstance(
            PieceDefinition definition,
            int rotation,
            int meta,
            int mutability) {
        if (definition == null) {
            throw new IllegalArgumentException("Piece definition cannot be null");
        }

        this.definition = definition;
        this.rotation = rotation;
        this.meta = meta;
        this.mutability = mutability;
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

}
