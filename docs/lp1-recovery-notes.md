# LP1 selector recovery notes

_Last updated: 2026-10-18_

This file is a hand-off / recovery point for the ongoing reverse-engineering work on Level Pack One (LP1), especially the `Selector` values used by OpenAargon data files.

## Safety rule

Do **not** write values to `lp1.csv` unless they are supported by concrete evidence from the original game assets / binaries or by a method that has first been validated against a known reference pack.

As of this note, `lp1.csv` has **not** been changed on the basis of guesses.

## Confirmed findings

### Original LP1 skill DLL

The original LP1 `PACK.INI` contains:

```ini
SelectSkillDll=LevelPackOne
```

This confirms that the correct level-pack-specific selector/skill library is `LevelPackOne`.

The corresponding binary in the reference game package is:

```text
Aargon Deluxe Collectors Edition 1.4.CE/RELEASE_LevelPackOne.dll
```

Observed size: **28,672 bytes**.

### Reference package available in repository

The branch contains an unpacked reference copy under:

```text
Aargon Deluxe Collectors Edition 1.4.CE/
```

and also the archive:

```text
Aargon Deluxe Collectors Edition 1.4.CE.zip
```

Candidate reference DLLs visible in the package include at least:

```text
RELEASE_LevelPackOne.dll
RELEASE_Classic.dll
RELEASE_Tutorial.dll
```

This gives us the material needed for binary comparison once the correct baseline DLL is identified.

## What is still unresolved

1. Determine exactly which original DLL corresponds to the current OpenAargon "base" selector mapping.
2. Locate the selector dispatch/table/function in that known/reference DLL.
3. Locate the homologous structure in `RELEASE_LevelPackOne.dll`.
4. Derive exact LP1 selector indices from that structure.
5. Independently validate the mapping against original LP1 assets before editing `lp1.csv`.

## Current secondary validation path: atlas/tile comparison

In parallel with the DLL comparison, inspect the data path used by OpenAargon rendering:

- enumerate entries currently mapped with `atlas=base` (from `render-map.csv` and related data),
- determine how `base.png` and `lp1.png` were generated from the original assets,
- compare tile indices between the base atlas and LP1 atlas,
- cross-check those indices with original `.pce` / `black_tiles*.bmp` data where possible.

If the LP1 atlas preserves the original game indexing, whole groups of selectors may be validated directly from the source assets. This route should be treated as corroboration unless its index-preservation rules are proven.

## Intended validation strategy

A result is strong enough to commit to `lp1.csv` only when at least one of the following holds:

- the exact selector index is recovered directly from the LP1 DLL/table and the extraction method has been validated against a known pack, or
- original asset indexing provides an unambiguous mapping and that indexing relationship has been independently verified.

Prefer both when available.

## Resume here

When resuming work:

1. Inspect the OpenAargon code/data to identify the precise semantics of the `Selector` column and the existing known-good base mapping.
2. Identify the original base `SelectSkillDll` value from its `PACK.INI`, then map it to its `RELEASE_*.dll` binary.
3. Compare that DLL structurally with `RELEASE_LevelPackOne.dll` (exports, code layout, nearby constants/strings, and candidate tables).
4. Validate any recovered table against known base CSV values before applying the same interpretation to LP1.
5. Finish the atlas/index cross-check for every LP1 entry currently falling back to `atlas=base`.
6. Only then update `lp1.csv`.

## Known branch / repository

```text
Repository: Mateozaur/OpenAargon
Branch: linux-modernization
```
