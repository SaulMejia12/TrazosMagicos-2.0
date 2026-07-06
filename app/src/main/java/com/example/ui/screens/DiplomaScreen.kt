package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.ui.components.StickerModel
import com.example.ui.viewmodel.TracingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// Theme data structures for preview and bitmap generation
data class DiplomaTheme(
    val id: String,
    val name: String,
    val emoji: String,
    // Compose colors
    val composeStart: Color,
    val composeEnd: Color,
    val composeBorder: Color,
    val composeText: Color,
    val composeAccent: Color,
    // Native graphic color values (ARGB integers)
    val nativeStart: Int,
    val nativeEnd: Int,
    val nativeBorder: Int,
    val nativeText: Int,
    val nativeAccent: Int
)

val DIPLOMA_THEMES = listOf(
    DiplomaTheme(
        id = "rainbow",
        name = "Arcoíris",
        emoji = "🌈",
        composeStart = Color(0xFFFFFDF0),
        composeEnd = Color(0xFFFFF0F5),
        composeBorder = Color(0xFFFBBF24),
        composeText = Color(0xFFBE185D),
        composeAccent = Color(0xFFD97706),
        nativeStart = 0xFFFFFDF0.toInt(),
        nativeEnd = 0xFFFFF0F5.toInt(),
        nativeBorder = 0xFFFBBF24.toInt(),
        nativeText = 0xFFBE185D.toInt(),
        nativeAccent = 0xFFD97706.toInt()
    ),
    DiplomaTheme(
        id = "space",
        name = "Espacio",
        emoji = "🚀",
        composeStart = Color(0xFF0F172A),
        composeEnd = Color(0xFF1E3A8A),
        composeBorder = Color(0xFF38BDF8),
        composeText = Color(0xFF38BDF8),
        composeAccent = Color(0xFF22D3EE),
        nativeStart = 0xFF0F172A.toInt(),
        nativeEnd = 0xFF1E3A8A.toInt(),
        nativeBorder = 0xFF38BDF8.toInt(),
        nativeText = 0xFF38BDF8.toInt(),
        nativeAccent = 0xFF22D3EE.toInt()
    ),
    DiplomaTheme(
        id = "forest",
        name = "Bosque",
        emoji = "🦊",
        composeStart = Color(0xFFF0FDF4),
        composeEnd = Color(0xFFD1FAE5),
        composeBorder = Color(0xFF15803D),
        composeText = Color(0xFF065F46),
        composeAccent = Color(0xFFEA580C),
        nativeStart = 0xFFF0FDF4.toInt(),
        nativeEnd = 0xFFD1FAE5.toInt(),
        nativeBorder = 0xFF15803D.toInt(),
        nativeText = 0xFF065F46.toInt(),
        nativeAccent = 0xFFEA580C.toInt()
    ),
    DiplomaTheme(
        id = "unicorn",
        name = "Fantasía",
        emoji = "🦄",
        composeStart = Color(0xFFFDF2F8),
        composeEnd = Color(0xFFFAE8FF),
        composeBorder = Color(0xFFEC4899),
        composeText = Color(0xFF701A75),
        composeAccent = Color(0xFFDB2777),
        nativeStart = 0xFFFDF2F8.toInt(),
        nativeEnd = 0xFFFAE8FF.toInt(),
        nativeBorder = 0xFFEC4899.toInt(),
        nativeText = 0xFF701A75.toInt(),
        nativeAccent = 0xFFDB2777.toInt()
    )
)

