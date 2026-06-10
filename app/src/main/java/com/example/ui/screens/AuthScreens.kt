package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.StartupViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    var animateStart by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateStart = true
    }

    val logoScale by animateFloatAsState(
        targetValue = if (animateStart) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (animateStart) 1f else 0f,
        animationSpec = tween(1200, easing = EaseInOutCubic),
        label = "logoAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splashPulse")
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic ambient gradient spot behind the logo
        Box(
            modifier = Modifier
                .size(300.dp)
                .alpha(pulseGlowAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ElectricSky.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant modern branded logo shield
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ElectricSky, IndigoDusk)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(1.5.dp, TextWhite.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Startup Incubator Logo",
                    tint = Slate900,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Application name Display
            Text(
                text = "AI IDEA VALIDATOR",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = TextWhite,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(logoAlpha).scale(logoScale).testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tagline description
            Text(
                text = "STRATEGIC INCUBATOR PLATFORM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(logoAlpha * 0.8f).testTag("splash_subtitle")
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Indeterminate status accent loader
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = ElectricSky,
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: StartupViewModel,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Inputs error validation messages
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    var isLoggingIn by remember { mutableStateOf(false) }
    var showGoogleAccountsSheet by remember { mutableStateOf(false) }
    var googleSigningAccount by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Helper validation checkers
    fun checkInputsValid(): Boolean {
        var isValid = true
        if (name.trim().isEmpty()) {
            nameError = "Full Name is required"
            isValid = false
        } else {
            nameError = null
        }

        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        if (email.trim().isEmpty()) {
            emailError = "Email Address is required"
            isValid = false
        } else if (!emailRegex.matches(email.trim())) {
            emailError = "Please enter a valid email format"
            isValid = false
        } else {
            emailError = null
        }

        if (password.isEmpty()) {
            passwordError = "Password check is required"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordError = null
        }

        return isValid
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Header Title
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Brush.linearGradient(listOf(ElectricSky.copy(alpha = 0.15f), Color.Transparent)),
                        CircleShape
                    )
                    .border(1.dp, ElectricSky.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = ElectricSky,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to Incubator",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Secure your strategic ideation insights",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // User's Full Name Input
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError != null) nameError = null
                },
                label = { Text("Full Name") },
                placeholder = { Text("e.g. Ramana") },
                isError = nameError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricSky,
                    unfocusedBorderColor = Slate700,
                    focusedLabelColor = ElectricSky,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    errorBorderColor = SeverityHigh,
                    errorLabelColor = SeverityHigh
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_name_input"),
                singleLine = true
            )
            nameError?.let {
                Text(
                    text = it,
                    color = SeverityHigh,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Email Address Input
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
                label = { Text("Email Address") },
                placeholder = { Text("username@domain.com") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricSky,
                    unfocusedBorderColor = Slate700,
                    focusedLabelColor = ElectricSky,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    errorBorderColor = SeverityHigh,
                    errorLabelColor = SeverityHigh
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input"),
                singleLine = true
            )
            emailError?.let {
                Text(
                    text = it,
                    color = SeverityHigh,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Password Input Fields
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError != null) passwordError = null
                },
                label = { Text("Secret Password") },
                placeholder = { Text("••••••••") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    TextButton(
                        onClick = { passwordVisible = !passwordVisible },
                        colors = ButtonDefaults.textButtonColors(contentColor = ElectricSky)
                    ) {
                        Text(
                            text = if (passwordVisible) "HIDE" else "SHOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                isError = passwordError != null,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricSky,
                    unfocusedBorderColor = Slate700,
                    focusedLabelColor = ElectricSky,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    errorBorderColor = SeverityHigh,
                    errorLabelColor = SeverityHigh
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                singleLine = true
            )
            passwordError?.let {
                Text(
                    text = it,
                    color = SeverityHigh,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Standard Login Button Action
            Button(
                onClick = {
                    if (checkInputsValid()) {
                        isLoggingIn = true
                        coroutineScope.launch {
                            delay(1000) // Small, snappy validation feedback
                            viewModel.login(email.trim(), name.trim())
                            isLoggingIn = false
                        }
                    }
                },
                enabled = !isLoggingIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricSky,
                    contentColor = Slate900,
                    disabledContainerColor = ElectricSky.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoggingIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Slate900,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "SECURE INGRESS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider Text Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Slate700)
                )
                Text(
                    text = "OR SIGN IN WITH",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Slate700)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Styled Mock Google Sign-In Button (matches design rules)
            OutlinedButton(
                onClick = {
                    showGoogleAccountsSheet = true
                },
                enabled = !isLoggingIn && googleSigningAccount == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("google_login_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextWhite
                ),
                border = BorderStroke(1.dp, Slate700),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Modern custom Google G shape icon representation cleanly made
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            color = Slate900,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign in with Google",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Animated Google Account Selector bottom panel simulation
        AnimatedVisibility(
            visible = showGoogleAccountsSheet,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate800, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(BorderStroke(1.dp, Slate700), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Mini Google Logo indicator
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    color = Slate900,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Choose an account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                        TextButton(onClick = { showGoogleAccountsSheet = false }) {
                            Text("Cancel", color = ElectricSky)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "to continue to AI Idea Validator",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Account Option 1: Personal User Account (using metadata values!)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showGoogleAccountsSheet = false
                                googleSigningAccount = "ramanagajulavarthi116@gmail.com"
                                coroutineScope.launch {
                                    delay(1200) // Snappy account selector feedback delay
                                    viewModel.login("ramanagajulavarthi116@gmail.com", "Ramana Gajulavarthi")
                                    googleSigningAccount = null
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ElectricSky, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "R",
                                color = Slate900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Ramana Gajulavarthi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                "ramanagajulavarthi116@gmail.com",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Divider(color = Slate700, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    // Account Option 2: Platform Test Account
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showGoogleAccountsSheet = false
                                googleSigningAccount = "incubator.guest@gmail.com"
                                coroutineScope.launch {
                                    delay(1200)
                                    viewModel.login("incubator.guest@gmail.com", "Incubator Guest")
                                    googleSigningAccount = null
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MintFlame, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "G",
                                color = Slate900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Incubator Guest",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                "incubator.guest@gmail.com",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "To create or enter another account, select standard credentials login above.",
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Transparent full screen spinner when selecting Google account is loading
        if (googleSigningAccount != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate900.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = ElectricSky, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Signing in with Google Account...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = googleSigningAccount ?: "",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
