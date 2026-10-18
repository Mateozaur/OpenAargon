package io.github.phantamanta44.openar.game.map;

import io.github.phantamanta44.openar.Aargon;
import io.github.phantamanta44.openar.game.beam.Beam;
import io.github.phantamanta44.openar.game.beam.BeamTile;
import io.github.phantamanta44.openar.game.beam.Direction;
import io.github.phantamanta44.openar.game.piece.*;
import io.github.phantamanta44.openar.render.Line4I;
import io.github.phantamanta44.openar.render.Quad8I;
import io.github.phantamanta44.openar.render.RenderManager;
import io.github.phantamanta44.openar.render.legacy.LegacyRenderResolver;
import io.github.phantamanta44.openar.render.visual.PieceVisualCatalog;
import io.github.phantamanta44.openar.render.visual.SpriteDefinition;
import io.github.phantamanta44.openar.util.Array2D;
import io.github.phantamanta44.openar.util.math.FloatVector;
import io.github.phantamanta44.openar.util.math.IntVector;

public class GameField implements IGameField {

    private static final String EMPTY_SLOT_PATH = "texture/empty.png";

    private Array2D<BeamTile> beamGrid = new Array2D<>(20, 13);
    private Array2D<PieceInstance> pieceGrid = new Array2D<>(20, 13);
    private boolean goal = false, fail = false;

    public GameField() {
        PieceDefinition air = PieceRegistry.getDefinition(PieceId.of("air"));
        if (air == null) {
            throw new IllegalStateException("PieceRegistry must be initialized before GameField");
        }

        pieceGrid.fill(() -> new PieceInstance(air, 0, 0, 0));
        beamGrid.fill(BeamTile::new);
    }

    public void updateBeams() {
        beamGrid.forEach((b, c) -> b.clear());
        pieceGrid.forEach((instance, c) -> {
            IGamePiece p = instance.getImplementation();
            if (p instanceof ISourcePiece)
                ((ISourcePiece)p).getSourceBeams(
                        this,
                        c,
                        instance.getRotation(),
                        instance.getMeta()).forEach(b -> sourceBeam(b.clone(), c));
        });
        goal = true;
        fail = false;
        pieceGrid.forEach((instance, c) -> {
            IGamePiece p = instance.getImplementation();
            int rot = instance.getRotation(), meta = instance.getMeta();
            if (p instanceof IGoalPiece)
                goal &= ((IGoalPiece)p).isGoalMet(this, c, rot, meta);
            if (p instanceof IFailPiece)
                fail |= ((IFailPiece)p).isFailing(this, c, rot, meta);
        });
        if (fail)
            Aargon.getLogger().info("SYSTEM FAILING");
        else if (goal)
            Aargon.getLogger().info("SYSTEM SOLVED"); // TODO Pass winning/failing state to Aargon and do something
    }

    private void sourceBeam(Beam beam, IntVector coords) {
        BeamTile tile = beamGrid.get(coords);
        Beam in = tile.getIn(beam.getDir()), out = tile.getOut(beam.getDir());
        if (out != null)
            out.merge(beam);
        else
            tile.putOut(beam);
        if (in != null)
            in.merge(beam);
        IntVector next = beam.getDir().offset(coords);
        if (next.getX() < 0 || next.getY() < 0
                || next.getX() >= pieceGrid.getWidth() || next.getY() >= pieceGrid.getHeight())
            return;
        receiveBeam(beam, next, tile);
    }

    private void receiveBeam(Beam beam, IntVector coords, BeamTile from) {
        PieceInstance instance = getPieceInstance(coords);
        int rot = instance.getRotation(), meta = instance.getMeta();
        IGamePiece piece = instance.getImplementation();
        BeamTile beams = beamGrid.get(coords);
        Beam newBeam = new Beam(beam.getColor(), beam.getDir().getOpposite());
        beams.putIn(newBeam);
        Beam out = beams.getOut(beam.getDir().getOpposite());
        if (out != null)
            out.merge(newBeam);
        piece.getReflections(this, coords, rot, meta, new Beam(beam.getColor(), beam.getDir().getOpposite())).forEach(b -> sourceBeam(b, coords));
    }

    public void setPiece(IntVector coords, IGamePiece piece) {
        PieceDefinition definition = PieceRegistry.getDefinition(piece);
        pieceGrid.set(coords, new PieceInstance(definition, 0, 0, 0));
    }

