package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.StickerCard
import com.example.ui.components.StickerModel
import com.example.ui.viewmodel.TracingFeedback
import com.example.ui.viewmodel.TracingViewModel

@Composable
fun TracingScreen(
    viewModel: TracingViewModel,
    characterId: String,
    onNavigateBack: () -> Unit
) {
    // Select active letter/number in viewmodel
    LaunchedEffect(characterId) {
        viewModel.selectCharacter(characterId)
    }

    val character by viewModel.selectedChar.collectAsState()
    val activeStrokeIndex by viewModel.currentStrokeIndex.collectAsState()
    val activePointIndex by viewModel.currentPointIndex.collectAsState()
    val isLevelCompleted by viewModel.isLevelCompleted.collectAsState()
    val unlockedSticker by viewModel.unlockedSticker.collectAsState()
    val showRetryHint by viewModel.showRetryHint.collectAsState()
    val playerBgColor by viewModel.playerBgColor.collectAsState()
    val feedbackType by viewModel.tracingFeedback.collectAsState()
    val accuracyScore by viewModel.accuracyScore.collectAsState()
    val starsEarned by viewModel.starsEarned.collectAsState()

    val magicBackgrounds = remember {
        mapOf(
            "Rosa Pastel" to Color(0xFFFFF1F2),
            "Celeste Mágico" to Color(0xFFF0F9FF),
            "Verde Dinosaurio" to Color(0xFFF0FDF4),
            "Naranja Sol" to Color(0xFFFFF7ED),
            "Púrpura Galaxia" to Color(0xFFFAF5FF)
        )
    }
    val activeBgColor = magicBackgrounds[playerBgColor] ?: Color(0xFFF0F9FF)

    val rainbowBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF1744), // Neon pink
            Color(0xFFFF9100), // Bright orange
            Color(0xFFFFEA00), // Neon yellow
            Color(0xFF00E676), // Bright green
            Color(0xFF00B0FF), // Neon blue
            Color(0xFFD500F9)  // Bright purple
        )
    )

    // Mascot Bounce Animation for children engagement
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val ballPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ball_pulse"
    )

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(activeBgColor),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 550.dp)
                        .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back to Dashboard Home Button (3D styled card/box button!)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEC4899)) // Pink-500
                            .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                            .clickable { onNavigateBack() }
                            .testTag("tracing_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏠", fontSize = 24.sp)
                    }

                    // Header title box with custom light blue border (Vibrant Palette)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(2.dp, Color(0xFFBFDBFE), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                character?.isShape == true -> "Figura ${character?.displayName}"
                                character?.isLetter == true -> "Letra ${character?.displayName}"
                                else -> "Número ${character?.displayName}"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E3A8A), // Deep navy
                            textAlign = TextAlign.Center
                        )
                    }

                    // Reset drawing board (Orange background 3D-like with 🧹 emoji)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF97316)) // Orange-500
                            .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.playBubblePop()
                                viewModel.resetTracingState()
                            }
                            .testTag("btn_reset_drawing"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧹", fontSize = 24.sp)
                    }
                }
            }
        },
        containerColor = activeBgColor
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth > 600.dp

            val columnModifier = if (isTablet) {
                Modifier
                    .fillMaxHeight()
                    .width(550.dp)
                    .align(Alignment.TopCenter)
            } else {
                Modifier
                    .fillMaxSize()
            }

            character?.let { char ->
                Column(
                    modifier = columnModifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                // Top supportive tip with animation
                AnimatedVisibility(
                    visible = showRetryHint,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)), // Light red-pink
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "⚠️ ¡Cuidado! Mantente dentro del caminito de puntitos 🐾",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFDC2626), // Red-600
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !showRetryHint,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = "Sigue los puntitos comenzando en el círculo naranja brillante 🌟",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Interactive Drawing Canvas (Preschool dotted layout wrapper)
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(40.dp))
                        .background(Color.White)
                        .border(8.dp, Color.White, RoundedCornerShape(40.dp))
                        .shadow(12.dp, RoundedCornerShape(40.dp))
                        .pointerInput(char.id) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val normX = offset.x / size.width
                                    val normY = offset.y / size.height
                                    viewModel.onUserTouchMove(normX, normY)
                                },
                                onDragEnd = {
                                    viewModel.onUserDragEnd()
                                },
                                onDragCancel = {
                                    viewModel.onUserDragEnd()
                                },
                                onDrag = { change, _ ->
                                    val normX = change.position.x / size.width
                                    val normY = change.position.y / size.height
                                    viewModel.onUserTouchMove(normX, normY)
                                }
                            )
                        }
                        .testTag("tracing_canvas_board")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Draw Grid Background inside the Canvas (10% opacity blue dots matching radial-gradient)
                        val dotSpacing = 20.dp.toPx()
                        val dotRadius = 1.5.dp.toPx()
                        var gx = dotSpacing / 2
                        while (gx < canvasWidth) {
                            var gy = dotSpacing / 2
                            while (gy < canvasHeight) {
                                drawCircle(
                                    color = Color(0xFF3B82F6).copy(alpha = 0.12f),
                                    radius = dotRadius,
                                    center = Offset(gx, gy)
                                )
                                gy += dotSpacing
                            }
                            gx += dotSpacing
                        }

                        // 1. Draw preschool lined notebook background
                        val lineCount = 10
                        val spacing = canvasHeight / lineCount
                        for (i in 1 until lineCount) {
                            val lineY = i * spacing
                            val isCenterLine = i == lineCount / 2
                            drawLine(
                                color = if (isCenterLine) Color(0xFFFF8A80).copy(alpha = 0.5f) else Color(0xFFBBDEFB).copy(alpha = 0.6f),
                                start = Offset(0f, lineY),
                                end = Offset(canvasWidth, lineY),
                                strokeWidth = if (isCenterLine) 2.dp.toPx() else 1.dp.toPx(),
                                pathEffect = if (!isCenterLine) PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f) else null
                            )
                        }

                        // 2. Draw grey target guides for all strokes of the character
                        char.strokes.forEach { strokePoints ->
                            for (i in 0 until strokePoints.size - 1) {
                                val start = Offset(
                                    strokePoints[i].x * canvasWidth,
                                    strokePoints[i].y * canvasHeight
                                )
                                val end = Offset(
                                    strokePoints[i + 1].x * canvasWidth,
                                    strokePoints[i + 1].y * canvasHeight
                                )
                                drawLine(
                                    color = Color(0xFFCFD8DC),
                                    start = start,
                                    end = end,
                                    strokeWidth = 32.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                // Draw core thin dashed line inside
                                drawLine(
                                    color = Color(0xFF78909C),
                                    start = start,
                                    end = end,
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                                )
                            }
                        }

                        // 2b. Draw direction chevrons along the remaining (untraced) part of the active stroke
                        if (!isLevelCompleted && activeStrokeIndex < char.strokes.size) {
                            val activeStroke = char.strokes[activeStrokeIndex]
                            if (activePointIndex < activeStroke.size) {
                                val stepSize = 16
                                var i = activePointIndex + 4
                                while (i < activeStroke.size - 4) {
                                    val pt1 = activeStroke[i]
                                    val pt2 = activeStroke[(i + 5).coerceAtMost(activeStroke.size - 1)]

                                    val ax = pt1.x * canvasWidth
                                    val ay = pt1.y * canvasHeight
                                    val bx = pt2.x * canvasWidth
                                    val by = pt2.y * canvasHeight

                                    val dx = bx - ax
                                    val dy = by - ay
                                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                                    if (distance > 3f) {
                                        val ux = dx / distance
                                        val uy = dy / distance
                                        val px = -uy
                                        val py = ux

                                        val arrowLength = 10.dp.toPx()
                                        val arrowWidth = 6.dp.toPx()

                                        val lx = ax - ux * arrowLength + px * arrowWidth
                                        val ly = ay - uy * arrowLength + py * arrowWidth

                                        val rx = ax - ux * arrowLength - px * arrowWidth
                                        val ry = ay - uy * arrowLength - py * arrowWidth

                                        val chevronAlpha = (0.50f + 0.30f * ballPulseScale).coerceIn(0f, 1f)

                                        drawLine(
                                            color = Color(0xFF2563EB).copy(alpha = chevronAlpha), // Modern royal blue
                                            start = Offset(lx, ly),
                                            end = Offset(ax, ay),
                                            strokeWidth = 4.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                        drawLine(
                                            color = Color(0xFF2563EB).copy(alpha = chevronAlpha), // Modern royal blue
                                            start = Offset(rx, ry),
                                            end = Offset(ax, ay),
                                            strokeWidth = 4.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                    i += stepSize
                                }
                            }
                        }

                        // 3. Draw the child's glowing rainbow brush trail from the template progress
                        val strokeCount = char.strokes.size
                        for (s in 0 until strokeCount) {
                            val strokePoints = char.strokes[s]
                            val pointsToDraw = when {
                                isLevelCompleted -> strokePoints
                                s < activeStrokeIndex -> strokePoints
                                s == activeStrokeIndex -> {
                                    if (activePointIndex > 0) {
                                        strokePoints.take(activePointIndex + 1)
                                    } else {
                                        emptyList()
                                    }
                                }
                                else -> emptyList()
                            }

                            if (pointsToDraw.size > 1) {
                                for (i in 0 until pointsToDraw.size - 1) {
                                    val start = Offset(
                                        pointsToDraw[i].x * canvasWidth,
                                        pointsToDraw[i].y * canvasHeight
                                    )
                                    val end = Offset(
                                        pointsToDraw[i + 1].x * canvasWidth,
                                        pointsToDraw[i + 1].y * canvasHeight
                                    )
                                    drawLine(
                                        brush = rainbowBrush,
                                        start = start,
                                        end = end,
                                        strokeWidth = 24.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }

                        // 4. Draw the cute "Bolita" (little ball) that the child holds and drags
                        if (!isLevelCompleted && activeStrokeIndex < char.strokes.size) {
                            val activeStroke = char.strokes[activeStrokeIndex]
                            if (activePointIndex < activeStroke.size) {
                                val targetPt = activeStroke[activePointIndex]
                                val ballX = targetPt.x * canvasWidth
                                val ballY = targetPt.y * canvasHeight

                                val baseRadius = 18.dp.toPx()
                                val animatedRadius = baseRadius * ballPulseScale
                                val glowRadius = 38.dp.toPx() * ballPulseScale
                                val ringRadius = 20.dp.toPx() * ballPulseScale

                                // 1. Animated pulse shadow/glow around the ball
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFD54F).copy(alpha = 0.6f),
                                            Color(0xFFFF7043).copy(alpha = 0.2f),
                                            Color.Transparent
                                        ),
                                        center = Offset(ballX, ballY),
                                        radius = glowRadius
                                    ),
                                    radius = glowRadius,
                                    center = Offset(ballX, ballY)
                                )

                                // 2. Outer bright neon ring
                                drawCircle(
                                    color = Color(0xFFF43F5E), // Rose 500
                                    radius = ringRadius,
                                    center = Offset(ballX, ballY)
                                )

                                // 3. Glassy gloss ball body
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF8A80), // Light red-pink highlight
                                            Color(0xFFE11D48)  // Deep rose
                                        ),
                                        center = Offset(ballX - 4.dp.toPx() * ballPulseScale, ballY - 4.dp.toPx() * ballPulseScale),
                                        radius = animatedRadius
                                    ),
                                    radius = animatedRadius,
                                    center = Offset(ballX, ballY)
                                )

                                // 4. Tiny shiny highlight reflection on top-left of the ball
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.85f),
                                    radius = 5.dp.toPx() * ballPulseScale,
                                    center = Offset(ballX - 6.dp.toPx() * ballPulseScale, ballY - 6.dp.toPx() * ballPulseScale)
                                )

                                // 5. Add cute little eyes and a smiley face to make it a super friendly character!
                                val eyeRadius = 2.5.dp.toPx() * ballPulseScale
                                val pupilRadius = 1.2.dp.toPx() * ballPulseScale
                                val eyeOffsetX = 4.dp.toPx() * ballPulseScale
                                val eyeOffsetY = 1.dp.toPx() * ballPulseScale

                                // Left eye
                                drawCircle(
                                    color = Color.White,
                                    radius = eyeRadius,
                                    center = Offset(ballX - eyeOffsetX, ballY + eyeOffsetY)
                                )
                                drawCircle(
                                    color = Color.Black,
                                    radius = pupilRadius,
                                    center = Offset(ballX - eyeOffsetX, ballY + eyeOffsetY)
                                )
                                // Right eye
                                drawCircle(
                                    color = Color.White,
                                    radius = eyeRadius,
                                    center = Offset(ballX + eyeOffsetX, ballY + eyeOffsetY)
                                )
                                drawCircle(
                                    color = Color.Black,
                                    radius = pupilRadius,
                                    center = Offset(ballX + eyeOffsetX, ballY + eyeOffsetY)
                                )
                                // Happy Smile curve!
                                val smileWidth = 6.dp.toPx() * ballPulseScale
                                val smileHeight = 4.dp.toPx() * ballPulseScale
                                drawArc(
                                    color = Color.White,
                                    startAngle = 0f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = Offset(ballX - (smileWidth / 2), ballY + 3.dp.toPx() * ballPulseScale),
                                    size = androidx.compose.ui.geometry.Size(smileWidth, smileHeight),
                                    style = Stroke(width = 2.dp.toPx() * ballPulseScale, cap = StrokeCap.Round)
                                )
                            }
                        }
                    }

                    // Interactive Tip Character mascot (Fox 🦊)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .graphicsLayer {
                                translationY = bounceOffset
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF97316)) // Orange-500
                                .border(4.dp, Color.White, CircleShape)
                                .shadow(4.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🦊", fontSize = 32.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Progress Bottom Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progreso del trazo:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF37474F)
                        )

                        // Visual progress state
                        val totalPoints = char.strokes.flatMap { it }.size
                        var passedPoints = 0
                        for (s in 0 until char.strokes.size) {
                            if (s < activeStrokeIndex) {
                                passedPoints += char.strokes[s].size
                            } else if (s == activeStrokeIndex) {
                                passedPoints += activePointIndex
                            }
                        }
                        val progressFraction = passedPoints.toFloat() / totalPoints.toFloat()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFECEFF1))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFraction)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                                        )
                                    )
                            )
                        }

                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }

    // Result Modal Dialog: Celebrates on success, encourages/guides on mistakes!
    if (isLevelCompleted) {
        Dialog(
            onDismissRequest = { /* Force interaction with buttons */ },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .wrapContentHeight()
                        .testTag("celebration_modal_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when (feedbackType) {
                            TracingFeedback.EXCELLENT -> {
                                Text(
                                    text = "🎉 ¡EXCELENTE TRABAJO! 🎉",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFF5722),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "¡Has seguido el trazo perfecto con gran precisión!",
                                    fontSize = 14.sp,
                                    color = Color(0xFF546E7A),
                                    textAlign = TextAlign.Center
                                )
                            }
                            TracingFeedback.GOOD_EFFORT -> {
                                Text(
                                    text = "👍 ¡BUEN INTENTO! 👍",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0284C7),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "¡Estuviste cerca! Intenta mantenerte en la línea de puntitos para ganar 3 estrellas.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF546E7A),
                                    textAlign = TextAlign.Center
                                )
                            }
                            TracingFeedback.NEEDS_PRACTICE -> {
                                Text(
                                    text = "💪 ¡A PRACTICAR DE NUEVO! 💪",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFE11D48),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Te saliste bastante del caminito. Tómate tu tiempo y sigue los puntitos despacio.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Star rating display based on actual performance
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            for (i in 1..3) {
                                val isEarned = i <= starsEarned
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star $i",
                                    tint = if (isEarned) Color(0xFFFFD54F) else Color(0xFFE2E8F0),
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        // Accuracy Chip
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (feedbackType) {
                                    TracingFeedback.EXCELLENT -> Color(0xFFDCFCE7)
                                    TracingFeedback.GOOD_EFFORT -> Color(0xFFE0F2FE)
                                    TracingFeedback.NEEDS_PRACTICE -> Color(0xFFFFE4E6)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "🎯 Precisión del trazo: $accuracyScore%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (feedbackType) {
                                    TracingFeedback.EXCELLENT -> Color(0xFF166534)
                                    TracingFeedback.GOOD_EFFORT -> Color(0xFF075985)
                                    TracingFeedback.NEEDS_PRACTICE -> Color(0xFF9F1239)
                                },
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        // Sticker Unlock only for genuine excellence
                        if (feedbackType == TracingFeedback.EXCELLENT && unlockedSticker != null) {
                            Text(
                                text = "🏆 ¡HAS GANADO UN STICKER! 🏆",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF8E24AA)
                            )

                            StickerCard(
                                stickerId = unlockedSticker!!,
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(170.dp),
                                pulseAnimation = true
                            )
                        } else if (feedbackType == TracingFeedback.GOOD_EFFORT) {
                            Text(
                                text = "✨ ¡Consigue 3 estrellas para ganar un sticker!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED)
                            )
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.playBubblePop()
                                    viewModel.resetTracingState()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (feedbackType == TracingFeedback.NEEDS_PRACTICE) Color(0xFF10B981) else Color(0xFFFF9800)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("btn_repeat_tracing"),
                                shape = RoundedCornerShape(26.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Repeat", modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (feedbackType == TracingFeedback.NEEDS_PRACTICE) "Reintentar" else "Repetir",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.playBubblePop()
                                    viewModel.resetTracingState()
                                    onNavigateBack()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (feedbackType == TracingFeedback.NEEDS_PRACTICE) Color(0xFF94A3B8) else Color(0xFF4CAF50)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("btn_finish_level"),
                                shape = RoundedCornerShape(26.dp)
                            ) {
                                Text(
                                    text = if (feedbackType == TracingFeedback.NEEDS_PRACTICE) "Salir" else "Aceptar",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
