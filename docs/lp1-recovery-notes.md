# LP1 legacy rendering recovery notes

_Last updated: 2026-10-18_

Live hand-off state for the Collector's Edition Level Pack 1 (LP1) rendering investigation and migration.

## Long-term goal

OpenAargon should preserve the **semantic appearance and behaviour** of original Collector's Edition content without preserving the original game's runtime rendering tricks as architectural requirements.

The original PCE files, map tokens and `black_tiles*.bmp` resources remain authoritative evidence for importing/reconstructing what a piece/state means. They should become a compatibility / migration source, not the permanent runtime API.

Target architecture:

```text
legacy map/PCE/resource data
        |
        v
compatibility/import translation
        |
        v
canonical semantic visual identity/state
        |
        v
PieceVisualCatalog / style resolver
        |
        v
style-specific sprite or animation resource
```

A renderer should eventually ask for semantic variants such as a particular wall/border topology, not for "PCE rotation 5" or "tile 176 in black_tiles.bmp". Styles should be free to use different atlases, separate images, different resolutions, or other layouts.

## Safety rule

Do **not** add rows to `src/main/resources/legacy/styles/lp1.csv` from visual guesses, equal dimensions, index ranges, or unvalidated assumptions. A legacy-style override needs a concrete verified semantic slot deviation. Prefer migrating concrete failing cases toward canonical semantic visuals instead of adding another legacy atlas trick.

## Original LP1 resource/index contract — resolved with high confidence

For the original Collector's Edition mechanism, LP1 generally preserves shared PCE picture indices while replacing the ordinary `black_tiles.bmp` resource with a pack-local same-named sheet.

Confirmed model:

- `src/main/resources/legacy/render-map.csv` is the shared token/state mapping; style CSVs are sparse overrides.
- Generic `atlas=base` resolves against the currently selected legacy style atlas.
- Explicit `atlas=exp1` / `atlas=exp2` remain shared expansion atlases.
- Original `Pieces/*.pce` tables are authoritative for token/state -> picture index and logical tile-sheet selection.
- `blank.pce` documents ordinary defaults as `black_tiles.bmp` / `black_tiles_small.bmp`.
- LP1 has no alternate PCE corpus or map-level remap table.
- Real LP1 maps contain shared piece tokens.

Therefore do **not** globally remap LP1 base indices. This finding is useful for accurate migration, but it does not require the new runtime renderer to retain raw PCE indices.

## Resolved legacy cases — do not redo first

### Selector piece (`I`, token 73)

`Pieces/selector.pce` explicitly selects `black_tiles_exp1.bmp` / `black_tiles_small_exp1.bmp`. Its picture values reproduce existing Selector rows in `render-map.csv` under `atlas=exp1`. The Selector **piece itself** stays shared-exp1 and does not justify an LP1 override.

This is distinct from the new concrete failing case below: the LP1 level named SELECTOR has a broken **board border**, not a broken Selector piece mapping.

### DLL dead ends

- LP1 `PACK.INI` specifies `SelectSkillDll=LevelPackOne`.
- `RELEASE_LevelPackOne.dll` implements skill/level-selection UI, not token-to-sprite mapping.
- `RELEASE_ItemMap.dll::MAP::SelectTile(CPosition)` and `RELEASE_TileBlitter.dll` were not mapping sources.

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

LP1 contains no `black_tiles_exp*`, so it replaces only the ordinary pair while expansion sheets remain shared.

All four original Level Packs repeat the pack-local ordinary convention:

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

### Binary BMP confirmation

`GitHub.fetch_file(..., encoding=base64)` made ranged binary inspection possible after direct blob reading had failed with `UnicodeDecodeError`.

Base and LP1 full ordinary sheets are confirmed as:

```text
signature       BM
file size       921654
pixel offset    54
DIB header      40 bytes
width           640
height          480
planes          1
bits/pixel      24
compression     0 (BI_RGB)
image payload   921600
```

