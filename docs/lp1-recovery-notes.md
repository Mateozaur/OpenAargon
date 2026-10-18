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

Its single parent is `98625d829872afc6a3aad6a6c89bef0261781079` (`Normalize border T-junction rendering`). The parent change concerns only `PieceBorder.java` and provides no atlas-generation provenance. The introduction commit defines `LegacyTileSet.LEVEL_PACK_1` as `texture/legacy/lp1.png`, 20x15, but contains no checked-in conversion script or source-BMP-to-PNG remap procedure.

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

This repeated path/name convention is strong architectural evidence that active Level Packs intentionally supply their own ordinary `black_tiles` resource.

### Binary BMP header confirmation

Earlier direct binary blob access failed with a `UnicodeDecodeError`. `GitHub.fetch_file(..., encoding=base64)` later succeeded and supports line-ranged base64 reads.

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

Confirmed shared/base full ordinary BMP has the same structural geometry and payload; its X/Y resolution metadata is 5176 rather than LP1's 5272. Therefore `921654 = 54 + 640*480*3` is now a confirmed header fact for base and LP1, not an inference from file size.

### LP3 +2-byte anomaly resolved

Decoded LP3 headers:

```text
LP3 full:
  file size 921656
  offset 54
  640x480, 24 bpp, BI_RGB
  biSizeImage 921602

LP3 small:
  file size 57656
  offset 54
  160x120, 24 bpp, BI_RGB
  biSizeImage 57602
```

Thus LP3 does **not** use a different grid, bpp, compression, or DIB format. Both `biSizeImage` and total file size are exactly two bytes above the normal geometric pixel payload. A ranged read of the full-sheet tail matches the base-sheet tail through the normal endpoint and LP3 then has two additional NUL bytes. The small LP3 tail likewise ends with two additional zero bytes relative to its nominal geometric payload. Treat LP3's +2 as an encoder/file-tail quirk, not evidence of different atlas geometry or slot layout.

The connector can range-read base64, but reconstructing complete ~922 KB BMP and complete compressed PNG through chat-sized chunks is not practical. Direct pixel-for-pixel LP1 BMP -> `lp1.png` comparison therefore remains outstanding.

An additional attempt to obtain complete binary files through a raw GitHub/web/container download path also failed: direct raw/blob web opens produced cache misses, and the local download helper refused the raw URL because it had not been successfully exposed by the web layer. A web search for the exact repository image path did not surface the repository file. No local binary file was obtained by that route.

## LP1 PACK.INI

```ini
[Params]
Text = Try these levels in one of our secret deep space installations.
Position = 7000
Edit = false
SelectSkillDll=LevelPackOne
IsEmpty=false
```

It has no tile filename, Pictures path, atlas name, or resource-search-path field. Pack-local sheet lookup must be generic elsewhere, not configured per LP1 in `PACK.INI`.

## Real LP1 map / shared-PCE evidence

`Level Packs/Level Pack 1/Levels/` contains `Skill1` through `Skill4`. Map files are token-oriented board data plus metadata; sampled maps contain no atlas filename, picture-index table, or remap table.

Initial evidence from `Skill1/Level_001.map` (`LANDING LIGHTS`):

```text
O -> coin.pce -> ordinary/default
S -> source.pce -> ordinary/default
```

Expanded cross-skill sampling used `Level_001.map` from each remaining skill:

### Skill2 / Level_001 — `OUTSIDE THE BOX`

```text
I -> selector.pce  -> explicit exp1
O -> coin.pce      -> ordinary/default
W -> oneway.pce    -> ordinary/default, pictures 211..218
R -> refractor.pce -> ordinary/default, pictures 176..179
S -> source.pce    -> ordinary/default
```

### Skill3 / Level_001 — `POSTCARD FROM HOME`

```text
T -> threeway.pce -> ordinary/default, pictures 268..275
F -> filter.pce   -> ordinary/default, pictures 120/123/122/125/121/126/124/120
P -> prism.pce    -> ordinary/default, pictures 80..87
O -> coin.pce     -> ordinary/default
S -> source.pce   -> ordinary/default
```

### Skill4 / Level_001 — `MOTHER BOARD`

