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

## TILESFILE investigation checkpoint (2026-10-18)

### Confirmed

- The `linux-modernization` branch **does track the original Collector's Edition asset tree** at `Aargon Deluxe Collectors Edition 1.4.CE/`. The repository root also contains `Aargon Deluxe Collectors Edition 1.4.CE.zip`. Previous notes claiming that the `.pce` / `black_tiles*.bmp` corpus was external or unavailable in the repository were incorrect and must not be used as a premise in future work.
- GitHub code search not returning `TILESFILE` is therefore only an indexing/search limitation; it is not evidence that the files are absent. Future investigation should traverse the tracked Collector's Edition directory directly.
- Commit `2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8` added `base.png`, `lp1.png`, `lp2.png`, `lp3.png`, `lp4.png`, `classic.png`, `hotter.png`, `pocket.png`, `exp1.png`, and `exp2.png` together with the initial Collector's Edition legacy renderer.
- The commit records the atlas blobs but contains no documented conversion script, source-BMP path, or provenance metadata tying `base.png`/`lp1.png` to particular `black_tiles*.bmp` files. Git history at the introduction commit therefore does **not** by itself prove index preservation.
- GitHub exposes the repository PNG blobs, but pixel/cell comparison remains a fallback validation technique, not a substitute for tracing the original `TILESFILE` resource selection when the tracked original assets can answer it directly.

### Still unresolved

- Full inventory of `TILESFILE` / `SmallTILESFILE` across `Aargon Deluxe Collectors Edition 1.4.CE/Pieces/*.pce`. Only previously inspected examples such as `brick.pce` and `selector.pce` are currently established.
- Whether LP1 supplies/replaces a physical `black_tiles.bmp` under the same logical name, and how the original game resolves that filename when LP1 is active.
- Exact source files or conversion process used to produce OpenAargon's `base.png` and `lp1.png`.
- Whether ordinary `atlas=base` picture indices preserve semantic identity in LP1.

### Next best evidence

1. Directly traverse the tracked `Aargon Deluxe Collectors Edition 1.4.CE/` directory. Inventory every `Pieces/*.pce` `TILESFILE` / `SmallTILESFILE` value and locate every `black_tiles*.bmp` file and its directory.
2. Inspect LP1 `PACK.INI` plus any resource/search-path configuration in the tracked original files to determine whether `black_tiles.bmp` is resolved or replaced pack-locally while the shared `.pce` definitions remain unchanged.
3. Correlate the physical BMP files with OpenAargon's `base.png` and `lp1.png` where possible (dimensions, content, conversion history/provenance).
4. If original-resource tracing is still insufficient, compare `base.png` and `lp1.png` on their 20x15 cell grid and cross-check changed/same slots against authoritative PCE picture indices. Semantic slot identity matters; pixel equality is not required.

## Resume checkpoint — 2026-10-18

Resumed on branch `linux-modernization` from HEAD `b74ac775a35251dcfcac8ed9f52c4fde81558f9f`.

No functional code or mapping changes have been made in this resumed pass yet. The Selector and LP1 skill-DLL branches of the investigation remain parked. The immediate work sequence is unchanged: inventory every tracked `Pieces/*.pce` `TILESFILE` / `SmallTILESFILE` value, locate all `black_tiles*.bmp` variants, inspect LP1 `PACK.INI` and its resource lookup behavior, then use that evidence to decide whether any verified `atlas=base` slots require LP1 overrides.

## Next validation steps

1. Inventory `TILESFILE` / `SmallTILESFILE` values in tracked `Aargon Deluxe Collectors Edition 1.4.CE/Pieces/*.pce` files to separate ordinary/base pieces from `exp1` / `exp2` pieces.
2. Locate all original physical tile sheets (`black_tiles*.bmp`) and pack/style-specific variants in the tracked Collector's Edition tree.
3. Inspect LP1 `PACK.INI` and related configuration/code to establish the original resource lookup rules for `TILESFILE=black_tiles.bmp`.
4. Determine whether `base.png` and `lp1.png` correspond directly to particular original BMP sheets and whether tile order was preserved.
5. If provenance is insufficient, compare `base.png` and `lp1.png` cell-by-cell using their 20x15 grid and cross-check candidate cells against known PCE indices.
6. Record per-piece/index evidence. Add rows to `styles/lp1.csv` only for verified LP1 deviations; leave preserved mappings to the normal fallback.
7. Keep this file updated whenever a new class of mapping is proven or disproven.

