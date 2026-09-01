# KRIYA ZERO — Final Hackathon Checklist

Use this as the last-minute execution sheet. Do not expand scope unless every P0 item is already green.

## P0 — Do immediately

- [x] GitHub repository created and public: `omghotekar01-dotcom/KRIYA-ZERO`
- [x] Android project committed to `main`
- [x] CameraX live rear-camera flow implemented
- [x] Teach → Compile → Verify → Assessment flow implemented
- [x] Learned visual checkpoints implemented
- [x] Deterministic procedure/sequence verification implemented
- [x] Unit tests configured
- [x] GitHub Actions configured
- [x] Current known-good build passed unit tests and APK assembly
- [x] Debug APK artifact uploaded by CI
- [ ] Install the known-good APK on a real Android/iQOO phone
- [ ] Run the real-device smoke test before making any risky code changes
- [ ] Keep one copy of the known-good APK on the laptop/phone as fallback

Known-good baseline before this checklist-only commit:

- Commit: `7276a197246b5fe482764267fa8c0fec0c17e22b`
- Workflow run: `33535329863`
- Artifact: `kriya-zero-debug`
- Artifact SHA-256: `c709a3f00c827568548ab6b1745604ba037595e6e8903ac515f7f7b9e8a378eb`

## P0 — Phase 1 / submission

If the portal still accepts edits/submission:

- [ ] Track: **Smart Education**
- [ ] Project: **KRIYA ZERO — The One-Demonstration Physical Skill Compiler**
- [ ] Tagline: **Show it once. Learn it anywhere. Prove you can do it.**
- [ ] Repo link: `https://github.com/omghotekar01-dotcom/KRIYA-ZERO`
- [ ] Do not claim “works for every human skill”
- [ ] Use the credible scope: visually observable, structured tabletop procedures
- [ ] Describe the invention as: `demonstration → executable procedural graph → live verifier`
- [ ] Mention that PASS/FAIL is deterministic; AI/perception supplies evidence
- [ ] Submit before polishing screenshots or adding optional features

## P0 — Real-device acceptance

Perform this exact sequence on the phone:

1. Install APK.
2. Grant camera permission.
3. Confirm rear-camera preview and analysis frame.
4. Teach a 3–5 checkpoint physical procedure from scratch.
5. Compile the Skill Capsule.
6. Reset the table completely.
7. Perform Step 1 correctly and verify PASS.
8. Deliberately create a wrong Step 2 state and verify FAIL.
9. Correct Step 2 without restarting and verify PASS.
10. Skip directly toward a later learned state and confirm sequence rejection when distinguishable.
11. Finish the task and open the assessment report.
12. Repeat the full procedure three times.
13. Repeat once in airplane mode for the current offline-critical path.

Do not call the build jury-ready until this passes on the actual device.

## P0 — Demo fixture

Prefer a visually obvious 3–5 state procedure. Good states have large visible changes.

Recommended choices:

- colored blocks/LEGO sequence,
- breadboard/component placement with clearly visible component changes,
- simple mechanical parts arranged in an ordered pattern.

Avoid for the first jury demo:

- tiny wire-only changes,
- visually identical resistor values,
- hidden electrical correctness that the camera cannot see,
- flexible/soft objects that move unpredictably,
- handheld phone movement while teaching and verifying.

Use a phone stand or mark the phone position if possible.

## P0 — 3-minute jury story

Opening:

> “A video can tell me how somebody performed a practical task, but it cannot prove that I can perform it. KRIYA learns the physical procedure from a demonstration and turns it into an executable verifier.”

Then show only:

1. fresh Teach,
2. generated checkpoints,
3. reset,
4. correct state,
5. intentional wrong state,
6. correction,
7. final skill report.

Closing:

> “The important part is that this verifier did not exist before the demonstration. The phone learned the procedure and immediately became the instructor and assessor.”

## P1 — Only after the demo-critical path is stable

- [ ] Expose Saved Skills UI using the existing local persistence backend
- [ ] Add local image embedding model for viewpoint/semantic robustness
- [ ] Add local speech-to-text/narration capture
- [ ] Add richer semantic object/state evidence
- [ ] Add polished judge mode and visual guidance

Do not trade a working P0 demo for these P1 features.

## P2 — After selection / during hackathon if time permits

- AR overlays
- multilingual local explanations
- instructor analytics
- Skill Capsule sharing/export
- richer object/hand tracking
- multi-view verification
- calibration assistant
- device-specific performance profiling

## Things to carry

- [ ] Laptop + charger
- [ ] Phone charging cable/power bank
- [ ] Known-good APK offline copy
- [ ] Repository cloned locally
- [ ] Android Studio / SDK ready
- [ ] Demo fixture + spare components
- [ ] Phone stand/tripod or stable support
- [ ] Backup fixture with clearly separated visual states
- [ ] Printed/phone copy of demo script
- [ ] Hotspot only as fallback; critical demo should not require internet

## Do not do at the last minute

- Do not replace the verification engine with an LLM decision.
- Do not rewrite the architecture.
- Do not upgrade Gradle/Kotlin/Compose versions without a concrete blocker.
- Do not add a cloud backend to the critical demo path.
- Do not promise arbitrary open-world skill recognition.
- Do not demo a task whose checkpoints are visually indistinguishable.
- Do not install an untested build over the only known-good APK.

## Final green-light definition

The project is ready to present when all of these are true:

- [ ] submission is locked/saved,
- [ ] current APK is installed,
- [ ] camera works on the actual device,
- [ ] fresh procedure can be taught,
- [ ] correct state passes,
- [ ] wrong state fails,
- [ ] correction passes,
- [ ] full completion report appears,
- [ ] three consecutive runs succeed,
- [ ] backup APK exists,
- [ ] demo can be completed without explaining implementation details while operating it.

If anything below P0 is incomplete, ignore it until P0 is stable.
