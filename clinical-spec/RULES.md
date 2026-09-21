# Canonical rules and numeric contract

Authoritative machine-readable source: `clinical-rules.json`; current version in `VERSION`. Generated platform tables carry its SHA256. Ordered rules use the first matching band. Applications must reject incompatible ruleset versions in future saved records.

| Component | Points |
|---|---|
| SIC platelets ×10⁹/L | <100: 2; 100–<150: 1; ≥150: 0 |
| SIC INR | >1.4: 2; >1.2–≤1.4: 1; ≤1.2: 0 |
| SIC four-system SOFA sum | 0: 0; 1: 1; ≥2: 2; integer sum 0–16 only |
| SIC threshold | total ≥4 AND platelet+INR points >2; maximum 6 |
| DIC platelets ×10⁹/L | <50: 2; 50–<100: 1; ≥100: 0 |
| DIC D-dimer/ULN | >7: 3; >3–≤7: 2; ≤3: 0 |
| DIC PT patient−control (seconds) | ≥6: 2; ≥3–<6: 1; <3: 0 |
| DIC fibrinogen g/L | <1: 1; ≥1: 0 |
| DIC threshold | total ≥5; maximum 8 |

Negative PT delta remains visible, scores zero and requires an input/control warning. Positive patient PT, control PT, INR and ULN are required. Zero platelets, D-dimer or fibrinogen are permitted as literal measurements; no limit-of-detection string is parsed as a measurement. `<0.1` must not be silently converted to 0.1. Missing is not zero. Each score is all-or-nothing, independently of the other score.

## Decimal contract

Accept trimmed unsigned digits, 1–9 integer digits, optionally one decimal comma or point and 1–6 fractional digits. Reject exponents, grouping, mixed separators, signs, NaN, infinity and excess precision. These are representation limits, not physiological reference ranges. No silent rounding on input. Bounds keep .NET decimal multiplication exact and prevent overflow; BigDecimal is also used without binary floating-point.

Compare measured D-dimer directly with `3*ULN` or `7*ULN`, never with a rounded quotient. Display ratio may be rounded to six decimal places and is labeled accordingly. FEU/DDU are never converted. A single shared display unit/basis plus explicit same-assay confirmation establishes comparability in the foundation shells; unknown basis blocks DIC.

Fibrinogen mg/dL /100 = g/L. Platelets ×10⁹/L and 10³/µL are numerically identical.

## SOFA helper draft policy

Only respiratory, cardiovascular, hepatic, renal; no coagulation or CNS. Select worst component values in the same defined 24-hour assessment window. Drugs are µg/kg/min, administered for at least 1 hour. The caller must explicitly confirm this window; all doses are required, with a literal zero for an absent listed drug. Do not use the helper for unrepresented support (e.g. vasopressin alone, ECMO or confounded renal replacement) without specialist assessment.

| System | Implementation bands |
|---|---|
| P/F mmHg, no respiratory support | <300: 2; <400: 1; otherwise 0 |
| P/F with respiratory support | <100: 4; <200: 3; <300: 2; <400: 1; otherwise 0 |
| Cardiovascular | Worst of MAP <70 →1; dopamine >0–≤5 →2, >5–≤15 →3, >15 →4; dobutamine >0 →2; epinephrine/norepinephrine >0–≤0.1 →3, >0.1 →4 |
| Bilirubin mg/dL | <1.2: 0; ≥1.2: 1; ≥2: 2; ≥6: 3; ≥12: 4 |
| Creatinine mg/dL | <1.2: 0; ≥1.2: 1; ≥2: 2; ≥3.5: 3; ≥5: 4 |
| Urine mL/24 h | <200: 4; <500: 3; otherwise 0; renal=max(creatinine, urine) |

**Independent review required:** published tables contain rounded ranges (e.g. 1.2–1.9) and render highest cutoffs as >12 and >5 in some copies. The development helper closes those gaps at ≥12 and ≥5 and treats dopamine=5 as 2, >5 as 3. These explicit engineering choices must be approved before helper UI activation/clinical release. Do not describe the helper as a fully validated implementation yet.

SI support uses exact decimal factors: bilirubin µmol/L = mg/dL ×17.1; creatinine µmol/L = mg/dL ×88.4. Score by comparing SI input with converted conventional thresholds, without quotient rounding. This is unit invariant but differs near the rounded SI thresholds printed in SOFA tables (e.g. 1.2 mg/dL creatinine =106.08 µmol/L versus the printed 110). Documented pending clinical approval; not silently interchangeable policies.

The helper requires all raw data, including creatinine AND 24-hour urine, preventing an incomplete low renal score. This is intentionally stricter than workflows that allow a maximum score from a single criterion. The foundation UI uses manually entered four-system SOFA only.
