# KRIYA ZERO Architecture

## 1. Product contract

KRIYA ZERO is not an AR checklist and it is not a chatbot watching a camera.

Its core contract is:

```text
single narrated demonstration
        ↓
semantic observations over time
        ↓
executable Skill Capsule
        ↓
live learner observations
        ↓
deterministic procedure verification
        ↓
evidence-backed assessment
```

The hackathon MVP is deliberately bounded to **structured, visually observable tabletop procedures**. The architecture must not imply that arbitrary human activity can be reliably learned from one recording.

---

## 2. The Skill Capsule

A Skill Capsule is the stable boundary between probabilistic perception and deterministic execution.

Current domain representation:

```text
SkillCapsule
 ├── id
 ├── name
 ├── sourceNarration
 └── steps[]
      ├── id
      ├── order
      ├── instruction
      ├── requiredObjects[]
      ├── expectedStateTags[]
      └── dependsOn[]
```

The schema will expand without breaking the execution contract. Planned additions include spatial relations, object identities, confidence envelopes, negative constraints, safety gates and compact visual embeddings.

### Why a graph instead of generated prose?

Generated instructions are useful for teaching but insufficient for verification. A verifier needs explicit machine-readable constraints: what must exist, what state must hold, and which previous states must have been completed.

---

## 3. Layered architecture

### A. Capture layer

Android/CameraX owns camera access and lifecycle. Microphone access is declared for narrated demonstration capture.

Responsibilities:

- live preview
- frame sampling
- audio capture
- timestamps
- optional device motion metadata

The capture layer must remain model-agnostic.

### B. Perception adapters

Perception converts raw sensor input into semantic evidence.

Target adapter contract:

```kotlin
interface PerceptionAdapter {
    suspend fun observe(frame: FramePacket): Observation
}
```

`Observation` currently contains:

- normalized object label → confidence
- normalized visual state tags
- capture timestamp

Potential on-device implementations can combine lightweight object detection, hand/pose tracking, image embeddings, OCR and local speech transcription. No particular model family is allowed to own product semantics.

### C. Demonstration compiler

`ProcedureCompiler` converts temporally ordered demonstration segments into a Skill Capsule.

The current deterministic compiler:

1. sorts captured segments,
2. normalizes labels,
3. assigns stable step IDs,
4. emits checkpoint evidence,
5. creates explicit sequential dependencies,
6. retains source narration.

The next compiler adapter will add model-assisted discovery of action boundaries and state transitions while continuing to emit the same domain types.

### D. Verification engine

`VerificationEngine` is intentionally non-generative.

It checks:

- dependency completion,
- object confidence thresholds,
- expected state predicates,
- cumulative future-state skipping,
- retry history,
- assistance requests.

A language model may explain a failure to the user, but it must not be the sole authority deciding PASS/FAIL.

### E. Assessment layer

The report separately exposes:

- completion percentage,
- first-attempt accuracy,
- failed checks/corrections,
- sequence errors,
- assistance requests.

This prevents an opaque single score from hiding how the learner actually performed.

---

## 4. Current implementation truth table

| Capability | Current status | Notes |
|---|---|---|
| Android app shell | Implemented | Jetpack Compose |
| Real rear-camera preview | Implemented | CameraX |
| Camera permission handling | Implemented | Runtime permission gate |
| Skill Capsule schema | Implemented | Pure Kotlin domain layer |
| Demonstration compiler | Implemented | Semantic checkpoint input |
| Temporal dependencies | Implemented | Sequential graph today |
| Deterministic verifier | Implemented | Unit-tested logic |
| Failure correction | Implemented | Failed attempt retained |
| Skip-ahead detection | Implemented | Protects cumulative procedures |
| Evidence-backed report | Implemented | Separate metrics |
| Semantic fallback capture | Implemented | Manual/test adapter |
| Live object detection | Next | Must replace fallback path |
| Live state relation detection | Next | Core perception milestone |
| Local speech transcription | Next | Narrated demonstration input |
| Automatic action boundary discovery | Next | Model-assisted compiler |
| Persistent capsules | Next | Local storage |
| On-device explanation model | Later | Not required for pass/fail |

The app UI explicitly labels deterministic injection controls as a fallback/perception adapter. This is deliberate: repository behavior must remain auditable during development.

---

## 5. Why cumulative skip detection matters

Many physical procedures are cumulative:

```text
S1 = board
S2 = board + resistor
S3 = board + resistor + LED
```

A naive verifier checking only whether the requirements for `S2` are present would incorrectly accept `S3` while the learner is supposed to be at `S2`.

KRIYA checks whether the observation already satisfies a future checkpoint **before** accepting the current checkpoint. If so, the engine records a sequence error and leaves the learner on the required step.

This behavior is covered by a unit test.

---

## 6. Planned live-perception path

The first hackathon-grade perception path should optimize reliability over generality.

### Recommended target task

A small LED/breadboard or block-assembly procedure with:

- 4–6 visually distinguishable objects,
- fixed camera position,
- clear state transitions,
- no hidden internal state,
- a deliberate wrong-object or wrong-order moment.

### Pipeline

```text
CameraX frame
  ↓
ROI / workspace normalization
  ↓
object detector + lightweight visual embedding
  ↓
state relation rules
  ↓
Observation
  ↓
VerificationEngine
```

The demo should use a fixed workspace marker or known board layout if that improves determinism. A constrained system that works live is stronger than claiming unrestricted zero-shot physical intelligence and failing on stage.

---

## 7. Safety and privacy principles

- Camera analysis should remain local whenever feasible.
- Raw frames do not need to leave the device for deterministic verification.
- Skill Capsule evidence should be compact and inspectable.
- Safety-critical procedures require domain-specific constraints before deployment.
- KRIYA must not claim certification-grade assessment from the hackathon prototype.
- Users must know when the perception system is uncertain.

---

## 8. Repository quality gates

A feature is not considered complete because a screen exists.

For each core behavior we require, where applicable:

1. deterministic domain test,
2. Android compile gate,
3. real-device check,
4. demo failure path,
5. explicit fallback behavior,
6. documentation update.

GitHub Actions runs unit tests and builds a debug APK on every push once Actions is active for the repository.
