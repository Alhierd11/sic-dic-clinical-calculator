# Known limitations / milestone acceptance

This milestone satisfies repository/specification/test/CI/native-skeleton foundations. It does **not** satisfy the complete production bedside product specification.

| Area | Current state / remaining work |
|---|---|
| Source review | Publisher DIC Table 1 unavailable through current retrieval; transcription inspected. Obtain authorized full table and independent sign-off. |
| SOFA | Raw-data engine + tests only. Continuous highest boundaries and converted conventional SI thresholds are explicit draft choices. Helper UI blocked until clinical approval. |
| Native UI | Functional basic native shells. Single scrolling column, not optimized one-screen Express. Extract presentation models, adaptive layout, clinical result cards and field-specific errors. |
| Localization | RU/EN strings provided; clinical translation and layout audit pending. Theme/language switches currently clear transient inputs. |
| Detailed mode | Breakdown, ratio and delta; interactive per-band explanations not yet implemented. |
| History/settings | No lab preferences, saved ULN/control PT, local history, anonymous case groups, trends or delete-all storage workflow yet. No patient persistence currently. |
| Export | Explicit clipboard only. Share/PDF/print not implemented. |
| Quality | CI configured. Local environment lacks native SDKs/compilers; consult Actions for actual native results. No device testing or screenshots yet. |
| Accessibility | Native labels/semantics, large controls and Windows keyboard defaults. No completed WCAG/TalkBack/Narrator assessment. Dark-theme popup contrast needs device review. |
| Distribution | No signed packages; no fully locked transitive dependencies or demonstrated bit-for-bit rebuild. |
| Security | CodeQL/Dependabot foundations do not establish absence of critical issues. Main protection needs repository administration. |

No therapy, anticoagulant dosing, diagnostic certainty or patient-specific prognosis is provided. Pregnancy, obstetric and pediatric contexts require specialized interpretation.
