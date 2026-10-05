# Game Rules That Matter to Us (Competition Manual Section 11, TU03, pp. 99–117)

Not every rule, just the ones that affect our **robot design, code, or drive team**.
Rules marked * in the manual apply in every match. Check the manual for exact wording and penalties.

## Before the match
| Rule | Summary | What it means for us |
|---|---|---|
| **G304** Robot setup (p. 104, Fig. 11-1) | Robot must be: **A** fully on **own side** (Red A–C / Blue D–F), **B** not attached to field, **C** **touching a perimeter wall**, **D** not touching a FLOWER, **E** **not in the LOADING ZONE**, **F** within starting size (R102/R103), **G** touching **exactly 4 POLLEN**, **H** **completely still after INIT** | Starting spots for AUTO (#22). ⚠️ **INIT code must not move any motor or servo** |
| **G305** Select an OpMode (p. 105) | Must select and INIT an OpMode; AUTO OpModes need the 30 s timer | Use `@Autonomous(preselectTeleOp = "Main TeleOp")` so TeleOp is queued automatically |

## During AUTO (11.4.1, pp. 106–107)
| Rule | Summary | What it means for us |
|---|---|---|
| **G401** | No touching the robot or Driver Station during AUTO (except START/STOP, safety) | AUTO must run fully on its own |
| **G402** | Don't disrupt the opponent's AUTO. **Red side = A–C, Blue side = D–F.** Going onto their side is risky (MAJOR FOUL) | **Every AUTO path stays on our side** |
| **G403** | **No powered movement during the 8 s transition** after AUTO | AUTO must finish before 30 s. TeleOp INIT must not move anything |

## Scoring elements (11.4.3, pp. 107–110)
| Rule | Summary | What it means for us |
|---|---|---|
| **G405** | Don't deliberately eject elements off the field | Launcher aim and power |
| **G407** | **Control at most 4 elements** at a time | **Intake must hold max 4** (#4). Pushing ("bulldozing") isn't control |
| **G408** | Don't control the **opponent's NECTAR** | Intake/driver awareness |
| **G409** | Don't catch elements falling from a TIPPED HIVE before they touch something else | Don't wait under the HIVE; guards on top of the robot help |
| **G410** | **No NECTAR into FLOWERS until 1:00 left** (MAJOR FOUL per NECTAR) | Never score FLOWERS in AUTO |
| **G411** | Don't hoard elements to keep them from opponents | Strategy |

## Robot and field (11.4.4, pp. 110–112)
| Rule | Summary | What it means for us |
|---|---|---|
| **G412** | Robot must be under control, not dangerous | |
| **G414** | Team number and alliance color must stay visible | Robot signs mounted firmly |
| **G415** | Don't grab, grasp, attach to, entangle with, or hang from field elements | A concave shape to line up with a FLOWER is OK |
| **G416** | Stay within the expansion limits (R105), don't detach parts on purpose | Mechanism design |
| **G417** | **Only LAUNCHING into the upward CELL may move the HIVE.** No ramming the frame, no aiming at the outside of a CELL | AUTO paths keep clear of the HIVE. Aim for the CELL opening |
| **G418** | Elements go **into the top** of a FLOWER; only **POLLEN out of the bottom** | |

## Opponents (11.4.5, pp. 113–115)
| Rule | Summary |
|---|---|
| **G419** | Don't damage or functionally impair an opponent robot |
| **G420** | Don't tip over, attach to, or entangle an opponent |
| **G421** | **3-second limit on PINNING** an opponent (MAJOR FOUL + another every 3 s) |

## Humans (11.4.6, pp. 115–117)
| Rule | Summary |
|---|---|
| **G426** | Enter **1 NECTAR per TIP** of your HIVE, or all remaining NECTAR with **60 s or less** left |
| **G427** | NECTAR goes in **only through your LOADING ZONE**, by hand, touching the LOADING ZONE tiles first |
| **G428** | Humans don't remove elements from the field |

Note: the human player uses the LOADING ZONE during TeleOp, the same place robots PARK.
