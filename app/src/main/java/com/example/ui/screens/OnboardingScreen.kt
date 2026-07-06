package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.TracingViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    viewModel: TracingViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val currentName by viewModel.playerName.collectAsState()
    val currentAge by viewModel.playerAge.collectAsState()
    val currentAvatar by viewModel.playerAvatar.collectAsState()
    val currentBgColor by viewModel.playerBgColor.collectAsState()

    var nameInput by remember { mutableStateOf(if (currentName == "Pequeño Trazador") "" else currentName) }
    var selectedAge by remember { mutableStateOf(currentAge) }
    var selectedAvatar by remember { mutableStateOf(currentAvatar) }
    var selectedBgColor by remember { mutableStateOf(currentBgColor) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Map color names to real Color Swatches
    val colorSwatches = remember {
        listOf(
            BgColorOption("Rosa Pastel", Color(0xFFF472B6), Color(0xFFFFF1F2)), // Rose/Pink
            BgColorOption("Celeste Mágico", Color(0xFF3B82F6), Color(0xFFF0F9FF)), // Blue/Sky
            BgColorOption("Verde Dinosaurio", Color(0xFF10B981), Color(0xFFF0FDF4)), // Green
            BgColorOption("Naranja Sol", Color(0xFFF97316), Color(0xFFFFF7ED)), // Orange
            BgColorOption("Púrpura Galaxia", Color(0xFF8B5CF6), Color(0xFFFAF5FF)) // Purple
        )
    }

    // Dynamic background preview based on child's choice!
    val activeBgOption = colorSwatches.firstOrNull { it.name == selectedBgColor } ?: colorSwatches[1]
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            activeBgOption.bgColor,
            Color.White
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .systemBarsPadding()
    ) {
        val isTablet = maxWidth > 600.dp
        val horizontalPadding = if (isTablet) 64.dp else 16.dp

        // Ambient dots grid pattern in the background for extra preschool playful vibe
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Drawn using emoji/canvas to avoid asset dependencies
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(if (isTablet) 12 else 8) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(if (isTablet) 12 else 8) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(Color(0xFF86EFAC).copy(alpha = 0.25f), CircleShape)
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = horizontalPadding, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // --- HEADER DECORATION (Pencil + Palette) ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✏️", fontSize = if (isTablet) 58.sp else 46.sp)
                Text("🎨", fontSize = if (isTablet) 58.sp else 46.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- MAIN APP TITLE ---
            Text(
                text = "Trazos Mágicos",
                fontSize = if (isTablet) 46.sp else 36.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFEA580C), // Vibrant kid orange-coral
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- MAIN PROFILE CREATION CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp)
                    .border(
                        width = 4.dp,
                        color = Color(0xFF38BDF8), // Rounded thick blue borders
                        shape = RoundedCornerShape(32.dp)
                    )
                    .shadow(12.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Card Subtitle
                    Text(
                        text = "¡Hola amiguito! Crea tu personaje para aprender a escribir letras y números con hermosos premios.",
                        fontSize = if (isTablet) 16.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569), // Slate-600
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // --- SECTION 1: ¿CUÁL ES TU NOMBRE? ---
                    Text(
                        text = "¿CUÁL ES TU NOMBRE?",
                        fontSize = if (isTablet) 14.sp else 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF64748B), // Slate-500
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Left
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { if (it.length <= 15) nameInput = it },
                        placeholder = {
                            Text(
                                text = "ESCRIBE TU NOMBRE...",
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = if (isTablet) 20.sp else 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E293B),
                            letterSpacing = 1.sp
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFBBF24), // Yellow-400 thick focused border
                            unfocusedBorderColor = Color(0xFFFBBF24).copy(alpha = 0.7f),
                            focusedContainerColor = Color(0xFFFEF08A).copy(alpha = 0.15f), // Pale yellow
                            unfocusedContainerColor = Color(0xFFFEF08A).copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_name_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- SECTION 1.5: ¿CUÁNTOS AÑOS TIENES? ---
                    Text(
                        text = "¿CUÁNTOS AÑOS TIENES?",
                        fontSize = if (isTablet) 14.sp else 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF64748B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Left
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        (3..8).forEach { age ->
                            val isSelected = selectedAge == age
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) Color(0xFFFBBF24)
                                        else Color(0xFFF1F5F9)
                                    )
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFD97706) else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedAge = age }
                                    .testTag("onboarding_age_$age"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = age.toString(),
                                        color = if (isSelected) Color.White else Color(0xFF1E293B),
                                        fontWeight = FontWeight.Black,
                                        fontSize = if (isTablet) 24.sp else 20.sp
                                    )
                                    Text(
                                        text = "años",
                                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF64748B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isTablet) 12.sp else 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- SECTION 2: ELIGE TU AVATAR DE ANIMALITO ---
                    Text(
                        text = "ELIGE TU AVATAR DE ANIMALITO:",
                        fontSize = if (isTablet) 14.sp else 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF64748B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        textAlign = TextAlign.Left
                    )

                    // 12 beautiful animal emojis
                    val avatars = listOf(
                        "🐯", "🦊", "🐨", "🐸", "🦁", "🐱",
                        "🐼", "🐵", "🦄", "🦖", "🐰", "🐷"
                    )

                    // Grid layout of 2 rows of 6 items each
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (rowIndex in 0 until 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (colIndex in 0 until 6) {
                                    val avatar = avatars[rowIndex * 6 + colIndex]
                                    val isSelected = selectedAvatar == avatar

                                    Box(
                                        modifier = Modifier
                                            .size(if (isTablet) 60.dp else 46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color(0xFF818CF8).copy(alpha = 0.2f)
                                                else Color(0xFFF1F5F9)
                                            )
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) Color(0xFF6366F1) else Color(0xFFCBD5E1),
                                                shape = CircleShape
                                            )
                                            .clickable { selectedAvatar = avatar }
                                            .testTag("avatar_$avatar"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = avatar,
                                            fontSize = if (isTablet) 32.sp else 24.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- SECTION 3: ELIGE TU COLOR DE FONDO MÁGICO ---
                    Text(
                        text = "ELIGE TU COLOR DE FONDO MÁGICO:",
                        fontSize = if (isTablet) 14.sp else 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF64748B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        textAlign = TextAlign.Left
                    )

                    // Custom chips flow
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorSwatches.forEach { option ->
                            val isSelected = selectedBgColor == option.name

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) Color(0xFF0F172A) // Dark slate background when selected
                                        else Color.White
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) option.dotColor else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedBgColor = option.name }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("bg_color_${option.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Colored swatch circle
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(option.dotColor, CircleShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                    )
                                    Text(
                                        text = option.name,
                                        color = if (isSelected) Color.White else Color(0xFF334155),
                                        fontSize = if (isTablet) 14.sp else 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- EMPEZAR AVENTURA BUTTON ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isTablet) 76.dp else 64.dp)
                            // 3D Shadow effect
                            .background(
                                color = Color(0xFF15803D), // Dark Forest Green
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(bottom = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF4ADE80), // Light Green
                                        Color(0xFF22C55E)  // Vibrant Green
                                    )
                                )
                            )
                            .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                            .clickable {
                                // Save new profile in TracingViewModel SharedPreferences
                                val finalName = nameInput.trim().ifBlank { "Pequeño Trazador" }
                                viewModel.createNewProfile(finalName, selectedAge, selectedAvatar, selectedBgColor)
                                onNavigateToDashboard()
                            }
                            .testTag("btn_empezar_aventura"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("▶", color = Color.White, fontSize = if (isTablet) 22.sp else 18.sp)
                            Text(
                                text = "¡Empezar Aventura!",
                                color = Color.White,
                                fontSize = if (isTablet) 20.sp else 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data class to represent the magic background colors
data class BgColorOption(
    val name: String,
    val dotColor: Color,
    val bgColor: Color
)
