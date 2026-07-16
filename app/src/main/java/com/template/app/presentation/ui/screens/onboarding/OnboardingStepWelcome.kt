package com.template.app.presentation.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// STEP 1 -- Welcome Composable
@Composable
fun OnboardingStepWelcome(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = 6f
                        scaleX = 0.95f
                        scaleY = 0.95f
                    }
                    .background(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(64.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = -3f }
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(56.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize(0.75f)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(48.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesomeMotion,
                    contentDescription = "Artistic Flair Motion Icon",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your System Ecosystem,",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Controlled Remotely.",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Vela helps you control and manage your PC remotely via a relay.",
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 24.sp,
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}