# Release process and mandatory gates

Current version 0.1.0-dev is source foundations. Do not tag 1.0 or publish a clinical APK/EXE until every gate below has recorded evidence.

- Native unit/golden/boundary tests pass on both platforms and common output files match.
- Full native builds, Android lint, static analysis and dependency/security review pass; no unresolved critical issue.
- Independent clinical reviewers approve exact rules, DIC Table 1, SOFA intervals and SI policy; sources and errata are checked again.
- Required functionality and clinical context behaviors are complete; RU/EN and accessibility/human factors tested.
- Dependency lockfiles, verified Gradle wrapper and SDK/JDK patch versions are frozen. Two isolated clean builds reproduce unsigned artifact hashes, accounting explicitly for signatures/timestamps.
- Authentic screenshots, README, user guides and limitations match the built artifact.
- Release manifest records app/rules versions, source commit, dependency SBOM, SHA256 and validation evidence. Keys are held outside Git; no keystore, passwords or patient data in repository.
- Sign Android APK/AAB with a maintainer-controlled key and Windows package with a trusted signing process. Verify signatures and install/upgrade/uninstall offline.

Do not infer clinical clearance from a green CI badge. A source commit is not a release. GitHub release publishing is intentionally not automated in this foundation milestone. .NET 8 and target SDK 35 are pinned engineering baselines, not claims of current store-policy compliance; assess supported platform lifecycle at release.
