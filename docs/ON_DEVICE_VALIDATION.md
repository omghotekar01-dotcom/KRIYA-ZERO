# KRIYA ZERO — Real-Device Acceptance Protocol

This document defines the minimum evidence required before a build may be called **demo ready**.

A green CI build proves compilation and deterministic tests. It does **not** prove that camera exposure, framing, visual thresholds, lifecycle behavior or physical checkpoint discrimination work on the actual iQOO phone.

## Test environment

Record before each validation session:

- phone model
- Android version
- app commit SHA
- APK build/run number
- room / lighting condition
- phone-to-workspace distance
- whether phone is handheld or mounted
- demo fixture version

Do not change multiple physical variables while tuning a threshold.

---

## A. Clean-install smoke test

1. Uninstall previous KRIYA ZERO build.
2. Install the latest CI debug APK.
3. Launch from the app drawer.
4. Grant camera permission.
5. Confirm rear-camera preview appears.
6. Confirm the analysis guide is visible and aligned with the central workspace.
7. Leave and re-enter the app once.
8. Rotate/lock the phone as expected for the demo and confirm there is no crash.

**Pass:** no crash, black preview, frozen analysis or permission loop.

---

## B. Visual-stability baseline

Use an unchanged empty workspace.

1. Keep the phone stationary.
2. Observe the live scene for at least 10 seconds.
3. Capture the same visual checkpoint three separate times without changing the table.
4. Compare repeat-state similarity.

Target before tuning the acceptance threshold:

- repeated unchanged state should remain comfortably above the pass threshold,
- exposure/autofocus settling should not cause repeated false failures,
- hand movement outside the marked analysis region should have limited effect.

If this fails, fix framing/stabilization before testing more complex procedures.

---

## C. Checkpoint separability test

Teach the intended 4–6 step jury procedure.

Immediately inspect the visual separability report.

For every checkpoint:

1. identify its nearest competing learned checkpoint,
2. record their similarity,
3. re-capture states that are flagged `AMBIGUOUS`,
4. change the fixture only if states are genuinely too visually similar.

A good hackathon fixture deliberately creates visible transitions. Do not try to compensate for indistinguishable states by lowering thresholds until false passes appear.

---

## D. End-to-end fresh-teach test

The procedure used for this test must be freshly captured in the app.

1. Open **Teach**.
2. Capture starting state.
3. Perform and capture each new physical state.
4. Compile the Skill Capsule.
5. Confirm every intended step is present.
6. Confirm every camera-learned step displays visual evidence.
7. Reset the physical workspace completely.
8. Enter **Verify**.

**Pass:** the verifier is using the freshly created capsule, not only the built-in LED regression fixture.

---

## E. Correct execution test

Run the newly taught procedure exactly as demonstrated.

For each step:

1. reach the intended physical state,
2. hold the table stable briefly,
3. trigger verification,
4. record live visual similarity,
5. confirm progression to the next step.

**Pass:** 100% completion without false rejection under normal demo conditions.

Repeat at least three full runs.

---

## F. Wrong-state rejection test

Choose a checkpoint with an obvious physical difference.

Examples:

- component missing,
- wrong component present,
- component placed in the wrong region,
- orientation changed,
- required connection not yet made.

1. reach the wrong state,
2. verify,
3. confirm KRIYA stays on the current required step,
4. confirm the report increments a failed check,
5. correct the state without restarting,
6. verify again.

**Pass:** wrong state fails and corrected state subsequently passes.

---

## G. Skip-ahead test

For a procedure with steps `S1 → S2 → S3`, move directly from `S1` into the physical state taught as `S3`.

Expected behavior:

```text
SEQUENCE ERROR
future checkpoint detected
current required checkpoint remains S2
```

**Pass:** the future state cannot silently satisfy an earlier cumulative checkpoint.

---

## H. Viewpoint tolerance test

KRIYA's lightweight fingerprint path assumes a reasonably stable viewpoint. Quantify that assumption instead of hiding it.

Starting from a known passing state:

1. move the phone ~1 cm left/right,
2. change tilt slightly,
3. move closer/farther slightly,
4. repeat verification.

Record where accuracy breaks down.

For the hackathon, use a simple phone stand/marked placement if necessary. A constrained system with a measured operating envelope is more credible than an unmeasured open-world claim.

---

## I. Lighting test

Test at least:

- normal indoor lighting,
- slightly dimmer lighting,
- brighter direct overhead lighting.

Because luma is mean-normalized, uniform brightness changes should be less harmful than structural/chroma changes, but this must be confirmed on the real phone.

**Pass:** the normal venue-like range does not cause systematic false failures.

---

## J. Offline test

Only perform this after the demo-critical path has no remote dependencies.

1. force-close KRIYA,
2. enable airplane mode,
3. relaunch,
4. open or teach the demo procedure,
5. perform one correct checkpoint,
6. perform one wrong checkpoint,
7. finish the procedure,
8. open assessment report.

**Pass:** all critical demo behavior works without network.

Do not claim offline inference publicly until this passes.

---

## K. Persistence test

Once Saved Skills is wired into UI:

1. teach and save a capsule,
2. force-close the app,
3. relaunch,
4. load the capsule,
5. verify its first checkpoint,
6. reboot the phone and repeat.

**Pass:** no capsule corruption and learned visual evidence survives restart.

---

## L. Jury chaos test

Ask a teammate who did not teach the procedure to operate KRIYA with no coaching.

Then deliberately introduce one surprise at a time:

- hand enters frame,
- table bumped slightly,
- wrong object appears,
- component temporarily obscured,
- learner pauses between steps,
- learner asks for guidance,
- app backgrounded and resumed.

Write down every confusing behavior. Fix the highest-probability demo failures first.

---

# Release gate

A commit may be tagged as the jury fallback build only after all of the following are true:

- [ ] CI unit tests green
- [ ] debug APK produced
- [ ] clean install passes
- [ ] fresh Teach → Compile works
- [ ] three correct full executions pass
- [ ] wrong-state rejection passes
- [ ] correction without restart passes
- [ ] skip-ahead rejection passes
- [ ] venue-like lighting passes
- [ ] offline critical path passes
- [ ] persistence survives force-close once UI integration is enabled
- [ ] another person can operate the demo without developer intervention

Keep the last passing APK installed on a second device/laptop before experimenting with higher-risk perception features.
