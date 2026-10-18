# LP1 legacy rendering recovery notes

_Last updated: 2026-10-18_

Hand-off / live recovery state for the Collector's Edition Level Pack 1 (LP1) legacy-rendering investigation.

## Safety rule

Do **not** add rows to `src/main/resources/legacy/styles/lp1.csv` from visual guesses, equal atlas dimensions, or unvalidated binary hypotheses. An override must be supported by concrete original-asset evidence or a validated index-preservation method.

## Confirmed rendering model

- `src/main/resources/legacy/render-map.csv` is the shared mapping; style CSVs such as `styles/lp1.csv` are sparse overrides.
- Generic `atlas=base` is rendered from the currently selected legacy style atlas. `atlas=exp1` and `atlas=exp2` remain shared expansion atlases.
- `LegacyTileSet.BASE` and `LegacyTileSet.LEVEL_PACK_1` are both 20x15; equal dimensions alone do not prove semantic index equality.
- Original `Pieces/*.pce` picture tables are authoritative sources for piece render indices. `brick.pce` was previously checked against `render-map.csv`.
- The LP1 pack directory has no alternate `Pieces/*.pce` corpus. The open question is whether its own ordinary tile sheet preserves the shared PCE index layout.

## Resolved branches — do not redo first

### Selector (`I`, token 73)

`Pieces/selector.pce` explicitly uses:

```ini
TILESFILE=black_tiles_exp1.bmp
SmallTILESFILE=black_tiles_small_exp1.bmp
TOKEN  = I
```

Its picture values/offsets reproduce the existing Selector rows in `render-map.csv` under `atlas=exp1`. Selector therefore stays on shared `exp1` and must not get LP1 overrides.

### LP1 skill DLL / downstream DLLs

`PACK.INI` contains `SelectSkillDll=LevelPackOne`; reverse engineering of `RELEASE_LevelPackOne.dll` established that it implements LP1 skill/level-selection UI, not token-to-sprite mapping. `RELEASE_ItemMap.dll::MAP::SelectTile(CPosition)` and `RELEASE_TileBlitter.dll` are also not mapping sources.

## Atlas provenance checkpoint

Commit `2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8` introduced Collector's Edition legacy rendering and `base.png`, `lp1.png`, `lp2.png`, `lp3.png`, `lp4.png`, `classic.png`, `hotter.png`, `pocket.png`, `exp1.png`, and `exp2.png`, but did not document a conversion script/source-BMP provenance.

Known blobs:

```text
base.png  e1e903a202b7d8c301f46cfa12291abe4a383ae4  ~320631 bytes
lp1.png   87ab0fe3c09e2e26dca009d21e50791cfa1e3570  ~340425 bytes
```

## New confirmed LP1 asset evidence — 2026-10-18

The tracked original Collector's Edition tree is present directly in the repository under `Aargon Deluxe Collectors Edition 1.4.CE/` (plus the ZIP at repository root).

The LP1 pack is:

```text
Aargon Deluxe Collectors Edition 1.4.CE/Level Packs/Level Pack 1/
```

Its root contains `Levels/`, `PACK.INI`, `Pictures/`, and `Sounds/`.

LP1's own `Pictures/` directory contains pack-local files with the ordinary logical tile-sheet names:

```text
Aargon Deluxe Collectors Edition 1.4.CE/Level Packs/Level Pack 1/Pictures/black_tiles.bmp
  blob: 768d05581ab0a2a39beb9e29c492e869d07eb2fe
  size: 921654 bytes

Aargon Deluxe Collectors Edition 1.4.CE/Level Packs/Level Pack 1/Pictures/black_tiles_small.bmp
  blob: e27ac267529a3f56020508c07775414f114a4032
  size: 57654 bytes
```

An explicit search of the LP1 `Pictures/` inventory for `black_tiles_exp` returned no matches. LP1 therefore overrides the ordinary pair only; it does not supply pack-local `exp1`/`exp2` tile sheets. This supports the renderer model in which the current style supplies `base`, while `exp1` and `exp2` remain shared atlases.

The shared/root `Pictures/` directory contains the complete ordinary/expansion tile-sheet family:

```text
black_tiles.bmp                 921654 bytes  2c1cebca0a437c2080352e48425150d3c374a533
black_tiles_exp1.bmp            491576 bytes  adc86dffc884c39574c46bd1f7cbf329378be876
black_tiles_exp2.bmp             24630 bytes  777e69561f964de3d335225b0d244b7c27b4e26b
black_tiles_small.bmp            57654 bytes  f12083c95843dd58ee66f42dd5fe8569732d0a61
black_tiles_small_exp1.bmp       30776 bytes  d6259db4d766c373db8d86c6851a0bbef84ef24d
black_tiles_small_exp2.bmp        1590 bytes  f0df8506a8e4515c6c1ce53ce8dd7b0373bf44d3
```

The LP1 ordinary sheets have **exactly the same byte sizes** as the corresponding shared/base ordinary sheets while having **different blob identities**. This is strong structural evidence that LP1 replaces visual content while retaining the same BMP dimensions/format footprint and grid capacity. It is not yet proof that every tile slot keeps the same semantic meaning.

### LP1 PACK.INI exact content

```ini
[Params]
Text = Try these levels in one of our secret deep space installations.
Position = 7000
Edit = false
SelectSkillDll=LevelPackOne
IsEmpty=false
```

`PACK.INI` contains no tile filename, `Pictures` path, atlas name, or resource-search-path setting. Therefore the LP1 `black_tiles.bmp` selection is not configured explicitly in `PACK.INI`; any shadowing must come from general active-pack resource lookup.

