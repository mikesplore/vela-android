package com.template.app.presentation.ui.screens.chat

import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.AsyncImage
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.template.app.domain.model.AssistantChatMessage
import com.template.app.domain.model.ToolCall
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.Dp

// Determines if a string is a URL (album art, remote images) vs base64 (screenshots)
private fun String.isUrl() = startsWith("http://") || startsWith("https://")

@Composable
fun MessageBubble(
    message: AssistantChatMessage,
    modifier: Modifier = Modifier
) {
    val imageData = remember(message.imageBase64, message.artUrl) {
        when {
            !message.artUrl.isNullOrBlank() -> message.artUrl
            !message.imageBase64.isNullOrBlank() -> runCatching {
                val raw = if (message.imageBase64.contains(","))
                    message.imageBase64.substringAfter(",")
                else message.imageBase64
                Base64.decode(raw, Base64.DEFAULT)
            }.getOrNull()
            else -> null
        }
    }

    val isAlbumArt = !message.artUrl.isNullOrBlank() && message.artUrl.isUrl()
    val hasImage = imageData != null
    val hasText = message.text.isNotEmpty()
    val hasThinking = !message.thinkingText.isNullOrBlank()
    val hasToolCalls = message.toolCalls.isNotEmpty()
    val hasContent = hasImage || hasText || hasThinking || hasToolCalls || message.isStreaming

    if (!hasContent) return

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        if (message.isUser) {
            // ── User bubble ──────────────────────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp),
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    // User-sent image (e.g. photo attached)
                    if (hasImage) {
                        AsyncImage(
                            model = imageData,
                            contentDescription = null,
                            modifier = Modifier
                                .widthIn(max = 200.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.FillWidth
                        )
                        if (hasText) Spacer(Modifier.height(8.dp))
                    }
                    if (hasText) {
                        Text(
                            text = message.text,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        } else {
            // ── AI response ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                // 1. Thinking block (collapsed by default, tap to expand)
                if (hasThinking) {
                    ThinkingBlock(thinkingText = message.thinkingText!!)
                }

                // 2. Tool usage rows
                if (hasToolCalls) {
                    Spacer(Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        message.toolCalls.forEach { ToolRow(it) }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // 3. Screenshot image — full width
                if (hasImage && !isAlbumArt) {
                    AsyncImage(
                        model = imageData,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                0.5.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentScale = ContentScale.FillWidth
                    )
                    if (hasText) Spacer(Modifier.height(10.dp))
                }

                // 4. Album art — compact row
                if (isAlbumArt) {
                    AlbumArtRow(url = message.artUrl!!, caption = message.text)
                    return@Column  // text is embedded in AlbumArtRow
                }

                // 5. Main text / markdown
                if (hasText) {
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

                // 6. Loading pulse (streaming, nothing yet)
                if (message.isStreaming && !hasText && !hasThinking && !hasToolCalls) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

// ── Thinking toggle ───────────────────────────────────────────────────────────

@Composable
private fun ThinkingBlock(thinkingText: String) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { expanded = !expanded }
                .padding(vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                        CircleShape
                    )
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = "thinking",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f),
                letterSpacing = 0.01.sp
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (expanded) "⌄" else "›",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .padding(start = 13.dp)
                    .drawStartBorder(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        width = 1.5.dp
                    )
                    .padding(start = 10.dp)
            ) {
                Text(
                    text = thinkingText,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ── Tool usage row ────────────────────────────────────────────────────────────

@Composable
private fun ToolRow(tool: ToolCall) {
    val isDone = tool.status.lowercase() in listOf("done", "completed", "success")
    val isError = tool.status.lowercase() in listOf("error", "failed")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        if (isDone || isError) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(
                        if (isError)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
                        CircleShape
                    )
            )
        } else {
            // Animated pulse dot for running
            PulsingDot()
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = tool.name,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (isDone) 0.38f else 0.55f
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = tool.status,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size(5.dp)
            .background(
                Color(0xFF78C88C).copy(alpha = alpha),
                CircleShape
            )
    )
}

// ── Album art row ─────────────────────────────────────────────────────────────

@Composable
private fun AlbumArtRow(url: String, caption: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = if (caption.isNotEmpty()) 10.dp else 0.dp)
    ) {
        AsyncImage(
            model = url,
            contentDescription = "Album art",
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    0.5.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                ),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = caption.ifEmpty { "Now playing" },
                fontSize = 13.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
    }
}



private fun Modifier.drawStartBorder(color: ComposeColor, width: Dp): Modifier =
    this.drawBehind {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = width.toPx()
        )
    }