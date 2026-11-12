# Autonomous OpModes

## Game plan (decision 060)
| Start | What we run |
|---|---|
| **NEAR** | **Timed Auton RED / BLUE NEAR** |
| **FAR** | **Main Auton RED / BLUE FAR** |

The Driver Station shows 8 AUTOs: Main Auton RED / BLUE NEAR / FAR and Timed Auton RED / BLUE NEAR / FAR. Pick the one for our alliance and start. Every start is BACK touching a wall (decision 046); the INIT screen says where.

## Main Auton (issue #22, decision 060): LEAVE + PARK with the Pinpoint, robot moves
`MainAuton.java` (was `opmodes/test/testAuton.java`, "Test Auton") + four small files that pick the alliance and route: **Main Auton RED NEAR / RED FAR / BLUE NEAR / BLUE FAR**.
- Same driving code as Basic Auton (Pinpoint, gains, tolerances, start delay, lights), but the robot always starts at **(0, 0, 0)**, and the moves are written as robot moves: +X = forward, +Y = left.
- NEAR (`MAIN_NEAR_PATH`): forward 24, right 15, back to 2 in from the start. FAR (`MAIN_FAR_PATH`): forward 85, left 22.
- RED and BLUE drive the **same moves** (the field is rotated 180°, like Timed Auton). The alliance picks the INIT light color, the SET UP line, and the Main TeleOp that comes next.
- The moves were tuned on the floor, so they no longer match Basic Auton's field positions. Change them in `MainAuton.java` only.

## Basic Auton (issue #22, decisions 014, 020, 026): LEAVE + PARK, drive-only
⚠️ **Archived (decisions 058, 060):** the four OpModes are now in `opmodes/archive/` with `@Disabled`, so they don't show on the Driver Station. `BasicAuton.java` stays here, because Main Auton and Timed Auton use its code. To bring one back, delete its `@Disabled` line, then build and deploy.

Four OpModes in the **Basic Auton** group on the Driver Station. Pick the one that matches our alliance and starting spot:

| OpMode | Start | Route |
|---|---|---|
| Basic Auton RED NEAR | Tile **A6**, **back** against the **red wall**, facing into the field | Drive forward off the wall, slide toward the audience, park in the **far-wall third** of the LOADING ZONE (A5) |
| Basic Auton RED FAR | Tile **B1**, back against the **audience wall** | Off the wall, up column B (clear of the HIVE), park on the line between rows **4 and 5**, partly in the LOADING ZONE (the rest of row 5 is left for our partner) |
| Basic Auton BLUE NEAR | Tile **F1**, **back** against the **blue wall**, facing into the field | Same as RED NEAR, rotated 180° |
| Basic Auton BLUE FAR | Tile **E6**, back against the **far wall** | Same as RED FAR, rotated 180° |

- **Every start is BACK to the wall** (decision 046). The INIT screen says exactly how to place the robot ("SET UP: ...").
- **During INIT:** both lights show the alliance color. **Dpad up/down** sets a **start delay** (0-10 s) for when our partner's route crosses ours. Keep the robot **still** until the Pinpoint says READY. Nothing moves.
- **After START:** waits for the delay, then drives each waypoint using the **Pinpoint** (not timers). First it waits up to 3 s for the Pinpoint to be READY; if it isn't, the robot does NOT drive (YELLOW). Lights: **GREEN** = parked, **YELLOW** = stopped early (a waypoint timed out, out of time, or Pinpoint not ready).
- After AUTO, the matching **Main TeleOp** is queued up automatically.

All the code is in `BasicAuton.java`. The four small files only pick the alliance and route.
**Positions are written once, for RED** (field coordinates, decision 011). BLUE is calculated by rotating 180°. **The start heading decides which field direction is "forward"**: NEAR starts facing -90, so forward 23 means y goes from 63 to 40 and x stays 60 (decision 053). Each waypoint's comment says the move in robot words. The route sheets (`docs/game/diagrams/basic-auton-near.pdf` / `far.pdf`) are drawn from this code by `make_diagrams.py`.

| Route | Moves (robot center) | RED field positions |
|---|---|---|
| NEAR | forward 23, right 12, back 18 | (60, 63) → (60, 40) → (48, 40) → (48, 58): far-wall side of the LOADING ZONE, back 5 in off the wall |
| FAR | forward 85, left 22 (same as Main Auton FAR) | (-63, 36) → (22, 36) → (22, 58): on the line between rows 4 and 5, partly in the LOADING ZONE, leaving the rest of row 5 for our partner |

Main Auton (above) started as these moves from a (0, 0, 0) start; it has since been tuned on its own.

### Tuning (#22)
In `BasicAuton.java` (every value has a row in `docs/tuning-guide.md`):
- `ROBOT_LENGTH_IN`, `ROBOT_WIDTH_IN`: the competition robot's size
- `RED_NEAR_START`, `RED_NEAR_PATH`, `RED_FAR_START`, `RED_FAR_PATH`: the routes. Re-run `make_diagrams.py` after changing them
- `DRIVE_GAIN`, `MAX_DRIVE_POWER`, `MAX_SIDEWAYS_POWER`, `MIN_DRIVE_POWER`, the brake lookaheads, `TURN_GAIN`, `MIN_TURN_POWER`: how it drives
- `POSITION_TOLERANCE_IN`, `HEADING_TOLERANCE_DEG`, `SETTLED_SPEED_IN_PER_S`: when a waypoint counts (close enough AND stopped)
- `WAYPOINT_TIMEOUT_S` + `WAYPOINT_TIMEOUT_PER_INCH_S`: when to give up on a waypoint (long drives get longer)
- `USE_ENCODER_SPEED_CONTROL`: `false`, plain power like the whole robot (decision 055). If it is ever set back to `true`, re-tune the powers
- Needs the Pinpoint tuned on this robot first (#21): wrong pod offsets = wrong positions

## Timed Auton (NEAR: what we run; FAR: backup. Decisions 054, 057, 060)
Four OpModes in the **Timed Auton** group: **Timed Auton RED / BLUE NEAR** and **Timed Auton RED / BLUE FAR**. Same setups as Basic Auton (back touching a wall, decision 046).
- NEAR drives the same box as Basic Auton NEAR (forward, right, back) by **time**: `DRIVE_POWER` for `FORWARD_S`, `RIGHT_S`, `BACK_S` seconds, with `PAUSE_S` between moves
- FAR drives an L: forward 85 in, then left 23 in (shorter forward and a bit more left than Basic Auton FAR's 100 / 20): `FAR_FORWARD_S`, then `FAR_LEFT_S`. All in `TimedAuton.java`
- **No Pinpoint, no encoders, no telemetry, no lights.** Nothing to calibrate, nothing to wait for. But battery level and bumps change how far it goes
- RED and BLUE are the same moves (the field is rotated 180°); the two files only pick the name and the TeleOp that comes next
- **NEAR is what we run (decision 060)**; FAR is the backup for Main Auton FAR. Tune the seconds with a charged battery (rows in `docs/tuning-guide.md`)

## Advanced Auton (issue #25)
Pedro Pathing, later. Launching, the turret and the camera belong there, not in Basic Auton (decision 020).
