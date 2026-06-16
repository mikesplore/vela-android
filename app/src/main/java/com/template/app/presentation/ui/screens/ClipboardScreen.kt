package com.template.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.viewmodel.ClipboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardScreen(
    viewModel: ClipboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val colorScheme = MaterialTheme.colorScheme

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Remote Content Card - Transparent with Industrial Divider
                VelaCard(title = "Device Clipboard") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .padding(16.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp).align(Alignment.Center),
                                color = colorScheme.secondary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = state.content.ifBlank { "Clipboard is empty" },
                                color = if (state.content.isBlank()) colorScheme.onSurfaceVariant else colorScheme.onSurface,
                                fontSize = 14.sp,
                                minLines = 3
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { clipboardManager.setText(AnnotatedString(state.content)) },
                            enabled = state.content.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.secondaryContainer,
                                contentColor = colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Copy to Phone", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        IconButton(
                            onClick = { viewModel.clearClipboard() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.error.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.DeleteSweep, null, tint = colorScheme.error, modifier = Modifier.size(22.dp))
                        }
                    }
                }

                // Write Card
                VelaCard(title = "Send to Device") {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter text to sync...", color = colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            cursorColor = colorScheme.primary,
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outlineVariant,
                            focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp), // Rounded corner shape for textfield
                        minLines = 4,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { 
                            viewModel.writeClipboard(inputText)
                            inputText = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = inputText.isNotBlank() && !state.isUpdating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            disabledContainerColor = colorScheme.onSurface.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        if (state.isUpdating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Update Remote Clipboard", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                state.error?.let {
                    ErrorMessage(it)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }


}

@Composable
private fun VelaCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent) // Card is now transparent
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 13.sp, // Title made a bit bigger
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurfaceVariant,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        // Industrial divider line matching Dashboard style
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        content()
    }
}