## PCE inventory checkpoint

The authoritative shared `Pieces/` directory listing contains **29** `.pce` files:

```text
blackhole.pce blank.pce brick.pce coin.pce complex.pce convert.pce
double.pce doubleslit.pce explosion.pce filter.pce fuelcell.pce lbender.pce
mine.pce mirror.pce oneway.pce polarizer.pce prism.pce prismirror.pce
refractor.pce selector.pce slime.pce source.pce spaceship.pce splicer.pce
splitter.pce threeway.pce tnt.pce water.pce wood.pce
```

The earlier spoken count of 27 was wrong; it has been corrected to 29 from the enumerated list.

A repository code-search query for `TILESFILE` scoped to this directory returned zero results. This is an indexing/search limitation and is not evidence that declarations are absent. Files are being read directly instead.

`blank.pce` contains the PCE format documentation and establishes these defaults when a piece omits the flags:

```text
TilesFile default:      black_tiles.bmp
SmallTilesFile default: black_tiles_small.bmp
```

Direct inventory progress:

```text
blackhole.pce  explicit exp1: black_tiles_exp1.bmp / black_tiles_small_exp1.bmp
blank.pce      no explicit override; documents ordinary defaults above
brick.pce      no explicit TilesFile/SmallTilesFile -> ordinary defaults
```

Thus omission of `TilesFile` is positive evidence for use of the ordinary `black_tiles.bmp`, not an unknown value.

## Current open questions

1. What general loader/search-path code makes pack-local `Pictures/black_tiles.bmp` shadow the shared/base sheet?
2. Complete the remaining 26 direct `.pce` reads and classify each as ordinary/default, exp1, or exp2.
3. Do LP1 `black_tiles.bmp` tile slots preserve the semantic ordering expected by shared PCE picture indices?
4. Does OpenAargon's `lp1.png` correspond directly to LP1's pack-local `black_tiles.bmp`, with ordering preserved?

## Next validation steps

1. Continue direct `.pce` reads in batches and record each batch before proceeding.
2. Trace original resource/search-path behavior from configuration/code/assets; `PACK.INI` itself has now been ruled out as the place where tile lookup is configured.
3. Correlate LP1 BMP with OpenAargon `lp1.png`; only if provenance is insufficient, compare cells on the 20x15 grid against PCE indices.
4. Add `styles/lp1.csv` rows only for verified deviations. If slot semantics are preserved, the correct result may be no additional LP1 overrides.

## Existing relevant commits

```text
056778dc07aa42ea0d5b5bbe56364a03e74917f1  document Selector/exp1 in lp1.csv
6b1c90915b550a5d86babf5e01ab2a98eceb8c94  document Selector evidence in styles README
b21842799933df839472c8dd81fae84adc0c824e  create initial recovery notes
78586c5aef6c8d05d5560ce92fe3258bd3edc6ba  link recovery notes from root README
2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8  add Collector's Edition legacy rendering support
```

## Investigation action log — 2026-10-18

- Resumed `linux-modernization` and read this note before touching mappings/code.
- Corrected an initial guessed repository owner: the working repo is `Mateozaur/OpenAargon`; the `OpenAargon/OpenAargon` probe returned 404 and changed nothing.
- Requested the Collector's Edition root via GitHub contents API.
- Read `linux-modernization` branch metadata and recursively inspected the tree at checkpoint commit `5c39b456e2a2b0b77ac255504ffbb82d849b42bf`.
- Searched that tree for `black_tiles`, `PACK.INI`, and `Pieces/` paths.
- Attempted a shallow local clone into `/mnt/data/OpenAargon` for deterministic `find`/`rg`; it failed before checkout because the execution environment could not resolve `github.com`. No project files were modified by that attempt.
- Switched back to the connected GitHub API.
- Enumerated CE root, `Level Packs/`, LP1 root, shared `Pieces/`, LP1 `Pictures/`, and shared/root `Pictures/`.
- Confirmed LP1 contains pack-local ordinary `black_tiles.bmp` / `black_tiles_small.bmp`, equal in byte size but different in blob identity from the shared ordinary pair.
- Confirmed LP1 contains no `black_tiles_exp*` files; expansion sheets remain shared.
- Fetched LP1 `PACK.INI`; confirmed it contains no picture/tile/search-path configuration.
- Retrieved the authoritative `Pieces/` directory listing. Corrected preliminary count from 27 to 29 after recounting the enumerated names.
- Ran GitHub repository code search for `TILESFILE` scoped to the CE `Pieces` directory; it returned zero results and was classified as a search-index limitation.
- Directly fetched `blackhole.pce`, `blank.pce`, and `brick.pce`.
- Recorded `blackhole.pce` as explicit `exp1`.
- Recorded from `blank.pce` that omitted `TilesFile` / `SmallTilesFile` defaults are `black_tiles.bmp` / `black_tiles_small.bmp`.
- Recorded `brick.pce` as ordinary/default because it omits both file flags.
- No `styles/lp1.csv` or functional code changes have been made during this pass.
- Next action: fetch the next PCE batch (`coin`, `complex`, `convert`, then continue alphabetically) and append classifications.

## Resume here

LP1 definitely has pack-local ordinary tile sheets of the same byte size as base, different content blobs, and no local expansion sheets. `PACK.INI` does not select them. Direct PCE inventory has started and establishes that omitted tile-file flags mean the ordinary pair. Continue alphabetically from `coin.pce`, recording every batch, then trace active-pack lookup and semantic slot preservation.

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
```
