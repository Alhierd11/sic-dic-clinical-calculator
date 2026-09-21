# Testing

Run `python tools/verify.py` from the root: schema keywords used by this project, generated-file equality, independent Decimal evaluation of 102 authored vectors, JSON localization key parity, XML parsing, source-level offline guardrails. The in-repository validator is a deliberately limited validator for this exact schema, not a general JSON Schema implementation.

`tools/generate.py` converts existing authored vectors to TSV; it never computes expected scores. Java `VectorRunner` and .NET `SicDic.Core.Tests` are executable test harnesses: a mismatch throws and exits nonzero. Run them with the commands in README. CI additionally compares result files byte-for-byte. This avoids interpreting a no-tests-found exit code as success from a test framework.

Vector groups: SIC 17, DIC 21, conversion 4, invalid input 21, SOFA 39. Boundary, minimum/maximum, decimal-comma, exact-ratio, negative-PT and missing-data cases are included. No fixture uses real patients. Add a regression vector for every clinical defect.

CI jobs: clinical-parity, android (lint/core tests/debug+unsigned release), windows (WPF build/native vectors); quality-gate requires all three. CodeQL is separate. Python reference passing alone must never be reported as native parity passing.

Pending: UI unit/state tests, device/emulator instrumentation, Windows UI Automation, orientation/lifecycle/stale-result tests, focus/TalkBack/Narrator, 200% font scaling, contrast, narrow/large displays, persistence and export tests once implemented, mutation tests and property-based sweeps. No current screenshot/clinical or security certification is claimed.
