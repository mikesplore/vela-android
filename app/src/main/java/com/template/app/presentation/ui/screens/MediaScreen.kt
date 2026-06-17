package com.template.app.presentation.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.template.app.presentation.viewmodel.MediaViewModel
import kotlin.math.floor

@Composable
fun MediaScreen(
    viewModel: MediaViewModel = hiltViewModel()
) {
    val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    // 1. Add Scroll State
    val scrollState = rememberScrollState()

    val isPlaying = mediaState?.status?.contains("play", ignoreCase = true) == true
    val position = mediaState?.positionSeconds ?: 0.0
    val length = mediaState?.lengthSeconds ?: 1.0
    val progress = if (length > 0) (position / length).toFloat() else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "VinylRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationAngle"
    )

    var sliderPosition by remember(progress) { mutableFloatStateOf(progress) }
    var isDragging by remember { mutableStateOf(false) }
    val displayProgress = if (isDragging) sliderPosition else progress

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onBackground.copy(alpha = 0.5f),
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // 3. Replace weight Spacers with fixed heights
            Spacer(modifier = Modifier.height(24.dp))

            // Circular Vinyl Art
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .rotate(if (isPlaying) rotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color(0xFF121212),
                    shadowElevation = 12.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    0.0f to Color.Transparent,
                                    0.5f to Color.Black.copy(alpha = 0.3f),
                                    0.7f to Color.Transparent,
                                    0.9f to Color.Black.copy(alpha = 0.2f),
                                    1.0f to Color.Transparent
                                )
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (mediaState?.artUrl != null) {
                        AsyncImage(
                            model = mediaState?.artUrl,
                            contentDescription = "Album Art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(colorScheme.background)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Track Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mediaState?.title ?: "Not Playing",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(iterations = Int.MAX_VALUE, repeatDelayMillis = 2000)
                        .padding(horizontal = 16.dp)
                )
                Text(
                    text = mediaState?.artist ?: "Unknown Artist",
                    fontSize = 18.sp,
                    color = colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .basicMarquee(iterations = Int.MAX_VALUE, repeatDelayMillis = 3000)
                        .padding(horizontal = 24.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Progress Slider
            Column {
                Slider(
                    value = displayProgress,
                    onValueChange = {
                        isDragging = true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        viewModel.seekTo((sliderPosition * length).toInt())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = colorScheme.primary,
                        activeTrackColor = colorScheme.primary,
                        inactiveTrackColor = colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(if (isDragging) sliderPosition * length else position),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = formatTime(length),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.playPrevious() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(36.dp),
                        tint = colorScheme.onSurface
                    )
                }

                Surface(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(40.dp),
                            tint = colorScheme.onPrimaryContainer
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.playNext() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(36.dp),
                        tint = colorScheme.onSurface
                    )
                }
            }

            // Bottom padding to ensure content isn't cut off by navigation bars
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

private fun formatTime(seconds: Double): String {
    val mins = floor(seconds / 60).toInt()
    val secs = (seconds % 60).toInt()
    return String.format("%d:%02d", mins, secs)
}
