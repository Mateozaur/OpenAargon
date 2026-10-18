package io.github.phantamanta44.openar.game.piece;

/**
 * Renderer-independent description of how a wall-like tile connects to its
 * orthogonal neighbours.
 *
 * This is intentionally separate from legacy rotation/variant numbers. The
 * original Collector's Edition maps can use different rotation values for the
 * same geometry, so topology must not be inferred from a rotation index alone.
 */
public final class WallTopology {

    public static final int NORTH = 1;
    public static final int EAST = 2;
    public static final int SOUTH = 4;
    public static final int WEST = 8;

    private final int mask;

    private WallTopology(int mask) {
        this.mask = mask & 0x0F;
    }

    public static WallTopology of(
            boolean north,
            boolean east,
            boolean south,
            boolean west) {
        int mask = 0;
        if (north) mask |= NORTH;
        if (east) mask |= EAST;
        if (south) mask |= SOUTH;
        if (west) mask |= WEST;
        return new WallTopology(mask);
    }

    public static WallTopology fromMask(int mask) {
        return new WallTopology(mask);
    }

    public int getMask() {
        return mask;
    }

    public boolean hasNorth() {
        return (mask & NORTH) != 0;
    }

    public boolean hasEast() {
        return (mask & EAST) != 0;
    }

    public boolean hasSouth() {
        return (mask & SOUTH) != 0;
    }

    public boolean hasWest() {
        return (mask & WEST) != 0;
    }

}
