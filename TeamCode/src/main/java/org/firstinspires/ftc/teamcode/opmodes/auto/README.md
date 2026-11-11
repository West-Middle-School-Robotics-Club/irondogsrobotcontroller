# Autonomous OpModes

## Basic Auton (issue #22, decisions 014, 020, 026): LEAVE + PARK, drive-only
Four OpModes in the **Basic Auton** group on the Driver Station. Pick the one that matches our alliance and starting spot:

| OpMode | Start | Route |
|---|---|---|
| Basic Auton RED NEAR | Tile **A6**, **back** against the **red wall**, facing into the field | Drive forward off the wall, slide toward the audience, park in the **far-wall third** of the LOADING ZONE (A5) |
| Basic Auton RED FAR | Tile **B1**, back against the **audience wall** | Off the wall, up column B (clear of the HIVE), park in the **audience third** of the LOADING ZONE |
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
| FAR | forward 100, left 20 | (-63, 36) → (37, 36) → (37, 56): left side in the LOADING ZONE |

To tune a route on any clear floor, use `opmodes/test/testAuton` ("Test Auton NEAR / FAR"): same code, but it starts at (0, 0, 0) and holds the same moves as forward / left distances. Change one file, change the other by hand.

### Tuning (#22)
In `BasicAuton.java` (every value has a row in `docs/tuning-guide.md`):
- `ROBOT_LENGTH_IN`, `ROBOT_WIDTH_IN`: the competition robot's size
- `RED_NEAR_START`, `RED_NEAR_PATH`, `RED_FAR_START`, `RED_FAR_PATH`: the routes. Re-run `make_diagrams.py` after changing them
- `DRIVE_GAIN`, `MAX_DRIVE_POWER`, `MAX_SIDEWAYS_POWER`, `MIN_DRIVE_POWER`, the brake lookaheads, `TURN_GAIN`, `MIN_TURN_POWER`: how it drives
- `POSITION_TOLERANCE_IN`, `HEADING_TOLERANCE_DEG`, `SETTLED_SPEED_IN_PER_S`: when a waypoint counts (close enough AND stopped)
- `WAYPOINT_TIMEOUT_S` + `WAYPOINT_TIMEOUT_PER_INCH_S`: when to give up on a waypoint (long drives get longer)
- `USE_ENCODER_SPEED_CONTROL`: if the robot lurches back and forth at a waypoint, try `false` (plain power) and re-tune the powers
- Needs the Pinpoint tuned on this robot first (#21): wrong pod offsets = wrong positions

## Advanced Auton (issue #25)
Pedro Pathing, later. Launching, the turret and the camera belong there, not in Basic Auton (decision 020).
