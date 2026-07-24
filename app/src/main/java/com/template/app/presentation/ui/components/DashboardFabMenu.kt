package com.template.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DashboardFabActions(
    val showScreenshot: Boolean = false,
    val showLock: Boolean = false,
    val showPlayPause: Boolean = false
) {
    val hasAny: Boolean get() = showScreenshot || showLock || showPlayPause
}

@Composable
fun DashboardFabMenu(
    isExpanded: Boolean,
    actions: DashboardFabActions,
    onToggle: () -> Unit,
    onScreenshot: () -> Unit,
    onLock: () -> Unit,
    onPlayPause: () -> Unit
) {
    if (!actions.hasAny) return

    val colorScheme = MaterialTheme.colorScheme
    val gradient = Brush.horizontalGradient(listOf(colorScheme.primary, colorScheme.secondary))

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(220)) + expandVertically(
                tween(220),
                expandFrom = Alignment.Bottom
            ),
            exit = fadeOut(tween(180)) + shrinkVertically(
                tween(180),
                shrinkTowards = Alignment.Bottom
            )
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (actions.showScreenshot) {
                    OrbitalFabItem(
                        onClick = onScreenshot,
                        icon = Icons.Default.PhotoCamera,
                        label = "Screenshot"
                    )
                }
                if (actions.showLock) {
                    OrbitalFabItem(onClick = onLock, icon = Icons.Default.Lock, label = "Lock Screen")
                }
                if (actions.showPlayPause) {
                    OrbitalFabItem(
                        onClick = onPlayPause,
                        icon = Icons.Default.PlayArrow,
                        label = "Play / Pause"
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = onToggle,
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                modifier = Modifier.size(56.dp)
            ) {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        (fadeIn(tween(180)) + scaleIn(tween(180))).togetherWith(
                            fadeOut(tween(120)) + scaleOut(tween(120))
                        )
                    },
                    label = "fab_icon"
                ) { expanded ->
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.GridView,
                        contentDescription = "Menu",
                        tint = colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun OrbitalFabItem(onClick: () -> Unit, icon: ImageVector, label: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colorScheme.surfaceContainerHighest,
            tonalElevation = 0.dp,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            containerColor = colorScheme.secondaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(2.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
