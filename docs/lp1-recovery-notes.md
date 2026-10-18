# LP1 legacy rendering recovery notes

_Last updated: 2026-10-18_

Live hand-off state for Collector's Edition Level Pack 1 (LP1) rendering investigation and migration.

## Long-term goal

Preserve original Collector's Edition **semantic appearance and behaviour** without preserving the original game's runtime rendering tricks as architectural requirements.

Original maps, PCE files and `black_tiles*.bmp` are compatibility/import truth. Target runtime model:

```text
legacy map/PCE/resources
        -> compatibility/import translation
        -> canonical semantic visual identity/state
        -> PieceVisualCatalog / style resolver
        -> style-specific sprite/animation resource
```

The renderer should eventually ask for semantic variants (for example a border topology), not "PCE rotation 5" or "tile 176 in black_tiles.bmp". Styles may use unrelated atlases, separate images or different resolutions.

## Safety rule

Do **not** add rows to `src/main/resources/legacy/styles/lp1.csv` from visual guesses, equal dimensions, index ranges or unvalidated assumptions. Prefer migrating concrete failing cases toward canonical semantic visuals rather than adding another legacy-atlas trick.

## Historical LP1 contract — resolved with high confidence

For the original CE mechanism, LP1 generally preserves shared PCE picture indices while replacing the ordinary `black_tiles.bmp` with a pack-local same-named sheet.

Confirmed:

- `src/main/resources/legacy/render-map.csv` is shared token/state mapping; style CSVs are sparse overrides.
- `atlas=base` resolves against current style; explicit `exp1` / `exp2` remain shared expansion atlases.
- Original `Pieces/*.pce` tables are authoritative for token/state -> picture index and logical tile-sheet selection.
- `blank.pce` documents ordinary defaults as `black_tiles.bmp` / `black_tiles_small.bmp`.
- LP1 has no alternate PCE corpus or map-level index remap table.
- Real LP1 maps contain shared piece tokens.

Therefore do **not** globally remap LP1 base indices. This is an import/compatibility fact, not a requirement that the modern runtime expose raw PCE indices.

## Important resolved distinction

`Pieces/selector.pce` (`I`, token 73) explicitly uses `black_tiles_exp1.bmp` / `black_tiles_small_exp1.bmp`, and its values match existing `render-map.csv` Selector rows under `atlas=exp1`. The **Selector piece** is not the current bug.

The concrete failing case is the **outer board border of the LP1 level named SELECTOR**.

## DLL dead ends

- LP1 `PACK.INI`: `SelectSkillDll=LevelPackOne`.
- `RELEASE_LevelPackOne.dll` is skill/level-selection UI, not sprite mapping.
- `RELEASE_ItemMap.dll::MAP::SelectTile(CPosition)` and `RELEASE_TileBlitter.dll` were not mapping sources.

## Original CE resources

Shared/root sheets:

```text
Pictures/black_tiles.bmp                 921654  2c1cebca0a437c2080352e48425150d3c374a533
Pictures/black_tiles_exp1.bmp            491576  adc86dffc884c39574c46bd1f7cbf329378be876
Pictures/black_tiles_exp2.bmp             24630  777e69561f964de3d335225b0d244b7c27b4e26b
Pictures/black_tiles_small.bmp            57654  f12083c95843dd58ee66f42dd5fe8569732d0a61
Pictures/black_tiles_small_exp1.bmp       30776  d6259db4d766c373db8d86c6851a0bbef84ef24d
Pictures/black_tiles_small_exp2.bmp        1590  f0df8506a8e4515c6c1ce53ce8dd7b0373bf44d3
```

LP1 pack-local ordinary pair:

```text
Level Pack 1/Pictures/black_tiles.bmp        921654  768d05581ab0a2a39beb9e29c492e869d07eb2fe
Level Pack 1/Pictures/black_tiles_small.bmp   57654  e27ac267529a3f56020508c07775414f114a4032
```

