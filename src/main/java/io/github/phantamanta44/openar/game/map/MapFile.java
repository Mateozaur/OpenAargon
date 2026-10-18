package io.github.phantamanta44.openar.game.map;

import io.github.phantamanta44.openar.Aargon;
import io.github.phantamanta44.openar.game.beam.BeamColor;
import io.github.phantamanta44.openar.game.piece.PieceInstance;
import io.github.phantamanta44.openar.game.piece.PieceRegistry;
import io.github.phantamanta44.openar.game.piece.impl.PieceBorder;
import io.github.phantamanta44.openar.util.Array2D;
import io.github.phantamanta44.openar.util.math.IntVector;

public class MapFile {

    private final Array2D<PieceInstance> pieceGrid = new Array2D<>(20, 13);

    private MapFile() {
        // Cells are populated directly by parse().
    }

    public static MapFile parse(String[] t) {
        MapFile map = new MapFile();
        try {
            for (int i = 0; i < 13; i++) {
                for (int j = 0; j < 20; j++) {
                    char[] token = t[i].substring(j * 4, (j + 1) * 4).toCharArray();

                    int rotation = token[1] == ' '
                            ? 0
                            : Integer.parseInt(Character.toString(token[1]));
                    int meta = BeamColor.fromToken(Character.toString(token[2])).ordinal();
                    int mutability = Mutability.fromToken(Character.toString(token[3])).bitmask();

                    map.pieceGrid.set(j, i, new PieceInstance(
                            PieceRegistry.getDefinition(Character.toString(token[0])),
                            rotation,
                            meta,
                            mutability));
                }
            }
        } catch (Exception e) {
            Aargon.getLogger().error("Map parsing failed!");
            e.printStackTrace();
        }
        return map;
    }

    public void populate(GameField f) {
        pieceGrid.forEach(f::setPieceInstance);

        if (Boolean.getBoolean("aargon.debugBorders")) {
            Aargon.getLogger().info("Border diagnostics (x,y rot meta topology[NESW])");
            pieceGrid.forEach((state, c) -> {
                if (state.getImplementation() instanceof PieceBorder) {
                    boolean n = isBorder(c.getX(), c.getY() - 1);
                    boolean e = isBorder(c.getX() + 1, c.getY());
                    boolean s = isBorder(c.getX(), c.getY() + 1);
                    boolean w = isBorder(c.getX() - 1, c.getY());
                    Aargon.getLogger().info(
                            "BORDER {},{} rot={} meta={} topology={}{}{}{}",
                            c.getX(), c.getY(), state.getRotation(), state.getMeta(),
                            n ? "N" : "-", e ? "E" : "-", s ? "S" : "-", w ? "W" : "-");
                }
            });
        }

        f.updateBeams();
    }

    private boolean isBorder(int x, int y) {
        if (x < 0 || y < 0 || x >= 20 || y >= 13) return false;
        PieceInstance state = pieceGrid.get(new IntVector(x, y));
        return state != null && state.getImplementation() instanceof PieceBorder;
    }

}
