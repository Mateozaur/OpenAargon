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

### Repeated pack-local convention

Level Pack 2 also contains the same ordinary filenames in its own `Pictures/` directory:

```text
Level Packs/Level Pack 2/Pictures/black_tiles.bmp
  921654 bytes  1c049d0baf5de562518ac4df894dc2bb5be7c4b4
Level Packs/Level Pack 2/Pictures/black_tiles_small.bmp
   57654 bytes  fb9fd458550f87cc3696e980277813b9bf10e809
```

These sizes again exactly match the shared/base ordinary sheets while the blobs differ. The repeated directory/name/size pattern across LP1 and LP2 is strong evidence that same-named pack-local ordinary tile sheets are an intentional level-pack resource convention, not an LP1 accident. It still does not alone prove lookup precedence or semantic slot identity.

## LP1 PACK.INI

Exact content:

```ini
[Params]
Text = Try these levels in one of our secret deep space installations.
Position = 7000
Edit = false
SelectSkillDll=LevelPackOne
IsEmpty=false
```

No tile filename, `Pictures` path, atlas name, or resource-search-path field exists. Pack-local ordinary-sheet selection therefore is not explicitly configured in `PACK.INI`; any shadowing must come from general active-pack resource lookup.

## PCE inventory checkpoint

Authoritative `Pieces/` listing contains 29 `.pce` files (an earlier spoken count of 27 was wrong and was corrected):

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

GitHub code search for `TILESFILE` returned zero indexed results despite direct proof that the declarations exist, so direct file reads are authoritative.

## Current open questions

1. What general loader/search-path behavior makes active-pack `Pictures/black_tiles.bmp` shadow the shared/base sheet?
2. Do Level Packs 3 and 4 repeat the LP1/LP2 same-named ordinary-sheet convention?
3. Complete remaining direct `.pce` classifications.
4. Do LP1 ordinary tile slots preserve the semantic ordering expected by shared PCE picture indices?
5. Does OpenAargon's `lp1.png` directly correspond to LP1's pack-local `black_tiles.bmp` with coordinates/order preserved?

## Action log — 2026-10-18

- Resumed `linux-modernization`; read this note before touching mappings/code.
- Corrected initial bad repo owner probe (`OpenAargon/OpenAargon` -> 404); correct repo is `Mateozaur/OpenAargon`.
- Read branch metadata and recursive tree; searched tree for `black_tiles`, `PACK.INI`, and `Pieces/`.
- Attempted shallow local clone into `/mnt/data/OpenAargon`; failed before checkout because environment could not resolve `github.com`; no project state changed.
- Switched back to connected GitHub API.
- Enumerated CE root, `Level Packs/`, LP1 root, shared `Pieces/`, LP1 `Pictures/`, shared/root `Pictures/`.
- Confirmed LP1 pack-local ordinary sheets; confirmed no LP1 `black_tiles_exp*`.
- Fetched LP1 `PACK.INI`; ruled it out as explicit tile/search-path configuration.
- Recorded all six shared ordinary/expansion tile-sheet files with blobs/sizes.
- Compared LP1 ordinary sheets with base: same sizes, different blobs.
- Retrieved authoritative `Pieces/` listing; corrected count from 27 to 29.
- Ran indexed `TILESFILE` search; zero results, treated as indexing limitation.
- Direct-read PCE batch 1: `blackhole`, `blank`, `brick`.
- Direct-read PCE batch 2: `coin`, `complex`, `convert`.
- Direct-read PCE batch 3: `double`, `doubleslit`, `explosion`.
- Searched repo code for `Level Packs` and `LevelPack`; both returned zero indexed source results.
- Probed earlier recursive tree for `LevelPack`; no actionable loader-source evidence found.
- Re-enumerated `Level Packs/` to identify sibling packs for structural comparison.
- Enumerated `Level Pack 2/Pictures/` and searched returned inventory for `black_tiles`.
- Confirmed LP2 also supplies pack-local `black_tiles.bmp` (921654 bytes, blob `1c049d0b...`) and `black_tiles_small.bmp` (57654 bytes, blob `fb9fd458...`).
- No `styles/lp1.csv` or functional code changes have been made during this pass.

## Resume here

The strongest new evidence is now architectural: LP1 and LP2 both ship same-named ordinary tile sheets in pack-local `Pictures/`, with exactly the shared/base sizes but distinct content blobs, while LP1 does not override expansion sheets. Next check LP3/LP4 for the same convention, then continue PCE inventory and semantic slot validation. Do not add LP1 mapping overrides until a deviation is proven.

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
Latest notes commit before LP3/LP4 check: ca3904958749b55685d7cbf1e2d18d7e74b422bc
```