LP1 X/Y pixels-per-meter = 5272; shared/base = 5176. Geometry/payload are otherwise structurally identical.

LP3's historical `+2 byte` anomaly is resolved: full and small files keep the same 640x480 / 160x120, 24-bit BI_RGB geometry but report `biSizeImage` two bytes above the geometric payload and carry two additional terminal zero bytes. Treat it as an encoder/file-tail quirk, not alternate atlas geometry.

Complete LP1 BMP -> OpenAargon `lp1.png` pixel comparison remains outstanding because complete binary transfer was not practical. Raw GitHub/web/container download attempts also failed to materialize the files.

## Atlas provenance

Commit `2e50ecbe87b3907095cfcac4cf43401d5fd6b2` is **not** a commit SHA; do not use it. The actual introduction commit is:

```text
2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8
Add Collector's Edition legacy rendering support
```

It introduced `base.png`, `classic.png`, `exp1.png`, `exp2.png`, `hotter.png`, `lp1.png`, `lp2.png`, `lp3.png`, `lp4.png`, `pocket.png` in one commit. Its parent `98625d829872afc6a3aad6a6c89bef0261781079` only changes border T-junction rendering and provides no atlas-conversion provenance.

Known blobs:

```text
base.png e1e903a202b7d8c301f46cfa12291abe4a383ae4
lp1.png  87ab0fe3c09e2e26dca009d21e50791cfa1e3570
```

Repository/commit searches for conversion provenance were unreliable and returned no useful evidence even for known terms; treat those negatives as indexing limitations.

## LP1 PACK.INI

```ini
[Params]
Text = Try these levels in one of our secret deep space installations.
Position = 7000
Edit = false
SelectSkillDll=LevelPackOne
IsEmpty=false
```

There is no tile filename, Pictures path, atlas name, or resource-search-path setting.

## Real LP1 map / PCE sampling

`Level Packs/Level Pack 1/Levels/` contains `Skill1`-`Skill4`; sampled maps are token-oriented board data and contain no atlas/index/remap table.

Verified `Level_001` samples:

```text
Skill1 LANDING LIGHTS:
  O -> coin.pce   -> ordinary/default
  S -> source.pce -> ordinary/default

Skill2 OUTSIDE THE BOX:
  I -> selector.pce  -> explicit exp1
  O -> coin.pce      -> ordinary/default
  W -> oneway.pce    -> ordinary/default, 211..218
  R -> refractor.pce -> ordinary/default, 176..179
  S -> source.pce    -> ordinary/default

Skill3 POSTCARD FROM HOME:
  T -> threeway.pce -> ordinary/default, 268..275
  F -> filter.pce   -> ordinary/default, 120/123/122/125/121/126/124/120
  P -> prism.pce    -> ordinary/default, 80..87
  O -> coin.pce     -> ordinary/default
  S -> source.pce   -> ordinary/default

Skill4 MOTHER BOARD:
  # -> brick.pce     -> ordinary/default
  % -> double.pce    -> ordinary/default, 174/175/99/119
  < -> splitter.pce  -> ordinary/default, 180..187
  R/P/O/S            -> ordinary/default
```

`render-map.csv` was checked against these PCEs and matches their picture sequences; Selector remains exp1. Strings like `O Rn` contain piece token `O` followed by state encoding; the following letter is not another piece token.

## PCE inventory checkpoint

Authoritative `Pieces/` listing contains 29 `.pce` files. A preliminary spoken count of 27 was corrected.

Directly read/classified:

