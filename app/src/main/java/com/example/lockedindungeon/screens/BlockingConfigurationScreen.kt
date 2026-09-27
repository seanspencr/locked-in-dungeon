package com.example.lockedindungeon.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lockedindungeon.data.local.entities.BlockingType
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.viewmodels.BlockingConfigurationScreenState
import com.example.lockedindungeon.viewmodels.BlockingConfigurationViewmodel
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun BlockingConfigurationScreen(
    packageName: String,
    onNavigateBack: () -> Unit,
    viewModel: BlockingConfigurationViewmodel = hiltViewModel()
){
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val iconDrawable = remember(packageName) {
        try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    LaunchedEffect(packageName) {
        viewModel.setApp(packageName)
    }


    Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.Center,
            Alignment.CenterVertically
        ){
            Text(text = "Configure Blocking for ${state.displayName}",
                modifier = Modifier.padding(vertical = 10.dp),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }

        if (state.targetType == TargetType.APP) {
            iconDrawable?.let {
                val iconBitmap = it.toBitmap(80, 80)
                Image(
                    bitmap = iconBitmap.asImageBitmap(),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                )
            }
        }
        Text(text = state.displayName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text("Blocking Mode")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            BlockingType.entries.forEach { type ->
                FilterChip(
                    selected = state.blockingType == type,
                    onClick = { viewModel.onBlockingTypeChange(type) },
                    label = { Text(type.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state.blockingType) {
            BlockingType.TIMER -> {
                TimerInput(
                    state = state,
                    onHourChange = { viewModel.onHourChange(it) },
                    onMinuteChange = { viewModel.onMinuteChange(it) },
                    onSubmit = { viewModel.submit(onNavigateBack) }
                )
            }
            BlockingType.BLACKLIST -> {
                BlacklistInput(
                    state = state,
                    onStartTimeChange = { h, m ->
                        viewModel.onHourChange(h)
                        viewModel.onMinuteChange(m)
                    },
                    onEndTimeChange = { h, m ->
                        viewModel.onEndHourChange(h)
                        viewModel.onEndMinuteChange(m)
                    },
                    onSubmit = { viewModel.submit(onNavigateBack) }
                )
            }
            BlockingType.WHITELIST -> {
                WhitelistInput(
                    state = state,
                    onStartTimeChange = { h, m ->
                        viewModel.onHourChange(h)
                        viewModel.onMinuteChange(m)
                    },
                    onEndTimeChange = { h, m ->
                        viewModel.onEndHourChange(h)
                        viewModel.onEndMinuteChange(m)
                    },
                    onSubmit = { viewModel.submit(onNavigateBack) }
                )
            }
        }
    }
}

@Composable
fun TimerInput(
    state: BlockingConfigurationScreenState,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Duration")

        Text("${state.hour} hours")
        Slider(
            value = state.hour.toFloat(),
            onValueChange = { onHourChange(it.toInt()) },
            valueRange = 0f..23f,
            steps = 23
        )

        Text("${state.minute} minutes")
        Slider(
            value = state.minute.toFloat(),
            onValueChange = { onMinuteChange(it.toInt()) },
            valueRange = 0f..59f,
            steps = 59
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        ) {
            if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text("Submit")
        }
    }
}

@Composable
fun BlacklistInput(
    state: BlockingConfigurationScreenState,
    onStartTimeChange: (Int, Int) -> Unit,
    onEndTimeChange: (Int, Int) -> Unit,
    onSubmit: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Blacklist Period", style = MaterialTheme.typography.titleMedium, color = Color.Cyan)
        Spacer(modifier = Modifier.height(16.dp))
        CircularTimeRangePicker(
            startHour = state.hour,
            startMinute = state.minute,
            endHour = state.endHour,
            endMinute = state.endMinute,
            onStartTimeChange = onStartTimeChange,
            onEndTimeChange = onEndTimeChange
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        ) {
            if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text("Submit")
        }
    }
}

@Composable
fun WhitelistInput(
    state: BlockingConfigurationScreenState,
    onStartTimeChange: (Int, Int) -> Unit,
    onEndTimeChange: (Int, Int) -> Unit,
    onSubmit: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Whitelist Period", style = MaterialTheme.typography.titleMedium, color = Color.Cyan)
        Spacer(modifier = Modifier.height(16.dp))
        CircularTimeRangePicker(
            startHour = state.hour,
            startMinute = state.minute,
            endHour = state.endHour,
            endMinute = state.endMinute,
            onStartTimeChange = onStartTimeChange,
            onEndTimeChange = onEndTimeChange
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        ) {
            if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text("Submit")
        }
    }
}

@Composable
fun CircularTimeRangePicker(
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    onStartTimeChange: (Int, Int) -> Unit,
    onEndTimeChange: (Int, Int) -> Unit
) {
    var draggingStart by remember { mutableStateOf(false) }
    var draggingEnd by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = min(size.width, size.height).toFloat() / 2f - 40.dp.toPx()
                        val startAngle = (startHour + startMinute / 60f) * 15f - 90f
                        val endAngle = (endHour + endMinute / 60f) * 15f - 90f

                        val startPos = Offset(
                            center.x + radius * cos(startAngle * PI.toFloat() / 180f),
                            center.y + radius * sin(startAngle * PI.toFloat() / 180f)
                        )
                        val endPos = Offset(
                            center.x + radius * cos(endAngle * PI.toFloat() / 180f),
                            center.y + radius * sin(endAngle * PI.toFloat() / 180f)
                        )

                        val distStart = (offset - startPos).getDistance()
                        val distEnd = (offset - endPos).getDistance()

                        if (distStart < 40.dp.toPx() || distEnd < 40.dp.toPx()) {
                            if (distStart < distEnd) draggingStart = true else draggingEnd = true
                        }
                    },
                    onDragEnd = {
                        draggingStart = false
                        draggingEnd = false
                    },
                    onDragCancel = {
                        draggingStart = false
                        draggingEnd = false
                    }
                ) { change, _ ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val angle = (atan2(change.position.y - center.y, change.position.x - center.x) * (180f / PI.toFloat()) + 360f + 90f) % 360f
                    val totalMinutes = (angle / 15f * 60f).toInt()
                    val h = (totalMinutes / 60) % 24
                    val m = totalMinutes % 60

                    if (draggingStart) onStartTimeChange(h, m)
                    if (draggingEnd) onEndTimeChange(h, m)
                }
            }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 40.dp.toPx()

            // Draw background track
            drawCircle(
                color = Color.DarkGray.copy(alpha = 0.5f),
                radius = radius,
                style = Stroke(width = 30.dp.toPx())
            )

            val startAngle = (startHour + startMinute / 60f) * 15f - 90f
            val endAngle = (endHour + endMinute / 60f) * 15f - 90f
            var sweep = endAngle - startAngle
            if (sweep < 0) sweep += 360f

            // Draw active arc
            drawArc(
                color = Color.Cyan,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw ticks and numbers
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 14.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
            }

            for (i in 0 until 24) {
                val angle = i * 15f - 90f
                val rad = angle * PI.toFloat() / 180f
                val start = Offset(center.x + (radius - 15.dp.toPx()) * cos(rad), center.y + (radius - 15.dp.toPx()) * sin(rad))
                val end = Offset(center.x + (radius + 15.dp.toPx()) * cos(rad), center.y + (radius + 15.dp.toPx()) * sin(rad))
                drawLine(Color.White.copy(alpha = 0.3f), start, end, strokeWidth = 2.dp.toPx())

                if (i % 6 == 0) {
                    val text = if (i == 0) "24" else i.toString()
                    val textRadius = radius - 35.dp.toPx()
                    drawContext.canvas.nativeCanvas.drawText(
                        text,
                        center.x + textRadius * cos(rad),
                        center.y + textRadius * sin(rad) + 5.dp.toPx(),
                        paint
                    )
                }
            }

            // Draw start handle
            drawCircle(
                color = Color.Black,
                radius = 15.dp.toPx(),
                center = Offset(
                    center.x + radius * cos(startAngle * PI.toFloat() / 180f),
                    center.y + radius * sin(startAngle * PI.toFloat() / 180f)
                )
            )
            drawCircle(
                color = Color.Cyan,
                radius = 15.dp.toPx(),
                center = Offset(
                    center.x + radius * cos(startAngle * PI.toFloat() / 180f),
                    center.y + radius * sin(startAngle * PI.toFloat() / 180f)
                ),
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw end handle
            drawCircle(
                color = Color.Black,
                radius = 15.dp.toPx(),
                center = Offset(
                    center.x + radius * cos(endAngle * PI.toFloat() / 180f),
                    center.y + radius * sin(endAngle * PI.toFloat() / 180f)
                )
            )
            drawCircle(
                color = Color.Cyan,
                radius = 15.dp.toPx(),
                center = Offset(
                    center.x + radius * cos(endAngle * PI.toFloat() / 180f),
                    center.y + radius * sin(endAngle * PI.toFloat() / 180f)
                ),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val startTotal = startHour * 60 + startMinute
            val endTotal = endHour * 60 + endMinute
            var diff = endTotal - startTotal
            if (diff < 0) diff += 24 * 60
            val h = diff / 60
            val m = diff % 60
            Text(
                text = String.format(Locale.getDefault(), "%02dHr %02dMin", h, m),
                style = MaterialTheme.typography.titleLarge,
                color = Color.Cyan
            )
        }
    }
}
