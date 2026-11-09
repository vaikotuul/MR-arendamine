package com.example.mobiilr

import android.view.MotionEvent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberOnGestureListener

private class FrameHolder {
    var frame: Frame? = null
}

private fun Frame.floorHit(event: MotionEvent): HitResult? {
    if (camera.trackingState != TrackingState.TRACKING) return null
    return hitTest(event).firstOrNull { hit ->
        val plane = hit.trackable
        plane is Plane &&
                plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                plane.trackingState == TrackingState.TRACKING &&
                plane.isPoseInPolygon(hit.hitPose) &&
                hit.distance <= 5.0f
    }
}

@Composable
fun ArScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val anchors = remember { mutableStateListOf<Anchor>() }
    val frameHolder = remember { FrameHolder() }
    var planeFound by remember { mutableStateOf(false) }
    var fastSpin by remember { mutableStateOf(false) }
    var debugInfo by remember { mutableStateOf("") }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val duck = rememberModelInstance(modelLoader, "models/Duck.glb")

    val transition = rememberInfiniteTransition(label = "spin")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (fastSpin) 800 else 4000, easing = LinearEasing)
        ),
        label = "angle"
    )

    DisposableEffect(Unit) {
        onDispose {
            anchors.forEach { it.detach() }
            anchors.clear()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            planeRenderer = true,
            onSessionUpdated = { session, frame ->
                frameHolder.frame = frame
                debugInfo = "tracking=${frame.camera.trackingState} " +
                        "reason=${frame.camera.trackingFailureReason} " +
                        "planes=${session.getAllTrackables(Plane::class.java).size}"
                if (!planeFound) {
                    planeFound = frame.getUpdatedTrackables(Plane::class.java)
                        .any { it.trackingState == TrackingState.TRACKING }
                }
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { event, node ->
                    if (node != null) {
                        fastSpin = !fastSpin
                    } else {
                        val anchor = frameHolder.frame?.floorHit(event)?.createAnchor()
                        if (anchor != null) anchors.add(anchor)
                    }
                }
            )
        ) {
            anchors.forEach { anchor ->
                AnchorNode(anchor = anchor) {
                    duck?.let {
                        ModelNode(
                            modelInstance = it,
                            autoAnimate = false,
                            scaleToUnits = 0.25f,
                            centerOrigin = Position(y = -1f),
                            rotation = Rotation(y = angle)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                when {
                    !planeFound -> "Liiguta telefoni aeglaselt, et pinda leida"
                    anchors.isEmpty() -> "Puuduta pinda, et part sinna panna"
                    else -> "Pardid: ${anchors.size} "
                },
                color = Color.White
            )
            Text(debugInfo, color = Color.White)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onBack) { Text("Back") }
            Button(
                onClick = {
                    anchors.forEach { it.detach() }
                    anchors.clear()
                }
            ) { Text("Clear") }
        }
    }
}