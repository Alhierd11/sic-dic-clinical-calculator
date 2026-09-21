# Threat model

Assets: calculation integrity, version/source traceability, transient laboratory data, distribution signing keys. Trust boundaries: clinician input → parser → domain engine → presentation → optional OS clipboard; build dependencies → CI → future signed distribution. No server trust boundary exists at runtime.

| Threat | Control | Residual / gate |
|---|---|---|
| Wrong decimal or boundary | Strict parse, decimal arithmetic, cross multiplication, common vectors | Clinical rules independently reviewed before release |
| Mixed assay/basis | Explicit comparison confirmation and known shared basis | User can attest incorrectly; better structured lab profiles pending |
| Stale displayed result | Invalidate on all input/context changes | UI automation and in-memory state lifecycle review pending |
| Missing values become normal | All mandatory data required per score | No fabricated defaults |
| Leakage | No persistence/network; backup disabled; explicit copy | Clipboard, screenshots, OS facilities |
| Tampered rules | Generated checksum, CI regeneration check, reviewed pull requests | Signed release manifest and device verification pending |
| Supply chain | SHA-pinned Actions, Dependabot, least privilege, CodeQL | Transitive lockfiles, verified wrapper, SBOM and vulnerability closure pending |
| Unsigned counterfeit binary | No release published | Signing/reproducibility required before distribution |

Never log clinical inputs or exceptions containing them. Security scanning is evidence, not proof that vulnerabilities are absent. Protect main and prohibit force pushes; connector administration limitations are documented in GITHUB_ADMIN.md.
