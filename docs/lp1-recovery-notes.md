# LP1 legacy rendering recovery notes

_Last updated: 2026-10-18_

Live hand-off state for the Collector's Edition Level Pack 1 (LP1) legacy-rendering investigation.

## Safety rule

Do **not** add rows to `src/main/resources/legacy/styles/lp1.csv` from visual guesses, equal dimensions, or unvalidated binary hypotheses. An override needs concrete original-asset evidence or a validated index-preservation method.

## Confirmed model

- `src/main/resources/legacy/render-map.csv` is the shared mapping; style CSVs are sparse overrides.
- Generic `atlas=base` uses the currently selected legacy style atlas; `exp1` / `exp2` remain shared expansion atlases.
- `LegacyTileSet.BASE` and `LEVEL_PACK_1` are both 20x15, but dimensions alone do not prove semantic slot equality.
- Original `Pieces/*.pce` picture tables are authoritative sources for piece render indices.
- LP1 has no alternate `Pieces/*.pce` corpus; the key question is whether its ordinary sheet preserves shared-PCE slot semantics.

## Resolved — do not redo first

### Selector (`I`, token 73)

`Pieces/selector.pce` explicitly uses `black_tiles_exp1.bmp` / `black_tiles_small_exp1.bmp`. Its picture values and offsets reproduce the existing Selector rows in `render-map.csv` under `atlas=exp1`. Selector stays on shared exp1 and needs no LP1 override.

### LP1 skill / downstream DLLs

`RELEASE_LevelPackOne.dll` implements LP1 skill/level-selection UI, not token-to-sprite mapping. `RELEASE_ItemMap.dll::MAP::SelectTile(CPosition)` and `RELEASE_TileBlitter.dll` are also not mapping sources.

## Atlas provenance checkpoint

Commit `2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8` introduced Collector's Edition legacy rendering plus `base.png`, `lp1.png`, `lp2.png`, `lp3.png`, `lp4.png`, `classic.png`, `hotter.png`, `pocket.png`, `exp1.png`, `exp2.png`; no conversion script/source-BMP provenance was documented there.

Known blobs:

```text
base.png  e1e903a202b7d8c301f46cfa12291abe4a383ae4  ~320631 bytes
lp1.png   87ab0fe3c09e2e26dca009d21e50791cfa1e3570  ~340425 bytes
```

## Original tile-sheet evidence

Tracked CE tree: `Aargon Deluxe Collectors Edition 1.4.CE/`.

Shared/root sheets:

```text
Pictures/black_tiles.bmp                 921654  2c1cebca0a437c2080352e48425150d3c374a533
Pictures/black_tiles_exp1.bmp            491576  adc86dffc884c39574c46bd1f7cbf329378be876
Pictures/black_tiles_exp2.bmp             24630  777e69561f964de3d335225b0d244b7c27b4e26b
Pictures/black_tiles_small.bmp            57654  f12083c95843dd58ee66f42dd5fe8569732d0a61
Pictures/black_tiles_small_exp1.bmp       30776  d6259db4d766c373db8d86c6851a0bbef84ef24d
Pictures/black_tiles_small_exp2.bmp        1590  f0df8506a8e4515c6c1ce53ce8dd7b0373bf44d3
```

LP1 pack-local ordinary sheets:

```text
Level Packs/Level Pack 1/Pictures/black_tiles.bmp
  921654 bytes  768d05581ab0a2a39beb9e29c492e869d07eb2fe
Level Packs/Level Pack 1/Pictures/black_tiles_small.bmp
   57654 bytes  e27ac267529a3f56020508c07775414f114a4032
```

LP1 contains no `black_tiles_exp*` files. Thus LP1 overrides only the ordinary pair; exp1/exp2 remain shared.

LP1 ordinary sheets exactly match base ordinary sheets in byte size but differ in blob identity. This is strong structural evidence for same-shape content replacement, but not yet proof of semantic slot preservation.

### Repeated pack-local convention across all four level packs

All four original Level Packs ship same-named ordinary tile sheets under their own `Pictures/` directories:

```text
Level Pack 1 black_tiles.bmp        921654  768d05581ab0a2a39beb9e29c492e869d07eb2fe
Level Pack 1 black_tiles_small.bmp   57654  e27ac267529a3f56020508c07775414f114a4032
Level Pack 2 black_tiles.bmp        921654  1c049d0baf5de562518ac4df894dc2bb5be7c4b4
Level Pack 2 black_tiles_small.bmp   57654  fb9fd458550f87cc3696e980277813b9bf10e809
Level Pack 3 black_tiles.bmp        921656  a6bb5fe81ddbb399640a051a5459bd66bdbafa0a
Level Pack 3 black_tiles_small.bmp   57656  f039e588b1af4a0bc4af90ac4a3cd20454889ec6
Level Pack 4 black_tiles.bmp        921654  3f63fff739958dc1473564d083d6dc725c52eacf
Level Pack 4 black_tiles_small.bmp   57654  97dbb2e35c8eb67c3b0e9f6e7bcd76fc6d08fddc
```

The repeated directory and filename convention across LP1-LP4 is very strong architectural evidence that active Level Packs intentionally supply their own ordinary `black_tiles` resources. LP1, LP2 and LP4 have the same byte sizes as the shared/root pair. LP3 is a deliberate caveat: both LP3 files are exactly two bytes larger than the corresponding shared/other-pack files. Therefore do **not** claim that every pack uses a byte-identical BMP format merely from file size.

