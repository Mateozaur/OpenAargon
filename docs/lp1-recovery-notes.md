# LP1 rendering recovery notes

_Last updated: 2026-10-18_

Hand-off source of truth for `Mateozaur/OpenAargon`, branch `linux-modernization`.

## Goal

Preserve original CE **semantics/appearance** without preserving original runtime rendering tricks. PCE/`black_tiles*` remain compatibility/import truth, not the target runtime API.

```text
legacy map/PCE/assets
  -> compatibility translation
  -> canonical semantic visual state
  -> PieceVisualCatalog/style resolver
  -> arbitrary style resources
```

Styles should consume semantic identities (e.g. oriented border edge/corner), not PCE rotation/tile numbers.

## Safety rule

Do not add speculative `legacy/styles/lp1.csv` rows. Prefer semantic migration of concrete failures over new atlas tricks.

## Historical LP1 contract — resolved

Original CE LP1 generally preserves shared PCE picture-index semantics while replacing ordinary `black_tiles.bmp` with a pack-local same-named sheet.

Confirmed:
- shared `render-map.csv`, sparse style overrides;
- `atlas=base` selects style; explicit exp1/exp2 remain shared;
- PCE tables authoritative for token/state -> index + logical sheet;
- LP1 no alternate PCE corpus/remap table;
- LP1 no local expansion sheets;
- LP1-LP4 repeat pack-local ordinary-sheet convention.

Representative blobs:
```text
base black_tiles.bmp 921654 2c1cebca0a437c2080352e48425150d3c374a533
LP1  black_tiles.bmp 921654 768d05581ab0a2a39beb9e29c492e869d07eb2fe
LP1  small           57654 e27ac267529a3f56020508c07775414f114a4032
```

Base/LP1 full BMP headers confirmed 640x480, 24-bit BI_RGB, offset 54, payload 921600. LP3 +2-byte files are same geometry with two terminal NUL bytes / `biSizeImage` quirk.

**Do not globally remap LP1 indices.** This is historical compatibility truth; modern runtime may discard raw indices.

## Provenance / tool limits

Legacy atlas intro commit: `2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8` (`Add Collector's Edition legacy rendering support`). Parent `98625d829872afc6a3aad6a6c89bef0261781079` only changed border T-junction rendering; no conversion script found.

Known PNG blobs: base `e1e903a2...`, lp1 `87ab0fe3...`.

Full BMP<->PNG comparison remains unavailable. Base64 ranged reads work; full binary materialization did not. GitHub indexed search is unreliable and missed known content, so search negatives are not absence evidence.

## Sampling/PCE checkpoint

Authoritative PCE count 29 (earlier spoken 27 corrected). Direct reads cover most ordinary/expansion cases. `convert.pce` is ordinary despite indices 260..267; `water.pce` is explicit exp2. Level_001 from all four LP1 skill groups matched PCE sequences against `render-map.csv`; ordinary and expansion pieces coexist as expected. Selector **piece** token `I` is correctly exp1 and is not the current failure.

## Concrete failing case — LP1 SELECTOR

Exact map:
```text
Aargon Deluxe Collectors Edition 1.4.CE/Level Packs/Level Pack 1/
Levels/Skill1/Level_002.map
MAPTEXT:[SELECTOR]
blob 1cd45a781fee5a0f9e19263a8dd84fbd3d9bd27e
```

`MapFile.parse` decodes each 4-char cell as token / decimal rotation / BeamColor(meta) / mutability. Blank color -> BLACK; `X` -> WHITE.

Normal outer frame:
```text
=3 n top-left      =4 n top       =5 n top-right
=2 n left                           =6 n right
=1 n bottom-left   =  n bottom    =7 n bottom-right
```
Special separator cells include `=2Xn`, `=1Xn`.

Legacy oriented rotation meaning established for this frame:
```text
0 bottom edge (interior N)
1 bottom-left corner (NE)
2 left edge (E)
3 top-left corner (SE)
4 top edge (S)
5 top-right corner (SW)
6 right edge (W)
7 bottom-right corner (NW)
```

