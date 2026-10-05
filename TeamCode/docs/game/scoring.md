# Match Flow and Scoring (Competition Manual Section 10, TU03, pp. 81–97)

## The game in one paragraph (10.1, p. 81)
Two ALLIANCES of 2 teams collect **POLLEN** (yellow) and **NECTAR** (red/blue). They **LAUNCH** them into their
HIVE's upward **CELL** to **TIP** the HIVE, place them in **FLOWERS**, and move them to their **GARDEN**.
Each TIP lets the human player enter **1 more NECTAR**; with 60 s left, all remaining NECTAR can be entered.
Robots finish by claiming FLOWERS with NECTAR and returning to their **LOADING ZONE**.

## Match timing (10.1, 10.4, and Table 9-1 p. 79)
| Period | Clock | Length | Notes |
|---|---|---|---|
| **AUTO** | 2:30 → 2:00 | 30 s | No driver input (G401) |
| Transition | — | 8 s | **No powered movement** (G403). OK to stop AUTO and INIT/start TeleOp, if INIT doesn't move anything |
| **TELEOP** | 2:00 → 0:00 | 2 min | |
| FLOWER ownership unlocked | 1:00 | | FLOWER scoring and entering all NECTAR allowed from here |
| End game warning | 0:20 | | |

## Setup before a match (10.3, pp. 82–85)
**Drive team:** up to 4 people: 1 DRIVE COACH (may be an adult), up to 3 STUDENT DRIVERS/HUMAN PLAYER.

**Driver Station (10.3.3, G305):** select the AUTO OpMode **with the 30 s timer enabled**, then press **INIT**.
Every team must INIT an OpMode, even without an AUTO.

**Scoring elements (10.3.1, Fig. 10-2 p. 83):**
| Where | POLLEN | NECTAR |
|---|---|---|
| Each of the 4 FLOWERS | 4 each (16) | — |
| Red GARDEN / Blue GARDEN | 4 each (8) | — |
| **Pre-loaded in each robot** | **4 each (16)** | — |
| Upward-facing CELL of each HIVE | — | 3 of that color (6) |
| Each ALLIANCE AREA | — | 5 of that color (10) |

**Robots (10.3.4, G304):** start touching **exactly 4 preloaded POLLEN** (in/on the robot, or on our side's tiles touching it).
See [rules.md](rules.md) for all starting rules. If order matters, robots are placed: Red 1st, Blue 1st, Red 2nd, Blue 2nd.

## When things are scored (10.5, p. 86)
| What | When it's assessed |
|---|---|
| HIVE TIPS | All match long. TIPS finished **before TELEOP starts** count as AUTO |
| POLLEN/NECTAR left in a CELL | **End of match** |
| FLOWER scoring | All match, final count at the end |
| GARDEN | **End of match** |
| **LEAVE and AUTO PARK** | **End of AUTO** |
| TELEOP PARK | End of match |

Scoring is done by volunteers: **make it obvious and unambiguous** that you've met the criteria.

## Scoring criteria (10.5.1–10.5.4, pp. 87–90)
- **HIVE TIP:** the HIVE flips to its other stable position (the down CELL becomes the up CELL). **Only** by LAUNCHING into the **upward** CELL (G417). Launching at a HIVE while it's tipping can stop the tip
- **Left in a CELL:** POLLEN/NECTAR in an upward CELL at the end of the match
- **FLOWER:** element at least partly between the top and middle rings. Only from **1:00 left** (G410)
  - **Bottom NECTAR Bonus:** alliance with the lowest scoring NECTAR of its color in a FLOWER
  - **FLOWER Owner:** alliance with the **top-most** NECTAR in a FLOWER scores **every** element in it, whoever placed them
- **GARDEN:** element at least partly in the GARDEN zone. Scores for the **GARDEN's color**, no matter who put it there. Not protected
- **LEAVE:** robot is **no longer touching the perimeter wall**
- **PARK:** robot is **at least partially in its LOADING ZONE** (Fig. 10-7 p. 90)

## Point values (Table 10-2, p. 91)
| Action | AUTO | TELEOP |
|---|---|---|
| **LEAVE** | **3** | — |
| **PARK** (LOADING ZONE) | **5** | **5** |
| **HIVE TIP** | **20** | **20** |
| POLLEN/NECTAR left in an upward CELL (end) | — | 2 each |
| Bottom NECTAR Bonus | — | 5 |
| POLLEN/NECTAR in an **owned** FLOWER | — | 2 each |
| POLLEN/NECTAR in GARDEN (end) | — | 1 each |

## Ranking points (Tables 10-2, 10-3, p. 91)
| RP | How | Threshold (regular events) |
|---|---|---|
| **SWARM** | Combined LEAVE + PARK points | **16 points** |
| **POLLINATOR 1** | Number of TIPS | **4 TIPS** |
| **POLLINATOR 2** | Number of TIPS | **7 TIPS** |
| WIN / TIE | | 3 / 1 |
Regional and World Championship thresholds will come in Team Updates.

⭐ **Both alliance robots doing LEAVE + PARK in AUTO = 2 × (3 + 5) = 16 → SWARM RP from AUTO alone.** (#22)

## Penalties (Table 10-4, p. 92)
| Penalty | Effect |
|---|---|
| MINOR FOUL | **+5** points to the opponent |
| MAJOR FOUL | **+20** points to the opponent |
| YELLOW CARD | Warning; a second one becomes a RED CARD |
| RED CARD | Team DISQUALIFIED for the match (0 points, 0 RP) |
| DISABLED | Robot must stop for the rest of the match |

Timing words: **MOMENTARY** < ~3 s, **CONTINUOUS** > ~10 s, **REPEATED** = more than once in a match.
**STRATEGIC** = done on purpose for an advantage (bigger penalties).
