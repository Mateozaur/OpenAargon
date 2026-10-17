package io.github.phantamanta44.openar.render.legacy;

import java.io.File;
import java.util.Locale;

public enum LegacyTileSet {
    BASE("base", "texture/legacy/base.png", 20, 15),
    CLASSIC("classic", "texture/legacy/classic.png", 20, 13),
    LEVEL_PACK_1("lp1", "texture/legacy/lp1.png", 20, 15),
    LEVEL_PACK_2("lp2", "texture/legacy/lp2.png", 20, 15),
    LEVEL_PACK_3("lp3", "texture/legacy/lp3.png", 20, 15),
    LEVEL_PACK_4("lp4", "texture/legacy/lp4.png", 20, 15),
    POCKET("pocket", "texture/legacy/pocket.png", 20, 15),
    HOTTER("hotter", "texture/legacy/hotter.png", 20, 15);

    private final String id;
    private final String texturePath;
    private final int columns;
    private final int rows;

    LegacyTileSet(String id, String texturePath, int columns, int rows) {
        this.id = id;
        this.texturePath = texturePath;
        this.columns = columns;
        this.rows = rows;
    }

    public String getId() { return id; }
    public String getTexturePath() { return texturePath; }
    public int getColumns() { return columns; }
    public int getRows() { return rows; }

    public static LegacyTileSet fromId(String value) {
        if (value == null || value.trim().isEmpty()) return BASE;
        String v = value.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
        switch (v) {
            case "classic": return CLASSIC;
            case "lp1": case "levelpack1": return LEVEL_PACK_1;
            case "lp2": case "levelpack2": return LEVEL_PACK_2;
            case "lp3": case "levelpack3": return LEVEL_PACK_3;
            case "lp4": case "levelpack4": case "medical": return LEVEL_PACK_4;
            case "pocket": case "pocketlevels": return POCKET;
            case "hotter": return HOTTER;
            default: return BASE;
        }
    }

    public static LegacyTileSet detect(File mapFile) {
        String forced = System.getProperty("aargon.tileset");
        if (forced != null && !forced.trim().isEmpty()) return fromId(forced);
        if (mapFile == null) return BASE;
        String p = mapFile.getAbsolutePath().replace('\\', '/').toLowerCase(Locale.ROOT);
        if (p.contains("/level packs/classic/")) return CLASSIC;
        if (p.contains("/level packs/level pack 1/")) return LEVEL_PACK_1;
        if (p.contains("/level packs/level pack 2/")) return LEVEL_PACK_2;
        if (p.contains("/level packs/level pack 3/")) return LEVEL_PACK_3;
        if (p.contains("/level packs/level pack 4/")) return LEVEL_PACK_4;
        if (p.contains("/level packs/pocket levels/")) return POCKET;
        if (p.contains("/level packs/hotter/")) return HOTTER;
        return BASE;
    }
}