LP1 has no `black_tiles_exp*`, so expansion sheets remain shared.

All four level packs repeat the pack-local ordinary-sheet convention:

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

`GitHub.fetch_file(..., encoding=base64)` enabled ranged binary inspection after direct blob reading failed with `UnicodeDecodeError`.

Base and LP1 full ordinary BMPs are confirmed 640x480, 24-bit BI_RGB, pixel offset 54, pixel payload 921600, total 921654. LP1 X/Y pixels-per-meter = 5272; base = 5176.

LP3 `+2 byte` anomaly is resolved: same full/small geometry and 24-bit BI_RGB format, but `biSizeImage` and file size include two extra terminal NUL bytes. It is an encoder/file-tail quirk, not different slot geometry.

A complete LP1 BMP -> OpenAargon `lp1.png` pixel comparison remains outstanding because complete binary transfer was not practical. Alternate raw GitHub/web/container materialization attempts also failed.

## Atlas provenance

Actual introduction commit:

```text
2e50ecbe87b3907095cfcf1ff77e82ec8f12d8e8
Add Collector's Edition legacy rendering support
```

It introduced all legacy PNG atlases together. Parent `98625d829872afc6a3aad6a6c89bef0261781079` only changes border T-junction rendering; no checked-in conversion provenance was found.

Known PNG blobs:

```text
base.png e1e903a202b7d8c301f46cfa12291abe4a383ae4
lp1.png  87ab0fe3c09e2e26dca009d21e50791cfa1e3570
```

Repository/commit search negatives around conversion provenance are not trusted as absence evidence because connector indexing missed known terms/commits.

## LP1 PACK.INI

```ini
[Params]
Text = Try these levels in one of our secret deep space installations.
Position = 7000
Edit = false
SelectSkillDll=LevelPackOne
IsEmpty=false
```

No tile filename, Pictures path, atlas or resource-search-path setting exists.

## Real LP1 map/PCE sampling

Sampled `Level_001` from every skill group:

```text
Skill1 LANDING LIGHTS:
  O Coin ordinary; S Source ordinary

Skill2 OUTSIDE THE BOX:
  I Selector exp1; O Coin ordinary; W OneWay ordinary (211..218);
  R Refractor ordinary (176..179); S Source ordinary

Skill3 POSTCARD FROM HOME:
  T ThreeWay ordinary (268..275); F Filter ordinary;
  P Prism ordinary (80..87); O/S ordinary

Skill4 MOTHER BOARD:
  # Brick ordinary; % Double ordinary (174/175/99/119);
  < Splitter ordinary (180..187); R/P/O/S ordinary
```

`render-map.csv` matches checked PCE sequences. Strings such as `O Rn` encode state after the piece token; following letters are not separate pieces.

## PCE inventory checkpoint

Authoritative shared `Pieces/` listing contains 29 `.pce` files. Preliminary spoken count 27 was corrected.

Directly read/classified:

```text
blackhole exp1; blank defaults; brick ordinary #; coin ordinary O;
complex exp1; convert ordinary (260..267); double ordinary %;
doubleslit ordinary +; explosion animation-list-only/unclassified;
filter ordinary F; mine ordinary m; mirror ordinary /; oneway ordinary W;
polarizer ordinary :; prism ordinary P; prismirror exp1 M;
refractor ordinary R; selector exp1 I; source ordinary S;
spaceship exp1 p; splitter ordinary <; threeway ordinary T;
tnt ordinary X; water exp2 w; wood ordinary =
```

`convert.pce` proves high index does not imply expansion atlas; `water.pce` is explicit exp2 contrast.

## Concrete failing case — LP1 level SELECTOR

User supplied a screenshot from current OpenAargon.

Observed:

- outer board border renders as repeated green connector/node-like graphics instead of intended frame;
- title-area frame at the top renders correctly;
- title frame uses a different rendering path;
- interior pieces are coherent enough that this is not evidence of a global LP1 atlas failure.

