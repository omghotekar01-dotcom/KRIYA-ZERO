# KRIYA ZERO

**The One-Demonstration Physical Skill Compiler**

> **Show it once. Learn it anywhere. Prove you can do it.**

![Android CI](https://github.com/omghotekar01-dotcom/KRIYA-ZERO/actions/workflows/android.yml/badge.svg)

KRIYA ZERO is an Android-first, phone-native prototype for the iQOO Hackathon Pune 2026. It turns a short physical demonstration into an executable **Skill Capsule** that can guide and verify another learner.

The project is intentionally focused on a hard but bounded question:

> Can a phone learn a new, visually observable tabletop procedure from demonstration and immediately become a verifier for the next person — without manually coding a different verifier for that procedure?

## What is working now

### Real phone capture

- CameraX rear-camera preview
- Camera + microphone runtime permissions
- YUV camera-frame analysis on device
- central-workspace visual sampling

### Zero-authoring visual checkpoints

A demonstration checkpoint can be captured directly from the live camera as a compact 12×12 YUV **visual fingerprint**.

The fingerprint path requires no task-specific object class, cloud request or manually authored pass/fail image set.

A rolling five-frame consensus reduces autofocus/exposure jitter before the checkpoint is stored or verified.

### Executable Skill Capsule

`ProcedureCompiler` converts captured checkpoints into:

```text
Skill Capsule
 ├─ ordered steps
 ├─ narration
 ├─ learned visual evidence
 ├─ optional object/state evidence
 └─ temporal dependencies
```

### Local Skill Capsule persistence

Compiled Skill Capsules can be saved on-device as local `.kriya` files, loaded again by ID or recency, and deleted without requiring a network connection. Corrupted saved capsules are ignored instead of crashing the capsule library path.

### Deterministic verification

The execution engine, not an LLM, decides progression.

It currently handles:

- checkpoint PASS / FAIL
- correction after a failed attempt
- confidence thresholds for semantic evidence
- learned visual-state similarity
- future-state / skip-ahead detection
- first-attempt accuracy
- sequence errors
- guidance requests
- final assessment report

### Full Android product loop

```text
HOME
  ↓
TEACH — camera learns new physical checkpoints
  ↓
COMPILE — demonstration becomes a Skill Capsule
  ↓
VERIFY — live camera state is compared with learned checkpoints
  ↓
ASSESS — evidence-backed report
```

A deterministic LED-circuit fixture is also retained as a regression/fallback path while perception is hardened.

## Why this is not just an AR checklist

Most work-instruction systems begin with an authored procedure.

KRIYA ZERO is exploring a different primitive:

```text
demonstration
    ↓
learned state checkpoints
    ↓
executable temporal representation
    ↓
immediate verifier
```

The architecture separates probabilistic perception from deterministic procedure logic. Language or vision models can help interpret a demonstration, but they do not become the sole authority deciding whether a learner passed a physical checkpoint.

## Current visual verifier

The lightweight MVP fingerprint is deliberately transparent:

1. CameraX provides a YUV frame.
2. The outer 10% is ignored to focus on the workspace.
3. The remaining image is sampled into a 12×12 grid.
4. Luma/chroma values form a compact fingerprint.
5. Five recent fingerprints are averaged.
6. Learned and live fingerprints are compared locally.
7. Luma is mean-normalized so global brightness changes have less influence.

This is **not** claimed to be a general-purpose vision model. It is a practical zero-authoring verifier for a constrained, fixed-camera hackathon workspace and a foundation for stronger image embeddings / semantic detectors.

## Architecture

See:

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/HACKATHON_PLAN.md`](docs/HACKATHON_PLAN.md)
- [`docs/DEMO_SCRIPT.md`](docs/DEMO_SCRIPT.md)

Key source areas:

```text
app/src/main/java/com/xyro/kriyazero/
 ├── camera/
 │   └── VisualFingerprintExtractor.kt
 ├── domain/
 │   ├── Models.kt
 │   └── FingerprintStabilizer.kt
 ├── engine/
 │   ├── ProcedureCompiler.kt
 │   └── VerificationEngine.kt
 ├── data/
 │   └── DemoScenario.kt
 └── ui/
     ├── CameraPreview.kt
     └── LiveKriyaApp.kt
```

## Build

### Android Studio

Recommended local prerequisites:

- JDK 17
- Android Studio with Android SDK 35
- Android device with a rear camera

Clone the repository, open the root project in Android Studio, allow Gradle sync, and run the `app` configuration on the phone.

### CI

GitHub Actions runs:

```text
unit tests
    ↓
assembleDebug
    ↓
upload debug APK artifact
```

Superseded CI runs are automatically cancelled during rapid development so the newest commit remains the relevant quality gate.

## Tests currently cover

- demonstration ordering and dependency compilation
- semantic evidence normalization
- successful end-to-end deterministic procedure verification
- visible mismatch → correction behavior
- cumulative future-state / skip-ahead rejection
- assistance accounting
- visual fingerprint identity
- brightness-shift tolerance
- strong chroma mismatch rejection
- visual-only procedure verification
- rolling camera consensus

## Hackathon demo target

The final event demo should use a fixed, high-contrast tabletop setup with 4–6 visible states.

The judge should be able to watch this happen:

```text
Teach new procedure
→ reset table
→ perform correct state
→ PASS
→ deliberately make wrong state
→ FAIL
→ correct it
→ PASS
→ attempt future state early
→ SEQUENCE ERROR
→ finish
→ assessment report
```

The critical path should work with network disabled.

## Honest current limitations

The repository is actively being hardened. Current limitations include:

- visual fingerprints assume a reasonably stable phone/workspace viewpoint,
- Skill Capsule persistence is local file-based storage rather than a synchronized multi-device library,
- narration is currently text input; local speech transcription is not wired yet,
- object/state semantics are optional development metadata rather than a live trained detector,
- the visual fingerprint is not suitable for hidden-state or safety-critical procedures,
- real-device thresholds still need calibration on the final iQOO phone and physical demo setup.

Those constraints are deliberate and documented rather than hidden behind claims of unrestricted one-shot physical intelligence.

## Product direction

After the hackathon primitive is reliable, the same Skill Capsule contract can accept stronger perception adapters:

- local image embeddings
- object detection
- hand / pose tracking
- spatial relation reasoning
- OCR
- local speech transcription
- local language-model explanations

Potential domains include engineering labs, ITIs, vocational training, operator onboarding, field service, practical assessment and procedure knowledge preservation.

---

**Team XYRO · iQOO Hackathon Pune 2026**
