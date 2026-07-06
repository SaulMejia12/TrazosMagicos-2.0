package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
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
import com.example.ui.viewmodel.TracingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSelectionScreen(
    viewModel: TracingViewModel,
    onProfileSelected: () -> Unit,
    onCreateNewProfile: () -> Unit,
    onNavigateToParents: () -> Unit
) {
    val profiles by viewModel.childProfiles.collectAsState()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0F2FE), // Light sky blue
                        Color(0xFFEFF6FF)  // Clean white-blue
                    )
                )
            )
    ) {
        // Subtle background sparkles/decorations
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fun brand header
                Text(
                    text = "✍️ Trazos Mágicos",
                    fontSize = if (isTablet) 24.sp else 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0369A1)
                )

                // Parent area shortcut with dynamic gate
                IconButton(
                    onClick = onNavigateToParents,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(16.dp))
                        .testTag("btn_parents_gate_profiles")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Zona de Padres",
                        tint = Color(0xFF4A148C)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title
                Text(
                    text = "¿QUIÉN VA A TRAZAR HOY? 🎨🌟",
                    fontSize = if (isTablet) 32.sp else 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E3A8A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Selecciona tu perfil o crea uno nuevo para guardar tu progreso",
                    fontSize = if (isTablet) 16.sp else 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // List of profiles and a "+" button inside a playful Grid
                val columns = if (isTablet) 3 else 2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .widthIn(max = 600.dp)
                ) {
                    items(profiles) { profile ->
                        ProfileCard(
                            name = profile.name,
                            age = profile.age,
                            avatar = profile.avatar,
                            bgColorHex = getBgColorHex(profile.bgColor),
                            onClick = {
                                viewModel.switchProfile(profile.id)
                                onProfileSelected()
                            },
                            modifier = Modifier.testTag("profile_card_${profile.id}")
                        )
                    }

                    // Plus Card to add a new child profile
                    item {
                        AddProfileCard(
                            onClick = onCreateNewProfile,
                            modifier = Modifier.testTag("profile_card_add_new")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun ProfileCard(
    name: String,
    age: Int,
    avatar: String,
    bgColorHex: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .border(4.dp, Color.White, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(bgColorHex))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Avatar Balloon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = avatar, fontSize = 48.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Child Name
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            // Child Age Badge
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$age años",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
            }
        }
    }
}

@Composable
fun AddProfileCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .border(3.dp, Color(0xFFCBD5E1), RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Perfil",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Crear Nuevo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "¡Nuevo Trazador!",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getBgColorHex(bgColorName: String): Long {
    return when (bgColorName) {
        "Celeste Mágico" -> 0xFFE0F2FE
        "Amarillo Sol" -> 0xFFFEF08A
        "Rosado Algodón" -> 0xFFFCE7F3
        "Verde Pradera" -> 0xFFDCFCE7
        "Naranja Divertido" -> 0xFFFFEDD5
        "Morado Fantasía" -> 0xFFF3E5F5
        else -> 0xFFE0F2FE
    }
}
