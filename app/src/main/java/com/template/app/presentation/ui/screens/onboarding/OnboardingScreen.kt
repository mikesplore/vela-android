package com.template.app.presentation.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.viewmodel.OnboardingViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val baseUrl by viewModel.baseUrl.collectAsStateWithLifecycle()
    val pairingCode by viewModel.pairingCode.collectAsStateWithLifecycle()
    val pairingPin by viewModel.pairingPin.collectAsStateWithLifecycle()
    val showPassword by viewModel.showPassword.collectAsStateWithLifecycle()
    val testState by viewModel.testState.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val capabilitiesState by viewModel.capabilitiesState.collectAsStateWithLifecycle()
    val availableModuleCount by viewModel.availableModuleCount.collectAsStateWithLifecycle()

    // Tech gradient layout matching the Vela control theme
    val gradientBg = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBg)
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: App Logo and Step Progress Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = "Vela Logo Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VELA",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("app_logo_title")
                    )
                }

                // Smooth horizontal pill step tracker
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    repeat(5) { step ->
                        val isActive = step == currentPage
                        val animatedWidth by animateDpAsState(
                            targetValue = if (isActive) 32.dp else 12.dp,
                            animationSpec = tween(300),
                            label = "step_pills"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.6.dp)
                                .width(animatedWidth)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                )
                        )
                    }
                }
            }

            // Central Animated Content with smooth slides
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "onboarding_phases"
                ) { targetPage ->
                    when (targetPage) {
                        0 -> OnboardingStepWelcome()
                        1 -> OnboardingStepInstallInfo()
                        2 -> OnboardingStepSettings(
                            baseUrl = baseUrl,
                            pairingCode = pairingCode,
                            pairingPin = pairingPin,
                            showPassword = showPassword,
                            testState = testState,
                            onUrlChange = viewModel::setBaseUrl,
                            onCodeChange = viewModel::setPairingCode,
                            onPinChange = viewModel::setPairingPin,
                            onTogglePassword = viewModel::toggleShowPassword,
                            onPerformPairing = viewModel::manualPairing,
                            onQrScanned = viewModel::onQrScanned,
                            onSkipOnboarding = {
                                viewModel.nextPage()
                            },
                            onContinue = {
                                viewModel.nextPage() // → capabilities
                            }
                        )
                        3 -> OnboardingStepCapabilities(
                            state = capabilitiesState,
                            availableCount = availableModuleCount,
                            onRetry = viewModel::loadCapabilities,
                            onContinue = { viewModel.nextPage() } // → greeting
                        )
                        4 -> OnboardingStepGreeting(
                            username = "${username?.replaceFirstChar { it.uppercase() }}",
                            onFinish = {
                                viewModel.finishOnboarding()
                                onOnboardingComplete()
                            }
                        )
                    }
                }
            }

            // Bottom Navigation Buttons (Next / Back)
            if (currentPage < 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage > 0) {
                        TextButton(
                            onClick = { viewModel.prevPage() },
                            modifier = Modifier
                                .testTag("btn_back")
                                .height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back icon")
                                Text("Back", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Button(
                        onClick = { viewModel.nextPage() },
                        modifier = Modifier
                            .testTag("btn_next")
                            .height(48.dp)
                            .widthIn(min = 120.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next icon")
                        }
                    }
                }
            }
        }
    }
}

data class StepItem(
    val num: String,
    val icon: ImageVector,
    val title: String,
    val desc: String
)