val DIPLOMA_BADGES = listOf("🏆", "🌟", "👑", "🎖️", "🦕", "🚀")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiplomaScreen(
    viewModel: TracingViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Player stats from view model
    val playerName by viewModel.playerName.collectAsState()
    val playerAge by viewModel.playerAge.collectAsState()
    val playerAvatar by viewModel.playerAvatar.collectAsState()
    val progressList by viewModel.allProgress.collectAsState()
    val stickerList by viewModel.allStickers.collectAsState()

    // Calculations
    val totalStars = progressList.sumOf { it.starsEarned }
    val unlockedStickersCount = stickerList.map { it.stickerId }.distinct().size

    // User Interactive states
    var selectedTheme by remember { mutableStateOf(DIPLOMA_THEMES.first()) }
    var selectedBadge by remember { mutableStateOf(DIPLOMA_BADGES.first()) }
    var parentSignature by remember { mutableStateOf("") }

    // Loading & Dialog States
    var isGeneratingImage by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Rainbow playful background brush
    val screenBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF5F3FF), // Light Lavender
            Color(0xFFEFF6FF)  // Soft Sky Blue
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
                // Back Button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color(0xFFEDE9FE), CircleShape)
                        .shadow(2.dp, CircleShape)
                        .testTag("btn_back_diploma")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color(0xFF6D28D9)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "🎓 Mi Diploma Mágico",
                        fontSize = if (isTablet) 24.sp else 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF5B21B6)
                    )
                    Text(
                        text = "¡Certifica tus súper logros escribiendo!",
                        fontSize = if (isTablet) 14.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackgroundBrush)
                .padding(innerPadding)
        ) {
            if (isTablet) {
                // Horizontal Landscape layout for tablets
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Left Panel: Customizer Controls (Scrollable)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DiplomaCustomizerPanel(
                            selectedTheme = selectedTheme,
                            onThemeSelected = { selectedTheme = it },
                            selectedBadge = selectedBadge,
                            onBadgeSelected = { selectedBadge = it },
                            parentSignature = parentSignature,
                            onSignatureChanged = { parentSignature = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ActionButtonsBlock(
                            isGeneratingImage = isGeneratingImage,
                            onShare = {
                                scope.launch {
                                    isGeneratingImage = true
                                    val bitmap = withContext(Dispatchers.IO) {
                                        generateDiplomaBitmap(
                                            context = context,
                                            playerName = playerName,
                                            playerAge = playerAge,
                                            playerAvatar = playerAvatar,
                                            theme = selectedTheme,
                                            badge = selectedBadge,
                                            totalStars = totalStars,
                                            stickersCount = unlockedStickersCount,
                                            parentSignature = parentSignature
                                        )
                                    }
                                    isGeneratingImage = false
                                    shareDiploma(context, bitmap)
                                }
                            },
                            onSave = {
                                scope.launch {
                                    isGeneratingImage = true
                                    val bitmap = withContext(Dispatchers.IO) {
                                        generateDiplomaBitmap(
                                            context = context,
                                            playerName = playerName,
                                            playerAge = playerAge,
                                            playerAvatar = playerAvatar,
                                            theme = selectedTheme,
                                            badge = selectedBadge,
                                            totalStars = totalStars,
                                            stickersCount = unlockedStickersCount,
                                            parentSignature = parentSignature
                                        )
                                    }
                                    val saved = withContext(Dispatchers.IO) {
                                        saveDiplomaToGallery(context, bitmap)
                                    }
                                    isGeneratingImage = false
                                    if (saved) {
                                        showSuccessDialog = true
                                    } else {
                                        Toast.makeText(context, "No se pudo guardar el diploma", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }

                    // Right Panel: Live Visual Diploma Preview (takes more space)
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "✨ VISTA PREVIA EN VIVO ✨",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF7C3AED),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            DiplomaPreview(
                                playerName = playerName,
                                playerAge = playerAge,
                                playerAvatar = playerAvatar,
                                theme = selectedTheme,
                                badge = selectedBadge,
                                totalStars = totalStars,
                                stickersCount = unlockedStickersCount,
                                parentSignature = parentSignature,
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                        }
                    }
                }
            } else {
                // Vertical Stack layout for phones
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Live Preview Card
                    Text(
                        text = "✨ VISTA PREVIA EN VIVO ✨",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    DiplomaPreview(
                        playerName = playerName,
                        playerAge = playerAge,
                        playerAvatar = playerAvatar,
                        theme = selectedTheme,
                        badge = selectedBadge,
                        totalStars = totalStars,
                        stickersCount = unlockedStickersCount,
                        parentSignature = parentSignature,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. Customizer Controls
                    DiplomaCustomizerPanel(
                        selectedTheme = selectedTheme,
                        onThemeSelected = { selectedTheme = it },
                        selectedBadge = selectedBadge,
                        onBadgeSelected = { selectedBadge = it },
                        parentSignature = parentSignature,
                        onSignatureChanged = { parentSignature = it }
                    )

                    // 3. Actions Block
                    ActionButtonsBlock(
                        isGeneratingImage = isGeneratingImage,
                        onShare = {
                            scope.launch {
                                isGeneratingImage = true
                                val bitmap = withContext(Dispatchers.IO) {
                                    generateDiplomaBitmap(
                                        context = context,
                                        playerName = playerName,
                                        playerAge = playerAge,
                                        playerAvatar = playerAvatar,
                                        theme = selectedTheme,
                                        badge = selectedBadge,
                                        totalStars = totalStars,
                                        stickersCount = unlockedStickersCount,
                                        parentSignature = parentSignature
                                    )
                                }
                                isGeneratingImage = false
                                shareDiploma(context, bitmap)
                            }
                        },
                        onSave = {
                            scope.launch {
                                isGeneratingImage = true
                                val bitmap = withContext(Dispatchers.IO) {
                                    generateDiplomaBitmap(
                                        context = context,
                                        playerName = playerName,
                                        playerAge = playerAge,
                                        playerAvatar = playerAvatar,
                                        theme = selectedTheme,
                                        badge = selectedBadge,
                                        totalStars = totalStars,
                                        stickersCount = unlockedStickersCount,
                                        parentSignature = parentSignature
                                    )
                                }
                                val saved = withContext(Dispatchers.IO) {
                                    saveDiplomaToGallery(context, bitmap)
                                }
                                isGeneratingImage = false
                                if (saved) {
                                    showSuccessDialog = true
                                } else {
                                    Toast.makeText(context, "No se pudo guardar el diploma", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Celebration Success Dialog
            if (showSuccessDialog) {
                Dialog(onDismissRequest = { showSuccessDialog = false }) {
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
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = "Celebración",
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(42.dp)
                                )
                            }

                            Text(
                                text = "¡Diploma Guardado! 🎉",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E1B4B),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Tu hermoso diploma se guardó con éxito en la galería de fotos. ¡Ya puedes compartirlo o imprimirlo para colgarlo en tu habitación! 🌟🏆",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4B5563),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { showSuccessDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_close_success_diploma")
                            ) {
                                Text(
                                    text = "¡Súper! 💖",
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
    }
}

@Composable
fun DiplomaCustomizerPanel(
    selectedTheme: DiplomaTheme,
    onThemeSelected: (DiplomaTheme) -> Unit,
    selectedBadge: String,
    onBadgeSelected: (String) -> Unit,
    parentSignature: String,
    onSignatureChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🎨 Personaliza tu Diploma",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1F2937)
            )

            // Theme selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "1. Elige tu diseño favorito:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B5563)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DIPLOMA_THEMES.forEach { theme ->
                        val isSelected = theme.id == selectedTheme.id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) theme.composeBorder.copy(alpha = 0.2f) else Color(0xFFF3F4F6))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) theme.composeBorder else Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onThemeSelected(theme) }
                                .testTag("theme_selector_${theme.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(theme.emoji, fontSize = 20.sp)
                                Text(
                                    text = theme.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) theme.composeText else Color(0xFF4B5563)
                                )
                            }
                        }
                    }
                }
            }

            // Badge/Emblem Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "2. Elige tu emblema de campeón:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B5563)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DIPLOMA_BADGES.forEach { badge ->
                        val isSelected = badge == selectedBadge
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFFFEF3C7) else Color(0xFFF3F4F6))
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) Color(0xFFD97706) else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onBadgeSelected(badge) }
                                .testTag("badge_selector_$badge"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(badge, fontSize = 24.sp)
                        }
                    }
                }
            }

            // Signature of pride
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "3. Firma de orgullo (Mamá, Papá o Maestro):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B5563)
                )
                OutlinedTextField(
                    value = parentSignature,
                    onValueChange = {
                        if (it.length <= 25) {
                            onSignatureChanged(it)
                        }
                    },
                    placeholder = { Text("Escribe tu nombre (Ej. Mamá María)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFFBFBFF),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_parent_signature")
                )
            }
        }
    }
}

