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

Normal outer frame uses blank meta:
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

Plain `WallTopology` alone is insufficient for asymmetric framing: left/right share N+S connectivity, top/bottom share E+W. Canonical state therefore needs oriented edge/corner role (or topology+facing). Legacy numeric rotation may be translated at the compatibility boundary but must not leak to styles.

## Root cause reproduced

Legacy token `=` resolves to canonical `border` / `PieceBorder`.

Before the new migration patch, non-BASE `PieceBorder` in `PieceVisualCatalog` was delegated to `LegacyRenderResolver` with raw rotation/meta.

`LegacyRenderResolver`: OA BLACK -> PCE color row 0. `wood.pce`/`render-map.csv` token `=`:
```text
PCE rows 0..6 -> 5,6,5,6,5,6,5,6
PCE row 7     -> 79,59,97,117,118,58,98,78
```

Thus SELECTOR blank-meta outer frame necessarily became repeating 5/6 under LP1, matching the screenshot. `X/WHITE` reaches row 7. This is a reproduced data-flow cause, not a visual guess.

Do **not** fix it by globally remapping LP1 `wood`: board border is a canonical PieceBorder concept and should not use generic PCE wood lookup.

## Native normalized border evidence

`texture/border.png` is 256x64 = 8x2 32px variants.

Path history shows it existed in 2016 initial commit `0d8b1ec...`; initial `PieceBorder` already rendered from it outside PCE/black_tiles. By `d1fbfdcd7f5fb3f57bec8ef04c4751187671d384` (`Piece movement`) offset was `rot*32`, row `(meta==6 ? 0 : 32)`. Current PieceBorder still uses the normalized sheet plus topology T-junction corrections.

Therefore routing non-BASE borders into generic legacy atlases was a later architectural regression relative to OpenAargon's long-standing normalized border model.

## Current semantic-border implementation

### New `BorderVisualVariant`

Commit `4fcdb9faa009652e1a206a264920b2fc29d2973c` added `render/visual/BorderVisualVariant.java`.

Canonical roles:
```text
BOTTOM_EDGE, BOTTOM_LEFT_CORNER, LEFT_EDGE, TOP_LEFT_CORNER,
TOP_EDGE, TOP_RIGHT_CORNER, RIGHT_EDGE, BOTTOM_RIGHT_CORNER,
TEE_SOUTH, TEE_NORTH
```

`fromLegacyRotation()` is explicitly a compatibility translation. Each role currently has a default `border.png` column; T-junction semantic roles alias the historical native columns 4/5. Styles may later map the semantic enum to arbitrary resources.

### `PieceBorder`

Commit `c3c25636922af662426a3f9fdb6c97d06e05991d` added `getVisualVariant(field, coords, legacyRotation)`.

- Legacy 0..7 is immediately translated to named semantic role.
- Board topology wins for W+E+S (`TEE_SOUTH`) and W+E+N (`TEE_NORTH`).
- Existing `getRenderRotation()` remains only as compatibility accessor returning default native column.
- Native `getTextureOffset()` now consumes the semantic variant.

### `PieceVisualCatalog`

Commit `297dded77df24569bca17eec6fc824287d8b6b7d` removes `PieceBorder` from the generic `LegacyRenderResolver` decision.

New context-aware overload:
```text
resolve(PieceInstance, VisualStyleId, IGameField, IntVector coords)
```
The old overload remains and delegates with null context.

For PieceBorder the catalog now:
- asks PieceBorder for `BorderVisualVariant`;
- preserves semantic WHITE/X vs ordinary meta as the two normalized rows;
- returns `texture/border.png` + semantic default column;
- intentionally ignores style for now as a canonical baseline.

This is deliberate: **all styles stop depending on `black_tiles` for board border immediately**. Future LP1/HD/custom border art should override semantic variants in the catalog/data registry, not reintroduce PCE tile numbers.

`PieceWall` and `PieceLegacyStub` still use the CE legacy resolver and are separate future migrations.

## GameField status

`GameField` already has `PieceInstance` + cell coordinate `c` during rendering. It still calls the old two-argument `PieceVisualCatalog.resolve(instance, boardStyle)` at this checkpoint. Next functional edit must pass `this, c` to activate topology-aware border resolution in the canonical catalog.

## Tests/build

No `src/test` suite and no test framework dependency in `pom.xml`. Do not add a framework solely for this migration. Validate by source consistency/build where available and user runtime test. Title-frame codepath remains unambiguously unidentified; it no longer blocks the fix because root cause is independently reproduced.

## Action log (material actions)

- Recovered repo/branch, inventoried CE assets/PCE/maps, documented tool limitations.
- Proved original LP1 index preservation and no-global-remap rule.
- Confirmed BMP geometry and LP3 tail anomaly via base64 binary ranges.
- Sampled LP1 levels/PCE/render-map across all four skill groups.
- User supplied SELECTOR screenshot and clarified universal semantic-rendering target.
- Reviewed LegacyRenderResolver, GameField, PieceVisualCatalog, PieceBorder, WallTopology, PieceInstance, VisualStyleId, SpriteDefinition, MapFile, BeamColor, PieceRegistry, pom.xml.
- No existing test suite found.
- Located exact SELECTOR map and decoded border state.
- Reproduced failure: blank meta -> BLACK -> PCE row0 -> repeated 5/6.
- Verified native border.png 8x2 and 2016 history outside PCE.
- Commit search remained unreliable; direct path history API succeeded.
- Added semantic enum (`4fcdb9fa`).
- Refactored PieceBorder semantic translation (`c3c25636`).
- Removed PieceBorder from generic legacy resolver and added context-aware catalog API (`297dded7`).
- No `styles/lp1.csv` rows changed.

Earlier note checkpoints include `49fa9e30`, `ee91c454`, `c6284f19`, `a2ea732b`, `8d640596`, `a2561ddc`, `f8e6bfd3`.

## Resume here

1. Update `GameField.bufferRenders` to call `PieceVisualCatalog.resolve(instance, boardStyle, this, c)`.
2. Inspect resulting source/import consistency; remove unused imports only if needed.
3. Search for all `PieceVisualCatalog.resolve` callers and ensure compatibility.
4. Validate branch/build if possible.
5. Update this note with validation result.
6. User runtime-test LP1 SELECTOR: ordinary outer border should stop showing repeated LP1 atlas tile 5/6. Verify both normal blank-meta frame and `X/WHITE` separator cells.
7. If geometry is correct but art is not LP1-themed, add style-specific border art **by `BorderVisualVariant`**, not by raw PCE indices.

Repository: `Mateozaur/OpenAargon`  
Branch: `linux-modernization`