## Existing relevant commits

```text
056778dc07aa42ea0d5b5bbe56364a03e74917f1  document Selector/exp1 in lp1.csv
6b1c90915b550a5d86babf5e01ab2a98eceb8c94  document Selector evidence in styles README
b21842799933df839472c8dd81fae84adc0c824e  create initial recovery notes
78586c5aef6c8d05d5560ce92fe3258bd3edc6ba  link recovery notes from root README
```

The Collector's Edition legacy-rendering support itself was introduced by commit `2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8` (`Add Collector's Edition legacy rendering support`).

## Resume here

Do **not** spend more time on Selector or the LP1 skill DLL first. Resume by directly traversing the tracked `Aargon Deluxe Collectors Edition 1.4.CE/` asset tree and tracing the ordinary `black_tiles` atlas family / `TILESFILE` values. Prove whether `atlas=base` indices retain their meaning in LP1. Only verified deviations should become `lp1.csv` rows.

Current checkpoint: the earlier claim that the original Collector's Edition asset tree was unavailable was wrong. Both `Aargon Deluxe Collectors Edition 1.4.CE/` and `Aargon Deluxe Collectors Edition 1.4.CE.zip` are present in the repository root on `linux-modernization`. The highest-value next action is therefore a direct inventory of `Pieces/*.pce`, `black_tiles*.bmp`, LP1 `PACK.INI`, and the resource lookup relationship between them.

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
```

## New-chat hand-off

If the current ChatGPT conversation hits its context-length limit, do not restart the investigation from scratch. Open a new chat and use this note as the source of truth for the current state.

Suggested prompt:

```text
Continue the OpenAargon LP1 legacy-rendering investigation in repository
Mateozaur/OpenAargon, branch linux-modernization.

First read docs/lp1-recovery-notes.md and treat it as the hand-off state.
Do not redo the Selector investigation: Selector (I / token 73) is already
proven to use the shared exp1 atlas and must not get LP1 overrides.

Important correction: the branch directly contains the original asset tree
Aargon Deluxe Collectors Edition 1.4.CE/ (and its ZIP at repository root).
Do not rely on GitHub code search to decide whether these assets exist.

Resume by directly inventorying Pieces/*.pce TILESFILE/SmallTILESFILE values,
all black_tiles*.bmp files, and LP1 PACK.INI/resource lookup behavior. Use that
evidence to verify whether mappings currently using atlas=base preserve their
tile-index semantics in LP1. Only add rows to styles/lp1.csv for deviations
supported by concrete evidence. Keep docs/lp1-recovery-notes.md updated with
new confirmed findings.
```

The chat/UI warning about conversation length is not itself a project-state change; this section exists only so work can be resumed safely if a new conversation becomes necessary.

## Investigation action log — 2026-10-18

- Restarted the investigation on `linux-modernization` and read this recovery note before touching mappings or code.
- Confirmed the working repository is `Mateozaur/OpenAargon`; an initial probe against the guessed owner `OpenAargon/OpenAargon` returned 404 and was discarded without changing project state.
- Requested the tracked Collector's Edition root through the GitHub contents API.
- Read branch metadata for `linux-modernization` and obtained recursive tree data for checkpoint commit `5c39b456e2a2b0b77ac255504ffbb82d849b42bf`.
- Searched that recursive tree for `black_tiles` paths.
- Searched the same tree for `PACK.INI` paths.
- Searched the same tree for `Pieces/` paths to establish the scope of the `.pce` inventory.
- The exact inventory conclusions are intentionally not recorded yet because the next step is to reproduce the traversal locally with deterministic `find`/`rg` commands and record their exact output rather than infer from truncated connector search output.
- Next action: obtain a local checkout of `linux-modernization`, then run exact filesystem inventories for `black_tiles*.bmp`, `PACK.INI`, and `Pieces/*.pce` `TILESFILE` / `SmallTILESFILE` declarations.