```text
# -> brick.pce     -> ordinary/default
% -> double.pce    -> ordinary/default, pictures 174/175/99/119
< -> splitter.pce  -> ordinary/default, pictures 180..187
R -> refractor.pce -> ordinary/default
P -> prism.pce     -> ordinary/default
O -> coin.pce      -> ordinary/default
S -> source.pce    -> ordinary/default
```

Notes about token parsing: strings such as `O Rn`, `F Cn`, `O Mn` contain an `O`/`F` piece token followed by color/rotation encoding; `M`, `C`, etc. there are not separate piece tokens.

This four-skill sample substantially broadens the original O/S proof. It demonstrates real LP1 levels using many ordinary shared PCE definitions and, in Skill2, an explicit expansion exception (`I`/Selector) in the same pack without any LP1-specific remap mechanism.

The resource/index contract is therefore:

1. LP1 maps store shared tokens, not atlas/index remaps.
2. Shared PCE definitions turn token/state into picture indices and select ordinary vs expansion logical sheets.
3. LP1 supplies a same-named pack-local ordinary `black_tiles.bmp`, but no local expansion replacement and no alternate PCE corpus.
4. LP1 ordinary assets are therefore intended to be consumed at shared PCE picture indices; explicitly expansion-backed pieces remain on shared expansion sheets.

## OpenAargon alignment

`src/main/resources/legacy/styles/lp1.csv` contains only header/comments and **no override rows**. Missing entries intentionally fall back to shared `legacy/render-map.csv`.

Previously verified:

```text
O / 79  -> coin      -> atlas=base, PCE-derived animation starts
S / 83  -> source    -> atlas=base, PCE-derived animation starts
p / 112 -> spaceship -> atlas=exp1
I / 73  -> selector  -> atlas=exp1
```

Cross-skill sample checks against the current `render-map.csv` confirm exact PCE-derived mappings:

```text
W / 87 -> oneway    -> atlas=base, tiles 211..218
R / 82 -> refractor -> atlas=base, tiles 176..179
T / 84 -> threeway  -> atlas=base, tiles 268..275
F / 70 -> filter    -> atlas=base, picture sequence 120/123/122/125/121/126/124/120
P / 80 -> prism     -> atlas=base, tiles 80..87
% / 37 -> double    -> atlas=base, tiles 174/175/99/119 repeated by rotation
< / 60 -> splitter  -> atlas=base, tiles 180..187
```

The fetched render map also shows explicit expansion pieces such as `selector` and `prismirror` remaining on `exp1`. Expansion and base families are deliberately mixed according to PCE selection, not by index magnitude or active LP1 style.

Thus current OpenAargon behavior matches the verified original contract across a broader LP1 sample: LP1 style substitutes the selected `base` atlas while preserving shared PCE indices; explicit expansion mappings are not redirected.

## PCE inventory checkpoint

Authoritative shared `Pieces/` listing contains 29 `.pce` files. A preliminary spoken count of 27 was corrected after enumeration.

Directly read/classified so far:

```text
blackhole.pce   explicit exp1
blank.pce       format/default documentation
brick.pce       ordinary/default; TOKEN #
coin.pce        ordinary/default; TOKEN O
complex.pce     explicit exp1
convert.pce     ordinary/default; indices 260..267
double.pce      ordinary/default; TOKEN %
doubleslit.pce  ordinary/default; TOKEN +
explosion.pce   animation-list-only; no [FLAGS], intentionally unclassified
filter.pce      ordinary/default; TOKEN F
mine.pce        ordinary/default; TOKEN m
mirror.pce      ordinary/default; TOKEN /
oneway.pce      ordinary/default; TOKEN W
polarizer.pce   ordinary/default; TOKEN :
prism.pce       ordinary/default; TOKEN P
prismirror.pce  explicit exp1; TOKEN M
refractor.pce   ordinary/default; TOKEN R
selector.pce    explicit exp1; TOKEN I
source.pce      ordinary/default; TOKEN S
spaceship.pce   explicit exp1; TOKEN p
splitter.pce    ordinary/default; TOKEN <
threeway.pce    ordinary/default; TOKEN T
tnt.pce         ordinary/default; TOKEN X
water.pce       explicit exp2; TOKEN w
wood.pce        ordinary/default; TOKEN =
```

`convert.pce` is important because its ordinary/default indices are in the 260s: index magnitude must not be used to infer atlas family. `water.pce` provides an explicit exp2 contrast.

