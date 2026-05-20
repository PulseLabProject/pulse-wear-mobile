package com.pl.pulselab

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Monitor
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF0A1628)
private val NavyLight = Color(0xFF152238)
private val Teal = Color(0xFF00BFA5)
private val TealDim = Color(0xFF00BFA5).copy(alpha = 0.12f)
private val TextPrimary = Color(0xFFF0F4F8)
private val TextSecondary = Color(0xFF8A9BB0)
private val DividerColor = Color(0xFF1E3048)

data class DataItem(val icon: ImageVector, val label: String, val description: String)

@Composable
fun DataConsentScreen(onAccept: () -> Unit, onDecline: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "fade"
    )

    LaunchedEffect(Unit) { visible = true }

    val dataItems = listOf(
        DataItem(Icons.Outlined.Favorite, "Frequência Cardíaca", "BPM em tempo real"),
        DataItem(Icons.Outlined.Monitor, "HRV", "Variabilidade cardíaca"),
        DataItem(Icons.Outlined.Science, "Nível de Estresse", "Índice estimado pelo sensor"),
        DataItem(Icons.Outlined.WaterDrop, "SpO2", "Saturação de oxigênio")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .alpha(alpha)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(TealDim)
                        .border(1.dp, Teal.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "PESQUISA ACADÊMICA",
                        color = Teal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "Autorização\nde Coleta",
                    color = TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 40.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Estudo sobre biomarcadores de estresse",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(Modifier.height(28.dp))

                Text(
                    "DADOS COLETADOS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(Modifier.height(16.dp))

                dataItems.forEach { item ->
                    DataRow(item)
                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(28.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(Modifier.height(20.dp))

                Text(
                    "Os dados coletados são armazenados de forma segura e utilizados exclusivamente para fins de pesquisa científica. A coleta ocorre em segundo plano enquanto o app está ativo.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(36.dp))

            Column {
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal)
                ) {
                    Text(
                        "Autorizar Coleta",
                        color = Navy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = onDecline,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Recusar e Sair",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DataRow(item: DataItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NavyLight)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(TealDim),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, contentDescription = null, tint = Teal, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.width(14.dp))

        Column {
            Text(item.label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(item.description, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun DataConsentPreview() {
    DataConsentScreen(onAccept = {}, onDecline = {})
}