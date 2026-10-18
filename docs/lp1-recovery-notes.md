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

The repeated directory and filename convention across LP1-LP4 is very strong architectural evidence that active Level Packs intentionally supply their own ordinary `black_tiles` resources. LP3 is a caveat: both LP3 files are exactly two bytes larger, so do not claim byte-identical BMP format from size alone.

### BMP-header inspection attempt

Attempted to fetch shared/base `black_tiles.bmp` as a binary blob. The GitHub connector failed with `UnicodeDecodeError` because it tried to decode the blob as UTF-8. No bytes were returned. BMP header fields and LP3's +2-byte cause remain unconfirmed.

## LP1 PACK.INI

```ini
[Params]
Text = Try these levels in one of our secret deep space installations.
Position = 7000
Edit = false
SelectSkillDll=LevelPackOne
IsEmpty=false
```

No tile filename, `Pictures` path, atlas name, or resource-search-path field exists. Pack-local ordinary-sheet selection is not explicitly configured in `PACK.INI`.

## LP1 level-format and shared-PCE evidence

`Level Packs/Level Pack 1/Levels/` contains `Skill1`-`Skill4`. `Skill1/Level_001.map` (`LANDING LIGHTS`) is a plain token-oriented map plus `MAPTEXT`, `SCRIPT`, and copyright fields. It contains tokens including `O` and `S` but no tile-sheet filename, atlas name, picture-index table, or pack-local remapping table.

Representative token correlation against the **shared** `Pieces/*.pce` corpus:

```text
LP1 map token O -> Pieces/coin.pce
  TOKEN = O
  no TilesFile / SmallTilesFile override
  therefore uses documented ordinary defaults black_tiles.bmp / black_tiles_small.bmp

LP1 map token S -> Pieces/source.pce
  TOKEN = S
  no TilesFile / SmallTilesFile override
  therefore uses documented ordinary defaults black_tiles.bmp / black_tiles_small.bmp

Pieces/spaceship.pce (contrast case)
  TOKEN = p
  TilesFile = black_tiles_exp1.bmp
  SmallTilesFile = black_tiles_small_exp1.bmp
```

`brick.pce` has `TOKEN = #`, no tile-file override, and therefore uses ordinary defaults.

The combined original-game data strongly supports this resource/index contract:

1. LP1 maps store shared piece tokens rather than atlas/index remaps.
2. Shared PCE definitions convert tokens/state into picture indices and choose ordinary vs expansion logical tile-sheet names.
3. LP1 supplies a same-named ordinary `black_tiles.bmp`, while expansion sheets remain shared.
4. Therefore LP1's ordinary sheet is intended to be consumed at the picture indices emitted by the shared PCE definitions.

This is substantially stronger evidence for semantic index preservation than equal dimensions alone. A direct loader-code trace or binary image comparison would still be useful as an independent confirmation, but there is no evidence supporting arbitrary LP1 index remapping or sparse overrides.

## OpenAargon alignment check

Read `src/main/resources/legacy/styles/lp1.csv`. It contains only the header plus documentation comments and **no override rows**. Its comments explicitly state that missing entries fall back to `legacy/render-map.csv` and that only entries verified against original LP1 assets should be added.

Read `src/main/resources/legacy/render-map.csv` and checked representative mappings used by the real LP1 sample level:

```text
TOKEN O / code 79 -> coin   -> atlas=base
TOKEN S / code 83 -> source -> atlas=base
```

The Coin rows use the same animation start indices present in `coin.pce` (for example 280, 90, 71, 110, 62, 129, ...). The Source rows use the same animation starts present in `source.pce` (20, 32, 12, 16, 24, 136, 28, 36). The same render map also keeps explicit expansion pieces such as Spaceship on `atlas=exp1`.

Therefore the current OpenAargon behavior is aligned with the original resource contract established above: LP1-specific style selection can replace the `base` atlas while shared render-map indices remain unchanged; explicit `exp1`/`exp2` mappings do not get redirected to LP1. No LP1-specific CSV rows are required to achieve that behavior for the verified examples.

## PCE inventory checkpoint

Authoritative `Pieces/` listing contains 29 `.pce` files. `blank.pce` documents defaults:

```text
TilesFile default:      black_tiles.bmp
SmallTilesFile default: black_tiles_small.bmp
```

