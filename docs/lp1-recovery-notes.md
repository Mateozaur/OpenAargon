# LP1 rendering recovery notes

_Last updated: 2026-10-18_

Hand-off source of truth for the Collector's Edition LP1 rendering investigation and migration on `Mateozaur/OpenAargon`, branch `linux-modernization`.

## Long-term target

Preserve original CE **semantics and appearance**, but do not preserve the original runtime rendering tricks as the OpenAargon architecture.

```text
legacy map/PCE/assets
  -> compatibility/import translation
  -> canonical semantic visual state
  -> PieceVisualCatalog/style resolver
  -> arbitrary style sprite/animation resources
```

PCE files and `black_tiles*.bmp` are authoritative evidence for compatibility/import, not the desired public runtime API. Renderer/style code should consume semantic identities such as an oriented border edge/corner, not raw PCE rotations or tile numbers.

## Safety rule

Do **not** add speculative rows to `src/main/resources/legacy/styles/lp1.csv`. A raw LP1 override needs a concrete verified semantic deviation. Prefer migrating concrete failures to semantic visuals instead of adding another atlas trick.

## Historical LP1 contract — high confidence

Original CE LP1 generally preserves shared PCE picture-index semantics while replacing the ordinary `black_tiles.bmp` with a pack-local same-named sheet.

Confirmed:
- `legacy/render-map.csv` is shared token/state mapping; style CSVs are sparse overrides.
- `atlas=base` selects current style; explicit `exp1`/`exp2` remain shared.
- `Pieces/*.pce` are authoritative for token/state -> picture index and logical tile-sheet family.
- `blank.pce` documents ordinary defaults `black_tiles.bmp` / `black_tiles_small.bmp`.
- LP1 has no alternate PCE corpus or map-level remap table.
- real LP1 maps use shared piece tokens.
- LP1 has pack-local ordinary sheets but no local `black_tiles_exp*`.
- LP1-LP4 all repeat the same pack-local ordinary-sheet convention.

Representative original blobs/sizes:
```text
base black_tiles.bmp       921654  2c1cebca0a437c2080352e48425150d3c374a533
LP1  black_tiles.bmp       921654  768d05581ab0a2a39beb9e29c492e869d07eb2fe
LP1  black_tiles_small.bmp  57654  e27ac267529a3f56020508c07775414f114a4032
```

Base/LP1 full BMP headers were decoded through ranged base64 reads: 640x480, 24-bit BI_RGB, pixel offset 54, 921600-byte pixel payload, 921654 total. LP3's historical +2 bytes were confirmed to be two extra terminal zero bytes / `biSizeImage` quirk, not different geometry.

Conclusion: **no global LP1 index remap**. This is compatibility truth only; modern rendering should still leave raw indices behind.

## Atlas provenance / tool limits

Legacy atlas introduction commit:
```text
2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8
Add Collector's Edition legacy rendering support
```
Its parent `98625d829872afc6a3aad6a6c89bef0261781079` only changes border T-junction rendering; no conversion script provenance was found.

Known PNG blobs:
```text
base.png e1e903a202b7d8c301f46cfa12291abe4a383ae4
lp1.png  87ab0fe3c09e2e26dca009d21e50791cfa1e3570
```

Complete BMP<->PNG pixel comparison remains unavailable: direct binary blob fetch failed UTF-8 decoding; base64 ranged reads work but full reconstruction is impractical; raw GitHub/web/container materialization attempts also failed. GitHub indexed code/commit search is known to miss existing content, so negative searches are not absence evidence.

## PCE / real-level sampling

Authoritative `Pieces/` count is 29 (an earlier spoken 27 was corrected). Direct reads cover most relevant ordinary/expansion cases, including:
- ordinary: brick, coin, convert, double, doubleslit, filter, mine, mirror, oneway, polarizer, prism, refractor, source, splitter, threeway, tnt, wood;
- exp1: blackhole, complex, prismirror, selector, spaceship;
- exp2: water;
- `convert.pce` is ordinary despite indices 260..267, proving index magnitude does not imply atlas family.

Sampled `Level_001` from every LP1 skill group and matched characteristic PCE sequences to `render-map.csv`; ordinary and explicit expansion pieces coexist exactly as expected. Selector **piece** token `I` is correctly exp1 and is not the current failure.

## Concrete failing case — LP1 `SELECTOR`

User supplied a screenshot showing:
- outer board border rendered as repeated green connector/node-like graphics;
- title-area frame rendered correctly by a different path;
- interior pieces otherwise coherent.

