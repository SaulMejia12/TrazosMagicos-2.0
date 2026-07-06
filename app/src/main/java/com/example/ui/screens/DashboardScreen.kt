package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.StickerReward
import com.example.data.local.TraceProgress
import com.example.ui.components.StickerCard
import com.example.ui.components.StickerModel
import com.example.ui.viewmodel.TracingViewModel
import com.example.utils.TraceCharacter
import com.example.utils.TracePaths

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TracingViewModel,
    onNavigateToTracing: (String) -> Unit,
    onNavigateToParents: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToRewards: () -> Unit,
    onNavigateToDiploma: () -> Unit
) {
    val progressList by viewModel.allProgress.collectAsState()
    val stickerList by viewModel.allStickers.collectAsState()
    val currentFilter by viewModel.categoryFilter.collectAsState()

    val playerName by viewModel.playerName.collectAsState()
    val playerAge by viewModel.playerAge.collectAsState()
    val playerAvatar by viewModel.playerAvatar.collectAsState()
    val playerBgColor by viewModel.playerBgColor.collectAsState()

    val magicBackgrounds = remember {
        mapOf(
            "Rosa Pastel" to listOf(Color(0xFFFFF1F2), Color(0xFFFDE1E8), Color(0xFFFCE7F3)),
            "Celeste Mágico" to listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE), Color(0xFFDBEAFE)),
            "Verde Dinosaurio" to listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7), Color(0xFFD1FAE5)),
            "Naranja Sol" to listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5), Color(0xFFFED7AA)),
            "Púrpura Galaxia" to listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF), Color(0xFFE9D5FF))
        )
    }
    val bgColors = magicBackgrounds[playerBgColor] ?: magicBackgrounds["Celeste Mágico"]!!

    var showParentGate by remember { mutableStateOf(false) }
    var parentGateInput by remember { mutableStateOf("") }
    var parentGateError by remember { mutableStateOf(false) }

    var showDiplomaLockDialog by remember { mutableStateOf(false) }
    val allTracesCompleted = progressList.size >= TracePaths.characters.size

    val characters = if (currentFilter == "NUMBER") {
        TracePaths.getNumbers()
    } else {
        TracePaths.getAlphabet()
    }

    // Computing dynamic stats for Header
    val totalStarsEarned = progressList.sumOf { it.starsEarned }
    val completedCount = progressList.filter { it.completedCount > 0 }.size
    val currentLevel = 1 + (completedCount / 2)
    val levelProgressModulo = completedCount % 3

    // Premium dynamic kid gradient backing (Vibrant Palette)
    val backgroundBrush = Brush.verticalGradient(colors = bgColors)

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColors.first()),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 720.dp)
                        .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Stars Earned Badge & Profile Switcher (Left)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.9f))
                                .border(2.dp, Color(0xFFBFDBFE), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFBBF24)), // Yellow-400
                                contentAlignment = Alignment.Center
                            ) {
                                Text("★", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalStarsEarned",
                                color = Color(0xFF1E3A8A), // Blue-900
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        // Child Avatar Switcher Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.9f))
                                .border(2.dp, Color(0xFFF9A825), RoundedCornerShape(20.dp))
                                .clickable { onNavigateToProfiles() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("dashboard_avatar_switch_button")
                        ) {
                            Text(text = playerAvatar, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cambiar",
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // 2. Dynamic Level Indicator (Center)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NIVEL $currentLevel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF60A5FA), // Blue-400
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Level progress dots
                            repeat(3) { dotIdx ->
                                val dotColor = if (dotIdx < levelProgressModulo || (levelProgressModulo == 0 && completedCount > 0)) {
                                    Color(0xFF22C55E) // Green-500
                                } else {
                                    Color(0xFFBFDBFE) // Blue-200
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                            }
                        }
                    }

                    // 3. Parents zone locked button styled as ⚙️ / lock button (Right)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.generateParentalPuzzle()
                                parentGateInput = ""
                                parentGateError = false
                                showParentGate = true
                            }
                            .testTag("parents_zone_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚙️", fontSize = 22.sp)
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth > 600.dp

            val columnModifier = if (isTablet) {
                Modifier
                    .fillMaxHeight()
                    .width(720.dp)
                    .align(Alignment.TopCenter)
            } else {
                Modifier
                    .fillMaxSize()
            }

            Column(
                modifier = columnModifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Safe Kid Mode Banner (Privacy, Ad-Free, Offline Verification)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Safe Kids Mode",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🛡️ Modo Seguro: 100% Offline • Sin Anuncios • Privado",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Cute player profile card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF93C5FD), Color(0xFFC084FC))
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEFF6FF))
                                .border(2.dp, Color(0xFF3B82F6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(playerAvatar, fontSize = 34.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "¡Hola, $playerName!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = "¡Tienes $playerAge años y estás escribiendo genial! 🎨🚀",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0288D1)
                            )
                        }
                    }
                }

                // Friendly Title Intro
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "¡Aprende Jugando! ⭐️",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0288D1),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Toca una letra o número para comenzar a dibujar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF546E7A),
                        textAlign = TextAlign.Center
                    )
                }

                // Playful Rounded Selector Tabs with 3D styled highlights (Numbers vs Alphabet)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Numbers selector (Orange)
                    val isNumbersSelected = currentFilter == "NUMBER"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isNumbersSelected) Color(0xFFF97316) else Color.White)
                            .border(
                                width = 2.dp,
                                color = if (isNumbersSelected) Color(0xFFEA580C) else Color(0xFFBFDBFE),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { viewModel.setCategoryFilter("NUMBER") }
                            .testTag("tab_numbers"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔢 ", fontSize = 20.sp)
                            Text(
                                text = "Números",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isNumbersSelected) Color.White else Color(0xFF1E3A8A)
                            )
                        }
                    }

                    // Letters selector (Cyan)
                    val isLettersSelected = currentFilter == "LETTER"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isLettersSelected) Color(0xFF06B6D4) else Color.White)
                            .border(
                                width = 2.dp,
                                color = if (isLettersSelected) Color(0xFF0891B2) else Color(0xFFBFDBFE),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { viewModel.setCategoryFilter("LETTER") }
                            .testTag("tab_letters"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔤 ", fontSize = 20.sp)
                            Text(
                                text = "Letras",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isLettersSelected) Color.White else Color(0xFF1E3A8A)
                            )
                        }
                    }
                }

                // Kids Learning Cards Grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = if (isTablet) 580.dp else 420.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(if (isTablet) 110.dp else 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(characters) { char ->
                            val characterProgress = progressList.firstOrNull { it.charId == char.id }
                            val starsCount = characterProgress?.starsEarned ?: 0
                            
                            val cardBg = if (char.isLetter) Color(0xFFE0F7FA) else Color(0xFFFFF3E0)
                            val accentColor = if (char.isLetter) Color(0xFF0097A7) else Color(0xFFEF6C00)

                            Card(
                                onClick = { onNavigateToTracing(char.id) },
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .testTag("char_card_${char.id}"),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Main Trace Character Glyph
                                        Text(
                                            text = char.displayName,
                                            fontSize = if (isTablet) 42.sp else 32.sp,
                                            fontWeight = FontWeight.Black,
                                            color = accentColor,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )

                                        // Display Earned Stars Below Character (Max 3)
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            repeat(3) { index ->
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Star",
                                                    tint = if (index < starsCount) Color(0xFFFFC107) else Color(0xFFB0BEC5),
                                                    modifier = Modifier.size(if (isTablet) 18.dp else 14.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Small green checkmark in the top-right corner if completed
                                    if (starsCount > 0 || (characterProgress?.completedCount ?: 0) > 0) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(18.dp)
                                                .background(Color(0xFF22C55E), CircleShape) // Green-500
                                                .border(1.5.dp, Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Completado",
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Trophy Sticker Rewards Section
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏆 Mis Premios Ganados",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD81B60),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "¡Traza letras y números para desbloquear stickers increíbles!",
                            fontSize = 13.sp,
                            color = Color(0xFF78909C),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val unlockedStickerIds = stickerList.map { it.stickerId }.toSet()

                        if (unlockedStickerIds.isEmpty()) {
                            // Cute friendly empty state
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🦁 🚀 🦖", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "¡Tu álbum de stickers está vacío!\nCompleta tu primer nivel para ganar un sticker.",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF90A4AE),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // Display Grid of Stickers (showing both unlocked and grayed out locked ones so they are encouraged!)
                            val displayStickers = StickerModel.list
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(if (isTablet) 110.dp else 90.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = if (isTablet) 360.dp else 240.dp)
                            ) {
                                items(displayStickers) { sticker ->
                                    val isUnlocked = unlockedStickerIds.contains(sticker.id)
                                    StickerCard(
                                        stickerId = sticker.id,
                                        isUnlocked = isUnlocked
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onNavigateToRewards,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD81B60)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_go_to_rewards"),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "🏆 Ver Todo Mi Álbum de Stickers ✨",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

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
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_go_to_diploma"),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = if (allTracesCompleted) "🎓 Ver y Compartir Mi Diploma de Logros ✨" else "🔒 Diploma de Logros (Bloqueado)",
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

    // Parental Gate Verification Dialog ("Adults Only")
    if (showParentGate) {
        Dialog(onDismissRequest = { showParentGate = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Parent Gate",
                        tint = Color(0xFF8E24AA),
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Área Exclusiva de Padres",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E24AA),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Por favor, resuelve esta operación matemática sencilla para demostrar que eres un adulto:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    // Parental verification sum puzzle
                    Text(
                        text = "${viewModel.num1} + ${viewModel.num2} = ?",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8E24AA),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = parentGateInput,
                        onValueChange = { parentGateInput = it },
                        label = { Text("Escribe tu respuesta") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = parentGateError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_gate_input")
                    )

                    if (parentGateError) {
                        Text(
                            text = "❌ Respuesta incorrecta. ¡Inténtalo de nuevo!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showParentGate = false }) {
                            Text("Cancelar", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val inputVal = parentGateInput.trim().toIntOrNull()
                                if (inputVal != null && viewModel.attemptParentalUnlock(inputVal)) {
                                    showParentGate = false
                                    onNavigateToParents()
                                } else {
                                    parentGateError = true
                                    parentGateInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                            modifier = Modifier.testTag("parent_gate_verify")
                        ) {
                            Text("Entrar", fontWeight = FontWeight.Bold)
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
                                colors = listOf(Color(0xFFF5F3FF), Color.White)
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
                            .background(Color(0xFFEDE9FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔒", fontSize = 36.sp)
                    }

                    Text(
                        text = "¡Diploma Bajo Llave! 🔒",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF5B21B6),
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
                            color = Color(0xFF7C3AED)
                        )
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = Color(0xFF7C3AED),
                            trackColor = Color(0xFFEDE9FE)
                        )
                    }

                    Button(
                        onClick = { showDiplomaLockDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_close_diploma_lock_dashboard")
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