```text
blackhole.pce   explicit exp1
blank.pce       default-sheet documentation
brick.pce       ordinary/default; #
coin.pce        ordinary/default; O
complex.pce     explicit exp1
convert.pce     ordinary/default; indices 260..267
double.pce      ordinary/default; %
doubleslit.pce  ordinary/default; +
explosion.pce   animation-list-only; intentionally unclassified
filter.pce      ordinary/default; F
mine.pce        ordinary/default; m
mirror.pce      ordinary/default; /
oneway.pce      ordinary/default; W
polarizer.pce   ordinary/default; :
prism.pce       ordinary/default; P
prismirror.pce  explicit exp1; M
refractor.pce   ordinary/default; R
selector.pce    explicit exp1; I
source.pce      ordinary/default; S
spaceship.pce   explicit exp1; p
splitter.pce    ordinary/default; <
threeway.pce    ordinary/default; T
tnt.pce         ordinary/default; X
water.pce       explicit exp2; w
wood.pce        ordinary/default; =
```

`convert.pce` proves index magnitude does not select an atlas family; `water.pce` is an explicit exp2 contrast.

## Concrete failing case — LP1 level SELECTOR

User supplied a screenshot of LP1 level SELECTOR from current OpenAargon.

Observed:

- the **outer board border** is visibly wrong: it renders as repeated connector/node-like graphics instead of the intended board frame;
- the title-area frame at the top renders correctly;
- the title frame is produced through a different rendering path;
- interior gameplay pieces appear coherent enough that this is not evidence of a global LP1 atlas failure.

This is the first concrete case to use for migration away from legacy atlas tricks. Do not treat it as justification for global `lp1.csv` remapping.

## Current OpenAargon architecture discovered while investigating SELECTOR

The branch already contains a partial canonical visual layer.

### `PieceVisualCatalog`

`GameField` resolves placed-piece visuals through `PieceVisualCatalog.resolve(instance, boardStyle)`. The catalog is deliberately documented as the canonical visual lookup entry point so callers do not know how old atlases encode a visual.

However, the catalog is still transitional. For `PieceWall`, `PieceLegacyStub`, and for `PieceBorder` whenever the resolved style is not `BASE`, it delegates to `LegacyRenderResolver` using:

```text
piece token
instance.getRotation()
instance.getMeta()
selected LegacyTileSet
```

For an LP1 border this means the semantic/topological native logic is bypassed and raw legacy rotation is fed into the old atlas mapping. This is now the primary suspect for the SELECTOR outer-border failure.

### `WallTopology`

`WallTopology` already exists as a renderer-independent four-neighbour connectivity model with N/E/S/W bitmask values. Its documentation explicitly states that original CE maps can use different rotation values for the same geometry and therefore topology **must not be inferred from a legacy rotation index alone**.

This is exactly the abstraction needed for the universal rendering direction.

### `PieceBorder`

`PieceBorder` already exposes:

```text
getBorderTopology(field, coords)
getRenderRotation(field, coords, rot)
```

For the native `texture/border.png` path it currently uses board topology to normalize two T-junction cases (west+east+south -> native variant 4; west+east+north -> native variant 5), otherwise falling back to the normalized map rotation. Its own comment says this remains specific to the native border sheet and that legacy atlas variants are resolved independently.

That split is precisely what now needs to evolve: style resolution should consume semantic topology rather than bypass it for LP1.

## Migration direction for SELECTOR border

Preferred direction:

1. Keep PCE/legacy atlas tables as compatibility evidence and fallback while migration is incomplete.
2. Make the placed-piece visual lookup capable of receiving board context / coordinates or an already-derived semantic variant.
3. For `PieceBorder`, derive `WallTopology` from the actual board.
4. Convert that topology to a **canonical border visual identity**, independent of legacy rotation numbers.
5. Let each style map that identity to its own sprite resource/coordinates.
6. Avoid introducing an LP1-specific raw tile-number permutation unless investigation proves it is necessary merely as a temporary compatibility bridge.

The correctly rendered title frame may provide a useful reference for the intended LP1 border graphics, but its codepath still needs to be located and compared.

## Current technical conclusion

Two conclusions coexist and must not be conflated:

1. **Historical/original contract:** LP1 ordinary resources preserve shared PCE index semantics with pack-local `black_tiles.bmp`; no broad LP1 remap is justified.
2. **Modernization contract:** OpenAargon should not expose or depend on that physical atlas/index scheme in its long-term runtime renderer. Canonical semantic visuals should sit between game state and style resources.

