# LP1 legacy rendering recovery notes

_Last updated: 2026-10-18_

Live hand-off state for the Collector's Edition Level Pack 1 (LP1) legacy-rendering investigation.

## Goal

Preserve original Collector's Edition rendering semantics while modernizing OpenAargon's legacy renderer. The question is not whether LP1 looks different, but whether LP1 changes the semantic meaning of shared PCE picture indices. Do not create LP1-specific mappings merely because LP1 has a different atlas.

## Safety rule

Do **not** add rows to `src/main/resources/legacy/styles/lp1.csv` from visual guesses, equal dimensions, index ranges, or unvalidated binary hypotheses. Add an override only for a concrete, verified semantic slot deviation.

## Confirmed rendering model

- `src/main/resources/legacy/render-map.csv` is the shared token/state mapping; style CSVs are sparse overrides.
- Generic `atlas=base` resolves against the currently selected legacy style atlas.
- Explicit `atlas=exp1` / `atlas=exp2` remain shared expansion atlases.
- Original `Pieces/*.pce` picture tables are authoritative for token/state -> picture index and logical tile-sheet selection.
- `blank.pce` documents ordinary defaults as `black_tiles.bmp` / `black_tiles_small.bmp`.
- LP1 has no alternate PCE corpus.
- Real LP1 maps contain shared piece tokens, not atlas/index remap tables.

## Resolved — do not redo first

### Selector (`I`, token 73)

`Pieces/selector.pce` explicitly selects `black_tiles_exp1.bmp` / `black_tiles_small_exp1.bmp`. Its picture values and offsets reproduce existing Selector rows in `render-map.csv` under `atlas=exp1`. Selector stays shared-exp1 and needs no LP1 override.

### DLL dead ends

- LP1 `PACK.INI` specifies `SelectSkillDll=LevelPackOne`.
- `RELEASE_LevelPackOne.dll` is LP1 skill/level-selection UI, not token-to-sprite mapping.
- `RELEASE_ItemMap.dll::MAP::SelectTile(CPosition)` and `RELEASE_TileBlitter.dll` are not mapping sources.

## Atlas provenance

Commit `2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8` (`Add Collector's Edition legacy rendering support`) introduced the legacy renderer plus all of these PNG atlases in one commit:

```text
base.png classic.png exp1.png exp2.png hotter.png
lp1.png lp2.png lp3.png lp4.png pocket.png
```

Its single parent is `98625d829872afc6a3aad6a6c89bef0261781079` (`Normalize border T-junction rendering`). The parent change concerns `PieceBorder.java`; it contains no atlas-generation provenance. The introduction commit's diff defines `LegacyTileSet.LEVEL_PACK_1` as `texture/legacy/lp1.png`, 20x15, but contains no checked-in conversion script or source-BMP-to-PNG remap procedure.

Known PNG blobs:

```text
base.png  e1e903a202b7d8c301f46cfa12291abe4a383ae4
lp1.png   87ab0fe3c09e2e26dca009d21e50791cfa1e3570
```

Repository/index searches for likely conversion provenance (`black_tiles.bmp`, ImageMagick/convert/png/bmp terms) returned no useful indexed results. Commit search also returned no matches even for the known introduction commit/message, so those search negatives are treated as connector/index limitations, not evidence of absence.

## Original CE tile-sheet evidence

Tracked tree: `Aargon Deluxe Collectors Edition 1.4.CE/`.

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
  921654  768d05581ab0a2a39beb9e29c492e869d07eb2fe
Level Packs/Level Pack 1/Pictures/black_tiles_small.bmp
   57654  e27ac267529a3f56020508c07775414f114a4032
