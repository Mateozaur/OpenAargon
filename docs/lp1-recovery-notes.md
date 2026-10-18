# LP1 legacy rendering recovery notes

_Last updated: 2026-10-18_

This file is the hand-off / recovery point for the Collector's Edition legacy-rendering investigation for Level Pack 1 (LP1).

## Safety rule

Do **not** add rows to `src/main/resources/legacy/styles/lp1.csv` from visual guesses, matching atlas dimensions, or unvalidated binary hypotheses. A style override must be supported by concrete evidence from the original assets or by a validated index-preservation method.

## Confirmed rendering model

OpenAargon loads the general mapping from `src/main/resources/legacy/render-map.csv`. Style CSVs such as `styles/lp1.csv` are sparse overrides. Entries with generic `atlas=base` use the currently selected legacy style atlas; `atlas=exp1` and `atlas=exp2` remain shared expansion atlases.

`LegacyTileSet.BASE` and `LegacyTileSet.LEVEL_PACK_1` are both 20x15, but equal dimensions alone do **not** prove that the same tile number represents the same sprite.

Classic is a separate visual style and must not be treated as synonymous with Base.

## Where tile indices actually come from

The original `.pce` piece-definition files are the primary source of the render indices used by `render-map.csv`.

Example: `Aargon Deluxe Collectors Edition 1.4.CE/Pieces/brick.pce` contains a `[PICTURES(COLOR,ROTATION)]` table whose values match the Brick rows in `render-map.csv`. This establishes that the PCE picture tables, including their arithmetic offsets where present, are authoritative input for the legacy mapping.

The LP1 directory does not contain alternate `.pce` piece definitions, so there is currently no evidence for a separate LP1 token-to-picture table. The remaining question is whether LP1's ordinary tile sheet preserves the picture-index layout expected by the shared PCE definitions.

## Selector (`I`, token code 73) is resolved

Selector is **not** an LP1-specific mapping problem.

Original file:

```text
Aargon Deluxe Collectors Edition 1.4.CE/Pieces/selector.pce
```

Its flags explicitly select:

```ini
TILESFILE=black_tiles_exp1.bmp
SmallTILESFILE=black_tiles_small_exp1.bmp
TOKEN  = I
```

Its `[PICTURES(COLOR,ROTATION)]` rows plus their offsets produce exactly the Selector tile values already stored in `render-map.csv` under `atlas=exp1` (for example `24..31 + 36 = 60..67`, `16..23 + 24 = 40..47`, and so on).

Therefore Selector must continue to use the shared `exp1` atlas when LP1 is selected and must **not** receive rows in `styles/lp1.csv`.

## DLL investigation: useful negative result

`PACK.INI` for LP1 contains `SelectSkillDll=LevelPackOne`, and the corresponding binary is `RELEASE_LevelPackOne.dll` (28,672 bytes). Reverse engineering showed that this DLL exports a factory for the LP1 skill/level-selection UI; it is not the source of token-to-sprite indices.

Likewise, `RELEASE_ItemMap.dll::MAP::SelectTile(CPosition)` selects a board cell/state, and `RELEASE_TileBlitter.dll` is a downstream drawing layer. These are not the mapping source. Do not resume the old plan of deriving Selector indices from `RELEASE_LevelPackOne.dll` unless new evidence contradicts the PCE result.

## Current open question

For entries whose shared mapping uses `atlas=base`, determine whether LP1 preserves the same picture-index semantics in `src/main/resources/texture/legacy/lp1.png` or whether specific pieces require sparse LP1 overrides.

Known atlas metadata:

```text
base.png  blob e1e903a202b7d8c301f46cfa12291abe4a383ae4  ~320631 bytes
lp1.png   blob 87ab0fe3c09e2e26dca009d21e50791cfa1e3570  ~340425 bytes
```

Both are represented as 20x15 atlases by `LegacyTileSet`; this is only structural evidence, not proof of index equivalence.

## Next validation steps

1. Inventory `TILESFILE` / `SmallTILESFILE` values in the original `Pieces/*.pce` files to separate ordinary/base pieces from `exp1` / `exp2` pieces.
2. Locate the original physical tile sheets (`black_tiles*.bmp`) and any pack/style-specific variants, or identify exactly how `base.png` and `lp1.png` were generated in commit/history/scripts.
3. If provenance is insufficient, compare `base.png` and `lp1.png` cell-by-cell using their 20x15 grid and cross-check candidate cells against known PCE indices.
4. Record per-piece/index evidence. Add rows to `styles/lp1.csv` only for verified LP1 deviations; leave preserved mappings to the normal fallback.
5. Keep this file updated whenever a new class of mapping is proven or disproven.

## Existing relevant commits

```text
056778dc07aa42ea0d5b5bbe56364a03e74917f1  document Selector/exp1 in lp1.csv
6b1c90915b550a5d86babf5e01ab2a98eceb8c94  document Selector evidence in styles README
b21842799933df839472c8dd81fae84adc0c824e  create initial recovery notes
78586c5aef6c8d05d5560ce92fe3258bd3edc6ba  link recovery notes from root README
```

The Collector's Edition legacy-rendering support itself was introduced by commit `2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8` (`Add Collector's Edition legacy rendering support`).

## Resume here

Do **not** spend more time on Selector or the LP1 skill DLL first. Resume by tracing the ordinary `black_tiles` atlas family / `TILESFILE` values and proving whether `atlas=base` indices retain their meaning in LP1. Only verified deviations should become `lp1.csv` rows.

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
```