The inventory is still incomplete, but a large majority of meaningful atlas-selection cases have now been directly read. Exhaustive classification is no longer required to support the LP1 conclusion unless a future discrepancy appears.

## Current conclusion

For the motivating question — whether shared `atlas=base` picture indices should generally be remapped for LP1 — current evidence says **no** with substantially increased confidence.

Independent evidence now includes:

- original pack-local resource naming/placement across LP1-LP4;
- no LP1 local expansion sheets or alternate PCE/remap corpus;
- real LP1 maps from all four skill groups using shared token definitions;
- ordinary and explicit-expansion PCE cases coexisting in LP1 exactly as expected;
- current OpenAargon render-map indices matching every distinctive sampled PCE table checked from Skill2-Skill4;
- direct binary BMP headers confirming matching base/LP1 full-sheet storage geometry;
- LP3's +2-byte anomaly resolved as two extra zero bytes / `biSizeImage` quirk rather than alternate geometry.

Do not add rows to `styles/lp1.csv` unless a specific semantic index deviation is proven.

Remaining independent checks, in priority order:

1. Direct pixel/coordinate comparison of original LP1 `black_tiles.bmp` against OpenAargon's `lp1.png` if a practical complete-binary transfer path becomes available.
2. Locate original active-pack resource lookup code/strings if a readable executable/source path becomes available.
3. Sample additional levels beyond `Level_001` only if looking for a rare token not covered by the current four-skill sample or investigating a concrete visual discrepancy.
4. Finish the last few unclassified PCE files only if needed for such a concrete exception.

Absent a new discrepancy or new binary/source access, further speculative LP1 remapping work has low value and risks introducing incorrect overrides.

## Action log — 2026-10-18

This log intentionally includes successful checks, negative searches, corrections, and failed tool/environment attempts.