The SELECTOR border bug is a good first migration case because a semantic `WallTopology` abstraction already exists and the current bug likely occurs exactly where the non-base style path falls back from canonical visual lookup to raw legacy rotation/index resolution.

## Action log — 2026-10-18

This log includes successful checks, negative searches, corrections and failed tool/environment attempts.

- Maintained this recovery note as hand-off source of truth throughout the investigation.
- Corrected initial wrong repo-owner probe; correct repository is `Mateozaur/OpenAargon`.
- Inventoried CE tree, PACK.INI, shared/pack-local sheets, sibling level packs, PCEs and LP1 levels.
- Attempted local shallow clone; DNS could not resolve github.com, so no usable clone was created.
- Confirmed LP1-LP4 same-named pack-local ordinary sheets; LP1 has no local expansion sheets.
- Classified PCE defaults/expansion exceptions; corrected PCE count from 27 to 29.
- Recorded `convert.pce` high ordinary indices and explicit exp1/exp2 counterexamples.
- Indexed source searches for loader behavior were inconclusive due search-index limitations.
- Initial binary blob read failed with UnicodeDecodeError; later base64 ranged reads succeeded.
- Confirmed base/LP1 BMP headers and resolved LP3 +2-byte tail anomaly.
- Sampled Level_001 from all four LP1 skill groups and matched characteristic tokens/PCE indices against `render-map.csv`.
- Attempted alternate full-binary raw GitHub/web/container route; no complete binary was materialized.
- Established high-confidence no-global-remap conclusion for original LP1 indices.
- User supplied LP1 SELECTOR screenshot showing concrete outer-border failure while title frame renders correctly by another path.
- User clarified long-term goal: stop reproducing original black_tiles/runtime tricks; use a universal semantic visual standard with legacy data only as compatibility/import truth.
- Re-read branch head at `a2ea732bf664fbf66683cc3bfe23ae7bcf9f840a` before starting semantic-border work.
- Read current `LegacyRenderResolver`, `GameField`, `PieceVisualCatalog`, `PieceBorder`, `WallTopology`, `PieceInstance`, `VisualStyleId`, and `SpriteDefinition` during architecture review.
- Discovered `GameField` already routes placed visuals through `PieceVisualCatalog`.
- Identified that non-BASE `PieceBorder` currently gets delegated from `PieceVisualCatalog` to `LegacyRenderResolver` using raw stored rotation/meta, bypassing board-derived topology.
- Confirmed `WallTopology` already provides the renderer-independent semantic connectivity model required for migration.
- Three no-op `update_ref` calls were accidentally issued against the already-current branch HEAD while preparing this checkpoint; all targeted the existing `a2ea732b...` SHA with matching lease and did not change repository content/history.
- No functional code or `styles/lp1.csv` rows have yet been changed in this new semantic-border stage.

## Recovery-note checkpoints

Known checkpoints include:

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
a2ea732b  Finalize LP1 cross-skill recovery checkpoint
```

The action log is authoritative when commit-message summaries are incomplete.

## Resume here

Continue from the **semantic border migration**, not from speculative LP1 atlas remapping.

Immediate next steps:

1. Locate and inspect the codepath that renders the correct title-area frame in LP1 SELECTOR.
2. Inspect how `GameField` can pass board context/coordinates into the canonical visual layer without coupling that layer to legacy atlas details.
3. Define canonical border variants from `WallTopology` (possibly all 16 masks, with style fallback where a variant is intentionally unsupported).
4. Map the existing native/base border resource and LP1 style resource to those semantic variants.
5. Add/adjust tests for topology-to-visual selection before removing any legacy fallback.
6. Validate specifically against the SELECTOR outer border.

Do not add global LP1 index overrides. If a temporary compatibility mapping is needed while migrating border resources, document it explicitly as transitional and keep semantic topology as the public contract.

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
```