package com.xyro.kriyazero.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
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
import com.xyro.kriyazero.domain.VisualFingerprint
import com.xyro.kriyazero.engine.ProcedureCompiler
import com.xyro.kriyazero.engine.VerificationEngine

private enum class LiveScreen { HOME, TEACH, CAPSULE, VERIFY, REPORT }

@Composable
fun KriyaLiveApp() {
    var screen by remember { mutableStateOf(LiveScreen.HOME) }
    val captured = remember { mutableStateListOf<DemonstrationSegment>() }
    var capsule by remember { mutableStateOf<SkillCapsule?>(null) }
    var engine by remember { mutableStateOf<VerificationEngine?>(null) }
    var report by remember { mutableStateOf<AssessmentReport?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("KRIYA ZERO", fontWeight = FontWeight.Black)
                        Text("one-demonstration skill compiler", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    if (screen != LiveScreen.HOME) {
                        TextButton(onClick = {
                            screen = LiveScreen.HOME
                            engine = null
                            report = null
                        }) { Text("Home") }
                    }
                },
            )
        },
    ) { insets ->
        when (screen) {
            LiveScreen.HOME -> LiveHome(
                modifier = Modifier.padding(insets),
                onTeach = {
                    captured.clear()
                    screen = LiveScreen.TEACH
                },
                onDemo = {
                    capsule = DemoScenario.skillCapsule()
                    screen = LiveScreen.CAPSULE
                },
            )

            LiveScreen.TEACH -> LiveTeach(
                modifier = Modifier.padding(insets),
                segments = captured,
                onCapture = { narration, objects, states, fingerprint ->
                    captured += DemonstrationSegment(
                        index = captured.size,
                        narration = narration,
                        observedObjects = csv(objects),
                        stateTags = csv(states),
                        visualFingerprint = fingerprint,
                    )
                },
                onCompile = { name ->
                    capsule = ProcedureCompiler().compile(name, captured)
                    screen = LiveScreen.CAPSULE
                },
            )

            LiveScreen.CAPSULE -> capsule?.let { active ->
                LiveCapsule(
                    modifier = Modifier.padding(insets),
                    capsule = active,
                    onVerify = {
                        engine = VerificationEngine(active)
                        screen = LiveScreen.VERIFY
                    },
                )
            }

            LiveScreen.VERIFY -> {
                val activeCapsule = capsule
                val activeEngine = engine
                if (activeCapsule != null && activeEngine != null) {
                    LiveVerify(
                        modifier = Modifier.padding(insets),
                        capsule = activeCapsule,
                        engine = activeEngine,
                        onReport = {
                            report = activeEngine.report()
                            screen = LiveScreen.REPORT
                        },
                    )
                }
            }

            LiveScreen.REPORT -> report?.let { activeReport ->
                LiveReport(
                    modifier = Modifier.padding(insets),
                    report = activeReport,
                    onAgain = {
                        capsule?.let {
                            engine = VerificationEngine(it)
                            report = null
                            screen = LiveScreen.VERIFY
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun LiveHome(
    modifier: Modifier,
    onTeach: () -> Unit,
    onDemo: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Show it once. Learn it anywhere. Prove you can do it.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
        )
        Text(
            "Capture a physical procedure as visual checkpoints. KRIYA compiles them into an executable Skill Capsule and verifies the next learner against the learned sequence.",
            style = MaterialTheme.typography.bodyLarge,
        )
        LiveChips(listOf("CAMERA-NATIVE", "LOCAL VISUAL MATCH", "DETERMINISTIC GATE"))

        LiveActionCard(
            title = "Teach a new physical skill",
            body = "No task-specific model is required for the fingerprint path: the live phone camera itself learns each checkpoint from your demonstration.",
            action = "Open Teach mode",
            onClick = onTeach,
        )
        LiveActionCard(
            title = "Run deterministic regression fixture",
            body = "Use the built-in LED circuit capsule to exercise wrong-state, correction and sequence-error behavior while developing perception.",
            action = "Open fixture",
            onClick = onDemo,
        )
    }
}

@Composable
private fun LiveTeach(
    modifier: Modifier,
    segments: List<DemonstrationSegment>,
    onCapture: (String, String, String, VisualFingerprint?) -> Unit,
    onCompile: (String) -> Unit,
) {
    var skillName by remember { mutableStateOf("New tabletop skill") }
    var narration by remember { mutableStateOf("") }
    var objects by remember { mutableStateOf("") }
    var states by remember { mutableStateOf("") }
    var latestFingerprint by remember { mutableStateOf<VisualFingerprint?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LiveHeader(
            kicker = "TEACH",
            title = "Demonstrate one checkpoint at a time",
            body = "Keep the phone position stable. Each capture stores a compact visual fingerprint learned from the current camera frame. Object/state labels are optional development metadata.",
        )

        CameraPermissionGate {
            KriyaCameraPreview(
                modifier = Modifier
                    .height(280.dp)
                    .clip(RoundedCornerShape(28.dp)),
                onFingerprint = { latestFingerprint = it },
            )
        }

        val cameraReady = latestFingerprint != null
        Surface(shape = RoundedCornerShape(18.dp), tonalElevation = 2.dp) {
            Text(
                text = if (cameraReady) {
                    "LIVE FINGERPRINT READY · ${latestFingerprint?.gridSize}×${latestFingerprint?.gridSize} grid"
                } else {
                    "Waiting for live camera fingerprint…"
                },
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
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
            label = { Text("What did you just do?") },
            minLines = 2,
        )
        OutlinedTextField(
            value = objects,
            onValueChange = { objects = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Optional object labels") },
            placeholder = { Text("resistor, LED") },
        )
        OutlinedTextField(
            value = states,
            onValueChange = { states = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Optional state tags") },
            placeholder = { Text("LED-oriented") },
        )

        Button(
            onClick = {
                onCapture(narration, objects, states, latestFingerprint)
                narration = ""
                objects = ""
                states = ""
            },
            enabled = narration.isNotBlank() &&
                (latestFingerprint != null || csv(objects).isNotEmpty() || csv(states).isNotEmpty()),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Capture learned checkpoint ${segments.size + 1}")
        }

        segments.forEach { segment ->
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Checkpoint ${segment.index + 1}", fontWeight = FontWeight.Bold)
                    Text(segment.narration)
                    Text(
                        if (segment.visualFingerprint != null) "Visual verifier learned" else "Semantic fallback only",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        FilledTonalButton(
            onClick = { onCompile(skillName) },
            enabled = segments.isNotEmpty() && skillName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Compile ${segments.size} checkpoint(s)")
        }
    }
}

@Composable
private fun LiveCapsule(
    modifier: Modifier,
    capsule: SkillCapsule,
    onVerify: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LiveHeader(
            kicker = "SKILL CAPSULE",
            title = capsule.name,
            body = "${capsule.steps.size} ordered checkpoints compiled into one executable procedure.",
        )

        capsule.orderedSteps.forEachIndexed { index, step ->
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${index + 1}. ${step.title}", fontWeight = FontWeight.Bold)
                    Text(step.instruction)
                    val labels = buildList {
                        if (step.visualFingerprint != null) add("VISUAL · learned")
                        addAll(step.requiredObjects.map { "OBJ · $it" })
                        addAll(step.expectedStateTags.map { "STATE · $it" })
                    }
                    LiveChips(labels)
                    if (step.dependsOn.isNotEmpty()) {
                        Text("Depends on ${step.dependsOn.joinToString()}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Button(onClick = onVerify, modifier = Modifier.fillMaxWidth()) {
            Text("Verify another learner")
        }
    }
}

@Composable
private fun LiveVerify(
    modifier: Modifier,
    capsule: SkillCapsule,
    engine: VerificationEngine,
    onReport: () -> Unit,
) {
    var latestFingerprint by remember { mutableStateOf<VisualFingerprint?>(null) }
    var decision by remember(engine) { mutableStateOf<VerificationDecision?>(null) }
    val current = engine.currentStep()
    val currentIndex = current?.let { capsule.orderedSteps.indexOfFirst { candidate -> candidate.id == it.id } }
        ?: capsule.orderedSteps.lastIndex
    val similarity = if (current?.visualFingerprint != null && latestFingerprint != null) {
        current.visualFingerprint.similarity(latestFingerprint!!)
    } else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LiveHeader(
            kicker = "VERIFY",
            title = current?.title ?: "Skill complete",
            body = current?.instruction ?: "All required checkpoints have passed.",
        )

        LinearProgressIndicator(
            progress = { engine.completedSteps().size.toFloat() / capsule.steps.size.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )

        CameraPermissionGate {
            KriyaCameraPreview(
                modifier = Modifier
                    .height(300.dp)
                    .clip(RoundedCornerShape(28.dp)),
                onFingerprint = { latestFingerprint = it },
            )
        }

        if (current != null) {
            if (current.visualFingerprint != null) {
                Surface(shape = RoundedCornerShape(18.dp), tonalElevation = 2.dp) {
                    Text(
                        text = if (similarity == null) {
                            "Waiting for camera evidence…"
                        } else {
                            "LIVE VISUAL SIMILARITY · ${(similarity * 100).toInt()}%"
                        },
                        modifier = Modifier.padding(14.dp),
                        fontWeight = FontWeight.Bold,
                    )
                }
                Button(
                    onClick = {
                        latestFingerprint?.let {
                            decision = engine.verify(liveObservation(current, it))
                        }
                    },
                    enabled = latestFingerprint != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Verify this live camera state")
                }
            } else {
                Surface(shape = RoundedCornerShape(18.dp), tonalElevation = 2.dp) {
                    Text(
                        "Built-in deterministic fixture: use the regression controls below.",
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }

            OutlinedButton(
                onClick = { decision = engine.verify(correctObservation(current)) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Regression: expected state") }

            OutlinedButton(
                onClick = { decision = engine.verify(mismatchObservation(current)) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Regression: visible mismatch") }

            capsule.orderedSteps.getOrNull(currentIndex + 1)?.let { future ->
                OutlinedButton(
                    onClick = { decision = engine.verify(correctObservation(future)) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Regression: skip ahead") }
            }

            TextButton(
                onClick = { engine.requestAssistance() },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) { Text("Request guidance") }
        }

        decision?.let { LiveDecision(it) }

        if (engine.currentStep() == null) {
            Button(onClick = onReport, modifier = Modifier.fillMaxWidth()) {
                Text("Open assessment report")
            }
        }
    }
}

@Composable
private fun LiveReport(
    modifier: Modifier,
    report: AssessmentReport,
    onAgain: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LiveHeader(
            kicker = "ASSESSMENT",
            title = report.capsuleName,
            body = "Verification results are derived from explicit checkpoint evidence and temporal constraints.",
        )
        LiveMetric("Completion", "${report.completionPercent}%")
        LiveMetric("First-attempt accuracy", "${report.firstAttemptAccuracyPercent}%")
        LiveMetric("Failed checks", report.failedAttempts.toString())
        LiveMetric("Sequence errors", report.sequenceErrors.toString())
        LiveMetric("Guidance requests", report.assistanceCount.toString())
        Button(onClick = onAgain, modifier = Modifier.fillMaxWidth()) { Text("Run again") }
    }
}

@Composable
private fun LiveDecision(decision: VerificationDecision) {
    val color = when (decision.status) {
        VerificationStatus.PASS, VerificationStatus.COMPLETE -> MaterialTheme.colorScheme.primaryContainer
        VerificationStatus.FAIL -> MaterialTheme.colorScheme.errorContainer
        VerificationStatus.SEQUENCE_ERROR -> MaterialTheme.colorScheme.secondaryContainer
    }
    Surface(color = color, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(decision.status.name.replace('_', ' '), fontWeight = FontWeight.Black)
            Text(decision.message)
            decision.visualSimilarity?.let {
                Text("Visual match ${(it * 100).toInt()}%", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun LiveActionCard(
    title: String,
    body: String,
    action: String,
    onClick: () -> Unit,
) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(body)
            FilledTonalButton(onClick = onClick) { Text(action) }
        }
    }
}

@Composable
private fun LiveHeader(kicker: String, title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(kicker, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LiveMetric(label: String, value: String) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun LiveChips(labels: List<String>) {
    if (labels.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEach { label -> AssistChip(onClick = {}, label = { Text(label) }) }
    }
}

private fun csv(value: String): Set<String> = value
    .split(',')
    .map { it.trim().lowercase() }
    .filter { it.isNotBlank() }
    .toSet()

private fun liveObservation(step: ProcedureStep, fingerprint: VisualFingerprint): Observation = Observation(
    objectConfidence = step.requiredObjects.associateWith { 0.99f },
    stateTags = step.expectedStateTags,
    visualFingerprint = fingerprint,
)

private fun correctObservation(step: ProcedureStep): Observation = Observation(
    objectConfidence = step.requiredObjects.associateWith { 0.99f },
    stateTags = step.expectedStateTags,
    visualFingerprint = step.visualFingerprint,
)

private fun mismatchObservation(step: ProcedureStep): Observation {
    val missingObject = step.requiredObjects.firstOrNull()
    val missingState = if (missingObject == null) step.expectedStateTags.firstOrNull() else null
    return Observation(
        objectConfidence = step.requiredObjects
            .filterNot { it == missingObject }
            .associateWith { 0.99f },
        stateTags = step.expectedStateTags.filterNot { it == missingState }.toSet(),
        visualFingerprint = null,
    )
}
