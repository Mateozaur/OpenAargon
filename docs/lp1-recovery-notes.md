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

`PACK.INI` previously showed `SelectSkillDll=LevelPackOne`; reverse engineering of `RELEASE_LevelPackOne.dll` established that it implements LP1 skill/level-selection UI, not token-to-sprite mapping. `RELEASE_ItemMap.dll::MAP::SelectTile(CPosition)` and `RELEASE_TileBlitter.dll` are also not mapping sources.

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

Its root contains exactly the relevant top-level entries `Levels/`, `PACK.INI`, `Pictures/`, and `Sounds/`.

Most importantly, LP1's own `Pictures/` directory contains pack-local files with the ordinary logical tile-sheet names:

```text
Aargon Deluxe Collectors Edition 1.4.CE/Level Packs/Level Pack 1/Pictures/black_tiles.bmp
  blob: 768d05581ab0a2a39beb9e29c492e869d07eb2fe
  size: 921654 bytes

Aargon Deluxe Collectors Edition 1.4.CE/Level Packs/Level Pack 1/Pictures/black_tiles_small.bmp
  blob: e27ac267529a3f56020508c07775414f114a4032
  size: 57654 bytes
```

This directly confirms the earlier hypothesis that LP1 supplies its own ordinary `black_tiles.bmp` / `black_tiles_small.bmp` under the same filenames used by base-piece PCE definitions. It is strong structural evidence for pack-local graphic replacement while retaining shared PCE definitions. It is **not yet sufficient by itself** to prove that every tile slot keeps the same semantic index; resource lookup behavior and/or slot correspondence still needs validation.

## Current open questions

1. What exactly does LP1 `PACK.INI` configure about picture/resource lookup?
2. What are all `TILESFILE` / `SmallTILESFILE` values across `Pieces/*.pce` (base vs exp1/exp2)?
3. Where are the root/base `black_tiles*.bmp` sheets and what are their blobs/sizes relative to LP1?
4. Does the original loader resolve `TILESFILE=black_tiles.bmp` against the active pack's `Pictures/` before the shared `Pictures/` directory?
5. Do LP1 `black_tiles.bmp` tile slots preserve the semantic ordering expected by shared PCE picture indices?
6. Does OpenAargon's `lp1.png` correspond directly to LP1's pack-local `black_tiles.bmp`, with ordering preserved?

## Next validation steps

1. Read LP1 `PACK.INI` exactly.
2. Inventory root/base and all pack-specific `black_tiles*.bmp` files with exact paths/blobs/sizes.
3. Inventory `Pieces/*.pce` `TILESFILE` / `SmallTILESFILE` declarations.
4. Trace original resource/search-path behavior from configuration/code/assets.
5. Correlate LP1 BMP with OpenAargon `lp1.png`; only if provenance is insufficient, compare cells on the 20x15 grid against PCE indices.
6. Add `styles/lp1.csv` rows only for verified deviations. If slot semantics are preserved, the correct result may be no additional LP1 overrides.

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
- No `styles/lp1.csv` or functional code changes have been made during this pass.
- Next action: fetch and read LP1 `PACK.INI`, then record its exact fields before tracing resource lookup.

## Resume here

Read LP1 `PACK.INI` next. Then inventory the base/root `black_tiles*.bmp` sheets and shared PCE `TILESFILE` declarations. The newly confirmed fact to preserve is that LP1 has pack-local `Pictures/black_tiles.bmp` and `Pictures/black_tiles_small.bmp` under the ordinary filenames, so the investigation should now focus on proving lookup precedence and index preservation rather than asking whether such LP1 sheets exist.

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
Latest live-note checkpoint commit before next action: 3ae4ec89451ca79d1435754036735c4ea90b023a
```
