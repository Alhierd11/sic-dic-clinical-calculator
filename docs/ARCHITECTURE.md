# Architecture

The monorepo separates clinical rules, pure engines, platform shells and verification. No browser runtime is embedded.

`clinical-spec/clinical-rules.json` → `tools/generate.py` → Java `Rules.java` / C# `Rules.g.cs`. SHA256 and version identify the source. Both pure engines validate strings before decimal arithmetic. Shared JSON golden vectors → deterministic TSV → dependency-free Java and .NET runners. CI requires native parity as well as agreement with authored expected values.

Android: Kotlin platform Views/Activity, Java library core, min API 26, target/compile 35. Windows: .NET 8 WPF, vector-rendered controls/high DPI, C# core targeting net8.0 for portable test execution. Standard platform controls provide keyboard/focus/accessibility foundations. The single-column UI is a development shell, not a finished high-density bedside design.

No persistence, background services, network client or patient identifiers. Editing a field/context immediately invalidates the displayed result and disables copy. Each algorithm can complete independently; no partial total is exposed. A result contains ruleset, app version and UTC calculation timestamp. UI errors currently give a summary; field-specific validation belongs to the next milestone.

Before production: extract shell presentation state into Android ViewModel and WPF MVVM, preserve in-memory input during theme/language/window changes, introduce immutable input/result snapshots and typed lab metadata, then test stale-result prevention across every state transition. Optional history must use explicit saves, separate anonymous cases, atomic writes, retention/deletion, no OS backup, and immutable ruleset provenance. Avoid exporting patient data to logs or issue reports.
