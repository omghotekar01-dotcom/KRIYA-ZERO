# iQOO Build Health

This file records the repository-backed demo health for KRIYA ZERO without turning unverified claims into product promises.

## Current verified repository state

- Primary development branch: `main`
- Protected fallback branch: `known-good/iqoo-phase1` at `7276a197246b5fe482764267fa8c0fec0c17e22b` — preserve this branch as the recovery point for the known-good phase-1 build.
- Critical demo path: Android phone first, local/offline, Teach → Compile → Verify → Assessment.
- PASS/FAIL authority: deterministic `VerificationEngine`; an LLM is not the authority for progression or assessment.
- Android CI run #38 for commit `2c5be73a25a4507fa1210fddbba91e3f07c53d6b` completed successfully.
- Verified CI steps include unit tests, debug APK assembly, SHA-256 checksum generation, and artifact upload.
- Verified artifact: `kriya-zero-debug`, non-expired and retained until 2026-09-15.
- Artifact digest reported by GitHub Actions: `sha256:b16ce0089fcca873116201bb6229f67158e47c4ca8c203fe151505474e9620e3`.

## Persistence status

The repository contains `SkillCapsuleCodec`, `SkillCapsuleStore`, and persistence tests. The store supports save, load-by-id, load-all, and delete operations using app-local files.

This does **not** yet mean the live Compose flow provides full restart/recovery UX. `LiveKriyaApp` currently keeps the active capsule in Compose state and does not wire `SkillCapsuleStore` into its user-facing flow. Until that integration is implemented and device-tested, describe persistence as implemented infrastructure rather than a verified end-user recovery feature.

## Demo safety rules

Before calling a new KRIYA state stable:

1. Android unit tests must pass.
2. `assembleDebug` must pass.
3. CI must publish a non-expired debug APK artifact for the tested commit.
4. The `known-good/iqoo-phase1` branch must remain untouched.
5. Camera/perception behavior that depends on the final iQOO device or physical tabletop setup must be called device-calibration pending until it is actually tested there.
6. Network access must not become necessary for the core Teach → Compile → Verify → Assessment demonstration.

## Next low-risk implementation target

Wire the existing `SkillCapsuleStore` into the live Android flow so a compiled capsule can be recovered after process restart, then add regression coverage and validate the APK in CI. Do this as a small isolated milestone rather than mixing it with perception or UI redesign work.
