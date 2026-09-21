# SIC + overt DIC Clinical Calculator — ISTH 2025

[![CI](https://github.com/Alhierd11/sic-dic-clinical-calculator/actions/workflows/ci.yml/badge.svg)](https://github.com/Alhierd11/sic-dic-clinical-calculator/actions/workflows/ci.yml)

Offline clinical decision-support calculator for SIC and ISTH 2025 overt DIC — Android & Windows.

**0.1.0-dev: foundation milestone, not a clinical release.** This repository contains real native project skeletons, clinical engines, a canonical ruleset, common golden vectors and CI. It does not yet contain the finished bedside product. [Русский](README.ru.md).

This software has not been independently validated or cleared/approved as a medical device unless a future release explicitly states otherwise.

## Implemented

- Native Kotlin Android views with a dependency-free Java BigDecimal domain module; native C#/WPF Windows shell and decimal domain module. No WebView or website wrapper.
- SIC and overt DIC arithmetic; complete score only when its required data are present and valid.
- Four-component SOFA **domain helper**, with explicit draft endpoint policy; helper UI is a later milestone.
- Russian default and English selector, express input and optional detailed breakdown, clinical context selectors, special population warnings, clear and explicit clipboard copy.
- No network code, telemetry, patient persistence, Android INTERNET permission or cloud backup. No AI, treatment or prognosis engine.
- 102 shared vectors across SIC, DIC, SOFA, conversions and invalid input. Both native runners execute the same generated fixture; CI compares their outputs byte-for-byte.

## Build and test

Python 3.12+, JDK 17 (not only a JRE), Gradle 8.11.1, Android SDK 35; .NET SDK 8.0. WPF requires Windows.

```sh
python tools/verify.py
python tools/generate.py --check
mkdir -p .build/java
javac -Xlint:all -Werror -d .build/java android/core/src/main/java/org/sicdic/core/*.java android/core/src/test/java/org/sicdic/core/VectorRunner.java
java -cp .build/java org.sicdic.core.VectorRunner shared-test-vectors/generated/vectors.tsv
dotnet run --project windows/SicDic.Core.Tests -c Release -- shared-test-vectors/generated/vectors.tsv
gradle -p android :core:check :app:lint :app:assembleDebug :app:assembleRelease
dotnet build windows/SicDic.App -c Release
```

Use the installed pinned Gradle version; this foundation does not ship a downloaded wrapper JAR. Restore requires network access; installed applications do not. CI release builds are unsigned development checks, not distributable clinical releases. Gradle/Kotlin dependency locking, fully pinned SDK patches and independent clean-build binary reproducibility are pending release gates.

## Clinical contract

`clinical-spec/clinical-rules.json` is authoritative. `tools/generate.py` emits platform tables containing its SHA256. Never edit generated rules. Numerical thresholds do not diagnose disease. D-dimer and ULN must use the same assay, units and FEU/DDU basis; unknown basis blocks DIC in the shell. No FEU↔DDU conversion.

See [rules](clinical-spec/RULES.md), [source verification](clinical-spec/SOURCES.md), [clinical validation](docs/CLINICAL_VALIDATION.md) and [known limitations](docs/KNOWN_LIMITATIONS.md). DIC source-table verification and independent SOFA endpoint review remain open.

## Roadmap and release status

No APK/EXE clinical release is published. Pending: production UX/MVVM, SOFA helper UI, laboratory preferences, explicit local history and trends, export/share/PDF/print, individual field errors, automated UI tests, accessibility/device verification, authentic screenshots, security closure, signing and reproducibility. [Release gates](docs/RELEASE.md) are mandatory, not completion claims.

## Project map

`android/`, `windows/`, `clinical-spec/`, `shared-test-vectors/`, `localization/`, `docs/`, `.github/`, `screenshots/`, `tools/`.

Apache-2.0. No endorsement by ISTH or the source authors is implied. Contributions must use synthetic data. See [CONTRIBUTING](CONTRIBUTING.md) and [SECURITY](SECURITY.md).