- Resumed `linux-modernization`; maintained this note as the hand-off source of truth.
- Corrected initial wrong repo-owner probe (`OpenAargon/OpenAargon`); correct repository is `Mateozaur/OpenAargon`.
- Inventoried CE tree, LP1 `PACK.INI`, shared/pack-local tile sheets, sibling Level Packs, shared PCE directory, and LP1 level directories.
- Attempted shallow local clone/checkout for deterministic local `find`/`rg`; environment could not resolve `github.com`, so no usable clone was created and project state was unchanged.
- Switched back to connected GitHub API.
- Confirmed LP1-LP4 all ship pack-local same-named ordinary `black_tiles` pairs; recorded LP3's +2-byte exception.
- Confirmed LP1 has no local `black_tiles_exp*` files.
- Read `blank.pce` default-sheet documentation and multiple PCE batches; corrected preliminary PCE count from 27 to authoritative 29.
- Recorded `convert.pce` as ordinary/default despite high picture indices, ruling out atlas inference from index range.
- Indexed searches for `TILESFILE`, `Level Packs`, and `LevelPack` failed to expose loader source despite known data; classified as search-index/source-availability limitations.
- Initial binary BMP blob fetch failed with `UnicodeDecodeError`; no bytes were returned by that path.
- Enumerated LP1 `Skill1`-`Skill4`; fetched `Skill1/Level_001.map` and confirmed token-map format without atlas/index/remap metadata.
- Correlated actual Skill1 LP1 `O`/`S` tokens with ordinary shared PCE defaults; used Spaceship/Selector as explicit-exp1 contrasts.
- Fetched `styles/lp1.csv`; confirmed no rows and fallback-to-shared-map intent.
- Fetched/searched `render-map.csv`; confirmed Coin/Source shared-PCE indices under `atlas=base` and expansion pieces under explicit expansion atlases.
- Recorded no-override/index-preservation conclusion.
- Finalized hand-off checkpoint in commit `49fa9e3046215d54230b2994ca1b3e3efcbd9b89` before continuing.
- Re-read branch head; confirmed the hand-off commit was current before the next investigation block.
- Fetched introduction commit `2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8` and raw commit metadata; confirmed all legacy PNG atlases were introduced together and parent is `98625d829872afc6a3aad6a6c89bef0261781079`.
- Fetched that parent; it only changes border T-junction rendering and contains no atlas-conversion provenance.
- Ran commit searches for `legacy`, `Collector`, `rendering support`, `BMP`, `atlas`, `lp1`, and `Collector's Edition`; connector returned no results even though the known introduction commit matches several terms, so commit-search output is not trusted as absence evidence.
- Ran indexed repository searches for `black_tiles.bmp` and likely ImageMagick/convert/png/bmp provenance; no useful results; treated as index limitation.
- Retried LP1 BMP access using `fetch_file` base64; succeeded and confirmed ranged binary reads are possible.
- Decoded LP1 full ordinary BMP header: 640x480, 24-bit, BI_RGB, offset 54, payload 921600, file 921654.
- Decoded shared/base full ordinary BMP header: same structural geometry/payload; X/Y metadata 5176 vs LP1 5272.
- Promoted `54 + 640*480*3` from hypothesis to confirmed BMP-header fact for base and LP1 full ordinary sheets.
- Decoded LP3 full and small BMP headers; confirmed same geometry/format but `biSizeImage` and file size are +2 bytes.
- Range-read LP3/base file tails; confirmed LP3 full appends two NUL bytes beyond the normal endpoint; small LP3 likewise carries two extra terminal zero bytes. Classified LP3 +2 as file-tail/encoder quirk, not atlas geometry difference.
- Enumerated Skill2 and fetched `Skill2/Level_001.map` (`OUTSIDE THE BOX`).
- Fetched `Skill3/Level_001.map` (`POSTCARD FROM HOME`) and `Skill4/Level_001.map` (`MOTHER BOARD`).
- Parsed relevant piece tokens from all three maps, taking care not to treat per-piece color/rotation characters as separate tokens.
- Direct-read additional PCEs needed by the sample: `refractor`, `water`, `wood`, `mirror`, `filter`, `polarizer`, `prism`, `tnt`, `threeway`, `splitter`, `mine`, `oneway`, `prismirror`, `doubleslit`, `double`.
- Identified Skill2 sample as `I` exp1 plus `O/W/R/S` ordinary; Skill3 as `T/F/P/O/S` ordinary; Skill4 as `#/%/</R/P/O/S` ordinary.
- Re-fetched current `render-map.csv` and verified sampled pieces against direct PCE reads: OneWay, Refractor, ThreeWay, Filter, Prism, Double, and Splitter all use `atlas=base` with the exact PCE picture sequences; Selector remains `exp1`.
- Recorded the cross-skill corpus result: no LP1-specific remapping mechanism appeared; ordinary and expansion PCE choices remain shared and coherent across all four skill groups.
- Attempted an alternate full-binary path using raw GitHub URL + web/container download. Raw/blob web fetches returned cache misses; the container downloader refused the raw URL because the web layer had not successfully exposed it. A web search for the exact image path did not return the repository asset. No binary file was materialized.
- No functional code and no `styles/lp1.csv` mapping rows have been changed; only recovery/documentation notes have been updated.

## Session checkpoint / notes commits

Known recovery-note checkpoints include:

```text
9695a790  Checkpoint LP1 recovery investigation
288e99d6  recovery-note update during asset/PCE audit
6c99fcf3  record LP1-LP4 pack-local tile-sheet evidence
2ac74af3  record BMP binary-reader limitation
822df309  record LP1 level-format evidence
0268560d  record shared-PCE token correlation
26eda22c  record OpenAargon alignment and no-override conclusion
49fa9e30  Finalize LP1 recovery handoff checkpoint
ee91c454  Record LP1 provenance and BMP header validation
c6284f19  Record LP3 header check and cross-skill LP1 sampling
```

The action log is authoritative; intermediate note commits may have additional hashes/messages in branch history.

## Resume here

The LP1 base-index question is provisionally resolved in favor of **index preservation with high confidence**. The conclusion is no longer based on dimensions or a single sample level: it is supported by original resource structure, decoded binary headers, real LP1 maps from all four skill groups, direct PCE atlas-selection data, and exact matching OpenAargon render-map entries for the sampled distinctive tokens.

Do not add LP1 overrides unless a specific index is proven semantically different.

The best remaining independent confirmation is a practical full-binary path for pixel/coordinate comparison of original LP1 `black_tiles.bmp` against `lp1.png`, or readable original loader/resource-search code. Both accessible binary-download routes tried in this session are documented above. In the absence of new access or an observed rendering discrepancy, the next useful work should be driven by a concrete failing visual/token case rather than speculative expansion of `lp1.csv`.

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
```
