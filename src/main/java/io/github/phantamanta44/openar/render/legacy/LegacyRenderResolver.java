package io.github.phantamanta44.openar.render.legacy;

import io.github.phantamanta44.openar.util.math.IntVector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class LegacyRenderResolver {

    private static final int TILE_SIZE = 32;

    // OpenAargon BeamColor ordinal -> official PCE color index.
    // OA: RED,GREEN,BLUE,CYAN,MAGENTA,YELLOW,WHITE,BLACK
    // PCE: BLACK,BLUE,GREEN,CYAN,RED,MAGENTA,YELLOW,WHITE
    private static final int[] PCE_COLOR_BY_META = {4, 2, 1, 3, 5, 6, 7, 0};

    /**
     * Collector's Edition base mapping. This is no longer treated as proof that
     * every alternate style uses the same atlas indices; style-specific maps may
     * override any entry independently.
     */
    private static final Map<Long, RenderDef> BASE_DEFINITIONS =
            loadDefinitions("legacy/render-map.csv", true);

    /**
     * Sparse style-specific overrides loaded from legacy/styles/<style>.csv.
     */
    private static final Map<LegacyTileSet, Map<Long, RenderDef>> STYLE_DEFINITIONS =
            loadStyleDefinitions();

    private static volatile LegacyTileSet tileSet = LegacyTileSet.BASE;

    private LegacyRenderResolver() { }

    public static void setTileSet(LegacyTileSet value) {
        tileSet = value != null ? value : LegacyTileSet.BASE;
    }

    public static LegacyTileSet getTileSet() {
        return tileSet;
    }

    /**
     * Backwards-compatible resolver using the currently selected board tileset.
     */
    public static Sprite resolve(String token, int rotation, int meta) {
        return resolve(token, rotation, meta, tileSet);
    }

    /**
     * Resolves a legacy sprite against an explicit tileset. Style-specific
     * definitions win over the base Collector's Edition mapping. Missing style
     * entries temporarily fall back to the base definition so styles can be
     * migrated incrementally without breaking existing level packs.
     */
    public static Sprite resolve(
            String token,
            int rotation,
            int meta,
            LegacyTileSet selectedTileSet) {
        if (token == null || token.isEmpty()) return null;

        LegacyTileSet style = selectedTileSet != null
                ? selectedTileSet
                : LegacyTileSet.BASE;
        int pceColor = toPceColor(meta);
        int rot = Math.floorMod(rotation, 8);
        long key = key(token.charAt(0), pceColor, rot);

        RenderDef def = null;
        Map<Long, RenderDef> styleMap = STYLE_DEFINITIONS.get(style);
        if (styleMap != null) {
            def = styleMap.get(key);
        }
        if (def == null) {
            def = BASE_DEFINITIONS.get(key);
        }
        if (def == null) return null;

        Atlas atlas = atlas(def.atlas, style);
        int tile = def.tile;
        int col = tile % atlas.columns;
        int row = tile / atlas.columns;
        if (row < 0 || row >= atlas.rows) return null;

        // TextureManager uses PNGDecoder.decodeFlipped(), so V is reversed.
        int u = col * TILE_SIZE;
        int v = (atlas.rows - 1 - row) * TILE_SIZE;
        return new Sprite(atlas.path, new IntVector(u, v), def.animated);
    }

    private static int toPceColor(int meta) {
        if (meta < 0 || meta >= PCE_COLOR_BY_META.length) return 0;
        return PCE_COLOR_BY_META[meta];
    }

    private static Atlas atlas(String family, LegacyTileSet selectedTileSet) {
        if ("exp1".equals(family)) return new Atlas("texture/legacy/exp1.png", 20, 8);
        if ("exp2".equals(family)) return new Atlas("texture/legacy/exp2.png", 8, 1);
        LegacyTileSet set = selectedTileSet != null ? selectedTileSet : LegacyTileSet.BASE;
        return new Atlas(set.getTexturePath(), set.getColumns(), set.getRows());
    }

    private static Map<LegacyTileSet, Map<Long, RenderDef>> loadStyleDefinitions() {
        Map<LegacyTileSet, Map<Long, RenderDef>> result = new HashMap<>();
        for (LegacyTileSet style : LegacyTileSet.values()) {
            if (style == LegacyTileSet.BASE) continue;
            Map<Long, RenderDef> definitions = loadDefinitions(
                    "legacy/styles/" + style.getId() + ".csv",
                    false);
            if (!definitions.isEmpty()) {
                result.put(style, definitions);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<Long, RenderDef> loadDefinitions(
            String resourcePath,
            boolean required) {
        Map<Long, RenderDef> map = new HashMap<>();
        InputStream in = LegacyRenderResolver.class.getClassLoader()
                .getResourceAsStream(resourcePath);
        if (in == null) {
            if (required) {
                throw new IllegalStateException("Missing " + resourcePath);
            }
            return Collections.emptyMap();
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                if (!headerSkipped) {
                    headerSkipped = true;
                    if (trimmed.startsWith("tokenCode,")) continue;
                }

                String[] p = line.split(",", -1);
                if (p.length < 8) continue;
                int tokenCode = Integer.parseInt(p[0]);
                String atlas = p[2];
                int color = Integer.parseInt(p[3]);
                int rotation = Integer.parseInt(p[4]);
                int tile = Integer.parseInt(p[5]);
                boolean animated = "1".equals(p[6]);
                map.put(
                        key((char)tokenCode, color, rotation),
                        new RenderDef(atlas, tile, animated));
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not load legacy render definitions from " + resourcePath,
                    e);
        }
        return Collections.unmodifiableMap(map);
    }

    private static long key(char token, int color, int rotation) {
        return ((long) token << 16) | ((long) color << 8) | (rotation & 0xFFL);
    }

    private static final class RenderDef {
        final String atlas;
        final int tile;
        final boolean animated;

        RenderDef(String atlas, int tile, boolean animated) {
            this.atlas = atlas;
            this.tile = tile;
            this.animated = animated;
        }
    }

    private static final class Atlas {
        final String path;
        final int columns;
        final int rows;

        Atlas(String path, int columns, int rows) {
            this.path = path;
            this.columns = columns;
            this.rows = rows;
        }
    }

    public static final class Sprite {
        private final String texturePath;
        private final IntVector textureOffset;
        private final boolean animated;

        Sprite(String texturePath, IntVector textureOffset, boolean animated) {
            this.texturePath = texturePath;
            this.textureOffset = textureOffset;
            this.animated = animated;
        }

        public String getTexturePath() { return texturePath; }
        public IntVector getTextureOffset() { return textureOffset; }
        public boolean isAnimated() { return animated; }
    }
}