@Composable
fun ActionButtonsBlock(
    isGeneratingImage: Boolean,
    onShare: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isGeneratingImage) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF7C3AED))
            }
        } else {
            // Share Button (Rainbow/Violet Style)
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_share_diploma")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Compartir")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Compartir Diploma 🚀",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // Save Button
            OutlinedButton(
                onClick = onSave,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7C3AED)),
                border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.linearGradient(colors = listOf(Color(0xFF7C3AED), Color(0xFFC084FC)))),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_diploma")
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = "Guardar")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardar en la Galería 📥",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DiplomaPreview(
    playerName: String,
    playerAge: Int,
    playerAvatar: String,
    theme: DiplomaTheme,
    badge: String,
    totalStars: Int,
    stickersCount: Int,
    parentSignature: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1.5f) // Fixed landscape 3:2 ratio
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .border(3.dp, theme.composeBorder, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(theme.composeStart, theme.composeEnd)))
                .padding(14.dp)
        ) {
            // Inner decorative border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, theme.composeBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Title Block
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DIPLOMA DE SÚPER TRAZADOR",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = theme.composeText,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("✨", fontSize = 14.sp)
                        }
                        Text(
                            text = "Otorgado con mucho orgullo a:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                    }

                    // Main Name & Body text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$playerName $playerAvatar",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.composeAccent,
                            textAlign = TextAlign.Center
                        )
                        val ageText = if (playerAge > 0) "a sus $playerAge años " else ""
                        Text(
                            text = "Quien ${ageText}ha completado sus prácticas de trazos mágicos con excelente constancia, precisión y dedicación en cada nivel. ¡Felicidades, eres genial!",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151),
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    // Badges & Stars summary row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Sign (Zorrito)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(1.dp)
                                    .background(Color.Gray)
                            )
                            Text("🦊 Zorrito Sabio", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color(0xFF4B5563))
                            Text("Guía de Trazos", fontSize = 6.sp, color = Color(0xFF6B7280))
                        }

                        // Center Badge
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.8f))
                                .border(1.5.dp, theme.composeBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(badge, fontSize = 24.sp)
                        }

                        // Right Sign (Parent)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(1.dp)
                                    .background(Color.Gray)
                            )
                            Text(
                                text = parentSignature.ifBlank { "Mamá / Papá ❤️" },
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4B5563),
                                maxLines = 1
                            )
                            Text("Firma de Orgullo", fontSize = 6.sp, color = Color(0xFF6B7280))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Native Bitmap Graphics Renderer for accurate 3:2 sharing image