### BMP-header inspection attempt

Attempted to fetch the shared/base `black_tiles.bmp` blob (`2c1cebca0a437c2080352e48425150d3c374a533`) through the connected GitHub blob reader. The connector failed with `UnicodeDecodeError` because it tried to decode the binary blob as UTF-8. No bytes were returned and no repository state changed.

Therefore width/height, bits-per-pixel, pixel offset, compression mode, and the exact reason for LP3's +2 bytes are not directly confirmed from the binary header. Arithmetic compatibility with a 54-byte 24-bit BMP header is only a hypothesis until binary bytes can be read.

## LP1 PACK.INI

```ini
[Params]
Text = Try these levels in one of our secret deep space installations.
Position = 7000
Edit = false
SelectSkillDll=LevelPackOne
IsEmpty=false
```

No tile filename, `Pictures` path, atlas name, or resource-search-path field exists. Pack-local ordinary-sheet selection therefore is not explicitly configured in `PACK.INI`; any shadowing must come from general active-pack resource lookup.

## LP1 level-format evidence

Enumerated `Level Packs/Level Pack 1/Levels/` and confirmed four skill directories: `Skill1`, `Skill2`, `Skill3`, `Skill4`.

Enumerated `Skill1/`; it contains ordinary `Level_###.map` files. Directly read `Skill1/Level_001.map` (`LANDING LIGHTS`). The file is a plain token-oriented board map plus `MAPTEXT`, `SCRIPT`, and copyright fields. It contains board tokens such as `n`, `O`, `C`, `S`, etc.; it contains **no tile-sheet filename, atlas name, picture-index table, or pack-local remapping table**.

This is consistent with LP1 maps using the same token-to-piece-definition mechanism as the shared game. It is not yet sufficient by itself to identify every token or prove lookup precedence, so the next step is to correlate representative LP1 map tokens with the shared `.pce` `TOKEN` declarations and their ordinary/expansion atlas choices.

## PCE inventory checkpoint

Authoritative `Pieces/` listing contains 29 `.pce` files:

```text
blackhole blank brick coin complex convert double doubleslit explosion filter
fuelcell lbender mine mirror oneway polarizer prism prismirror refractor selector
slime source spaceship splicer splitter threeway tnt water wood
```

`blank.pce` documents defaults:

```text
TilesFile default:      black_tiles.bmp
SmallTilesFile default: black_tiles_small.bmp
```

Direct reads so far:

```text
blackhole.pce   explicit exp1
blank.pce       format/default documentation
brick.pce       ordinary/default
coin.pce        ordinary/default
complex.pce     explicit exp1
convert.pce     ordinary/default (indices 260..267 prove index magnitude does not identify atlas)
double.pce      ordinary/default
doubleslit.pce  ordinary/default
explosion.pce   animation list only; no [FLAGS], intentionally unclassified
```

GitHub code search for `TILESFILE` returned zero indexed results despite direct proof that declarations exist, so direct file reads are authoritative.

## Current open questions

1. What general loader/search-path behavior makes active-pack `Pictures/black_tiles.bmp` shadow the shared/base sheet?
2. Correlate LP1 map tokens with shared `.pce` `TOKEN` declarations and atlas choices.
3. Do LP1 ordinary tile slots preserve the semantic ordering expected by shared PCE picture indices?
4. Does OpenAargon's `lp1.png` directly correspond to LP1's pack-local `black_tiles.bmp` with coordinates/order preserved?
5. What explains LP3's +2-byte BMP size difference?

## Action log — 2026-10-18

- Resumed `linux-modernization`; read this note before touching mappings/code.
- Corrected initial bad repo owner probe; correct repo is `Mateozaur/OpenAargon`.
- Read branch/tree; inventoried CE assets, LP1 `PACK.INI`, shared/pack-local tile sheets, and PCE directory.
- Attempted shallow local clone; failed because execution environment could not resolve `github.com`; no project state changed.
- Confirmed LP1-LP4 all carry pack-local same-named ordinary `black_tiles` pairs; LP3 pair is +2 bytes.
- Confirmed LP1 has no local `black_tiles_exp*`; expansion atlases remain shared.
- Direct-read PCE batches through `explosion.pce`; documented ordinary defaults and exp1 exceptions discovered so far.
- Repository indexed searches for `TILESFILE`, `Level Packs`, and `LevelPack` did not expose loader source and are treated as indexing/source-availability limits.
- Attempted binary BMP blob read for header inspection; failed with `UnicodeDecodeError`; logged as a connector limitation.
- Enumerated LP1 `Levels/` and confirmed `Skill1`-`Skill4` directories.
- Enumerated LP1 `Levels/Skill1/` and identified its `Level_###.map` files.
- Fetched `Skill1/Level_001.map` and confirmed it is a token-oriented board map with no embedded atlas/picture-index/remap metadata.
- No `styles/lp1.csv` or functional code changes have been made during this pass.

## Resume here

Correlate representative tokens in LP1 `Skill1/Level_001.map` (`n`, `O`, `C`, `S`, etc.) with shared `Pieces/*.pce` `TOKEN` declarations and their picture tables/atlas choices. If LP1 maps demonstrably use ordinary shared PCE definitions while LP1 supplies only a same-named ordinary sheet and no alternate PCE/index table, that substantially strengthens the semantic index-preservation case. Do not add LP1 mapping overrides unless a concrete deviation is found.

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
```
