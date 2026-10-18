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

The shared/root `Pictures/` directory contains the complete ordinary/expansion tile-sheet family:

```text
black_tiles.bmp
  blob: 2c1cebca0a437c2080352e48425150d3c374a533
  size: 921654 bytes
black_tiles_exp1.bmp
  blob: adc86dffc884c39574c46bd1f7cbf329378be876
  size: 491576 bytes
black_tiles_exp2.bmp
  blob: 777e69561f964de3d335225b0d244b7c27b4e26b
  size: 24630 bytes
black_tiles_small.bmp
  blob: f12083c95843dd58ee66f42dd5fe8569732d0a61
  size: 57654 bytes
black_tiles_small_exp1.bmp
  blob: d6259db4d766c373db8d86c6851a0bbef84ef24d
  size: 30776 bytes
black_tiles_small_exp2.bmp
  blob: f0df8506a8e4515c6c1ce53ce8dd7b0373bf44d3
  size: 1590 bytes
```

The LP1 ordinary sheets have **exactly the same byte sizes** as the corresponding shared/base ordinary sheets while having **different blob identities**:

```text
base black_tiles.bmp      921654 bytes  2c1cebca...
LP1  black_tiles.bmp      921654 bytes  768d0558...
base black_tiles_small.bmp 57654 bytes  f12083c9...
LP1  black_tiles_small.bmp 57654 bytes  e27ac267...
```

This is strong structural evidence that LP1 replaces the visual content of the ordinary sheet while retaining the same BMP dimensions/format footprint and therefore the same grid capacity. It still does **not** by itself prove that every tile slot keeps the same semantic meaning; that needs lookup/slot validation.

### LP1 PACK.INI exact content

```ini
[Params]
Text = Try these levels in one of our secret deep space installations.
Position = 7000
Edit = false
SelectSkillDll=LevelPackOne
IsEmpty=false
```

`PACK.INI` contains no tile filename, `Pictures` path, atlas name, or resource-search-path setting. Therefore the LP1 `black_tiles.bmp` selection is **not configured explicitly in PACK.INI**. If the active pack's `Pictures/black_tiles.bmp` shadows the shared/base file, that behavior must come from general resource lookup / active-pack path logic elsewhere.

## Current open questions

1. What general loader/search-path code makes pack-local `Pictures/black_tiles.bmp` shadow the shared/base sheet?
2. What are all `TILESFILE` / `SmallTILESFILE` values across `Pieces/*.pce` (base vs exp1/exp2)?
3. Do LP1 `black_tiles.bmp` tile slots preserve the semantic ordering expected by shared PCE picture indices?
4. Does OpenAargon's `lp1.png` correspond directly to LP1's pack-local `black_tiles.bmp`, with ordering preserved?

## Next validation steps

1. Inventory `Pieces/*.pce` `TILESFILE` / `SmallTILESFILE` declarations.
2. Check explicitly whether LP1 `Pictures/` contains any pack-local `black_tiles_exp1*` / `black_tiles_exp2*`; absence would support shared expansion-atlas behavior.
3. Trace original resource/search-path behavior from configuration/code/assets; `PACK.INI` itself has now been ruled out as the place where tile lookup is configured.
4. Correlate LP1 BMP with OpenAargon `lp1.png`; only if provenance is insufficient, compare cells on the 20x15 grid against PCE indices.
5. Add `styles/lp1.csv` rows only for verified deviations. If slot semantics are preserved, the correct result may be no additional LP1 overrides.

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
- Enumerated `Aargon Deluxe Collectors Edition 1.4.CE/` and confirmed `Level Packs/` and `Pieces/` are tracked directories.
- Enumerated `Level Packs/` and confirmed `Level Pack 1/` is a distinct pack directory.
- Enumerated `Level Packs/Level Pack 1/` and confirmed it contains `Levels/`, `PACK.INI` (155 bytes; blob `f4c248096b11a178b75f473efe59379246cf0a31`), `Pictures/`, and `Sounds/`.
- Enumerated `Pieces/` to establish the shared `.pce` corpus; full per-file `TILESFILE` inventory remains pending.
- Enumerated LP1 `Pictures/` and specifically searched its returned directory data for `black_tiles`.
- Confirmed LP1 contains its own `Pictures/black_tiles.bmp` (921654 bytes, blob `768d05581ab0a2a39beb9e29c492e869d07eb2fe`) and `Pictures/black_tiles_small.bmp` (57654 bytes, blob `e27ac267529a3f56020508c07775414f114a4032`).
- Fetched LP1 `PACK.INI` and recorded its exact five parameters. Confirmed it contains no picture/tile/search-path configuration; it only includes descriptive/settings fields and `SelectSkillDll=LevelPackOne`.
- Fetched the shared/root `Aargon Deluxe Collectors Edition 1.4.CE/Pictures/` directory and filtered its returned inventory for `black_tiles`.
- Recorded all six shared tile-sheet files (`ordinary`, `exp1`, `exp2`, and their `small` variants) with exact blobs and sizes.
- Compared shared ordinary sheets with LP1 ordinary sheets: both full-size files are 921654 bytes and both small files are 57654 bytes, but each LP1 file has a different blob from its shared counterpart. Recorded this as structural evidence of same-shape content replacement, not yet semantic index proof.
- No `styles/lp1.csv` or functional code changes have been made during this pass.
- Next action: explicitly test LP1 `Pictures/` for expansion-sheet names, then begin the shared PCE `TILESFILE` inventory.

## Resume here

LP1 definitely has pack-local ordinary tile sheets, and those sheets match the shared ordinary sheets in byte size while differing in content blob. `PACK.INI` does not explicitly select them. Resume by checking whether LP1 overrides expansion sheets, then inventory shared PCE `TILESFILE` declarations and trace the general active-pack lookup path. The main remaining proof burden is semantic slot/index preservation, not sheet existence or dimensions.

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
```