Treat this as the first concrete semantic-rendering migration case, not justification for global `lp1.csv` remapping.

## Current OpenAargon architecture

### `PieceVisualCatalog`

`GameField` resolves placed visuals through `PieceVisualCatalog.resolve(instance, boardStyle)`. This is already documented as the canonical visual lookup façade.

It is still transitional: `PieceWall`, `PieceLegacyStub`, and non-BASE `PieceBorder` delegate to `LegacyRenderResolver` using raw token + `instance.getRotation()` + meta + selected legacy tileset.

For LP1 border this bypasses board-derived topology and is the primary suspect for SELECTOR.

### `WallTopology`

`WallTopology` already provides renderer-independent four-neighbour N/E/S/W connectivity. Its own docs warn CE rotations can differ for identical geometry, so topology must not be inferred from legacy rotation alone.

### `PieceBorder`

`PieceBorder` exposes `getBorderTopology(field, coords)` and `getRenderRotation(field, coords, rot)`. Native `texture/border.png` logic uses actual topology to normalize two T-junction cases, otherwise normalized stored rotation. That native-sheet rule is not yet a general semantic style contract.

### `GameField`

The render loop already has both `PieceInstance instance` and cell coordinate `c`. Therefore board context/coordinates can be passed into `PieceVisualCatalog` without changing map storage or parsing.

### Tests/build

Current repository tree contains no `src/test` suite and `pom.xml` has no test framework dependency. Do not add an entire framework merely for this migration. Prefer extracting pure topology-to-semantic-variant logic that can be unit-tested later and keep the immediate change compile-oriented/minimal.

## Title-frame investigation

The correct title-area frame remains useful as a visual/reference path, but no obvious source path has yet been identified.

Searches attempted after the semantic checkpoint include:

- `Title` in recursive tree/code search;
- `frame` in tree;
- `texture/border` / `border.png`;
- `RenderManager.bufferQuad`;
- `MAPTEXT RenderManager bufferQuad texture`;
- recursive current-tree inspection of Java/render paths.

None yielded an unambiguous title-frame renderer yet. GitHub code search is known to be incomplete on this repository, so continue using recursive tree paths and direct file reads rather than treating these negatives as proof of absence.

## Migration direction for SELECTOR border

1. Keep PCE/legacy tables as compatibility evidence/fallback while migration is incomplete.
2. Allow canonical visual lookup to receive board context/coords or an already-derived semantic variant.
3. Derive `WallTopology` from the actual board for `PieceBorder`.
4. Convert topology to a canonical border visual identity independent of legacy rotation numbers.
5. Let each style map that identity to its own sprite resource/coordinates.
6. Do not expose temporary legacy tile numbers as the public contract.
7. If a temporary topology -> legacy-resource bridge is required, keep it internal and document it explicitly as transitional.

## Current technical conclusion

Two conclusions coexist:

1. **Historical:** LP1 ordinary resources preserve shared PCE index semantics with pack-local `black_tiles.bmp`; no broad LP1 remap is justified.
2. **Modernization:** OpenAargon should not expose/depend on that physical index scheme long-term. Canonical semantic visuals should sit between game state and style resources.

SELECTOR is a strong first migration case because `WallTopology` already exists and the failing non-BASE border path currently bypasses it.

## Action log — 2026-10-18

This log intentionally includes successful checks, negative searches, corrections and failed tool/environment attempts.

