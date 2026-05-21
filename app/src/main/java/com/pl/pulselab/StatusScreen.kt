package com.pl.pulselab

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bloodtype
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pl.pulselab.BuildConfig
import com.pl.pulselab.model.SensorBundle
import com.pl.pulselab.presentation.status.StatusUiState
import com.pl.pulselab.presentation.status.StatusViewModel

private val Navy = Color(0xFF0A1628)
private val NavyLight = Color(0xFF152238)
private val Teal = Color(0xFF00BFA5)
private val TealDim = Color(0xFF00BFA5).copy(alpha = 0.12f)
private val TextPrimary = Color(0xFFF0F4F8)
private val TextSecondary = Color(0xFF8A9BB0)
private val DividerColor = Color(0xFF1E3048)
private val Green = Color(0xFF4CAF50)
private val Red = Color(0xFFEF5350)

private data class SensorReading(val icon: ImageVector, val label: String, val value: String, val unit: String)

@Composable
fun StatusScreen(viewModel: StatusViewModel) {
    val uiState by viewModel.statusUiState.collectAsState()
    StatusScreenContent(
        uiState = uiState,
        onInjectDebugReading = { viewModel.injectDebugReading() }
    )
}

@Composable
fun StatusScreenContent(
    uiState: StatusUiState,
    onInjectDebugReading: (() -> Unit)? = null
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "fade"
    )
    LaunchedEffect(Unit) { visible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "sync")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "rotate"
    )

    val serverStatusText = if (uiState.syncError != null) "Erro" else "Online"
    val serverStatusColor = if (uiState.syncError != null) Red else Green

    val syncStatusText = when {
        uiState.isSyncing -> "Em andamento..."
        uiState.syncError != null -> "Falhou"
        uiState.lastSyncTime != null -> "Concluído"
        else -> "Aguardando"
    }
    val syncStatusColor = when {
        uiState.isSyncing -> Teal
        uiState.syncError != null -> Red
        else -> Green
    }

    val bundle = uiState.bundle
    val readings = listOf(
        SensorReading(Icons.Outlined.FavoriteBorder, "Frequência Cardíaca", bundle?.bpm?.toString() ?: "--", "bpm"),
        SensorReading(Icons.Outlined.BarChart, "HRV", bundle?.hrv?.toString() ?: "--", "ms"),
        SensorReading(Icons.Outlined.Psychology, "Nível de Estresse", bundle?.stressIndex?.toString() ?: "--", "/100"),
        SensorReading(Icons.Outlined.Bloodtype, "SpO2", bundle?.spo2?.toString() ?: "--", "%")
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
                .padding(horizontal = 24.dp, vertical = 48.dp)
        ) {
            Text(
                "PESQUISA ACADÊMICA",
                color = Teal,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Status da\nSincronização",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 40.sp
            )

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyLight)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Estado do Servidor", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(serverStatusText, color = serverStatusColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(serverStatusColor)
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyLight)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Sincronização", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(syncStatusText, color = syncStatusColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
                Icon(
                    Icons.Outlined.Sync,
                    contentDescription = null,
                    tint = syncStatusColor,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(if (uiState.isSyncing) syncRotation else 0f)
                )
            }

            Spacer(Modifier.height(28.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(Modifier.height(24.dp))

            Text(
                "ÚLTIMO ENVIO",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val lastSyncColor = if (uiState.lastSyncTime != null) Green else TextSecondary
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = lastSyncColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                val lastSyncLabel = if (uiState.lastSyncTime != null) "Hoje às ${uiState.lastSyncTime}" else "--"
                Text(lastSyncLabel, color = TextSecondary, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "LEITURAS ATUAIS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(16.dp))

            readings.forEach { reading ->
                ReadingCard(reading)
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = DividerColor)

            if (BuildConfig.DEBUG && onInjectDebugReading != null) {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1A1A2E))
                        .clickable { onInjectDebugReading() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.BugReport,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Inject debug reading",
                        color = Color(0xFFFFB74D),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = DividerColor)
            }

            Text(
                "A coleta ocorre automaticamente em segundo plano. \nEste app não requer interação contínua.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ReadingCard(reading: SensorReading) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NavyLight)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(TealDim),
                contentAlignment = Alignment.Center
            ) {
                Icon(reading.icon, contentDescription = null, tint = Teal, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(reading.label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(reading.value, color = Teal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(3.dp))
            Text(reading.unit, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 2.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun StatusScreenPreview() {
    StatusScreenContent(uiState = StatusUiState())
}

@Preview(showSystemUi = true)
@Composable
fun StatusScreenMonitoringPreview() {
    StatusScreenContent(
        uiState = StatusUiState(
            bundle = SensorBundle(bpm = 72, hrv = 48, spo2 = 98, stressIndex = 32, timestamp = 0L),
            isSyncing = false,
            lastSyncTime = "14:32",
            syncError = null
        )
    )
}
