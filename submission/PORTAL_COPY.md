# KRIYA ZERO — Reskilll Portal Copy

## Idea title
KRIYA ZERO — One-Demonstration Physical Skill Compiler

## Description
KRIYA ZERO is a phone-native practical skill learning and verification system that converts a single expert demonstration into an executable Skill Capsule.

An expert demonstrates a structured physical task once using the phone camera. KRIYA captures the sequence of physical states, learns visual checkpoints and converts them into a procedural graph containing steps, dependencies and verification conditions. A learner can then perform the same task while the phone observes the workspace, detects incorrect or skipped states, provides guidance and generates an evidence-backed completion report.

Unlike conventional video tutorials or SOP systems that only show instructions, KRIYA can verify whether the learner actually performed the procedure correctly. The hackathon MVP focuses on visually observable tabletop skills such as component placement, small assemblies and practical lab procedures.

The system is Android-first and designed for local, phone-based execution. CameraX provides live visual input, learned visual fingerprints represent freshly demonstrated checkpoints, multi-frame stabilization reduces camera noise, and deterministic procedure logic handles PASS/FAIL and sequence verification. AI/perception components provide evidence, but do not directly control assessment decisions.

The core innovation is:

**Demonstration → Executable Procedural Graph → Live Verifier**

The long-term vision is to make practical skill training scalable for engineering labs, vocational education, ITIs, apprenticeships, field technicians and workforce skilling — especially where constant access to expert instructors is limited.

## Prototype URL
https://github.com/omghotekar01-dotcom/KRIYA-ZERO

## Android proficiency
**Intermediate**

## LLM proficiency
**Experimented with local LLMs**

## Prior builds & hackathons
I regularly build end-to-end AI, mobile and engineering prototypes rather than limiting projects to conceptual submissions. My work includes Android/mobile applications, computer-vision systems, AI/LLM experiments, hardware-software projects and research-oriented prototypes, with multiple projects maintained through GitHub.

I have participated in multiple hackathons and innovation challenges and have worked on projects across AI/ML, fintech, industrial systems, AR/spatial computing, database systems and computer vision. This has given me experience in rapidly converting a problem statement into a working MVP, designing technical architectures, testing prototypes and presenting them under hackathon constraints.

For KRIYA ZERO, I have already created and open-sourced the Android prototype, deterministic procedure-verification engine, CameraX perception pipeline, learned visual checkpoints, sequence-error detection, assessment logic, automated tests and GitHub CI/APK builds before the on-site round.

## What makes you and your team stand out?
I am currently building KRIYA ZERO as a solo student team, which lets me move quickly across product thinking, Android development, AI/ML, computer vision and system architecture without waiting on separate functional teams.

My strongest edge is that I do not approach this as another chatbot or API-wrapper problem. I focus on finding the underlying technical primitive first and then building an end-to-end demonstrable system around it.

For KRIYA, that primitive is **demonstration → executable verifier**. Instead of manually authoring every procedure, the goal is for the phone to observe a previously unseen structured task, learn its physical checkpoints and immediately become capable of guiding and evaluating another learner.

I also have experience combining software with real-world engineering/hardware projects, which is useful because KRIYA operates at the boundary between AI and physical actions. I have already built a working Android foundation with live CameraX input, learned visual checkpoints, deterministic verification, temporal sequence checks, tests and CI rather than entering Phase 1 with only a concept.

My goal during the on-site hackathon would be to push this working base toward stronger on-device AI perception while preserving a reliable offline demo.

## Original-work disclosure
Use the original-work confirmation only with accurate disclosure:

- Pre-existing/open-source components: Android SDK, Kotlin, Jetpack Compose, CameraX and standard/open-source libraries.
- Original project work: KRIYA product concept, Skill Capsule representation, procedure compilation/verification architecture, application flow and project-specific implementation.

## Safe novelty statement
KRIYA ZERO learns previously unseen, visually observable, structured physical procedures from a single demonstration and automatically creates reusable guidance and verification checkpoints without manual procedure authoring or task-specific pass/fail training.

## Do not claim
- Do not claim absolute world-first status.
- Do not claim arbitrary human-skill recognition.
- Do not claim a fully deployed on-device LLM if it is not in the current verified build.
- Do not claim hardware/device validation that has not been performed.
