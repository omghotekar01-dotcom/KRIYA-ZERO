package com.xyro.kriyazero.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.xyro.kriyazero.camera.VisualFingerprintExtractor
import com.xyro.kriyazero.domain.FingerprintStabilizer
import com.xyro.kriyazero.domain.VisualFingerprint
import java.util.concurrent.Executors

@Composable
fun CameraPermissionGate(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        granted = result[Manifest.permission.CAMERA] == true
    }

    if (granted) {
        content()
    } else {
        Column(
            modifier = modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Camera access is required to observe demonstrations and verify physical checkpoints.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(
                onClick = {
                    launcher.launch(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                        ),
                    )
                },
            ) {
                Text("Enable camera + microphone")
            }
        }
    }
}

@Composable
fun KriyaCameraPreview(
    modifier: Modifier = Modifier,
    onFingerprint: (VisualFingerprint) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnFingerprint by rememberUpdatedState(onFingerprint)
    val guideColor = MaterialTheme.colorScheme.primary
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val stabilizer = remember { FingerprintStabilizer(windowSize = 5) }

    Box(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.matchParentSize(),
        )
        Canvas(modifier = Modifier.matchParentSize()) {
            val insetX = size.width * 0.10f
            val insetY = size.height * 0.10f
            drawRect(
                color = guideColor.copy(alpha = 0.92f),
                topLeft = Offset(insetX, insetY),
                size = Size(
                    width = size.width - (2f * insetX),
                    height = size.height - (2f * insetY),
                ),
                style = Stroke(width = 3.dp.toPx()),
            )
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        var lastEmissionMs = 0L
        var analysis: ImageAnalysis? = null

        cameraProviderFuture.addListener(
            {
                val provider = cameraProviderFuture.get()
                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { useCase ->
                        useCase.setAnalyzer(analysisExecutor) { image ->
                            try {
                                val now = SystemClock.elapsedRealtime()
                                if (now - lastEmissionMs >= 180L) {
                                    val fingerprint = VisualFingerprintExtractor.extract(image)
                                    if (fingerprint != null) {
                                        val stableFingerprint = stabilizer.push(fingerprint)
                                        lastEmissionMs = now
                                        mainExecutor.execute {
                                            currentOnFingerprint(stableFingerprint)
                                        }
                                    }
                                }
                            } finally {
                                image.close()
                            }
                        }
                    }

                runCatching {
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                }
            },
            mainExecutor,
        )

        onDispose {
            analysis?.clearAnalyzer()
            stabilizer.reset()
            if (cameraProviderFuture.isDone) {
                runCatching { cameraProviderFuture.get().unbindAll() }
            }
            analysisExecutor.shutdownNow()
        }
    }
}