Exact map located:
```text
Aargon Deluxe Collectors Edition 1.4.CE/Level Packs/Level Pack 1/
Levels/Skill1/Level_002.map
MAPTEXT:[SELECTOR]
blob 1cd45a781fee5a0f9e19263a8dd84fbd3d9bd27e
```

The map parser (`MapFile.parse`) decodes each 4-character cell as:
```text
[0] token
[1] decimal rotation (blank -> 0)
[2] BeamColor token / meta
[3] mutability token
```
`BeamColor.fromToken` maps an unrecognized/blank color to `BLACK`; `X` maps to `WHITE`.

SELECTOR's ordinary outer frame uses blank meta, e.g.:
```text
=3 n   top-left
=4 n   top edge
=5 n   top-right
=2 n   left edge
=6 n   right edge
=1 n   bottom-left
=  n   bottom edge (blank rotation -> 0)
=7 n   bottom-right
```
Two special separator cells use `X/WHITE`, including `=2Xn` and `=1Xn`.

This establishes the oriented legacy rotation semantics for the normal rectangle:
```text
0 bottom horizontal / interior north
1 bottom-left corner / interior NE
2 left vertical / interior east
3 top-left corner / interior SE
4 top horizontal / interior south
5 top-right corner / interior SW
6 right vertical / interior west
7 bottom-right corner / interior NW
```

Important architectural correction: plain `WallTopology` connectivity alone is insufficient for an asymmetric frame. Left/right both have N+S connectivity and top/bottom both have E+W. Canonical border state therefore needs an **oriented edge/corner role** (or topology plus facing), not only an N/E/S/W mask. Legacy rotation may be translated into those semantic names inside the compatibility layer, but styles should not consume the number.

## Root cause reproduced from data

`PieceRegistry` maps legacy token `=` to canonical piece id `border` / `PieceBorder`.

Current non-BASE `PieceBorder` path in `PieceVisualCatalog` delegates to `LegacyRenderResolver` with raw token, stored rotation and meta.

`LegacyRenderResolver` maps OpenAargon BLACK meta to PCE color row 0. In `wood.pce` / `render-map.csv`, token `=` has:
```text
PCE color rows 0..6: rotations -> 5,6,5,6,5,6,5,6
PCE color row 7:    rotations -> 79,59,97,117,118,58,98,78
```

Therefore SELECTOR's normal blank-meta outer border **necessarily resolves to repeated tile 5/6** in the current LP1 legacy path. This exactly matches the repeated connector/node appearance in the screenshot. This is now a reproduced data-flow root cause, not a visual guess.

The special `X/WHITE` cells map to PCE color row 7 instead, explaining why the data can reach the distinct 8-slot set only for those cells.

Do not fix this by globally rewriting LP1 wood rows: board border is a canonical `PieceBorder` concept in OpenAargon and should leave generic PCE wood lookup.

## Native OpenAargon border path — strong regression evidence

`src/main/resources/texture/border.png` is 256x64: exactly 8 columns x 2 rows of 32px variants.

History check:
- `border.png` already existed in the original 2016 repository (`0d8b1ec...`, initial commit).
- Initial `PieceBorder` already rendered from `texture/border.png`, entirely outside `black_tiles`/PCE.
- Commit `d1fbfdcd7f5fb3f57bec8ef04c4751187671d384` (`Piece movement`, 2016) used:
  `new IntVector(rot * 32, (meta == 6) ? 0 : 32)`.
- Current `PieceBorder` retains this 8x2 native-sheet concept and additionally has topology-aware T-junction normalization.

So redirecting non-BASE `PieceBorder` into generic legacy atlas lookup is a later regression relative to OpenAargon's long-standing normalized border resource model.

## Current architecture

`GameField` already routes placed visuals through `PieceVisualCatalog.resolve(instance, boardStyle)` and already has cell coordinate `c` while rendering.

`PieceVisualCatalog` is intended as canonical visual façade but is transitional. It currently delegates `PieceWall`, `PieceLegacyStub`, and non-BASE `PieceBorder` to `LegacyRenderResolver`. The `PieceBorder` case is the problem.

`WallTopology` already models renderer-independent N/E/S/W connectivity and explicitly warns that CE rotation values are not universally reliable topology. `PieceBorder` exposes topology helpers and native border rendering logic.

There is currently no `src/test` suite and `pom.xml` has no test framework dependency. Do not add an entire framework just for this migration; prefer small pure semantic mapping logic plus compile/runtime validation.

