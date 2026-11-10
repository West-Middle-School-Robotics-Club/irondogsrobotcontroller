# Printable worksheets

Print one, take it to the robot, fill it in with a pen, then a coder copies the numbers into the code and the results
go on the GitHub issue. Each PDF is made by a Python script here (`pip install reportlab`, then `python3 make_...py`).
Edit the script, not the PDF.

**Test OpModes are hidden on the Driver Station** (decision 052). Before using a sheet, delete the `@Disabled` line in
that test's file (`opmodes/test/`), then build and deploy. Put it back before a competition.

**The "Was" column and the driver speeds are read from the Java code** (`code_values.py`), so they always match the
code when you print. Changed a value in the code? Re-run the script before printing.

Where each number goes in the code (and what it is now): `TeamCode/docs/tuning-guide.md`.

## Robot tests (one sheet per test OpMode)
| Sheet | Test OpMode | Numbers it finds | Issue |
|---|---|---|---|
| `intake-test.pdf` | Intake Test | directions, speeds, NECTAR switch wiring, NECTAR vs POLLEN | #4 |
| `launcher-test.pdf` | Launcher Test | flywheel direction, trigger gate positions, READY velocity, power, hit-rate | #5 |
| `turret-test.pdf` | Turret Servo Raw Test, Turret Test | the 5 calibration steps in order, hood limits, home switch | #31 |
| `pinpoint-test.pdf` | Pinpoint Test | pod directions and offsets, drift over turns and a square | #6, #21 |
| `flower-flipper-test.pdf` | Flower Flipper Test | rest / A / B positions, shake speed | (decision 024) |
| `driver-tuning.pdf` | Main TeleOp | SLOW / NORMAL / TURBO percentages, straight-line assist angle | #3, #67 |
| `brake-mode-experiment.pdf` | Brake Mode Check | what brake mode is left over, stopping distance | #17 |

All six test sheets come from `make_test_worksheets.py`. Basic Auton has its own guide and route sheets (below).

## Guides and references
| Sheet | What | Script |
|---|---|---|
| `controller-layout.pdf` | driver (page 1) and operator (page 2) buttons | `make_controller_layout.py` |
| `hardware-map.pdf` | every device by subsystem, blanks for hub and port | `make_hardware_map.py` |
| `basic-auton-guide.pdf` | drive team guide for Basic Auton: OpMode choice, INIT screen, start delay, lights | `make_basic_auton_guide.py` |
| `../game/diagrams/basic-auton-near.pdf`, `basic-auton-far.pdf` | Basic Auton routes with a 5-run test log | `../game/diagrams/make_diagrams.py` |

Not covered yet: Camera Test (#7, waiting on the aiming design), Light Test, Encoder Mode Toggle (#11).
