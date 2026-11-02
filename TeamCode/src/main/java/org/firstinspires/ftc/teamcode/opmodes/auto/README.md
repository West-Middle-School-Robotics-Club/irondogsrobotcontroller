# Autonomous OpModes

## Basic Auton (issue #22, decisions 014, 020, 026): LEAVE + PARK, drive-only
Four OpModes in the **Basic Auton** group on the Driver Station. Pick the one that matches our alliance and starting spot:

| OpMode | Start | Route |
|---|---|---|
| Basic Auton RED NEAR | Tile **A6**, left side against the **red wall** | Slide off the wall, park in the wall end of the LOADING ZONE (A5) |
| Basic Auton RED FAR | Tile **B1**, back against the **audience wall** | Off the wall, up column B (clear of the HIVE), park in the field end of the LOADING ZONE |
| Basic Auton BLUE NEAR | Tile **F1**, left side against the **blue wall** | Same as RED NEAR, rotated 180° |
| Basic Auton BLUE FAR | Tile **E6**, back against the **far wall** | Same as RED FAR, rotated 180° |

- **During INIT:** both lights show the alliance color. **Dpad up/down** sets a **start delay** (0-10 s) for when our partner's route crosses ours. Keep the robot **still** until the Pinpoint says READY. Nothing moves.
- **After START:** waits for the delay, then drives each waypoint using the **Pinpoint** (not timers). Lights: **GREEN** = parked, **YELLOW** = stopped early (a waypoint timed out).
- After AUTO, the matching **Main TeleOp** is queued up automatically.

All the code is in `BasicAuton.java`. The four small files only pick the alliance and route.
**Positions are written once, for RED** (field coordinates, decision 011). BLUE is calculated by rotating 180°.

### Tuning (TODO #22)
In `BasicAuton.java`:
- `ROBOT_LENGTH_IN`, `ROBOT_WIDTH_IN`: the competition robot's size
- `RED_NEAR_START`, `RED_NEAR_PATH`, `RED_FAR_START`, `RED_FAR_PATH`: check on a taped practice field
- `DRIVE_GAIN`, `MAX_DRIVE_POWER`, `MIN_DRIVE_POWER`, `TURN_GAIN`: how it drives
- Needs the Pinpoint tuned on this robot first (#21): wrong pod offsets = wrong positions

## Advanced Auton (issue #25)
Pedro Pathing, later. Launching, the turret and the camera belong there, not in Basic Auton (decision 020).
