# Clinical validation status

Status: **not independently clinically validated**. Verification of software arithmetic does not establish diagnostic performance, safety in workflow, or regulatory clearance.

| Evidence | Status |
|---|---|
| Canonical rule tables, version and checksum | Implemented |
| Explicit boundary and invalid-input vectors | 102 authored cases |
| Python Decimal reference and structure checks | Executed locally; pass |
| Native Java/.NET parity | CI configured; see current run, not this document, for result |
| Publisher DIC Table 1 inspection | Pending; primary-text transcription only |
| SOFA raw-data endpoint/unit policy | Documented draft, independent approval pending |
| Independent clinician review | Pending; no reviewer identity invented |
| Real device, accessibility, human factors | Pending |
| Prospective clinical evaluation | Not performed |

Review workflow: two qualified reviewers independently compare rules and all boundary vectors with primary tables; resolve discrepancies with documented reasons; add counterexamples and update version; run both engines; verify context messages and missing-data handling; sign a dated review record tied to commit/rules SHA256. No treatment recommendations are within scope.

Required edge cases include exactly 3/7 ULN, exactly 3/6 seconds, exactly 1 g/L, SIC total 4 with coagulation sum 2, assay/basis mismatch, negative PT delta, missing mandatory values and low scores with persistent clinical suspicion. Special populations must have warnings and no substituted obstetric/pediatric score.
