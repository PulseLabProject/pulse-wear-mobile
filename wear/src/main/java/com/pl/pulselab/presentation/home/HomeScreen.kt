package com.pl.pulselab.presentation.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices

// HomeScreen is split into two layers:
//   HomeScreen        — knows about the ViewModel (stateful, not previewable)
//   HomeScreenContent — only knows about data + callbacks (stateless, previewable)
//
// This split is a common Compose pattern. It lets you preview the UI with
// hardcoded state without needing a real ViewModel or Application context.

// ─── STATEFUL WRAPPER ───────────────────────────────────────────────────────
// This is what MainActivity calls. It wires the ViewModel to the content.
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreenContent(
        uiState = uiState,
        onStart = { viewModel.startSession() },
        onStop  = { viewModel.stopSession() }
    )
}

// ─── STATELESS CONTENT ──────────────────────────────────────────────────────
// All the actual UI lives here. Receives state and callbacks — knows nothing
// about ViewModels, repositories, or coroutines.
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    AppScaffold {
        val listState = rememberTransformingLazyColumnState()

        ScreenScaffold(
            scrollState = listState,
            edgeButton = {
                // The bottom-edge button toggles the session
                when (uiState) {
                    is HomeUiState.Idle, is HomeUiState.Error -> {
                        EdgeButton(onClick = onStart) { Text("Start") }
                    }
                    is HomeUiState.Monitoring -> {
                        EdgeButton(onClick = onStop) { Text("Stop") }
                    }
                }
            }
        ) { contentPadding ->
            TransformingLazyColumn(contentPadding = contentPadding, state = listState) {

                item {
                    ListHeader(modifier = Modifier.fillMaxWidth()) {
                        Text("PulseLab")
                    }
                }

                // Each branch of `when` adds items to the lazy list.
                // `val state = uiState` lets us smart-cast inside each branch.
                when (val state = uiState) {

                    is HomeUiState.Idle -> {
                        item {
                            Text(
                                text = "Ready",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        item {
                            Text(
                                text = "Press Start to begin",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    is HomeUiState.Monitoring -> {
                        item {
                            // state.bpm is null for the first second while the sensor warms up
                            Text(
                                text = state.bpm?.let { "%.0f BPM".format(it) } ?: "...",
                                style = MaterialTheme.typography.displayLarge,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            // Counts how many readings reached the backend — useful for debugging
                            Text(
                                text = "Sent: ${state.readingsSent}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        item {
                            Button(onClick = onStop, modifier = Modifier.fillMaxWidth()) {
                                Text("Stop Session")
                            }
                        }
                    }

                    is HomeUiState.Error -> {
                        item {
                            Text(text = "Error", style = MaterialTheme.typography.titleLarge)
                        }
                        item {
                            Text(text = state.message, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// ─── PREVIEWS ───────────────────────────────────────────────────────────────
// These render in Android Studio's Design tab without needing a device.
// Add one preview per interesting state.

@WearPreviewDevices
@Composable
fun PreviewIdle() {
    HomeScreenContent(uiState = HomeUiState.Idle, onStart = {}, onStop = {})
}

@WearPreviewDevices
@Composable
fun PreviewMonitoring() {
    HomeScreenContent(
        uiState = HomeUiState.Monitoring(bpm = 78.0, sessionId = "preview", readingsSent = 12),
        onStart = {},
        onStop = {}
    )
}

@WearPreviewDevices
@Composable
fun PreviewError() {
    HomeScreenContent(
        uiState = HomeUiState.Error("Could not reach server"),
        onStart = {},
        onStop = {}
    )
}
