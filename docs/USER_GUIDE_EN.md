# Guide — development milestone

Do not use this build for clinical decisions; independent validation is incomplete.

1. For SIC enter platelets, INR and only the four-system SOFA sum. Select the sepsis context.
2. For DIC enter D-dimer and the same assay ULN, shared unit and FEU/DDU basis. Explicitly confirm comparability. Unknown basis blocks DIC.
3. Enter patient and laboratory control PT in seconds and fibrinogen with its unit. Select the underlying-condition context.
4. Mark pregnancy, postpartum/obstetric context or pediatric status when relevant.
5. Detailed mode adds component scores, displayed D-dimer ratio and PT delta. Select Calculate. Missing/invalid inputs are never zero-filled.
6. Copy is explicit. Editing invalidates the old result. New patient / clear resets the form.

Decimal comma or point is accepted, not thousands separators or inequality-style laboratory results. SIC and DIC complete independently. Numerical thresholds are not diagnoses; low scores do not exclude disease. Context warnings accompany results.

Result time is UTC calculation time, not sample collection time. No data are saved. Restarting or switching theme/language currently clears the form. History, lab settings, SOFA helper UI and PDF are pending. References are available in About and clinical-spec/SOURCES.md.
