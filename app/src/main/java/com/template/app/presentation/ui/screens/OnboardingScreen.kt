package com.template.app.presentation.ui.screens

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
import androidx.compose.ui.platform.LocalFocusManager
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
    val apiToken by viewModel.apiToken.collectAsStateWithLifecycle()
    val showPassword by viewModel.showPassword.collectAsStateWithLifecycle()
    val testState by viewModel.testState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Tech gradient layout matching the Vela control theme
    val gradientBg = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
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
                    repeat(3) { step ->
                        val isActive = step == currentPage
                        val animatedWidth by animateDpAsState(
                            targetValue = if (isActive) 32.dp else 12.dp,
                            animationSpec = tween(300),
                            label = "step_pills"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
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
                            // Slide left
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            // Slide right
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
                            apiToken = apiToken,
                            showPassword = showPassword,
                            testState = testState,
                            onUrlChange = viewModel::setBaseUrl,
                            onTokenChange = viewModel::setApiToken,
                            onTogglePassword = viewModel::toggleShowPassword,
                            onTestConnection = viewModel::testConnection,
                            onSkipOnboarding = {
                                viewModel.completeOnboarding(isDemo = true)
                                onOnboardingComplete()
                            },
                            onContinue = {
                                viewModel.completeOnboarding(isDemo = false)
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
                    // Back Button
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

                    // Next Button
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
        // Overlapping custom artistic layers matching Tailwind design
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Layer 1 (Back rotated +6 degrees)
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

            // Layer 2 (Middle rotated -3 degrees)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = -3f
                    }
                    .background(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(56.dp)
                    )
            )

            // Layer 3 (Center Card with shadow and beautiful auto_awesome_motion icon)
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
            text = "Screen by screen,",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "built for clarity.",
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
            text = "We'll guide you through each step of the flow to ensure your experience is nothing short of perfect.",
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 24.sp,
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// STEP 2 -- Server Setup Info
@Composable
fun OnboardingStepInstallInfo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Run the Vela Agent",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "To sync your system, Vela needs the lightweight Vela Agent service running on the target machine (Windows, macOS, or Linux).",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val steps = listOf(
            StepItem(
                num = "1",
                icon = Icons.Default.Download,
                title = "Download or Install Agent",
                desc = "Run standard container or script installers on your target PC, server, or cluster."
            ),
            StepItem(
                num = "2",
                icon = Icons.Default.Terminal,
                title = "Launch Agent Service",
                desc = "Ensure the agent is running and accessible. By default, it operates on Port 8000."
            ),
            StepItem(
                num = "3",
                icon = Icons.Default.VpnKey,
                title = "Copy API Tokens",
                desc = "Note down your system security key or login password from the agent config."
            )
        )

        steps.forEach { step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = step.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${step.num}. ${step.title}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = step.desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// STEP 3 -- Server Settings / Auth Composable
@Composable
fun OnboardingStepSettings(
    baseUrl: String,
    apiToken: String,
    showPassword: Boolean,
    testState: OnboardingViewModel.TestResult,
    onUrlChange: (String) -> Unit,
    onTokenChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onTestConnection: () -> Unit,
    onSkipOnboarding: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Connect Your Device",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        Text(
            text = "Input host coordinates and access keys below to secure connection pairings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )

        // Web Address / Host URL Field
        OutlinedTextField(
            value = baseUrl,
            onValueChange = onUrlChange,
            label = { Text("Base URL / Host Service") },
            placeholder = { Text("http://192.168.1.100:8000") },
            leadingIcon = { Icon(Icons.Default.Dns, contentDescription = "Host Icon") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("onboarding_host_input")
        )

        // Security API Token / secret credentials field
        OutlinedTextField(
            value = apiToken,
            onValueChange = onTokenChange,
            label = { Text("API Key / Password") },
            placeholder = { Text("Your agent API token") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
            trailingIcon = {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("onboarding_token_input")
        )

        // Row of Verification action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onTestConnection,
                enabled = testState !is OnboardingViewModel.TestResult.Testing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_test_connection")
            ) {
                if (testState is OnboardingViewModel.TestResult.Testing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = "BoltIcon", modifier = Modifier.size(18.dp))
                        Text("Test Connection", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Animated results visual alert boxes
        AnimatedVisibility(
            visible = testState !is OnboardingViewModel.TestResult.Idle,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            when (testState) {
                is OnboardingViewModel.TestResult.Success -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E3A2F),
                            contentColor = Color(0xFFA7F3D0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_success_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = "Connected successfully icon",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Agent Connected!",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Server Uptime: ${testState.uptime}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                is OnboardingViewModel.TestResult.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_error_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Connection failed icon",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Connection Failed",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = testState.message,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                else -> {}
            }
        }

        // Skip / Play Mode Actions Area
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main Button to Finish with Real Configuration if Tested successfully
            Button(
                onClick = onContinue,
                enabled = testState is OnboardingViewModel.TestResult.Success,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_onboarding_finish")
            ) {
                Text("Launch Console Panel", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }

            // Beautiful styled divider separating demo skips
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    "OR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }

            // Demo/Explore Offline sandbox integration button
            OutlinedButton(
                onClick = onSkipOnboarding,
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_skip_demo")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.OfflineBolt,
                        contentDescription = "Demo Mode Bolt",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Run Local Demo Console",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Helper data structure for lists
data class StepItem(
    val num: String,
    val icon: ImageVector,
    val title: String,
    val desc: String
)
