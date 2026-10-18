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

User screenshot: outer board border shows repeated green connector/node graphics; title-area frame is correct via another path; interior pieces mostly coherent.

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

Thus SELECTOR blank-meta outer frame necessarily became repeated 5/6 under LP1, matching the screenshot. `X/WHITE` reaches row 7. This is a reproduced data-flow cause.

Do **not** fix by globally remapping LP1 `wood`: board border is a canonical PieceBorder concept and should not use generic PCE wood lookup.

## Native normalized border evidence

`texture/border.png` is 256x64 = 8x2 32px variants.

Path history:
- existed in 2016 initial commit `0d8b1ec...`;
- initial `PieceBorder` already rendered from it outside PCE/black_tiles;
- `d1fbfdcd7f5fb3f57bec8ef04c4751187671d384` (`Piece movement`) used offset `rot*32`, row `(meta==6 ? 0 : 32)`;
- current PieceBorder retained normalized sheet + topology T-junction fixes.

Routing non-BASE borders into generic legacy atlases was therefore a later regression relative to OpenAargon's normalized border model.

## Implemented semantic-border patch

### 1. `BorderVisualVariant`
Commit `4fcdb9faa009652e1a206a264920b2fc29d2973c` added `render/visual/BorderVisualVariant.java`.

Semantic roles:
```text
BOTTOM_EDGE, BOTTOM_LEFT_CORNER, LEFT_EDGE, TOP_LEFT_CORNER,
TOP_EDGE, TOP_RIGHT_CORNER, RIGHT_EDGE, BOTTOM_RIGHT_CORNER,
TEE_SOUTH, TEE_NORTH
```

`fromLegacyRotation()` is explicitly compatibility-only. Each role currently has a default normalized `border.png` column; T-junction roles alias historical native columns 4/5. Future styles may map the same enum to unrelated resources.

### 2. `PieceBorder`
Commit `c3c25636922af662426a3f9fdb6c97d06e05991d` added `getVisualVariant(field, coords, legacyRotation)`.

- 0..7 immediately translated to named semantic role;
- actual board topology wins for W+E+S (`TEE_SOUTH`) and W+E+N (`TEE_NORTH`);
- `getRenderRotation()` retained only as compatibility accessor;
- native offset now consumes the semantic variant.

### 3. `PieceVisualCatalog`
Commit `297dded77df24569bca17eec6fc824287d8b6b7d` removed `PieceBorder` from generic `LegacyRenderResolver` selection.

Added context-aware API:
```text
resolve(PieceInstance, VisualStyleId, IGameField, IntVector coords)
```
Old overload remains and delegates with null context.

Border resolution now:
- obtains `BorderVisualVariant`;
- keeps semantic WHITE/X versus ordinary/default as normalized 2-row choice;
- returns `texture/border.png` + semantic column;
- intentionally ignores board style for now as the canonical baseline.

This means **all styles stop depending on `black_tiles` for PieceBorder immediately**. LP1/HD/custom themed border art should later override semantic variants, not PCE indices.

`PieceWall` and `PieceLegacyStub` remain on CE compatibility resolver for later migration.

### 4. `GameField`
Commit `724fc22a6f920fc9db10dd702ff0a4eac3be775c` changed `bufferRenders` to call:
```text
PieceVisualCatalog.resolve(instance, boardStyle, this, c)
```
Board context and coordinates now reach canonical border resolution, activating topology-aware semantic roles during normal gameplay rendering.

## Validation performed

- Branch HEAD after code patch: `724fc22a6f920fc9db10dd702ff0a4eac3be775c` before this notes commit.
- Maven compiler target/source = Java 8; patch uses Java-8-compatible constructs (`Math.floorMod`, enums, ordinary overloads).
- `.github/workflows` does not exist on this branch; commit status list is empty. No CI execution is available through the repository.
- Repository has no `src/test` suite and `pom.xml` has no test framework dependency.
- Compare `f8e6bfd3...` -> `724fc22a...` is ahead by 5 commits and changes only:
  - `docs/lp1-recovery-notes.md`
  - `GameField.java`
  - `PieceBorder.java`
  - new `BorderVisualVariant.java`
  - `PieceVisualCatalog.java`
- No `styles/lp1.csv`, `render-map.csv`, maps or original assets were modified.
- Static source review confirms existing two-argument PieceVisualCatalog API remains for compatibility; GameField uses new four-argument API.
- Full Maven compile was **not** run in this environment because there is no usable local checkout and no CI workflow. Runtime/build validation must therefore be performed by the user locally.

## Expected runtime result

For LP1 SELECTOR:
- blank-meta outer `=` cells must no longer resolve through LP1 `black_tiles` tile 5/6;
- they should render from normalized `texture/border.png` using their semantic edge/corner roles;
- `X/WHITE` separator cells should use the alternate normalized row;
- topology-aware T-junction normalization should remain active because GameField now passes board context;
- other legacy-backed pieces/walls should remain on their previous resolver paths.

The first runtime question is **geometry/correctness**, not final LP1-themed art. If border shape is correct but appearance is not LP1-themed, next step is a style-specific `BorderVisualVariant` resource map — never a global raw `wood`/PCE remap.

## Material action log

- Recovered repo/branch and continuously maintained this note.
- Inventoried CE assets/PCE/maps and documented tool limits.
- Proved historical LP1 index-preservation/no-global-remap rule.
- Confirmed BMP headers and LP3 +2-byte tail anomaly.
- Sampled LP1 levels/PCE/render-map across all skills.
- User supplied SELECTOR screenshot and clarified universal semantic-rendering target.
- Located exact SELECTOR map; decoded border rotations/meta.
- Reproduced failure blank meta -> BLACK -> PCE row0 -> repeated 5/6.
- Verified normalized `border.png` dimensions/history outside PCE.
- Added canonical border enum (`4fcdb9fa`).
- Refactored PieceBorder semantic translation (`c3c25636`).
- Removed PieceBorder from generic legacy resolver and added context-aware catalog (`297dded7`).
- Passed board context from GameField (`724fc22a`).
- Checked Java 8 Maven config, absence of tests/workflows/status checks, and changed-file scope.
- No raw LP1 mapping rows were added.

Earlier note checkpoints include `49fa9e30`, `ee91c454`, `c6284f19`, `a2ea732b`, `8d640596`, `a2561ddc`, `f8e6bfd3`, `bf192a11`.

## Resume here

**Patch is ready for local runtime/build validation.**

Next:
1. Pull/build `linux-modernization` and open LP1 `Skill1/Level_002.map` (`SELECTOR`).
2. Compare outer border against previous screenshot.
3. Check ordinary frame, X/WHITE separator and any T-junctions.
4. Report compile error verbatim if build fails; do not speculate before fixing it.
5. If geometry is correct but style is visually generic, implement style-specific border art keyed by `BorderVisualVariant`.
6. If geometry is still wrong, inspect the exact semantic variant selected per failing coordinate; do not return to global LP1 index remapping.

Repository: `Mateozaur/OpenAargon`  
Branch: `linux-modernization`