Plain `WallTopology` alone is insufficient for asymmetric framing: left/right share N+S and top/bottom share E+W. Canonical state therefore needs oriented edge/corner role (or topology+facing). Legacy numeric rotation is allowed only at the compatibility boundary.

## Root cause reproduced

Legacy token `=` resolves to canonical `border` / `PieceBorder`.

Pre-patch, non-BASE `PieceBorder` in `PieceVisualCatalog` delegated to `LegacyRenderResolver` with raw rotation/meta.

`LegacyRenderResolver`: OA BLACK -> PCE color row 0. `wood.pce`/`render-map.csv` token `=`:
```text
PCE rows 0..6 -> 5,6,5,6,5,6,5,6
PCE row 7     -> 79,59,97,117,118,58,98,78
```

Thus SELECTOR blank-meta outer frame necessarily became repeated 5/6 under LP1. `X/WHITE` reaches row 7.

Do **not** fix by globally remapping LP1 `wood`: board border is a canonical PieceBorder concept and should not use generic PCE wood lookup.

## Native normalized border evidence

`texture/border.png` is 256x64 = 8x2 32px variants.

Path history:
- existed in 2016 initial commit `0d8b1ec...`;
- initial `PieceBorder` already rendered from it outside PCE/black_tiles;
- `d1fbfdcd7f5fb3f57bec8ef04c4751187671d384` (`Piece movement`) used offset `rot*32`, row `(meta==6 ? 0 : 32)`;
- commit `98625d829872afc6a3aad6a6c89bef0261781079` (`Normalize border T-junction rendering`) added topology-aware W+E+S / W+E+N handling **as an OpenAargon rendering improvement**, using special columns in `border.png`.

Important historical conclusion:
- original CE `wood.pce` has no dedicated semantic T-junction entries;
- for SELECTOR `=2Xn` (WHITE, rot 2) the PCE result is tile 97;
- for SELECTOR `=1Xn` (WHITE, rot 1) the PCE result is tile 59;
- therefore the later OpenAargon T-junction normalization is not evidence of a dedicated CE/LP1 T-junction sprite.

## Implemented semantic-border patch

### 1. `BorderVisualVariant`
Commit `4fcdb9faa009652e1a206a264920b2fc29d2973c` added `render/visual/BorderVisualVariant.java`.

Semantic roles:
```text
BOTTOM_EDGE, BOTTOM_LEFT_CORNER, LEFT_EDGE, TOP_LEFT_CORNER,
TOP_EDGE, TOP_RIGHT_CORNER, RIGHT_EDGE, BOTTOM_RIGHT_CORNER,
TEE_SOUTH, TEE_NORTH
```

`fromLegacyRotation()` is explicitly compatibility-only.

### 2. `PieceBorder`
Commit `c3c25636922af662426a3f9fdb6c97d06e05991d` added `getVisualVariant(field, coords, legacyRotation)`.

- 0..7 immediately translated to named semantic role;
- board topology can identify W+E+S (`TEE_SOUTH`) and W+E+N (`TEE_NORTH`);
- `getRenderRotation()` retained only as compatibility accessor.

### 3. `PieceVisualCatalog`
Commit `297dded77df24569bca17eec6fc824287d8b6b7d` removed `PieceBorder` from generic `LegacyRenderResolver` selection.

Added context-aware API:
```text
resolve(PieceInstance, VisualStyleId, IGameField, IntVector coords)
```

### 4. `GameField`
Commit `724fc22a6f920fc9db10dd702ff0a4eac3be775c` changed `bufferRenders` to call:
```text
PieceVisualCatalog.resolve(instance, boardStyle, this, c)
```

### 5. Style-specific LP1 border resolver
Commits `56dc547827539c0b188a64972019f636a81b279a` and `9efd314d1b08413f431ee6fd11c1dfa660d5ce39` added `BorderVisualResolver` and routed LP1 border variants to the correct LP1 ordinary atlas family instead of generic `texture/border.png`.

LP1 ordinary oriented border mapping currently uses the verified WHITE-row family:
```text
BOTTOM_EDGE         79
BOTTOM_LEFT_CORNER  59
LEFT_EDGE           97
TOP_LEFT_CORNER    117
TOP_EDGE           118
TOP_RIGHT_CORNER    58
RIGHT_EDGE          98
BOTTOM_RIGHT_CORNER 78
```