    public void setPieceInstance(IntVector coords, PieceInstance instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Piece instance cannot be null");
        }
        pieceGrid.set(coords, instance);
    }

    public void setRotation(IntVector coords, int rotation) {
        pieceGrid.set(coords, getPieceInstance(coords).withRotation(rotation));
    }

    public void setMeta(IntVector coords, int meta) {
        pieceGrid.set(coords, getPieceInstance(coords).withMeta(meta));
    }

    public void setMutability(IntVector coords, int muta) {
        pieceGrid.set(coords, getPieceInstance(coords).withMutability(muta));
    }

    public PieceInstance getPieceInstance(IntVector coords) {
        return pieceGrid.get(coords);
    }

    @Override
    public IGamePiece getPiece(IntVector coords) {
        return getPieceInstance(coords).getImplementation();
    }

    @Override
    public int getRotation(IntVector coords) {
        return getPieceInstance(coords).getRotation();
    }

    @Override
    public int getMeta(IntVector coords) {
        return getPieceInstance(coords).getMeta();
    }

    @Override
    public BeamTile getBeams(IntVector coords) {
        return beamGrid.get(coords);
    }

    @Override
    public int getMutability(IntVector coords) {
        return getPieceInstance(coords).getMutability();
    }

    public synchronized void bufferRenders(float xSize, float ySize, float ratio) {
        int origX = (int)-xSize, origY = (int)(ySize / ratio);
        VisualStyleId boardStyle = LegacyRenderResolver.getTileSet().getVisualStyleId();

        pieceGrid.forEach((instance, c) -> {
            IGamePiece p = instance.getImplementation();
            int x = origX + c.getX() * 103;
            int y = origY - (c.getY() + 1) * 103;

            if (p == null || " ".equals(p.getToken())) {
                RenderManager.bufferQuad(new Quad8I(
                        EMPTY_SLOT_PATH, x, y, 103, 103, 0, 0, 32, 32));
                return;
            }

            int rot = instance.getRotation();
            int meta = instance.getMeta();

            /*
             * Visual lookup is now centralized. The catalog may use a legacy
             * compatibility adapter, a per-cell style override, or eventually a
             * native/custom visual definition. Native pieces currently fall back
             * to their existing IGamePiece texture methods.
             */
            SpriteDefinition sprite = PieceVisualCatalog.resolve(instance, boardStyle);

            if (sprite != null) {
                RenderManager.bufferQuad(new Quad8I(
                        EMPTY_SLOT_PATH, x, y, 103, 103, 0, 0, 32, 32));

                RenderManager.bufferQuad(new Quad8I(
                        sprite.getTexturePath(),
                        x, y, 103, 103,
                        sprite.getTextureOffset().getX(),
                        sprite.getTextureOffset().getY(),
                        32, 32));
            } else {
                String path = p.getTexturePath(this, c, rot, meta);
                IntVector offset = p.getTextureOffset(this, c, rot, meta);

                RenderManager.bufferQuad(new Quad8I(
                        path, x, y, 103, 103,
                        offset.getX(), offset.getY(), 32, 32));
            }
        });

        beamGrid.forEach((b, c) -> {
            for (Direction dir : Direction.values()) {
                if (b.getOut(dir) != null) {
                    FloatVector t = dir.offsetHalf(c);
                    Line4I base = new Line4I(origX + c.getX() * 103 + 52, origY - (c.getY() + 1) * 103 + 52,
                            (int)((float)origX + t.getX() * 103F + 52F), (int)((float)origY - (t.getY() + 1) * 103F + 52F),
                            b.getOut(dir).getColor(), 0.3F, 1F);
                    RenderManager.bufferLine(base);
                    accompanyLines(base);
                } else if (b.getIn(dir) != null) {
                    FloatVector t = dir.offsetHalf(c);
                    Line4I base = new Line4I(origX + c.getX() * 103 + 52, origY - (c.getY() + 1) * 103 + 52,
                            (int)((float)origX + t.getX() * 103F + 52F), (int)((float)origY - (t.getY() + 1) * 103F + 52F),
                            b.getIn(dir).getColor(), 0.3F, 1F);
                    RenderManager.bufferLine(base);
                    accompanyLines(base);
                }
            }
        });
    }

    private void accompanyLines(Line4I base) {
        RenderManager.bufferLine(new Line4I(base.x1, base.y1, base.x2, base.y2, base.r, base.g, base.b, 0.1F, 3F));
        RenderManager.bufferLine(new Line4I(base.x1, base.y1, base.x2, base.y2, base.r, base.g, base.b, 0.1F, 5F));
        RenderManager.bufferLine(new Line4I(base.x1, base.y1, base.x2, base.y2, base.r, base.g, base.b, 0.1F, 7F));
    }

}