Direct reads/classifications so far:

```text
blackhole.pce   explicit exp1
blank.pce       format/default documentation
brick.pce       ordinary/default; TOKEN #
coin.pce        ordinary/default; TOKEN O; directly used by LP1 Level_001
complex.pce     explicit exp1
convert.pce     ordinary/default
double.pce      ordinary/default
doubleslit.pce  ordinary/default
explosion.pce   animation list only; no [FLAGS], intentionally unclassified
source.pce      ordinary/default; TOKEN S; directly used by LP1 Level_001
spaceship.pce   explicit exp1; TOKEN p; contrast case
```

## Current conclusion

For the question that motivated this pass — whether shared `atlas=base` picture indices should generally be remapped for LP1 — the evidence currently says **no**. The original asset structure and real LP1 level data support index preservation with a pack-local replacement of the ordinary `black_tiles.bmp`. OpenAargon's existing fallback model already implements that structure.

Do not add speculative rows to `styles/lp1.csv`. A future LP1 override should require evidence of a **specific semantic slot deviation**, not merely a visual difference between `base.png` and `lp1.png`.

Independent confirmation still worth pursuing if binary access becomes available:
- compare original LP1 `black_tiles.bmp` directly with OpenAargon's `lp1.png` for coordinate preservation;
- inspect BMP headers, including LP3's +2-byte variant;
- locate original active-pack resource lookup code/strings if a readable binary/source path becomes available.

## Action log — 2026-10-18

- Resumed `linux-modernization`; read and continuously updated this note before/after investigation batches.
- Corrected initial bad repo owner probe; correct repo is `Mateozaur/OpenAargon`.
- Inventoried CE assets, LP1 `PACK.INI`, shared/pack-local tile sheets, sibling Level Packs, PCE directory, and LP1 levels.
- Attempted shallow local clone; failed because execution environment could not resolve `github.com`; no project state changed.
- Confirmed LP1-LP4 all carry pack-local same-named ordinary `black_tiles` pairs; LP3 pair is +2 bytes.
- Confirmed LP1 has no local `black_tiles_exp*`; expansion atlases remain shared.
- Direct-read PCE batches and documented ordinary defaults / exp1 exceptions.
- Indexed searches for `TILESFILE`, `Level Packs`, and `LevelPack` did not expose loader source; treated as indexing/source-availability limits.
- Attempted binary BMP blob read for header inspection; failed with `UnicodeDecodeError`; logged as connector limitation.
- Enumerated LP1 `Levels/`, `Skill1/`, and fetched `Skill1/Level_001.map`; confirmed token-map format without atlas/index/remap metadata.
- Re-read `brick.pce`, `source.pce`, `coin.pce`, and `spaceship.pce` to correlate actual LP1 map tokens and contrast ordinary/default vs explicit exp1 selection.
- Confirmed `Level_001.map` token `O` resolves through shared `coin.pce` ordinary/default and token `S` through shared `source.pce` ordinary/default; confirmed `spaceship.pce` token `p` explicitly selects exp1.
- Fetched current `styles/lp1.csv`; confirmed it contains no override rows and intentionally relies on fallback to the shared render map.
- Fetched `render-map.csv`; confirmed Coin (`79`) and Source (`83`) are mapped to `atlas=base` using their PCE-derived indices, while expansion pieces remain on explicit expansion atlases.
- Searched the fetched render-map response for Source rows to verify the full Source animation-start sequence and adjacent Spaceship `exp1` mapping.
- Recorded the conclusion that current OpenAargon fallback behavior matches the verified original LP1 resource/index contract; no new LP1 mapping rows are justified by this evidence.
- No `styles/lp1.csv` or functional code changes have been made during this pass.

## Resume here

The LP1 base-index question is now provisionally resolved in favor of **index preservation**: real LP1 maps use shared PCE token definitions; ordinary PCE definitions use logical `black_tiles.bmp`; every original Level Pack supplies a same-named pack-local ordinary sheet; LP1 supplies no expansion replacement or alternate PCE/remap table; and OpenAargon's current `atlas=base` fallback preserves those PCE-derived indices while switching the selected base atlas.

Do not add LP1 overrides unless a specific index is proven semantically different. If continuing this investigation, prioritize independent binary/provenance confirmation rather than expanding speculative mappings.

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
```
