# KRIYA ZERO

**The One-Demonstration Physical Skill Compiler**

> Show it once. Learn it anywhere. Prove you can do it.

KRIYA ZERO is an Android-first, phone-native prototype for the iQOO Hackathon Pune 2026. It learns a structured, visually observable physical procedure from a single narrated demonstration and turns it into an executable **Skill Capsule** that can guide and verify another learner.

## Core hypothesis

Most digital training systems store instructions. KRIYA ZERO stores an **executable procedural graph**:

`demonstration → objects + states + actions + constraints + checkpoints → verifier`

A Skill Capsule can therefore power teaching, progress tracking, error detection and assessment from the same representation.

## Hackathon MVP

The first end-to-end demo focuses on a small tabletop assembly procedure. The app supports:

1. **Teach** — record a narrated demonstration.
2. **Compile** — convert captured steps into a procedure graph.
3. **Practice** — guide a learner through the learned procedure.
4. **Verify** — compare observed checkpoints against expected states.
5. **Assess** — produce an evidence-backed completion report.

The architecture intentionally keeps perception/model adapters replaceable. A deterministic demo adapter is included so the full product flow remains testable while on-device vision and speech adapters are integrated.

## Principles

- Phone-first; camera and microphone are product inputs, not decoration.
- Local-first and privacy-aware.
- No claim that arbitrary human activity can be learned from one video.
- MVP targets **visually observable, structured tabletop procedures**.
- LLMs structure/explain; deterministic state logic owns pass/fail decisions.
- Robust demo > speculative breadth.

## Repository status

Active build. See `docs/ARCHITECTURE.md`, `docs/HACKATHON_PLAN.md`, and the Android app under `app/` as implementation lands.
