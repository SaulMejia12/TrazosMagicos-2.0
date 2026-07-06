package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.StickerCard
import com.example.ui.components.StickerModel
import com.example.ui.viewmodel.TracingViewModel
import com.example.utils.TracePaths

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    viewModel: TracingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDiploma: () -> Unit
) {
    val stickerList by viewModel.allStickers.collectAsState()
    val progressList by viewModel.allProgress.collectAsState()
    val playerName by viewModel.playerName.collectAsState()
    val playerAvatar by viewModel.playerAvatar.collectAsState()

    val unlockedStickerIds = stickerList.map { it.stickerId }.toSet()
    val totalStickers = StickerModel.list.size
    val unlockedCount = unlockedStickerIds.size

    val allTracesCompleted = progressList.size >= TracePaths.characters.size
    var showDiplomaLockDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Selected sticker for detail popup
    var selectedStickerDetail by remember { mutableStateOf<StickerModel?>(null) }

    // Fun background gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF1F2), // Light soft pink/rose
            Color(0xFFEFF6FF), // Clean light sky blue
            Color(0xFFF0FDF4)  // Light mint green at the bottom
        )
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular back button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color(0xFFF3E5F5), CircleShape)
                        .shadow(2.dp, CircleShape)
                        .testTag("btn_back_rewards")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color(0xFF8E24AA)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title
                Column {
                    Text(
                        text = "🏆 Mis Súper Premios",
                        fontSize = if (isTablet) 24.sp else 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD81B60)
                    )
                    Text(
                        text = "Álbum de $playerName $playerAvatar",
                        fontSize = if (isTablet) 14.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF546E7A)
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 🏆 Kid-friendly Progress Board Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDF2F8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = "Celebración",
                                tint = Color(0xFFEC4899),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡Vas súper bien, $playerName!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Has conseguido $unlockedCount de $totalStickers stickers",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom playful Progress Bar
                            val progress = if (totalStickers > 0) unlockedCount.toFloat() / totalStickers else 0f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color(0xFFF472B6), Color(0xFF3B82F6))
                                            )
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Go to Diploma Button
                            Button(
                                onClick = {
                                    if (allTracesCompleted) {
                                        onNavigateToDiploma()
                                    } else {
                                        showDiplomaLockDialog = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (allTracesCompleted) Color(0xFF6D28D9) else Color(0xFF94A3B8)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .align(Alignment.End)
                                    .testTag("btn_go_to_diploma_from_rewards")
                            ) {
                                Text(
                                    text = if (allTracesCompleted) "🎓 Ver Mi Diploma Mágico" else "🔒 Diploma Mágico (Bloqueado)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Grid of all rewards/stickers
                Text(
                    text = "👇 Toca un sticker desbloqueado para verlo en grande 👇",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(if (isTablet) 120.dp else 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("rewards_grid")
                ) {
                    items(StickerModel.list) { sticker ->
                        val isUnlocked = unlockedStickerIds.contains(sticker.id)
                        Box(
                            modifier = Modifier
                                .clickable(enabled = isUnlocked) {
                                    selectedStickerDetail = sticker
                                }
                        ) {
                            StickerCard(
                                stickerId = sticker.id,
                                isUnlocked = isUnlocked,
                                modifier = Modifier.testTag("reward_sticker_${sticker.id}")
                            )
                        }
                    }
                }
            }

            // Expanded Detail Dialog for unlocked stickers
            selectedStickerDetail?.let { sticker ->
                Dialog(onDismissRequest = { selectedStickerDetail = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .shadow(12.dp, RoundedCornerShape(32.dp))
                            .border(4.dp, Color.White, RoundedCornerShape(32.dp)),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            sticker.secondaryColor.copy(alpha = 0.2f),
                                            Color.White
                                        )
                                    )
                                )
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Large Sticker Bubble
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(sticker.primaryColor, sticker.secondaryColor)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = sticker.emoji, fontSize = 64.sp)
                            }

                            Text(
                                text = sticker.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = sticker.subtitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = sticker.primaryColor,
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFEF08A))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Estrella",
                                    tint = Color(0xFFCA8A04),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "¡Premio Especial!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF854D0E)
                                )
                            }

                            Button(
                                onClick = { selectedStickerDetail = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = sticker.primaryColor
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_close_sticker_detail")
                            ) {
                                Text(
                                    text = "¡Genial! 🌟",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDiplomaLockDialog) {
        Dialog(onDismissRequest = { showDiplomaLockDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFDF2F8), Color.White)
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFCE7F3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔒", fontSize = 36.sp)
                    }

                    Text(
                        text = "¡Diploma Bajo Llave! 🔒",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF831843),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "¡Para ganar tu gran Diploma Mágico debes completar todos los trazos de letras y números primero! Sigue practicando con entusiasmo. ✨✍️",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4B5563),
                        textAlign = TextAlign.Center
                    )

                    // Progress bar
                    val completedCount = progressList.size
                    val totalCount = TracePaths.characters.size
                    val progressRatio = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Progreso: $completedCount de $totalCount completados",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDB2777)
                        )
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = Color(0xFFDB2777),
                            trackColor = Color(0xFFFCE7F3)
                        )
                    }

                    Button(
                        onClick = { showDiplomaLockDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_close_diploma_lock")
                    ) {
                        Text(
                            text = "¡A practicar! 💪✨",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