- Corrected initial wrong repository-owner probe; correct repo is `Mateozaur/OpenAargon`.
- Inventoried CE tree, LP1 PACK.INI, shared/pack-local tile sheets, sibling packs, PCEs and LP1 levels.
- Attempted shallow local clone; environment DNS could not resolve github.com, so no usable clone was created.
- Confirmed LP1-LP4 pack-local ordinary sheet convention and LP1 lack of local expansion sheets.
- Classified PCE ordinary/expansion cases; corrected PCE count 27 -> 29.
- Recorded `convert.pce` high ordinary indices and exp1/exp2 counterexamples.
- Indexed source searches for loader behavior were inconclusive due connector search limits.
- Direct binary blob read failed with UnicodeDecodeError; base64 ranged access later succeeded.
- Confirmed base/LP1 BMP headers and resolved LP3 +2-byte tail anomaly.
- Sampled Level_001 from all four LP1 skills and matched characteristic tokens/PCE sequences against `render-map.csv`.
- Attempted alternate full-binary raw GitHub/web/container route; no complete binary was materialized.
- Established high-confidence no-global-remap conclusion for original LP1 indices.
- User supplied SELECTOR screenshot showing concrete outer-board-border failure while title frame renders correctly by another path.
- User clarified long-term goal: universal semantic visual standard, with legacy assets only as compatibility/import truth.
- Read `LegacyRenderResolver`, `GameField`, `PieceVisualCatalog`, `PieceBorder`, `WallTopology`, `PieceInstance`, `VisualStyleId`, `SpriteDefinition` and `pom.xml` during architecture review.
- Discovered `GameField` already routes placed visuals through `PieceVisualCatalog`.
- Identified non-BASE `PieceBorder` delegation to `LegacyRenderResolver` using raw stored rotation/meta, bypassing topology.
- Confirmed `WallTopology` is already the renderer-independent semantic connectivity abstraction needed for migration.
- Three accidental no-op `update_ref` calls targeted the already-current `a2ea732b...` HEAD with matching lease; no repository content/history changed.
- Checkpointed semantic migration direction in `8d640596d61eb024a9c4adc3344b234750a657da`.
- Re-fetched exact current `PieceVisualCatalog`, `GameField`, `PieceBorder`, `WallTopology` implementations after that checkpoint.
- Re-read `pom.xml`; confirmed no existing test framework/dependency and no `src/test` tree was found in current recursive repository tree.
- Confirmed `GameField` render loop already has cell coordinate `c`, so semantic lookup can receive context without changing persistent map format.
- Searched for the correct title-frame codepath/resources via `Title`, `frame`, `texture/border`, `border.png`, `RenderManager.bufferQuad`, `MAPTEXT RenderManager bufferQuad texture`, and recursive tree/render-path inspection; no unambiguous renderer found yet.
- Because repository code search has already missed known content, these title-frame search negatives are recorded as unresolved discovery, not absence evidence.
- No functional code and no `styles/lp1.csv` mapping rows have been changed yet in the semantic-border stage.

## Recovery-note checkpoints

Known checkpoints:

```text
9695a790  Checkpoint LP1 recovery investigation
26eda22c  Record OpenAargon LP1 fallback alignment
49fa9e30  Finalize LP1 recovery handoff checkpoint
ee91c454  Record LP1 provenance and BMP header validation
c6284f19  Record LP3 header check and cross-skill LP1 sampling
a2ea732b  Finalize LP1 cross-skill recovery checkpoint
8d640596  Record semantic rendering migration checkpoint
```

The action log is authoritative when commit summaries are incomplete.

## Resume here

Continue the **semantic border migration**, not speculative LP1 atlas remapping.

Immediate work:

1. Use recursive tree paths/direct reads to locate the title-area frame renderer if practical.
2. Re-fetch exact key classes immediately before functional edits.
3. Introduce the smallest context-aware canonical lookup path needed for `PieceBorder`.
4. Define a pure canonical border variant from `WallTopology` rather than legacy rotation.
5. Map native/base and LP1 visual resources to that semantic identity; if an internal legacy bridge is temporarily necessary, keep it explicitly transitional.
6. Compile/validate without introducing a new test framework solely for this change.
7. Validate specifically against the SELECTOR outer border.

Do not add global LP1 index overrides.

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
```