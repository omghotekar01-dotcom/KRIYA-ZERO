package com.xyro.kriyazero.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xyro.kriyazero.data.DemoScenario
import com.xyro.kriyazero.domain.AssessmentReport
import com.xyro.kriyazero.domain.DemonstrationSegment
import com.xyro.kriyazero.domain.Observation
import com.xyro.kriyazero.domain.ProcedureStep
import com.xyro.kriyazero.domain.SkillCapsule
import com.xyro.kriyazero.domain.VerificationDecision
import com.xyro.kriyazero.domain.VerificationStatus
import com.xyro.kriyazero.engine.ProcedureCompiler
import com.xyro.kriyazero.engine.VerificationEngine

private enum class AppScreen {
    HOME,
    TEACH,
    CAPSULE,
    PRACTICE,
    REPORT,
}

@Composable
fun KriyaApp() {
    var screen by remember { mutableStateOf(AppScreen.HOME) }
    var activeCapsule by remember { mutableStateOf<SkillCapsule?>(null) }
    var verificationEngine by remember { mutableStateOf<VerificationEngine?>(null) }
    var assessmentReport by remember { mutableStateOf<AssessmentReport?>(null) }
    val capturedSegments = remember { mutableStateListOf<DemonstrationSegment>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("KRIYA ZERO", fontWeight = FontWeight.Black)
                        Text(
                            "physical skill compiler",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
                actions = {
                    if (screen != AppScreen.HOME) {
                        TextButton(
                            onClick = {
                                screen = AppScreen.HOME
                                verificationEngine = null
                                assessmentReport = null
                            },
                        ) {
                            Text("Home")
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (screen) {
            AppScreen.HOME -> HomeScreen(
                modifier = Modifier.padding(padding),
                onTeach = {
                    capturedSegments.clear()
                    screen = AppScreen.TEACH
                },
                onUseDemo = {
                    activeCapsule = DemoScenario.skillCapsule()
                    screen = AppScreen.CAPSULE
                },
            )

            AppScreen.TEACH -> TeachScreen(
                modifier = Modifier.padding(padding),
                segments = capturedSegments,
                onAddSegment = { narration, objects, stateTags ->
                    capturedSegments += DemonstrationSegment(
                        index = capturedSegments.size,
                        narration = narration,
                        observedObjects = parseSemanticCsv(objects),
                        stateTags = parseSemanticCsv(stateTags),
                    )
                },
                onCompile = { skillName ->
                    activeCapsule = ProcedureCompiler().compile(skillName, capturedSegments)
                    screen = AppScreen.CAPSULE
                },
            )

            AppScreen.CAPSULE -> activeCapsule?.let { capsule ->
                CapsuleScreen(
                    modifier = Modifier.padding(padding),
                    capsule = capsule,
                    onStartAssessment = {
                        verificationEngine = VerificationEngine(capsule)
                        assessmentReport = null
                        screen = AppScreen.PRACTICE
                    },
                )
            }

            AppScreen.PRACTICE -> {
                val capsule = activeCapsule
                val engine = verificationEngine
                if (capsule != null && engine != null) {
                    PracticeScreen(
                        modifier = Modifier.padding(padding),
                        capsule = capsule,
                        engine = engine,
                        onFinish = {
                            assessmentReport = engine.report()
                            screen = AppScreen.REPORT
                        },
                    )
                }
            }

            AppScreen.REPORT -> assessmentReport?.let { report ->
                ReportScreen(
                    modifier = Modifier.padding(padding),
                    report = report,
                    onRunAgain = {
                        activeCapsule?.let {
                            verificationEngine = VerificationEngine(it)
                            assessmentReport = null
                            screen = AppScreen.PRACTICE
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    onTeach: () -> Unit,
    onUseDemo: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            "Show it once. Learn it anywhere. Prove you can do it.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
        )
        Text(
            "KRIYA turns a physical demonstration into an executable procedure graph that can teach, verify and assess the next learner.",
            style = MaterialTheme.typography.bodyLarge,
        )

        ChipRow(listOf("PHONE-FIRST", "LOCAL-FIRST", "DETERMINISTIC PASS/FAIL"))

        FeatureCard(
            eyebrow = "TEACH",
            title = "Create a Skill Capsule",
            body = "Use the camera while demonstrating a structured tabletop task. Capture semantic checkpoints, then compile them into steps, dependencies and verifier evidence.",
            actionLabel = "Teach a new skill",
            onAction = onTeach,
        )

        FeatureCard(
            eyebrow = "30-HOUR SAFE MODE",
            title = "Run the LED circuit fixture",
            body = "A deterministic reference capsule exercises the complete product loop and regression tests while live on-device perception adapters are integrated.",
            actionLabel = "Open demo capsule",
            onAction = onUseDemo,
        )

        PipelineCard()
    }
}

@Composable
private fun TeachScreen(
    modifier: Modifier,
    segments: List<DemonstrationSegment>,
    onAddSegment: (String, String, String) -> Unit,
    onCompile: (String) -> Unit,
) {
    var skillName by remember { mutableStateOf("Untitled physical skill") }
    var narration by remember { mutableStateOf("") }
    var objects by remember { mutableStateOf("") }
    var states by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionHeader(
            kicker = "TEACH MODE",
            title = "Demonstrate the procedure",
            body = "Camera capture is live. For this foundation build, semantic evidence is entered through the fallback adapter; live vision/audio adapters feed the exact same schema next.",
        )

        CameraPermissionGate {
            KriyaCameraPreview(
                modifier = Modifier
                    .height(270.dp)
                    .clip(RoundedCornerShape(28.dp)),
            )
        }

        OutlinedTextField(
            value = skillName,
            onValueChange = { skillName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Skill name") },
            singleLine = true,
        )
        OutlinedTextField(
            value = narration,
            onValueChange = { narration = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Narration for this checkpoint") },
            minLines = 2,
        )
        OutlinedTextField(
            value = objects,
            onValueChange = { objects = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Observed objects · comma separated") },
            placeholder = { Text("breadboard, resistor, LED") },
        )
        OutlinedTextField(
            value = states,
            onValueChange = { states = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Visual state tags · comma separated") },
            placeholder = { Text("resistor-seated, LED-oriented") },
        )

        Button(
            onClick = {
                onAddSegment(narration, objects, states)
                narration = ""
                objects = ""
                states = ""
            },
            enabled = narration.isNotBlank() &&
                (parseSemanticCsv(objects).isNotEmpty() || parseSemanticCsv(states).isNotEmpty()),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Capture checkpoint ${segments.size + 1}")
        }

        if (segments.isNotEmpty()) {
            Text(
                "Captured checkpoints",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            segments.forEach { segment ->
                CompactCheckpointCard(segment)
            }
        }

        FilledTonalButton(
            onClick = { onCompile(skillName) },
            enabled = skillName.isNotBlank() && segments.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Compile ${segments.size} checkpoint(s) into Skill Capsule")
        }
    }
}

@Composable
private fun CapsuleScreen(
    modifier: Modifier,
    capsule: SkillCapsule,
    onStartAssessment: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionHeader(
            kicker = "COMPILED",
            title = capsule.name,
            body = "${capsule.steps.size} executable checkpoints generated. Each step contains evidence plus explicit temporal dependencies.",
        )

        capsule.orderedSteps.forEachIndexed { index, step ->
            ProcedureStepCard(index, step)
        }

        Button(
            onClick = onStartAssessment,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start learner verification")
        }
    }
}

@Composable
private fun PracticeScreen(
    modifier: Modifier,
    capsule: SkillCapsule,
    engine: VerificationEngine,
    onFinish: () -> Unit,
) {
    var lastDecision by remember(engine) { mutableStateOf<VerificationDecision?>(null) }
    val currentStep = engine.currentStep()
    val index = currentStep?.let { capsule.orderedSteps.indexOfFirst { candidate -> candidate.id == it.id } }
        ?: capsule.orderedSteps.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionHeader(
            kicker = "VERIFY MODE",
            title = currentStep?.title ?: "Skill complete",
            body = currentStep?.instruction
                ?: "Every required checkpoint has been verified.",
        )

        LinearProgressIndicator(
            progress = {
                engine.completedSteps().size.toFloat() / capsule.steps.size.toFloat()
            },
            modifier = Modifier.fillMaxWidth(),
        )

        CameraPermissionGate {
            KriyaCameraPreview(
                modifier = Modifier
                    .height(300.dp)
                    .clip(RoundedCornerShape(28.dp)),
            )
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp,
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "PERCEPTION ADAPTER",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    "Camera is live; semantic verification controls below currently inject deterministic observations. This makes the execution engine testable before the on-device detector is connected.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (currentStep != null) {
            ChipRow(
                currentStep.requiredObjects.map { "OBJ · $it" } +
                    currentStep.expectedStateTags.map { "STATE · $it" },
            )

            Button(
                onClick = {
                    lastDecision = engine.verify(correctObservation(currentStep))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Verify expected live state")
            }

            OutlinedButton(
                onClick = {
                    lastDecision = engine.verify(mismatchObservation(currentStep))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Inject visible mismatch")
            }

            val nextStep = capsule.orderedSteps.getOrNull(index + 1)
            if (nextStep != null) {
                OutlinedButton(
                    onClick = {
                        lastDecision = engine.verify(correctObservation(nextStep))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Try skipping to next checkpoint")
                }
            }

            TextButton(
                onClick = { engine.requestAssistance() },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("Request guidance")
            }
        }

        lastDecision?.let { decision ->
            DecisionCard(decision)
        }

        if (engine.currentStep() == null) {
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Open verified skill report")
            }
        }
    }
}

@Composable
private fun ReportScreen(
    modifier: Modifier,
    report: AssessmentReport,
    onRunAgain: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(
            kicker = "EVIDENCE-BACKED ASSESSMENT",
            title = report.capsuleName,
            body = if (report.completedSteps == report.totalSteps) {
                "Procedure completed and every required checkpoint reached a verified state."
            } else {
                "Assessment is incomplete."
            },
        )

        MetricCard("Completion", "${report.completionPercent}%")
        MetricCard("First-attempt accuracy", "${report.firstAttemptAccuracyPercent}%")
        MetricCard("Corrections / failed checks", report.failedAttempts.toString())
        MetricCard("Sequence errors", report.sequenceErrors.toString())
        MetricCard("Guidance requests", report.assistanceCount.toString())

        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 2.dp,
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    if (report.independentlyCompleted) "INDEPENDENT COMPLETION" else "ASSISTED COMPLETION",
                    fontWeight = FontWeight.Black,
                )
                Text(
                    "The report separates visual failures, sequence errors and requests for help instead of collapsing everything into a single opaque AI score.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Button(
            onClick = onRunAgain,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Run assessment again")
        }
    }
}

@Composable
private fun PipelineCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("THE CORE PRIMITIVE", fontWeight = FontWeight.Black)
            Text(
                "Demonstration → semantic evidence → procedure graph → deterministic verifier",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Models can be swapped. The Skill Capsule contract and execution rules stay stable.",
            )
        }
    }
}

@Composable
private fun FeatureCard(
    eyebrow: String,
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                eyebrow,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodyMedium)
            FilledTonalButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun ProcedureStepCard(index: Int, step: ProcedureStep) {
    Card(
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${index + 1}", fontWeight = FontWeight.Black)
                }
                Column {
                    Text(step.title, fontWeight = FontWeight.Bold)
                    Text(step.instruction, style = MaterialTheme.typography.bodySmall)
                }
            }
            HorizontalDivider()
            ChipRow(
                step.requiredObjects.map { "OBJ · $it" } +
                    step.expectedStateTags.map { "STATE · $it" },
            )
            if (step.dependsOn.isNotEmpty()) {
                Text(
                    "Depends on ${step.dependsOn.joinToString()}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun CompactCheckpointCard(segment: DemonstrationSegment) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Checkpoint ${segment.index + 1}", fontWeight = FontWeight.Bold)
            Text(segment.narration, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${segment.observedObjects.size} object(s) · ${segment.stateTags.size} state tag(s)",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun DecisionCard(decision: VerificationDecision) {
    val container = when (decision.status) {
        VerificationStatus.PASS,
        VerificationStatus.COMPLETE -> MaterialTheme.colorScheme.primaryContainer
        VerificationStatus.FAIL -> MaterialTheme.colorScheme.errorContainer
        VerificationStatus.SEQUENCE_ERROR -> MaterialTheme.colorScheme.secondaryContainer
    }

    Surface(
        color = container,
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(decision.status.name.replace('_', ' '), fontWeight = FontWeight.Black)
            Text(decision.message)
            if (decision.missingObjects.isNotEmpty()) {
                Text("Missing objects: ${decision.missingObjects.joinToString()}")
            }
            if (decision.missingStateTags.isNotEmpty()) {
                Text("Missing states: ${decision.missingStateTags.joinToString()}")
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SectionHeader(kicker: String, title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            kicker,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
        )
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ChipRow(labels: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEach { label ->
            AssistChip(
                onClick = {},
                label = { Text(label) },
            )
        }
    }
}

private fun parseSemanticCsv(value: String): Set<String> = value
    .split(',')
    .map { it.trim().lowercase() }
    .filter { it.isNotBlank() }
    .toSet()

private fun correctObservation(step: ProcedureStep): Observation = Observation(
    objectConfidence = step.requiredObjects.associateWith { 0.97f },
    stateTags = step.expectedStateTags,
)

private fun mismatchObservation(step: ProcedureStep): Observation {
    val missingObject = step.requiredObjects.firstOrNull()
    val missingState = if (missingObject == null) step.expectedStateTags.firstOrNull() else null

    return Observation(
        objectConfidence = step.requiredObjects
            .filterNot { it == missingObject }
            .associateWith { 0.97f },
        stateTags = step.expectedStateTags.filterNot { it == missingState }.toSet(),
    )
}