Commit `39c5965b055d1363f8edb57c6ffad4ee816cd7b6` tested `TEE_SOUTH -> 97` and `TEE_NORTH -> 59`, matching the original CE `wood.pce` values for SELECTOR's `=2Xn` and `=1Xn`. Runtime screenshots confirm that these are historically consistent single-tile results, not visually normalized T-junctions.

## Runtime validation completed 2026-10-18

User successfully built locally with Maven (`BUILD SUCCESS`) and ran representative levels.

### LP1
- SELECTOR outer frame now uses the correct LP1-themed border family instead of repeated connector/node tiles.
- ordinary edges/corners are visually coherent;
- SELECTOR's `X/WHITE` separator junctions follow the historical CE single-tile mapping and therefore do **not** form a visually normalized T.
- Future enhancement: an editable/customized level or modern style may intentionally supply true semantic T-junction art. This will be a useful acceptance test for the modern style architecture, but should be kept distinct from faithful CE rendering.

### LP2-LP4 smoke test
User ran one representative level from each pack.

Observed:
- pack-specific assets/atlases load and levels run;
- the same general border-family problem seen in LP1 remains for LP2, LP3 and LP4 because style-specific semantic border mappings have not yet been added for those packs;
- this should be solved next time by generalizing the LP1 `BorderVisualResolver` approach to LP2/LP3/LP4 rather than diagnosing each pack from scratch.

### Hotter
- representative level rendered correctly in the smoke test;
- no immediate blocker observed.

### Pocket
- not tested yet.

## Validation / safety status

- Local Maven build succeeded after semantic-border and LP1 style resolver changes.
- No raw LP1 mapping rows were added to `legacy/styles/lp1.csv`.
- `render-map.csv`, maps and original CE assets were not modified.
- `PieceWall` and `PieceLegacyStub` remain on CE compatibility resolver paths.
- The modern semantic border layer remains separate from PCE compatibility data.

## Material action log

- Recovered repo/branch and continuously maintained this note.
- Inventoried CE assets/PCE/maps and documented tool limits.
- Proved historical LP1 index-preservation/no-global-remap rule.
- Confirmed BMP headers and LP3 +2-byte tail anomaly.
- Sampled LP1 levels/PCE/render-map across all skills.
- Located exact SELECTOR map; decoded border rotations/meta.
- Reproduced failure blank meta -> BLACK -> PCE row0 -> repeated 5/6.
- Verified normalized `border.png` dimensions/history outside PCE.
- Added canonical border enum (`4fcdb9fa`).
- Refactored PieceBorder semantic translation (`c3c25636`).
- Removed PieceBorder from generic legacy resolver and added context-aware catalog (`297dded7`).
- Passed board context from GameField (`724fc22a`).
- Added LP1 semantic border resolver (`56dc5478`, `9efd314d`).
- Verified CE SELECTOR junction values from `wood.pce`; tested historical single-tile junction mapping (`39c5965b`).
- Confirmed OpenAargon's explicit T-junction normalization was a later renderer enhancement (`98625d82`), not a dedicated CE style asset contract.
- Runtime-tested LP1-LP4 and Hotter; Pocket remains untested.

## Resume here

**Today's smoke test is complete.**

Next session:
1. Keep LP1 faithful CE border behavior as the baseline; do not invent T-junction art yet.
2. Generalize `BorderVisualResolver` from LP1 to LP2, LP3 and LP4 using each pack's ordinary atlas and the same semantic edge/corner roles.
3. Validate one representative level per updated pack after each mapping.
4. Test Pocket when convenient.
5. Keep Hotter unchanged unless a concrete failure appears.
6. Later, after level editing/custom style support exists, add an optional modern style with true `TEE_SOUTH` / `TEE_NORTH` art as an architecture test.
7. Continue to avoid global raw PCE remaps; style resources must remain behind semantic visual identities.

Repository: `Mateozaur/OpenAargon`  
Branch: `linux-modernization`
