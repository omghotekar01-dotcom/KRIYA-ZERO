# KRIYA ZERO — Hackathon Build Plan

## Winning constraint

The project must prove one hard thing live:

> A procedure that was not manually hard-coded into the verifier can be converted into a Skill Capsule, and the phone can then detect a learner divergence and recover after correction.

Everything else is secondary.

---

## P0 — Must work before the event demo

### 1. Android product loop

- [x] Home / Teach / Capsule / Verify / Report flow
- [x] real rear-camera preview
- [x] runtime camera permission handling
- [x] phone-first portrait layout
- [ ] persistent local capsule storage
- [ ] crash-safe navigation/state restoration

### 2. Procedure representation

- [x] Skill Capsule domain schema
- [x] ordered step graph
- [x] object evidence
- [x] state evidence
- [x] explicit dependencies
- [x] skip-ahead protection for cumulative procedures
- [ ] spatial relation predicates
- [ ] negative predicates / forbidden states

### 3. Live perception

- [ ] lock the physical demo workspace
- [ ] capture reference frames during Teach mode
- [ ] detect 4–6 demo objects reliably
- [ ] derive simple state relations from fixed workspace geometry
- [ ] emit real `Observation` objects from CameraX frames
- [ ] confidence/uncertainty UI
- [ ] run without network for the final demo path

### 4. Narration

- [ ] record narration audio
- [ ] local transcription adapter
- [ ] align transcript segments with visual checkpoints
- [ ] fallback manual text remains available but not required in final demo

### 5. Demonstration compiler

- [x] deterministic semantic compiler
- [ ] automatic temporal checkpoint proposal
- [ ] merge duplicate adjacent states
- [ ] infer object additions/removals
- [ ] infer stronger ordering constraints
- [ ] human confirmation screen before saving capsule

### 6. Verification + report

- [x] confidence thresholding
- [x] wrong-state rejection
- [x] correction/retry handling
- [x] sequence error tracking
- [x] assistance tracking
- [x] first-attempt accuracy
- [ ] save timestamped evidence snapshots locally
- [ ] export compact verification receipt

---

## P1 — High-value polish

- [ ] live camera overlay around detected objects
- [ ] highlight expected component/region
- [ ] spoken guidance
- [ ] multilingual explanation layer
- [ ] haptic feedback for pass/fail
- [ ] capsule library with search/filter
- [ ] judge mode with one-tap reset
- [ ] airplane-mode indicator
- [ ] telemetry screen showing device-local inference timing

---

## P2 — Do only after the demo is robust

- [ ] ARCore spatial anchoring
- [ ] local conversational assistant
- [ ] multiple simultaneous learners
- [ ] cloud capsule sharing
- [ ] teacher analytics dashboard
- [ ] generalized arbitrary-object recognition
- [ ] unrestricted open-world skill discovery

These features are intentionally below P0. They should never compromise the live demonstration.

---

## Demo-task selection rubric

Pick a physical task only if it satisfies all of the following:

1. **Visible state** — success is observable from the phone camera.
2. **Short** — 4–6 meaningful transitions.
3. **Distinct objects** — components are visually separable.
4. **Intentional failure** — a judge can understand the wrong state immediately.
5. **Safe** — no dangerous voltage, heat, tools or chemicals.
6. **Resettable** — the table can return to the start state in under 30 seconds.
7. **Offline** — verification does not depend on internet availability.

Current preferred fixture: a low-voltage LED/breadboard arrangement or a purpose-built block/component board with high-contrast objects.

---

## Definition of done for the on-site MVP

The build is demo-ready only when all of these pass on the actual phone:

1. Launch from a clean install.
2. Grant camera permission.
3. Teach/capture a new procedure.
4. Compile a Skill Capsule.
5. Reset physical workspace.
6. Perform Step 1 correctly → automatic PASS.
7. Create a deliberate Step 2 mismatch → automatic FAIL.
8. Correct Step 2 → automatic PASS without restarting.
9. Attempt a future state early → SEQUENCE ERROR.
10. Finish remaining states.
11. Open report.
12. Repeat with Wi-Fi/mobile data disabled.
13. Force-close/reopen without corrupting saved capsule.

If steps 1–12 are reliable but a fancy feature is unfinished, ship the reliable build.

---

## Risk register

| Risk | Consequence | Mitigation |
|---|---|---|
| Open-world visual recognition is unreliable | demo failure | constrain workspace + object set; use explicit uncertainty |
| Lighting changes | confidence drops | normalize ROI, fixed demo mat, capture varied references |
| Similar components | wrong identity | high-contrast labels/markers for MVP, then remove progressively |
| Future state satisfies earlier state | false pass | already prevented by future-state check |
| LLM produces unsafe pass | false assessment | LLM never owns deterministic pass/fail |
| Model too slow on phone | lag | sample frames, quantize, separate fast detector from explanation model |
| Speech model fails | teaching blocked | checkpoint capture can proceed with manual narration fallback |
| Network unavailable | demo failure | final critical path must be device-local |
| Camera permission denied | dead end | explicit permission recovery UI |
| Build breaks late | lost demo | CI + tagged known-good APK before experimentation |

---

## Commit discipline

Use small meaningful commits around milestones:

- `feat:` product capability
- `fix:` behavior correction
- `test:` verification coverage
- `docs:` pitch/architecture evidence
- `ci:` build quality gates

Do not mix speculative experiments into the last known-good demo branch immediately before judging.
