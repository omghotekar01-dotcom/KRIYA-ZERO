# KRIYA ZERO — Jury Demo Script

Target runtime: **3–4 minutes**.

## Opening — 15 seconds

> "AI can explain how to build this. But explanation is not proof that I can actually do it. KRIYA turns one physical demonstration into a verifier that can watch the next person perform the procedure."

Do not begin with architecture slides. Begin with the object/table and phone.

---

## Act 1 — Teach

1. Open **Teach Mode**.
2. Show the live phone camera framing the tabletop task.
3. Perform/capture the expert procedure.
4. Narrate naturally while completing each visible state.
5. Finish capture.

Desired UI response:

```text
Procedure learned
5 checkpoints
5 critical objects
4 temporal dependencies
Skill Capsule ready
```

Say:

> "These verification rules were generated from the demonstration; we did not build a different Android screen for this procedure."

Only say this when the live compiler path truly supports it.

---

## Act 2 — Inspect the Skill Capsule

Briefly show the generated graph:

```text
S1 → S2 → S3 → S4 → S5
```

Each step should expose compact evidence such as:

- required objects,
- expected state,
- dependency.

Say:

> "The LLM can help structure or explain this representation, but it does not decide whether the learner passes. Pass/fail is applied by explicit verifier constraints."

This is a key technical differentiation.

---

## Act 3 — Learner performs correctly

Reset the physical workspace.

Start **Verify Mode**.

Perform the first checkpoint correctly.

Expected behavior:

```text
STEP 1 VERIFIED
```

Continue one more correct transition if time allows.

---

## Act 4 — Deliberate failure

At the chosen failure step, create an obvious wrong state.

Examples:

- wrong component,
- missing component,
- incorrect orientation,
- skipped required state.

Expected response:

```text
CHECKPOINT NOT VERIFIED
Missing: resistor
or
State mismatch: led-oriented
```

The app must stay on the same required step.

Say:

> "This is not an answer-generation error. The physical state itself failed the procedure constraint."

---

## Act 5 — Recovery

Correct the object/state without restarting the assessment.

Expected response:

```text
STEP VERIFIED
Continue
```

This recovery moment is important. It proves the product is a live tutor/verifier rather than a post-hoc classifier.

---

## Optional Act — Skip-ahead test

Move directly into a known later state before completing the current step.

Expected response:

```text
SEQUENCE ERROR
Future checkpoint detected
Complete Step N first
```

This demonstrates temporal understanding rather than simple object presence.

---

## Act 6 — Assessment

Finish the task and open the report.

Example:

```text
Completion                 100%
First-attempt accuracy      80%
Corrections                   1
Sequence errors               0
Guidance requests             0
```

Say:

> "A normal course tells you that someone opened the content. KRIYA records whether the required physical states were actually achieved, where the learner failed, and whether help was needed."

---

## Offline proof

If the critical inference path is completely device-local by judging day:

1. enable airplane mode,
2. repeat one verification checkpoint,
3. show that it still passes/fails.

Do not claim full offline operation until every dependency used in the demo has been verified offline on the actual device.

---

## Jury questions to expect

### "Isn't this just Vuforia / AR instructions?"

Answer:

> "Traditional systems usually start from an authored procedure and then display or check it. Our research question is demonstration-to-verifier: use one new demonstration to create the executable procedure representation that drives later verification. The hackathon MVP intentionally constrains the physical task domain so we can prove that primitive reliably."

### "Can it learn any human skill?"

Answer:

> "No, and we don't make that claim. The MVP is for structured, visually observable tabletop procedures. Hidden state, high-speed motion and safety-critical work need domain-specific sensors and constraints."

### "Why use an LLM at all?"

Answer:

> "The language model is useful for narration parsing, structuring and explanations. It is not the safety boundary. Explicit visual and temporal constraints decide progression."

### "What happens when vision is uncertain?"

Answer:

> "The correct behavior is not to guess. The perception adapter exposes confidence; below threshold the engine holds the step and asks for a clearer view or human confirmation."

### "What is the business?"

Answer:

> "The same Skill Capsule primitive can serve engineering labs, vocational training, operator onboarding, field service and practical assessment. Experts capture procedures once; learners receive guided practice plus consistent evidence."

### "What is new?"

Answer carefully:

> "We are not claiming that visual work instructions or procedural recognition are new fields. Our differentiating build is the zero-authoring workflow: demonstration → executable graph → immediate live verifier on the phone, rather than manually authoring each verifier step."

Avoid unsupported phrases such as "world's first" unless a formal prior-art search has established them.

---

## Demo fallback hierarchy

If a model component fails during development, degrade gracefully in this order:

1. live detector + real verifier,
2. live detector with one manually confirmed state relation,
3. precomputed semantic checkpoints from a newly captured demonstration,
4. deterministic built-in fixture.

Never disguise fallback mode as live AI. Reliability plus technical honesty is stronger than a staged false claim.