```

LP1 contains no `black_tiles_exp*`, so LP1 replaces only the ordinary pair while expansion sheets remain shared.

All four original Level Packs ship same-named pack-local ordinary pairs:

```text
LP1 full  921654  768d05581ab0a2a39beb9e29c492e869d07eb2fe
LP1 small  57654  e27ac267529a3f56020508c07775414f114a4032
LP2 full  921654  1c049d0baf5de562518ac4df894dc2bb5be7c4b4
LP2 small  57654  fb9fd458550f87cc3696e980277813b9bf10e809
LP3 full  921656  a6bb5fe81ddbb399640a051a5459bd66bdbafa0a
LP3 small  57656  f039e588b1af4a0bc4af90ac4a3cd20454889ec6
LP4 full  921654  3f63fff739958dc1473564d083d6dc725c52eacf
LP4 small  57654  97dbb2e35c8eb67c3b0e9f6e7bcd76fc6d08fddc
```

This repeated path/name convention is strong architectural evidence that active Level Packs intentionally supply their own ordinary `black_tiles` resource. LP3's +2-byte files are a known exception; do not generalize all packs as byte-size-identical.

### Binary BMP header confirmation

Earlier direct binary blob access failed with a `UnicodeDecodeError`. A later attempt using `GitHub.fetch_file(..., encoding=base64)` succeeded for the beginning of the binary and allows the BMP header to be decoded directly.

Confirmed LP1 `black_tiles.bmp` header:

```text
signature       BM
file size       921654
pixel offset    54
DIB header      40 bytes
width           640
height          480
planes          1
bits/pixel      24
compression     0 (BI_RGB / uncompressed)
image payload   921600
X pixels/meter  5272
Y pixels/meter  5272
colors used     0
important       0
```

Confirmed shared/base `Pictures/black_tiles.bmp` has the same structural fields and pixel payload dimensions; its X/Y resolution metadata is 5176 instead of LP1's 5272. Thus the earlier arithmetic hypothesis `921654 = 54 + 640*480*3` is now **confirmed** for both base and LP1 full ordinary sheets, rather than merely inferred from size.

The connector supports line-ranged base64 reads, but reconstructing the complete ~922 KB BMP and complete compressed PNG through chat-sized chunks is not practical in this session. Therefore direct pixel-for-pixel LP1 BMP -> `lp1.png` coordinate comparison remains unavailable.

## LP1 PACK.INI

```ini
[Params]
Text = Try these levels in one of our secret deep space installations.
Position = 7000
Edit = false
SelectSkillDll=LevelPackOne
IsEmpty=false
```

It has no tile filename, Pictures path, atlas name, or resource-search-path field. Pack-local sheet lookup must therefore be generic elsewhere, not configured per LP1 in `PACK.INI`.

## Real LP1 map / shared-PCE evidence

`Level Packs/Level Pack 1/Levels/` contains `Skill1` through `Skill4`.

`Skill1/Level_001.map` (`LANDING LIGHTS`) is token-oriented board data plus `MAPTEXT`, `SCRIPT`, and copyright fields. It contains tokens including `O` and `S`, but no atlas filename, picture-index table, or remap table.

Verified token correlations:

```text
LP1 token O -> Pieces/coin.pce
  TOKEN=O
  no TilesFile override -> ordinary black_tiles.bmp

LP1 token S -> Pieces/source.pce
  TOKEN=S
  no TilesFile override -> ordinary black_tiles.bmp

Pieces/brick.pce
  TOKEN=#
  no TilesFile override -> ordinary black_tiles.bmp

Pieces/spaceship.pce contrast
  TOKEN=p
  TilesFile=black_tiles_exp1.bmp
  SmallTilesFile=black_tiles_small_exp1.bmp
