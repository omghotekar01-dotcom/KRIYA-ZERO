# KRIYA ZERO — iQOO Pune Phase 1 Submission Pack

This folder is the single source for the iQOO Pune Phase 1 submission materials.

## Upload these

### Deck / document — REQUIRED
Upload **`KRIYA_ZERO_FINAL_DECK.pptx`**.

If the portal or browser has trouble with PPTX, upload **`KRIYA_ZERO_FINAL_IDEA.pdf`** instead.

Both files are generated reproducibly by GitHub Actions from `scripts/generate_submission_assets.py`.

### Prototype URL
`https://github.com/omghotekar01-dotcom/KRIYA-ZERO`

### Problem statement
**Smart Education**

### Idea title
**KRIYA ZERO — One-Demonstration Physical Skill Compiler**

### Tagline
**Show it once. Learn it anywhere. Prove you can do it.**

## Files in this folder

- `KRIYA_ZERO_FINAL_DECK.pptx` — final 8-slide Phase 1 pitch deck.
- `KRIYA_ZERO_FINAL_IDEA.pdf` — concise two-page idea/proposal PDF.
- `PORTAL_COPY.md` — exact copy-paste answers for the Reskilll form.
- `VIDEO_MASTER_PROMPT.md` — 20-second AI-video generation prompt.

## Truth boundary

The current repository implements the Android foundation, live CameraX input, Teach → Compile → Verify → Assessment flow, learned visual checkpoints, stabilized visual matching, temporal sequence checks, deterministic verification logic, assessment metrics, local persistence backend, tests and GitHub CI/APK generation.

Do **not** claim that the current prototype can reliably learn arbitrary human activity. The hackathon MVP scope is **visually observable, structured tabletop procedures**.

Do **not** claim a fully deployed on-device LLM or semantic vision embedding model unless that integration has been completed and verified on the target phone. The current reliable verifier uses learned visual evidence plus deterministic procedure logic.

## Known-good Android baseline

The last explicitly verified Android baseline before submission packaging is commit:

`7276a197246b5fe482764267fa8c0fec0c17e22b`

Workflow run: `33535329863`

That run passed unit tests, assembled the debug APK and uploaded the APK artifact.

## Short demo story

1. Teach a fresh 3–5 state procedure.
2. KRIYA compiles checkpoints.
3. Reset the workspace.
4. Perform one state correctly — PASS.
5. Intentionally perform/skip a state — FAIL / sequence divergence.
6. Correct it without restarting — PASS.
7. Complete the task and show the evidence-backed skill report.

The core jury line is:

> The important part is that this verifier did not exist before the demonstration. The phone learned the procedure and immediately became the instructor and assessor.