// -------------------------------------------------------------
fun generateDiplomaBitmap(
    context: Context,
    playerName: String,
    playerAge: Int,
    playerAvatar: String,
    theme: DiplomaTheme,
    badge: String,
    totalStars: Int,
    stickersCount: Int,
    parentSignature: String
): Bitmap {
    val width = 1200
    val height = 800
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 1. Background Gradient
    val bgPaint = Paint().apply { isAntiAlias = true }
    val gradient = LinearGradient(
        0f, 0f, 0f, height.toFloat(),
        theme.nativeStart, theme.nativeEnd,
        Shader.TileMode.CLAMP
    )
    bgPaint.shader = gradient
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // 2. Main Border Frame
    val borderPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 12f
        color = theme.nativeBorder
    }
    canvas.drawRect(30f, 30f, width - 30f, height - 30f, borderPaint)

    // Inner Border
    val innerBorderPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = theme.nativeBorder
    }
    canvas.drawRect(45f, 45f, width - 45f, height - 45f, innerBorderPaint)

    // 3. Decorative Corner Sparkles
    val cornerPaint = Paint().apply {
        isAntiAlias = true
        textSize = 44f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("✨", 85f, 105f, cornerPaint)
    canvas.drawText("✨", width - 85f, 105f, cornerPaint)
    canvas.drawText("✨", 85f, height - 75f, cornerPaint)
    canvas.drawText("✨", width - 85f, height - 75f, cornerPaint)

    // 4. Main Header Title
    val titlePaint = Paint().apply {
        isAntiAlias = true
        color = theme.nativeText
        textSize = 50f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("🎓 DIPLOMA DE SÚPER TRAZADOR 🎓", width / 2f, 160f, titlePaint)

    // Subtitle
    val subtitlePaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF4B5563.toInt()
        textSize = 28f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Este certificado se otorga con mucho orgullo a:", width / 2f, 230f, subtitlePaint)

    // 5. Player Name & Avatar
    val namePaint = Paint().apply {
        isAntiAlias = true
        color = theme.nativeAccent
        textSize = 66f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 2f, 2f, 0xFFE5E7EB.toInt())
    }
    canvas.drawText("$playerName $playerAvatar", width / 2f, 330f, namePaint)

    // 6. Descriptive Text Body
    val bodyPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF1F2937.toInt()
        textSize = 26f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    val ageText = if (playerAge > 0) "a sus $playerAge años " else ""
    canvas.drawText(
        "Quien ${ageText}ha completado sus prácticas de trazos mágicos con",
        width / 2f, 410f, bodyPaint
    )
    canvas.drawText(
        "una excelente precisión, gran constancia y dedicación en cada nivel.",
        width / 2f, 455f, bodyPaint
    )
    canvas.drawText(
        "¡Felicidades, eres todo un campeón/campeona de las letras y números! 🎉🌟",
        width / 2f, 500f, bodyPaint
    )

    // 7. Stats display row
    val statsPaint = Paint().apply {
        isAntiAlias = true
        color = theme.nativeBorder
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(
        "⭐ Estrellas Totales: $totalStars   •   🏅 Stickers Coleccionados: $stickersCount / ${StickerModel.list.size}",
        width / 2f, 570f, statsPaint
    )

    // 8. Custom Badge Emblem (Gold Circle + Icon Emoji)
    val circlePaint = Paint().apply {
        isAntiAlias = true
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(width / 2f, 680f, 50f, circlePaint)

    val circleStroke = Paint().apply {
        isAntiAlias = true
        color = theme.nativeBorder
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    canvas.drawCircle(width / 2f, 680f, 50f, circleStroke)

    val badgePaint = Paint().apply {
        isAntiAlias = true
        textSize = 64f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(badge, width / 2f, 702f, badgePaint)

    // 9. Signatures Line Left & Right
    val linePaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF9CA3AF.toInt()
        strokeWidth = 3f
    }

    // Left (Zorrito Sabio)
    canvas.drawLine(150f, 690f, 420f, 690f, linePaint)
    val sigPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF374151.toInt()
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("🦊 Zorrito Sabio", 285f, 725f, sigPaint)
    val sigLabelPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF6B7280.toInt()
        textSize = 16f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Guía de Trazos", 285f, 750f, sigLabelPaint)

    // Right (Parent)
    canvas.drawLine(width - 420f, 690f, width - 150f, 690f, linePaint)
    canvas.drawText(parentSignature.ifBlank { "Mamá / Papá ❤️" }, width - 285f, 725f, sigPaint)
    canvas.drawText("Firma de Orgullo", width - 285f, 750f, sigLabelPaint)

    return bitmap
}

// Share Diploma file securely via Cache & FileProvider
fun shareDiploma(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs() // Ensure directories exist
        val file = File(cachePath, "diploma_magico.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        if (contentUri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir Diploma Mágico"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al compartir: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

// Save Diploma file directly into Public MediaStore Pictures/TrazosMagicos gallery folder
fun saveDiplomaToGallery(context: Context, bitmap: Bitmap): Boolean {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "diploma_trazos_${System.currentTimeMillis()}.png")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TrazosMagicos")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    if (imageUri == null) return false

    return try {
        resolver.openOutputStream(imageUri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