The correct title-frame codepath has not yet been conclusively identified. Searches for Title/frame/border/render calls were inconclusive because repository indexing is unreliable. It is useful reference evidence but no longer blocks the border fix because the failure is reproduced independently.

## Migration design for this case

1. Introduce a canonical oriented border variant (edge/corner/facing), not raw rotation.
2. Derive it from board context/topology where unambiguous; use legacy rotation only inside the compatibility translation where orientation cannot be recovered from connectivity alone.
3. Make `PieceVisualCatalog` accept enough board context (`IGameField`, coords) for border semantic resolution.
4. Route `PieceBorder` through a border-specific semantic visual resolver for **all styles**, not generic `LegacyRenderResolver`.
5. Initially use the normalized native `border.png` resource as the canonical border sheet unless a verified style-specific border resource is added; this deliberately removes the failing dependency on LP1 `black_tiles`.
6. Keep style-specific border override capability behind the semantic variant API so future LP1/HD/custom themes can supply arbitrary resources without changing map/PCE semantics.
7. Do not add global `styles/lp1.csv` wood/border overrides.

## Action log

- Corrected repo owner and established `Mateozaur/OpenAargon` / `linux-modernization`.
- Inventoried CE assets, LP1 PACK.INI, shared/pack-local sheets, sibling packs, PCEs and levels.
- Local clone attempt failed DNS; no usable local checkout.
- Verified LP1-LP4 ordinary-sheet convention and LP1 lack of expansion replacements.
- Classified PCE ordinary/expansion cases and corrected PCE count to 29.
- Verified representative LP1 maps across all four skills against PCE/render-map data.
- Used base64 binary ranges to confirm BMP headers and resolve LP3 +2-byte tail anomaly.
- Established high-confidence no-global-LP1-remap historical conclusion.
- User supplied SELECTOR screenshot and clarified universal semantic-rendering goal.
- Read `LegacyRenderResolver`, `GameField`, `PieceVisualCatalog`, `PieceBorder`, `WallTopology`, `PieceInstance`, `VisualStyleId`, `SpriteDefinition`, `MapFile`, `BeamColor`, `PieceRegistry`, and `pom.xml`.
- Found no existing `src/test` framework.
- Identified non-BASE PieceBorder -> generic LegacyRenderResolver bypass as primary suspect.
- Three accidental no-op `update_ref` calls earlier targeted unchanged HEAD; no content/history changed.
- Tree/code searches for title-frame renderer remained inconclusive and are not treated as absence evidence.
- Exact SELECTOR map found at Skill1/Level_002; parsed its border rotations/meta.
- Confirmed blank map meta -> BLACK and X -> WHITE.
- Confirmed `render-map.csv` token 61 / `wood` rows: PCE colors 0..6 -> repeating 5/6; color 7 -> eight distinct indices.
- Reproduced screenshot failure from current resolver data flow: normal SELECTOR border blank meta -> PCE row 0 -> repeated 5/6.
- Confirmed native `border.png` is 256x64 (8x2) and dates to the 2016 initial repo; `PieceBorder` historically rendered from it outside PCE/black_tiles.
- Commit search for generic "border" was again unreliable/empty; direct path history API successfully found initial/Piece movement history.
- No functional code or `styles/lp1.csv` rows have been changed yet at this checkpoint.

## Recovery-note checkpoints

```text
49fa9e30  Finalize LP1 recovery handoff checkpoint
ee91c454  Record LP1 provenance and BMP header validation
c6284f19  Record LP3 header check and cross-skill LP1 sampling
a2ea732b  Finalize LP1 cross-skill recovery checkpoint
8d640596  Record semantic rendering migration checkpoint
a2561ddc  Record semantic-border investigation progress
```

## Resume here

Root cause for LP1 SELECTOR outer-border regression is now reproduced. Continue with the **functional semantic border migration**:

1. Re-fetch latest `PieceVisualCatalog`, `GameField`, `PieceBorder` before editing.
2. Add canonical oriented border variant type/translation.
3. Add context-aware PieceVisualCatalog resolution and have GameField pass board+coords.
4. Remove non-BASE PieceBorder from generic LegacyRenderResolver path.
5. Resolve PieceBorder through semantic variant to normalized `texture/border.png` first; preserve an explicit extension point for style-specific semantic overrides.
6. Compile/check code paths; no new test framework solely for this patch.
7. User should validate LP1 SELECTOR runtime screenshot after build.

Do not solve SELECTOR by globally remapping LP1 `wood`/token `=` in `styles/lp1.csv`.
