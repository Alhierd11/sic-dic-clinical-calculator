# GitHub administration

Repository: Alhierd11/sic-dic-clinical-calculator (existing public repository). The available connector exposes content/commit APIs but no mutation endpoint for repository description/topics or branch rules. These settings have not been claimed as applied.

After the first green CI, in Settings → Rules → Rulesets create a rule for main: require pull requests and at least one review; require `quality-gate` and CodeQL checks; block force pushes and deletion; apply to administrators where practical. Do not require status names until they have actually appeared in Actions.

About description: Offline clinical decision-support calculator for SIC and ISTH 2025 overt DIC — Android & Windows.

Topics: clinical-calculator, critical-care, intensive-care, sepsis, dic, coagulopathy, isth, android, windows, kotlin, dotnet, medical-software.

Enable private vulnerability reporting and secret scanning/push protection where available. Workflow files configure CI, Dependabot and CodeQL; their presence does not prove that all repository-level settings are enabled.
