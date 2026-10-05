# The Field (Competition Manual Section 9, TU02, pp. 63–79)

All drawings in the manual are seen **from the audience**: audience at the bottom, **Red on the left**, **Blue on the right**.
Key figures: **Fig. 9-2** zones (p. 65), **Fig. 9-5** tile grid (p. 67), **Fig. 10-2** match setup (p. 83), **Fig. 11-1** legal starting spots (p. 105).

## Size and tiles (9.2, 9.4, pp. 64–67)
- **144 × 144 in**, made of **36 foam tiles**, each **24 × 24 in**
- Tile names (Fig. 9-5): columns **A–F** left → right (Red → Blue), rows **1–6** audience → far wall
- Dimensions in the manual are nominal, **± 1 in**. Fields vary a little between events, so our code shouldn't depend on exact measurements

## Map (approximate, from Figs. 9-2 and 10-2)
```
                              FAR WALL
           A         B         C         D         E         F
      +---------+---------F---------+---------+---------+---------+
   6  |         |         |         |         |         |  =====  |  ===== Blue GARDEN (along far wall)
      +---------+---------+---------+---------+---------+---------+
   5  |[Red LZ] |         |         |         |         |         |
  R   +---------+---------+---------+---------+---------+---------F  B
  E 4 |         |         | Red     | Blue    |         |         |  L
  D   +---------+---------+-- HIVE -+- HIVE --+---------+---------+  U
    3 |         |         | (C3-C4) | (D3-D4) |         |         |  E
  W   F---------+---------+---------+---------+---------+---------+
  A 2 |         |         |         |         |         |[Blue LZ]|  W
  L   +---------+---------+---------+---------+---------+---------+  A
  L 1 |  =====  |         |         |         |         |         |  L
      +---------+---------+---------+---------F---------+---------+  L
           RED SIDE (A–C)           |          BLUE SIDE (D–F)
                            AUDIENCE WALL
   F = FLOWER (on the wall, at a tile seam: far wall B/C, blue wall 4/5, audience wall D/E, red wall 2/3)   LZ = LOADING ZONE   ===== = GARDEN
```
⚠️ **The field is a 180° rotation, not a mirror image.** Red's LOADING ZONE is near the far wall (A5);
Blue's is near the audience (F2). Gardens are in opposite corners.

## Zones and areas (9.3, pp. 65–66)
| Zone | Where | Size | Notes |
|---|---|---|---|
| **ALLIANCE AREA** | Outside the field: Red left, Blue right | ~97 × 54 in | Where the drive team stands. 5 NECTAR start here |
| **LOADING ZONE** | Red: red wall at **A5**. Blue: blue wall at **F2** | ~23 × 11 in, against the wall | **PARK** here (5 pts). Humans enter NECTAR here. Robots may **not start** in it (G304.E) |
| **GARDEN** | Red: corner **A1** along the audience wall. Blue: corner **F6** along the far wall | ~23 × 2 in strip | 4 POLLEN start here. 1 pt per element at **end of match** |

**Red side** = columns **A, B, C**. **Blue side** = **D, E, F**. Robots must stay on their own side during AUTO (G402).

## HIVE Structure (9.6, pp. 69–72)
- In the **center** of the field: a frame (~49.5 in wide × 39 in deep at the base) holding a **Red HIVE** (column C side) and a **Blue HIVE** (column D side)
- Each HIVE has **2 CELLS**, one at each end, ~18.8 in apart, on a pivot **~44 in above the tiles**
- **Bi-stable:** one CELL faces **up** at a time. Launching enough elements into the **upward** CELL **TIPS** the HIVE, and the other CELL then faces up
- **CELL opening:** ~**20 in wide × 14 in tall × 12 in deep**
- At match start, the CELL that "points at" a FLOWER is tilted **down** (p. 83). 3 NECTAR of the HIVE's color start in the upward CELL

## FLOWERS (9.7, pp. 72–73)
- **4 FLOWERS**, attached to the perimeter walls, roughly one per wall (see map)
- Top opening ~**4 in** diameter, **~21.5 in** above the tiles, with a small backstop
- **Retrieval opening** at the bottom (~3.55 in tall) for robots to remove **POLLEN** (not NECTAR)
- FLOWER scoring only counts from **1:00 left** (G410)

## Scoring elements (9.8, p. 74)
| Element | Size | Color | Count |
|---|---|---|---|
| **POLLEN** | ~2.8 in ball | Yellow | 40 |
| **NECTAR** | ~3.6 in ball | Red / Blue | 8 of each |
Not perfectly round and may vary in size. Intake and launcher must handle both sizes.

## AprilTags (9.9, pp. 74–77)
- 36h11 family, 3.25 in square, in **clusters of 4** on one sticker
- ⚠️ Each cluster is on the **bottom face of a CELL, facing DOWN** toward the tiles. **The camera must tilt up** (#7)
- They move whenever a HIVE tips, so **don't use them for field position** (decision 002)

| CELL | Tag IDs |
|---|---|
| Red, far side | 30, 31, 32, 33 |
| Red, audience side | 34, 35, 36, 37 |
| Blue, audience side | 38, 39, 40, 41 |
| Blue, far side | 42, 43, 44, 45 |

## Our field coordinate system (decision 011)
Used by all autonomous code. Matches the manual's tile names and our direction convention (decision 007).
- **Origin:** center of the field (where C3, C4, D3, D4 meet)
- **+X → far wall** (away from the audience). **+Y → red wall** (left, from the audience). **Heading:** counter-clockwise, 0° = facing +X
- **Tile centers:** rows 1–6 at **x = −60, −36, −12, +12, +36, +60**. Columns A–F at **y = +60, +36, +12, −12, −36, −60**
- Walls at x = ±72 and y = ±72 (inside surface)
- **Blue = Red rotated 180°:** (x, y, heading) → (−x, −y, heading + 180°)

Approximate positions (inches, ± a couple of inches; check the real field):
| Thing | Red | Blue |
|---|---|---|
| LOADING ZONE center | (36, 66.5) | (−36, −66.5) |
| GARDEN (strip along wall, from corner) | x ≈ −71, y = 49 → 72 | x ≈ +71, y = −49 → −72 |
| FLOWER on own alliance wall | (−24, 72) | (24, −72) |
| FLOWER on far / audience wall | far wall (72, 24) | audience wall (−72, −24) |
| HIVE | column C, x ≈ −24 → 24 | column D, x ≈ −24 → 24 |
