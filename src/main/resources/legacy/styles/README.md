# Legacy visual style overrides

`render-map.csv` describes the Collector's Edition base mapping.

Alternate visual styles can override only the entries that differ from the base
mapping by adding a file named after the `LegacyTileSet` id, for example:

- `classic.csv`
- `lp1.csv`
- `lp2.csv`
- `lp3.csv`
- `lp4.csv`
- `pocket.csv`
- `hotter.csv`

Each file uses the same columns as `legacy/render-map.csv`:

```text
tokenCode,piece,atlas,color,rotation,tile,animated,animationIndex
```

The lookup order is:

1. the selected style's sparse override file;
2. the base Collector's Edition mapping as a compatibility fallback.

This fallback is intentionally temporary for entries that have not yet been
verified against the original style assets. It must not be interpreted as proof
that alternate atlases use the same tile indices as Base.

`atlas=base` means "use the atlas belonging to the selected visual style".
`exp1` and `exp2` remain fixed shared expansion atlases.

A verified example is Selector (`I`, token code 73): the original
`Pieces/selector.pce` explicitly selects `black_tiles_exp1.bmp`, and its
`[PICTURES(COLOR,ROTATION)]` table matches the `atlas=exp1` entries in
`render-map.csv`. Selector therefore does not receive per-style overrides such
as entries in `styles/lp1.csv`.
