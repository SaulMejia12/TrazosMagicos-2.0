package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class StickerModel(
    val id: String,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    companion object {
        val list = listOf(
            StickerModel("dino", "🦖", "Dino Lector", "¡Súper Inteligente!", Color(0xFF4CAF50), Color(0xFF8BC34A)),
            StickerModel("rocket", "🚀", "Cohete Veloz", "¡Hasta el Espacio!", Color(0xFF673AB7), Color(0xFF9C27B0)),
            StickerModel("panda", "🐼", "Panda Pintor", "¡Mucho Arte!", Color(0xFF212121), Color(0xFF757575)),
            StickerModel("sun", "☀️", "Sol Brillante", "¡Día Radiante!", Color(0xFFFF9800), Color(0xFFFFC107)),
            StickerModel("star", "⭐", "Estrella Mágica", "¡Brillas Mucho!", Color(0xFFFFE082), Color(0xFFFFB300)),
            StickerModel("unicorn", "🦄", "Unicornio", "¡Pura Fantasía!", Color(0xFFE91E63), Color(0xFFF48FB1)),
            StickerModel("frog", "🐸", "Ranita Saltarina", "¡Gran Salto!", Color(0xFF009688), Color(0xFF4CAF50)),
            StickerModel("crab", "🦀", "Cangrejo Bailarín", "¡Qué Ritmo!", Color(0xFFFF5722), Color(0xFFFF8A65)),
            StickerModel("lion", "🦁", "León Valiente", "¡Rugido Fuerte!", Color(0xFF795548), Color(0xFFFF9800)),
            StickerModel("fox", "🦊", "Zorrito Sabio", "¡Gran Astucia!", Color(0xFFFF5722), Color(0xFFFFC107)),
            StickerModel("whale", "🐳", "Ballena Feliz", "¡Un Gran Splash!", Color(0xFF2196F3), Color(0xFF00BCD4)),
            StickerModel("koala", "🐨", "Koala Abrazo", "¡Muy Cariñoso!", Color(0xFF9E9E9E), Color(0xFFBDBDBD))
        )

        fun find(id: String): StickerModel {
            return list.firstOrNull { it.id == id } ?: list[0]
        }
    }
}

@Composable
fun StickerCard(
    stickerId: String,
    modifier: Modifier = Modifier,
    isUnlocked: Boolean = true,
    pulseAnimation: Boolean = false
) {
    val sticker = StickerModel.find(stickerId)
    
    // Scale pulse animation for new unlocks
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by if (pulseAnimation) {
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "scale"
        )
    } else {
        rememberUpdatedState(1.0f)
    }

    val cardBrush = if (isUnlocked) {
        Brush.linearGradient(
            colors = listOf(sticker.primaryColor, sticker.secondaryColor)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
        )
    }

    Card(
        modifier = modifier
            .padding(6.dp)
            .scale(scale)
            .aspectRatio(0.82f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 8.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBrush)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Sticker Emoji Badge with Circle Backing
                Box(
                    modifier = Modifier
                        .size(if (pulseAnimation) 90.dp else 64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isUnlocked) sticker.emoji else "🔒",
                        fontSize = if (pulseAnimation) 48.sp else 34.sp,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = if (isUnlocked) sticker.title else "Bloqueado",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color.White else Color(0xFF616161),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                
                Text(
                    text = if (isUnlocked) sticker.subtitle else "¡Sigue trazando!",
                    fontSize = 11.sp,
                    color = if (isUnlocked) Color.White.copy(alpha = 0.85f) else Color(0xFF757575),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
