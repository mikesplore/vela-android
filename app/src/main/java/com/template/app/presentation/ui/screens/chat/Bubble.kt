package com.template.app.presentation.ui.screens.chat

import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.template.app.domain.model.AssistantChatMessage

@Composable
fun MessageBubble(
    message: AssistantChatMessage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        val hasContent = message.text.isNotEmpty() || 
                        !message.imageBase64.isNullOrBlank() || 
                        !message.artUrl.isNullOrBlank()
        
        if (hasContent) {
            Surface(
                color = if (message.isUser) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = if (message.isUser) 16.dp else 0.dp,
                        vertical = 10.dp
                    )
                ) {
                    // Display Image if available
                    if (!message.imageBase64.isNullOrBlank() || !message.artUrl.isNullOrBlank()) {
                        val imageData = remember(message.imageBase64, message.artUrl) {
                            if (!message.artUrl.isNullOrBlank()) {
                                message.artUrl
                            } else if (!message.imageBase64.isNullOrBlank()) {
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
                            } else null
                        }

                        if (imageData != null) {
                            AsyncImage(
                                model = imageData,
                                contentDescription = "Assistant Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                            if (message.text.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                    if (message.text.isNotEmpty()) {
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
                }
            }
        }
    }
}
