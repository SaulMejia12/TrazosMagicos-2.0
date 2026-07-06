package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.TracingViewModel

@Composable
fun WelcomeScreen(
    viewModel: TracingViewModel,
    onStartGame: () -> Unit
) {
    val playerName by viewModel.playerName.collectAsState()
    val playerAvatar by viewModel.playerAvatar.collectAsState()

    // Infinite transition for joyful kid animations
    val infiniteTransition = rememberInfiniteTransition(label = "super_welcome_animations")

    // Gentle wiggling / rotation for the app logo
    val logoRotation by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_wiggle"
    )

    // Vertical bounce for the primary content card
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_bounce"
    )

    // Pulsing animation for the chunky 3D Start Button
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btn_pulse"
    )

    // Star sparkle twinkling scale & rotation
    val sparkleScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_twinkle"
    )

    // Side-to-side floating offset for background balloons
    val floatOffset1 by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_balloon_1"
    )

    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_balloon_2"
    )

    // Super colorful kids-themed sky background gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFBAE6FD), // Light bright Sky Blue (Sky-200)
            Color(0xFFE0F2FE), // Pale Cyan Blue (Sky-100)
            Color(0xFFF0F9FF), // Creamy Blue (Sky-50)
            Color(0xFFFEF08A)  // Joyful Sunny Yellow bottom glow (Yellow-200)
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .systemBarsPadding()
    ) {
        val isTablet = maxWidth > 600.dp

        // Scale properties based on screen size
        val titleFontSize = if (isTablet) 54.sp else 46.sp
        val subtitleFontSize = if (isTablet) 20.sp else 16.sp
        val heroSize = if (isTablet) 320.dp else 250.dp
        val imageSize = if (isTablet) 180.dp else 130.dp
        val buttonHeight = if (isTablet) 90.dp else 80.dp
        val buttonFontSize = if (isTablet) 28.sp else 24.sp

        val contentModifier = if (isTablet) {
            Modifier
                .fillMaxHeight()
                .width(650.dp)
                .align(Alignment.TopCenter)
        } else {
            Modifier
                .fillMaxSize()
        }

        val playButtonModifier = if (isTablet) {
            Modifier.width(420.dp)
        } else {
            Modifier.fillMaxWidth()
        }

        // --- DECORATIVE KIDS ELEMENTS (BALLOONS & SPARKLERS) ---

        // Left Pink Balloon (🎈)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = if (isTablet) 48.dp else 24.dp, top = 100.dp)
                .graphicsLayer { translationX = floatOffset1 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎈", fontSize = if (isTablet) 72.sp else 54.sp)
                Text("✨", fontSize = 20.sp, modifier = Modifier.offset(y = (-10).dp))
            }
        }

        // Right Orange Balloon (🎈)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = if (isTablet) 48.dp else 28.dp, top = 140.dp)
                .graphicsLayer { translationX = floatOffset2 }
        ) {
            Text("🎈", fontSize = if (isTablet) 64.sp else 48.sp)
        }

        // Floating Pencil illustration decoration (Left bottom)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = if (isTablet) 32.dp else 16.dp, top = 180.dp)
                .graphicsLayer { 
                    translationY = floatOffset2
                    rotationZ = -15f
                }
        ) {
            Text("✏️", fontSize = if (isTablet) 58.sp else 44.sp)
        }

        // Floating Rainbow decoration (Right middle)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = if (isTablet) 32.dp else 20.dp, bottom = 40.dp)
                .graphicsLayer { 
                    translationY = floatOffset1 
                    rotationZ = 10f
                }
        ) {
            Text("🌈", fontSize = if (isTablet) 68.sp else 50.sp)
        }

        // Decorative Green Grass Landfill at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(if (isTablet) 140.dp else 110.dp)
                .clip(RoundedCornerShape(topStart = 120.dp, topEnd = 120.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF86EFAC), // Mint Light Green (Green-300)
                            Color(0xFF4ADE80)  // Solid Grass Green (Green-400)
                        )
                    )
                )
        ) {
            // Little white flowers peaking from grass
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 40.dp, end = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🌸", fontSize = if (isTablet) 32.sp else 24.sp)
                Text("🌼", fontSize = if (isTablet) 36.sp else 28.sp)
                Text("🌸", fontSize = if (isTablet) 32.sp else 24.sp)
            }
        }

        // --- MAIN SCROLLABLE/SCALED CONTAINER ---
        Column(
            modifier = contentModifier
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP HEADER (Rainbow Styled Super Title)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                // Little sparkling sub-header
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f))
                        .border(2.dp, Color(0xFF60A5FA), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 16.sp, modifier = Modifier.graphicsLayer { scaleX = sparkleScale; scaleY = sparkleScale })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "¡MUNDO DE COLORES!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E3A8A), // Blue-900
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("✨", fontSize = 16.sp, modifier = Modifier.graphicsLayer { scaleX = sparkleScale; scaleY = sparkleScale })
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Beautiful, eye-catching title styling!
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Trazos Mágicos",
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E3A8A), // Deep navy
                        textAlign = TextAlign.Center,
                        lineHeight = if (isTablet) 62.sp else 52.sp,
                        modifier = Modifier.shadow(0.dp)
                    )
                }

                Text(
                    text = "¡Hola $playerAvatar $playerName! ¿Listo para jugar? 🌟",
                    fontSize = subtitleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0369A1), // Sky-700
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            // 2. CENTRAL HERO AREA (Bouncing App Icon within a Magical Rainbow Portal Card)
            Box(
                modifier = Modifier
                    .graphicsLayer { translationY = bounceY }
                    .size(heroSize)
                    .clip(RoundedCornerShape(if (isTablet) 72.dp else 56.dp))
                    .background(Color.White)
                    .border(
                        width = if (isTablet) 10.dp else 8.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFF43F5E), // Pink-500
                                Color(0xFFF59E0B), // Amber-500
                                Color(0xFF10B981), // Emerald-500
                                Color(0xFF3B82F6)  // Blue-500
                            )
                        ),
                        shape = RoundedCornerShape(if (isTablet) 72.dp else 56.dp)
                    )
                    .shadow(20.dp, RoundedCornerShape(if (isTablet) 72.dp else 56.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Internal kid layout with grid overlay and stars
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFEFF6FF), Color.White)
                            )
                        )
                ) {
                    // Internal sparkles
                    Text("⭐", fontSize = if (isTablet) 28.sp else 22.sp, modifier = Modifier.align(Alignment.TopStart).padding(20.dp).graphicsLayer { scaleX = sparkleScale; scaleY = sparkleScale })
                    Text("✨", fontSize = if (isTablet) 32.sp else 26.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).graphicsLayer { scaleX = sparkleScale; scaleY = sparkleScale })
                    Text("🎨", fontSize = if (isTablet) 30.sp else 24.sp, modifier = Modifier.align(Alignment.BottomStart).padding(20.dp))
                    Text("💫", fontSize = if (isTablet) 30.sp else 24.sp, modifier = Modifier.align(Alignment.TopEnd).padding(20.dp))

                    // Bouncing & wiggling 3D pencil app icon
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.trazos_magicos_app_icon_1783175349515),
                            contentDescription = "Logo Trazos Mágicos",
                            modifier = Modifier
                                .size(imageSize)
                                .graphicsLayer { rotationZ = logoRotation }
                                .clip(RoundedCornerShape(32.dp))
                                .border(4.dp, Color.White, RoundedCornerShape(32.dp))
                                .shadow(8.dp, RoundedCornerShape(32.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Little badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF59E0B)) // Golden yellow
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "¡Letras y Números! 🎒",
                                color = Color.White,
                                fontSize = if (isTablet) 14.sp else 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // 3. BOTTOM CHUNKY 3D BUTTON (Ultra eye-catching toy feel)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // Large 3D-effect button
                Box(
                    modifier = playButtonModifier
                        .scale(pulseScale)
                        .height(buttonHeight)
                        // Dark pink shadow beneath for 3D depth
                        .background(
                            color = Color(0xFFBE185D), // Dark Pink-700
                            shape = RoundedCornerShape(40.dp)
                        )
                        .padding(bottom = 6.dp) // The actual button body slides up to reveal depth
                        .clip(RoundedCornerShape(40.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF472B6), // Light Pink-400
                                    Color(0xFFEC4899)  // Deep Pink-500
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(40.dp)
                        )
                        .clickable { onStartGame() }
                        .testTag("btn_welcome_start"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "¡JUGAR AHORA!",
                            color = Color.White,
                            fontSize = buttonFontSize,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🚀", fontSize = if (isTablet) 32.sp else 28.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Child Safety Banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Seguro para Niños • 100% Sin Anuncios",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1) // Sky-700
                        )
                    }
                }
            }
        }
    }
}
