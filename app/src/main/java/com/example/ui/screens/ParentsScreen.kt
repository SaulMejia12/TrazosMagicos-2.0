package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.StickerModel
import com.example.ui.viewmodel.TracingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentsScreen(
    viewModel: TracingViewModel,
    onNavigateBack: () -> Unit
) {
    val progressList by viewModel.allProgress.collectAsState()
    val stickerList by viewModel.allStickers.collectAsState()

    var showResetConfirm by remember { mutableStateOf(false) }

    // Lock parents area if we navigate away/back
    DisposableEffect(Unit) {
        onDispose {
            viewModel.lockParentsArea()
        }
    }

    // Computing high-fidelity statistics from local Room progress
    val totalFinished = progressList.sumOf { it.completedCount }
    val uniqueLetters = progressList.filter { it.charType == "LETTER" && it.completedCount > 0 }.size
    val uniqueNumbers = progressList.filter { it.charType == "NUMBER" && it.completedCount > 0 }.size
    val uniqueShapes = progressList.filter { it.charType == "SHAPE" && it.completedCount > 0 }.size
    val totalStars = progressList.sumOf { it.starsEarned }
    val avgAccuracy = if (progressList.isNotEmpty()) {
        progressList.map { it.bestAccuracy }.average().toInt()
    } else {
        0
    }

    val totalStickersUnlocked = stickerList.map { it.stickerId }.distinct().size
    val totalStickersPool = StickerModel.list.size

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFEDE7F6), // Soft Lavender
            Color(0xFFF3E5F5), // Soft purple tint
            Color(0xFFE8EAF6)  // Pale indigo
        )
    )

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEDE7F6)),
                contentAlignment = Alignment.Center
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "📊 Zona de Padres: Reportes",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A148C)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("parents_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color(0xFF4A148C)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFEDE7F6)
                    ),
                    modifier = Modifier.widthIn(max = 720.dp)
                )
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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Player Profile Editor Card
                // Multi-Profile Management Card
                val profiles by viewModel.childProfiles.collectAsState()
                val activeProfileId by viewModel.activeProfileId.collectAsState()

                var editingProfileId by remember { mutableStateOf<String?>(null) }
                var editName by remember { mutableStateOf("") }
                var editAge by remember { mutableStateOf(4) }
                var editAvatar by remember { mutableStateOf("🦊") }
                var editBgColor by remember { mutableStateOf("Celeste Mágico") }

                var showDeleteConfirmId by remember { mutableStateOf<String?>(null) }
                var showResetConfirmId by remember { mutableStateOf<String?>(null) }

                val avatarOptions = listOf("🦊", "🦁", "🐼", "🦄", "🦖", "🚀", "🐱", "🐶", "🐰", "🐯")
                val colorOptions = listOf("Celeste Mágico", "Amarillo Sol", "Rosado Algodón", "Verde Pradera", "Naranja Divertido", "Morado Fantasía")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("👥", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Control de Perfiles de Niños",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A148C)
                            )
                        }

                        Text(
                            text = "Modifica, restablece el progreso de trazado, elimina o cambia los perfiles de los niños registrados.",
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )

                        profiles.forEach { profile ->
                            val isActive = profile.id == activeProfileId
                            val isEditing = profile.id == editingProfileId

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isActive) 2.dp else 1.dp,
                                        color = if (isActive) Color(0xFF8E24AA) else Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) Color(0xFFFBF4FC) else Color(0xFFFAFAFA)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (!isEditing) {
                                        // View mode
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White)
                                                        .border(1.dp, Color(0xFFE0E0E0), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(profile.avatar, fontSize = 32.sp)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = profile.name,
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF1E293B)
                                                        )
                                                        if (isActive) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(Color(0xFF8E24AA))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = "ACTIVO",
                                                                    color = Color.White,
                                                                    fontSize = 8.sp,
                                                                    fontWeight = FontWeight.Black
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = "${profile.age} años • Fondo: ${profile.bgColor}",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (!isActive) {
                                                Button(
                                                    onClick = { viewModel.switchProfile(profile.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier.weight(1f).height(32.dp).testTag("btn_activate_${profile.id}"),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Usar Perfil", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    editingProfileId = profile.id
                                                    editName = profile.name
                                                    editAge = profile.age
                                                    editAvatar = profile.avatar
                                                    editBgColor = profile.bgColor
                                                },
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.weight(1f).height(32.dp).testTag("btn_edit_profile_${profile.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8E24AA))
                                            ) {
                                                Text("Editar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = { showResetConfirmId = profile.id },
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.weight(1.2f).height(32.dp).testTag("btn_reset_progress_${profile.id}"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                                            ) {
                                                Text("Restablecer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            if (profiles.size > 1) {
                                                IconButton(
                                                    onClick = { showDeleteConfirmId = profile.id },
                                                    modifier = Modifier.size(32.dp).testTag("btn_delete_profile_${profile.id}")
                                                ) {
                                                    Text("🗑️", fontSize = 16.sp)
                                                }
                                            }
                                        }
                                    } else {
                                        // Edit mode inside the child card
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Editar Perfil",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF8E24AA)
                                            )

                                            OutlinedTextField(
                                                value = editName,
                                                onValueChange = { editName = it },
                                                label = { Text("Nombre del Niño(a)") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_name_${profile.id}"),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            // Age Selector
                                            Column {
                                                Text(
                                                    text = "Edad: $editAge años",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF4A148C)
                                                )
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                                ) {
                                                    (3..8).forEach { age ->
                                                        val isSelected = editAge == age
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) Color(0xFF8E24AA) else Color(0xFFF5F5F5))
                                                                .clickable { editAge = age }
                                                                .testTag("age_option_${profile.id}_$age"),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = age.toString(),
                                                                color = if (isSelected) Color.White else Color.Black,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 14.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Avatar Selector
                                            Column {
                                                Text(
                                                    text = "Avatar:",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF4A148C)
                                                )
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                                ) {
                                                    avatarOptions.take(6).forEach { avatar ->
                                                        val isSelected = editAvatar == avatar
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) Color(0xFFF3E5F5) else Color(0xFFF5F5F5))
                                                                .border(
                                                                    width = 2.dp,
                                                                    color = if (isSelected) Color(0xFF8E24AA) else Color.Transparent,
                                                                    shape = CircleShape
                                                                )
                                                                .clickable { editAvatar = avatar },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(avatar, fontSize = 20.sp)
                                                        }
                                                    }
                                                }
                                            }

                                            // BgColor Selector
                                            Column {
                                                Text(
                                                    text = "Color de Fondo Mágico:",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF4A148C)
                                                )
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                                ) {
                                                    colorOptions.take(3).forEach { colorName ->
                                                        val isSelected = editBgColor == colorName
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(if (isSelected) Color(0xFF8E24AA) else Color(0xFFF5F5F5))
                                                                .clickable { editBgColor = colorName }
                                                                .padding(vertical = 4.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = colorName.split(" ").first(),
                                                                color = if (isSelected) Color.White else Color(0xFF4A148C),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                OutlinedButton(
                                                    onClick = { editingProfileId = null },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Cancelar", fontSize = 12.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.editProfileDirect(
                                                            profileId = profile.id,
                                                            name = editName,
                                                            age = editAge,
                                                            avatar = editAvatar,
                                                            bgColor = editBgColor
                                                        )
                                                        editingProfileId = null
                                                    },
                                                    modifier = Modifier.weight(1f).testTag("btn_save_profile_${profile.id}"),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Guardar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Delete Confirmation Dialog
                if (showDeleteConfirmId != null) {
                    val pId = showDeleteConfirmId!!
                    val profileToDelete = profiles.find { it.id == pId }
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirmId = null },
                        title = { Text("¿Eliminar perfil?") },
                        text = { Text("¿Estás seguro de que deseas eliminar el perfil de ${profileToDelete?.name}? Se perderán todos sus datos y progresos para siempre.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteProfile(pId)
                                    showDeleteConfirmId = null
                                },
                                modifier = Modifier.testTag("btn_confirm_delete")
                            ) {
                                Text("Eliminar", color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirmId = null }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // Reset Progress Confirmation Dialog
                if (showResetConfirmId != null) {
                    val pId = showResetConfirmId!!
                    val profileToReset = profiles.find { it.id == pId }
                    AlertDialog(
                        onDismissRequest = { showResetConfirmId = null },
                        title = { Text("¿Restablecer progreso?") },
                        text = { Text("¿Estás seguro de que deseas borrar todo el progreso y stickers conseguidos por ${profileToReset?.name}? Esta acción no se puede deshacer.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.resetProfileProgress(pId)
                                    showResetConfirmId = null
                                },
                                modifier = Modifier.testTag("btn_confirm_reset")
                            ) {
                                Text("Restablecer", color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetConfirmId = null }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // Header Welcome Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.InsertChart,
                                contentDescription = "Stats",
                                tint = Color(0xFF7B1FA2),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Seguimiento de Aprendizaje",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A148C)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Aquí puedes monitorear de forma totalmente privada el progreso de escritura, las puntuaciones de precisión y las recompensas desbloqueadas por el niño.",
                            fontSize = 13.sp,
                            color = Color(0xFF616161)
                        )
                    }
                }

                // Grid stats metrics (Letters, Numbers, Stars, Stickers)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔤", fontSize = 28.sp)
                            Text(
                                text = "$uniqueLetters / 26",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00ACC1)
                            )
                            Text(
                                "Letras Escritas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF757575),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔢", fontSize = 28.sp)
                            Text(
                                text = "$uniqueNumbers / 10",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFB8C00)
                            )
                            Text(
                                "Números Escritos",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF757575),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎨", fontSize = 28.sp)
                            Text(
                                text = "$uniqueShapes / 4",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF8E24AA)
                            )
                            Text(
                                "Figuras Escritas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF757575),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⭐", fontSize = 28.sp)
                            Text(
                                text = "$totalStars",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFB300)
                            )
                            Text(
                                "Estrellas Ganadas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF757575),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆", fontSize = 28.sp)
                            Text(
                                text = "$totalStickersUnlocked / $totalStickersPool",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF8E24AA)
                            )
                            Text(
                                "Stickers Desbloqueados",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF757575),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Accuracy and completion quality details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📈 Precisión Promedio de Trazos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424242)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Calidad de trazos:",
                                fontSize = 14.sp,
                                color = Color(0xFF616161)
                            )
                            Text(
                                text = "$avgAccuracy%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4CAF50)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { avgAccuracy.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFFE0E0E0)
                        )

                        Text(
                            text = "💡 Sugerencia: Si la precisión es baja, anima al niño a trazar con más calma para perfeccionar su motricidad fina.",
                            fontSize = 11.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Kid-Safe COPPA Privacy Shield Announcement
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(24.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF2E7D32), Color(0xFF4CAF50))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Escudo de Privacidad Infantil",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }

                        Text(
                            text = "• 100% Sin conexión (No requiere internet para jugar).\n" +
                                   "• Sin publicidad externa (Cero anuncios molestos o riesgosos).\n" +
                                   "• Datos Privados (Todos los datos de progreso se guardan localmente en el dispositivo del teléfono y jamás se comparten con terceros).\n" +
                                   "• Conforme con COPPA y regulaciones de protección al menor.",
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Dangerous Area - Reset progress
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "⚠️ Control de Progreso",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )

                        Text(
                            text = "Puedes borrar todo el historial de letras trazadas, stickers ganados y reiniciar la aplicación para que comience a aprender de nuevo.",
                            fontSize = 12.sp,
                            color = Color(0xFF5D4037)
                        )

                        Button(
                            onClick = { showResetConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .testTag("btn_reset_all_data")
                                .fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Reiniciar todo"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Borrar Todo el Progreso", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Wipe confirmation Dialog
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("¿Estás seguro?") },
            text = { Text("Esto borrará permanentemente todo el historial, estrellas, puntuación y los stickers ganados por el niño. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllLearningData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.testTag("confirm_wipe_btn")
                ) {
                    Text("Sí, Borrar Todo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancelar")
                }
            },
            modifier = Modifier.testTag("reset_confirm_dialog")
        )
    }
}