```

This establishes the resource/index contract:

1. LP1 maps store shared tokens, not atlas/index remaps.
2. Shared PCE definitions turn token/state into picture indices and select a logical ordinary/expansion sheet.
3. LP1 supplies a same-named pack-local ordinary `black_tiles.bmp`, but no local expansion replacement and no alternate PCE corpus.
4. Therefore the LP1 ordinary sheet is intended to be consumed at the picture indices emitted by the shared PCE definitions.

The binary header confirmation adds independent evidence that base and LP1 ordinary full sheets share the exact 640x480/24-bit/uncompressed storage geometry and payload length. It does not, by itself, prove every pixel coordinate is semantically identical; the PCE/map/resource architecture is the stronger semantic argument.

## OpenAargon alignment

`src/main/resources/legacy/styles/lp1.csv` contains only its header/comments and **no override rows**. Missing entries intentionally fall back to shared `legacy/render-map.csv`.

Representative verified shared mappings:

```text
TOKEN O / code 79 -> coin   -> atlas=base
TOKEN S / code 83 -> source -> atlas=base
TOKEN p / code 112 -> spaceship -> atlas=exp1
```

Coin animation starts match `coin.pce` (including 280, 90, 71, 110, 62, 129, ...). Source starts match `source.pce` (20, 32, 12, 16, 24, 136, 28, 36). Explicit expansion pieces remain on `exp1`.

Thus current OpenAargon behavior matches the verified original contract: when LP1 is active, shared PCE-derived `atlas=base` indices are retained while the selected base atlas becomes LP1; explicit expansion mappings are not redirected.

## PCE inventory checkpoint

Authoritative shared `Pieces/` listing contains 29 `.pce` files. A preliminary spoken count of 27 was corrected after enumeration.

Directly read/classified:

```text
blackhole.pce   explicit exp1
blank.pce       format/default documentation
brick.pce       ordinary/default; TOKEN #
coin.pce        ordinary/default; TOKEN O; used by LP1 Level_001
complex.pce     explicit exp1
convert.pce     ordinary/default; indices 260..267
double.pce      ordinary/default
doubleslit.pce  ordinary/default
explosion.pce   animation-list-only; no [FLAGS], intentionally unclassified
source.pce      ordinary/default; TOKEN S; used by LP1 Level_001
spaceship.pce   explicit exp1; TOKEN p
selector.pce    explicit exp1; TOKEN I; fully resolved
```

`convert.pce` is important because its ordinary/default indices are in the 260s: index magnitude must not be used to infer atlas family.

The inventory remains intentionally incomplete. If independent provenance/pixel confirmation remains unavailable and more assurance is needed, the next fallback is to sample additional real LP1 map tokens and read their corresponding shared PCEs; exhaustive classification of all 29 files is secondary.

## Current conclusion

For the motivating question — whether shared `atlas=base` picture indices should generally be remapped for LP1 — current evidence says **no**.

The original asset/resource architecture, real LP1 map tokens, shared PCE definitions, repeated pack-local ordinary-sheet convention, OpenAargon mapping alignment, and now directly decoded BMP headers all support **index preservation with pack-local replacement of the ordinary sheet**.

Do not add rows to `styles/lp1.csv` unless a specific semantic index deviation is proven.

Useful remaining independent checks, in priority order:

1. Direct pixel/coordinate comparison of original LP1 `black_tiles.bmp` against OpenAargon's `lp1.png` if complete binary transfer becomes practical.
2. Locate original active-pack resource lookup code/strings if a readable executable/source path becomes available.
3. Decode LP3 BMP headers to explain the +2-byte exception (useful sanity check, not required for LP1 conclusion).
4. Sample more LP1 level tokens across skills and classify their PCE atlas selection if stronger corpus coverage is desired.

## Action log — 2026-10-18

This log intentionally includes successful checks, negative searches, corrections, and failed tool/environment attempts.

- Resumed `linux-modernization`; maintained this note as the hand-off source of truth.
- Corrected initial wrong repo-owner probe (`OpenAargon/OpenAargon`); correct repository is `Mateozaur/OpenAargon`.
- Inventoried CE tree, LP1 `PACK.INI`, shared/pack-local tile sheets, sibling Level Packs, shared PCE directory, and LP1 level directories.
- Attempted shallow local clone/checkout for deterministic local `find`/`rg`; environment could not resolve `github.com`, so no usable clone was created and project state was unchanged.
- Switched back to connected GitHub API.
- Confirmed LP1-LP4 all ship pack-local same-named ordinary `black_tiles` pairs; recorded LP3's +2-byte exception.
- Confirmed LP1 has no local `black_tiles_exp*` files.
- Read `blank.pce` default-sheet documentation and multiple PCE batches; classified files listed above.
- Corrected preliminary PCE count from 27 to authoritative 29.
- Recorded `convert.pce` as ordinary/default despite high picture indices, ruling out atlas inference from index range.
- Indexed searches for `TILESFILE`, `Level Packs`, and `LevelPack` failed to expose loader source despite known data; classified as search-index/source-availability limitations.
- Initial binary BMP blob fetch failed with `UnicodeDecodeError`; no bytes were returned by that path.
- Enumerated LP1 `Skill1`-`Skill4`; fetched `Skill1/Level_001.map` and confirmed token-map format without atlas/index/remap metadata.
- Re-read `brick.pce`, `source.pce`, `coin.pce`, `spaceship.pce`; correlated actual LP1 `O`/`S` tokens with ordinary defaults and `p` with explicit exp1.
- Fetched `styles/lp1.csv`; confirmed no rows and fallback-to-shared-map intent.
- Fetched/searched `render-map.csv`; confirmed Coin/Source shared-PCE indices under `atlas=base` and expansion pieces under explicit expansion atlases.
- Recorded no-override/index-preservation conclusion.
- Finalized hand-off checkpoint in commit `49fa9e3046215d54230b2994ca1b3e3efcbd9b89` before continuing.
- Re-read branch head before continuation; it was `49fa9e3046215d54230b2994ca1b3e3efcbd9b89`.
- Fetched introduction commit `2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8` and its raw commit metadata; confirmed all legacy PNG atlases were added together, `LegacyTileSet.LEVEL_PACK_1` is 20x15, and the single parent is `98625d829872afc6a3aad6a6c89bef0261781079`.
- Fetched the parent commit; it only changes border T-junction rendering and provides no atlas-conversion provenance.
- Ran commit searches for `legacy`, `Collector`, `rendering support`, `BMP`, `atlas`, `lp1`, and `Collector's Edition`; connector returned no results even though the known introduction commit matches several terms, so commit-search output is not trusted as absence evidence.
- Ran indexed repository searches for `black_tiles.bmp` and likely ImageMagick/convert/png/bmp provenance; no useful results; likewise treated as index limitation.
- Retried LP1 BMP access with `fetch_file` base64 mode; succeeded and confirmed ranged reads are possible.
- Decoded LP1 full ordinary BMP header: 640x480, 24-bit, BI_RGB/uncompressed, 54-byte pixel offset, 921600-byte pixel payload, 921654-byte file.
- Decoded shared/base full ordinary BMP header: same structural geometry/payload; only X/Y resolution metadata differs (5176 base vs 5272 LP1).
- Promoted the previous 54+640*480*3 size equation from hypothesis to confirmed BMP-header fact for base and LP1 full ordinary sheets.
- Tested line-ranged base64 access. It works, but reconstructing complete BMP + compressed PNG through chat-sized chunks is impractical; direct pixel comparison remains outstanding.
- No functional code and no `styles/lp1.csv` mapping rows have been changed; only this recovery note has been updated.

## Session checkpoint / notes commits

Known recovery-note checkpoints from this work include:

```text
9695a790  Checkpoint LP1 recovery investigation
288e99d6  recovery-note update during asset/PCE audit
6c99fcf3  record LP1-LP4 pack-local tile-sheet evidence
2ac74af3  record BMP binary-reader limitation
822df309  record LP1 level-format evidence
0268560d  record shared-PCE token correlation
26eda22c  record OpenAargon alignment and no-override conclusion
49fa9e30  Finalize LP1 recovery handoff checkpoint
```

The action log is authoritative; intermediate note commits may have additional hashes/messages in branch history.

## Resume here

The LP1 base-index question remains provisionally resolved in favor of **index preservation**, now with an additional independent binary-header confirmation that base and LP1 ordinary full sheets share the same 640x480, 24-bit uncompressed geometry and exact pixel payload length.

Do not add LP1 overrides unless a specific index is proven semantically different.

Next best attempt: either obtain a practical complete-binary path and compare LP1 BMP vs `lp1.png` coordinates, or sample additional real LP1 maps/tokens across skills and verify their PCE-selected atlas families. The incomplete PCE inventory is a secondary fallback, not a reason to invent mappings.

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
```
