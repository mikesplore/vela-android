package com.template.app.presentation.ui.screens.chat

import android.util.Base64
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.template.app.domain.model.AssistantChatMessage
import com.template.app.domain.model.ToolCall

@Composable
fun MessageBubble(
    message: AssistantChatMessage,
    modifier: Modifier = Modifier
) {
    var showFullImage by remember { mutableStateOf(false) }

    val imageData = remember(message.imageBase64, message.artUrl) {
        when {
            !message.artUrl.isNullOrBlank() -> message.artUrl
            !message.imageBase64.isNullOrBlank() -> {
                try {
                    val pureBase64 = if (message.imageBase64.contains(",")) {
                        message.imageBase64.substringAfter(",")
                    } else {
                        message.imageBase64
                    }
                    Base64.decode(pureBase64, Base64.DEFAULT)
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }

    val hasImage = imageData != null
    val hasText = message.text.isNotEmpty()
    val hasThinking = !message.thinkingText.isNullOrBlank()
    val hasToolCalls = message.toolCalls.isNotEmpty()
    val hasContent = hasImage || hasText || hasThinking || hasToolCalls

    if (!hasContent) return

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (message.isUser) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
            } else {
                Color.Transparent
            },
            shape = if (message.isUser) {
                RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 4.dp
                )
            } else {
                RoundedCornerShape(0.dp)
            }
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = if (message.isUser) 14.dp else 0.dp,
                    vertical = if (message.isUser) 10.dp else 0.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Thinking block
                if (hasThinking) {
                    var isExpanded by remember { mutableStateOf(false) }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .animateContentSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Thought",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        
                        if (isExpanded) {
                            Box(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
                                Markdown(
                                    content = message.thinkingText ?: "",
                                    colors = markdownColor(
                                        text = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        codeText = MaterialTheme.colorScheme.primary,
                                        inlineCodeText = MaterialTheme.colorScheme.primary,
                                        linkText = MaterialTheme.colorScheme.primary,
                                        codeBackground = MaterialTheme.colorScheme.surfaceVariant,
                                        inlineCodeBackground = MaterialTheme.colorScheme.surfaceVariant,
                                        dividerColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    typography = markdownTypography(
                                        paragraph = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                        code = MaterialTheme.typography.bodySmall
                                    )
                                )
                            }
                        }
                    }
                }

                // Tool Usage status
                if (hasToolCalls) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        message.toolCalls.forEach { tool ->
                            ToolUsageItem(tool)
                        }
                    }
                }

                // Compact image thumbnail
                if (hasImage) {
                    Box(
                        modifier = Modifier
                            .width(if (message.isUser) 160.dp else 220.dp)
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { showFullImage = true }
                    ) {
                        AsyncImage(
                            model = imageData,
                            contentDescription = "Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // "View" pill overlay, bottom-right
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.45f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "view",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Text content
                if (hasText) {
                    if (message.isUser) {
                        Text(
                            text = message.text,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Markdown(
                            content = message.text,
                            colors = markdownColor(
                                text = MaterialTheme.colorScheme.onSurface,
                                codeText = MaterialTheme.colorScheme.primary,
                                inlineCodeText = MaterialTheme.colorScheme.primary,
                                linkText = MaterialTheme.colorScheme.primary,
                                codeBackground = MaterialTheme.colorScheme.surfaceVariant,
                                inlineCodeBackground = MaterialTheme.colorScheme.surfaceVariant,
                                dividerColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            typography = markdownTypography(
                                paragraph = MaterialTheme.typography.bodyLarge,
                                code = MaterialTheme.typography.bodyMedium
                            )
                        )
                    }
                }
                
                if (message.isStreaming && !hasText && !hasThinking && !hasToolCalls) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    // Full-screen image viewer dialog
    if (showFullImage && hasImage) {
        Dialog(onDismissRequest = { showFullImage = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { showFullImage = false }
            ) {
                AsyncImage(
                    model = imageData,
                    contentDescription = "Full image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun ToolUsageItem(tool: ToolCall) {
    val statusColor = when (tool.status.lowercase()) {
        "completed", "success" -> MaterialTheme.colorScheme.primary
        "error", "failed" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    val icon = when (tool.status.lowercase()) {
        "completed", "success" -> Icons.Default.CheckCircle
        "error", "failed" -> Icons.Default.Build // Could be Error icon
        else -> Icons.Default.Pending
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 6.dp, horizontal = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = statusColor
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Using ${tool.name}...",
            style = MaterialTheme.typography.labelMedium,
            color = statusColor
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = tool.status,
            style = MaterialTheme.typography.labelSmall,
            color = statusColor.copy(alpha = 0.8f)
        )
    }
}
