# Contributing

Use a focused branch and pull request. Explain the clinical or user problem, changed behavior and verification. Use synthetic data only; do not paste clinical screenshots or identifiers. Follow CODE_OF_CONDUCT.md.

Clinical changes start in clinical-spec/clinical-rules.json and require primary-source evidence, independent clinical review, version update, explicit boundary vectors and regeneration with `python tools/generate.py`. Never edit generated tables directly. Run `python tools/verify.py` and both native vector runners; require CI. Do not weaken safety checks to make a test pass.

Keep platform UI code free of scoring thresholds. Document assumptions and unresolved limitations. Tests must detect clinically meaningful defects, not merely duplicate implementation. New dependencies need license/security/offline review. Submit changes under Apache-2.0. No personal contributor details are required beyond normal Git attribution.
