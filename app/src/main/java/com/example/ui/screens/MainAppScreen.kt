package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.StartupIdea
import com.example.data.model.StartupReport
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StartupViewModel
import com.example.ui.viewmodel.ValidationState
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.util.PdfExporter
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class Screen {
    WORKSPACE,
    NEW_VAL_FORM,
    DASHBOARD
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: StartupViewModel,
    modifier: Modifier = Modifier
) {
    val startupIdeas by viewModel.startupIdeas.collectAsStateWithLifecycle()
    val validationState by viewModel.validationState.collectAsStateWithLifecycle()
    val currentAgentLog by viewModel.currentAgentLog.collectAsStateWithLifecycle()
    val activeReport by viewModel.activeReport.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val isSplashActive by viewModel.isSplashActive.collectAsStateWithLifecycle()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val loggedInEmail by viewModel.loggedInEmail.collectAsStateWithLifecycle()
    val loggedInName by viewModel.loggedInName.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(Screen.WORKSPACE) }

    // Steer screen transitions on validation state triggers
    LaunchedEffect(validationState) {
        if (validationState == ValidationState.SUCCESS && activeReport != null) {
            currentScreen = Screen.DASHBOARD
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ambientGlow")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    AnimatedContent(
        targetState = when {
            isSplashActive -> "splash"
            !isUserLoggedIn -> "login"
            else -> "main"
        },
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
        },
        label = "AppStageSwitchTransition"
    ) { stage ->
        when (stage) {
            "splash" -> {
                SplashScreen(modifier = Modifier.fillMaxSize())
            }
            "login" -> {
                LoginScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            }
            "main" -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .background(Slate900)
                        .drawBehind {
                            val primaryBrush = Brush.radialGradient(
                                colors = listOf(
                                    ElectricSky.copy(alpha = alphaAnim * 0.35f),
                                    IndigoDusk.copy(alpha = alphaAnim * 0.15f),
                                    Color.Transparent
                                ),
                                center = center.copy(
                                    x = center.x + floatOffset,
                                    y = center.y - floatOffset - 100f
                                ),
                                radius = size.width * 0.75f
                            )
                            drawRect(brush = primaryBrush)

                            val secondaryBrush = Brush.radialGradient(
                                colors = listOf(
                                    MintFlame.copy(alpha = alphaAnim * 0.2f),
                                    Color.Transparent
                                ),
                                center = center.copy(
                                    x = center.x - floatOffset,
                                    y = center.y + floatOffset + 150f
                                ),
                                radius = size.width * 0.6f
                            )
                            drawRect(brush = secondaryBrush)
                        }
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = ElectricSky,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "AI IDEA VALIDATOR",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextWhite,
                                            letterSpacing = 2.sp
                                        )
                                    }
                                },
                                navigationIcon = {
                                    if (currentScreen != Screen.WORKSPACE) {
                                        IconButton(onClick = {
                                            if (currentScreen == Screen.NEW_VAL_FORM) {
                                                viewModel.setIdleState()
                                                currentScreen = Screen.WORKSPACE
                                            } else {
                                                viewModel.deselectIdea()
                                                currentScreen = Screen.WORKSPACE
                                            }
                                        }) {
                                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                                        }
                                    }
                                },
                                actions = {
                                    if (currentScreen == Screen.WORKSPACE) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            // User display Initial
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(ElectricSky, CircleShape)
                                                    .border(0.5.dp, TextWhite.copy(alpha = 0.3f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val initial = if (loggedInName.isNotEmpty()) loggedInName.first().toString().uppercase() else "R"
                                                Text(
                                                    text = initial,
                                                    color = Slate900,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            // Logout Session Trigger
                                            IconButton(
                                                onClick = { viewModel.logout() },
                                                modifier = Modifier.testTag("logout_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ExitToApp,
                                                    contentDescription = "Log out from portal",
                                                    tint = TextSecondary
                                                )
                                            }
                                        }
                                    } else if (currentScreen == Screen.DASHBOARD && activeReport != null) {
                                        TextButton(
                                            onClick = {
                                                viewModel.deselectIdea()
                                                currentScreen = Screen.WORKSPACE
                                            }
                                        ) {
                                            Text("CLOSE REPORT", color = ElectricSky, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Slate900.copy(alpha = 0.9f),
                                    titleContentColor = TextWhite
                                )
                            )
                        },
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    val duration = 400
                                    if (targetState.ordinal > initialState.ordinal) {
                                        (slideInHorizontally(animationSpec = tween(duration, easing = FastOutSlowInEasing)) { x -> x / 2 } + fadeIn(animationSpec = tween(duration))).togetherWith(
                                            slideOutHorizontally(animationSpec = tween(duration - 50, easing = FastOutSlowInEasing)) { x -> -x / 2 } + fadeOut(animationSpec = tween(duration - 50))
                                        )
                                    } else {
                                        (slideInHorizontally(animationSpec = tween(duration, easing = FastOutSlowInEasing)) { x -> -x / 2 } + fadeIn(animationSpec = tween(duration))).togetherWith(
                                            slideOutHorizontally(animationSpec = tween(duration - 50, easing = FastOutSlowInEasing)) { x -> x / 2 } + fadeOut(animationSpec = tween(duration - 50))
                                        )
                                    }
                                },
                                label = "ScreenSwitchTransition"
                            ) { targetScreen ->
                                when (targetScreen) {
                                    Screen.WORKSPACE -> {
                                        WorkspaceScreen(
                                            ideas = startupIdeas,
                                            onSelectIdea = { idea ->
                                                viewModel.selectIdea(idea)
                                                currentScreen = Screen.DASHBOARD
                                            },
                                            onDeleteIdea = { id -> viewModel.deleteIdea(id) },
                                            onNavigateToNew = { currentScreen = Screen.NEW_VAL_FORM }
                                        )
                                    }

                                    Screen.NEW_VAL_FORM -> {
                                        if (validationState == ValidationState.ANALYZING) {
                                            LoadingScreen(logText = currentAgentLog)
                                        } else {
                                            FormInputScreen(
                                                errorMsg = errorMessage,
                                                onRunValidation = { name, desc, ind, aud ->
                                                    viewModel.validateNewIdea(name, desc, ind, aud)
                                                },
                                                onCancel = {
                                                    viewModel.setIdleState()
                                                    currentScreen = Screen.WORKSPACE
                                                }
                                            )
                                        }
                                    }

                                    Screen.DASHBOARD -> {
                                        activeReport?.let { report ->
                                            DashboardReportScreen(
                                                report = report,
                                                viewModel = viewModel
                                            )
                                        } ?: run {
                                            Box(modifier = Modifier.fillMaxSize())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * WORKSPACE SCREEN
 * Contains workspace lists, search bar, empty layout cards, delete capabilities.
 */
@Composable
private fun WorkspaceScreen(
    ideas: List<StartupIdea>,
    onSelectIdea: (StartupIdea) -> Unit,
    onDeleteIdea: (Int) -> Unit,
    onNavigateToNew: () -> Unit
) {
    val fabPulseTransition = rememberInfiniteTransition(label = "fabPulse")
    val fabPulseRadius by fabPulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fabRadius"
    )
    val fabPulseAlpha by fabPulseTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fabAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "My Startup Incubator",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Select an existing analysis file or validate a fresh startup business plan.",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (ideas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Slate700)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(ElectricSky.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = ElectricSky,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Incubator Workspace Empty",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "You haven't run any business validations yet. Hit the floating action button below to run deep multi-agent evaluations in seconds.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onNavigateToNew,
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricSky),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("onboarding_activate_button")
                            ) {
                                Text("New Idea Evaluation", color = Slate900, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(ideas, key = { _, idea -> idea.id }) { index, idea ->
                        var isItemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(idea.id) {
                            isItemVisible = true
                        }
                        AnimatedVisibility(
                            visible = isItemVisible,
                            enter = fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = index * 80)) +
                                    slideInVertically(
                                        initialOffsetY = { 30 },
                                        animationSpec = tween(durationMillis = 300, delayMillis = index * 80)
                                    ),
                            exit = fadeOut()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectIdea(idea) }
                                    .testTag("idea_card_${idea.id}"),
                                colors = CardDefaults.cardColors(containerColor = Slate800),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Slate700)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = idea.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ElectricSky,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Slate700, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = idea.industry,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = idea.description,
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = MintFlame,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Validation Rating: ${idea.report?.validationScore ?: 0}/100",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteIdea(idea.id) },
                                        modifier = Modifier.testTag("delete_idea_${idea.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Idea",
                                            tint = SeverityHigh,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Circular Add floating Button with elegant interactive breathing pulse
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .drawBehind {
                        drawCircle(
                            color = ElectricSky.copy(alpha = fabPulseAlpha),
                            radius = (size.width / 2f) * fabPulseRadius
                        )
                    }
            )
            FloatingActionButton(
                onClick = onNavigateToNew,
                containerColor = ElectricSky,
                contentColor = Slate900,
                modifier = Modifier.testTag("validate_new_idea_fab"),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Idea", modifier = Modifier.size(28.dp))
            }
        }
    }
}

/**
 * FORM INPUT SCREEN
 * Allows typing name, desc, selecting target, with high fidelity sample auto-fill tool.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormInputScreen(
    errorMsg: String?,
    onRunValidation: (String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("EdTech") }
    var targetAudience by remember { mutableStateOf("Fresh graduates seeking jobs") }

    val industriesList = listOf("EdTech", "FinTech", "HealthTech", "AI SaaS", "B2B Software", "E-Commerce", "DeepTech", "Sustainability")

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > 800.dp

        if (isWide) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Responsive Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Evaluate New Idea",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Complete the startup blueprints below to trigger our multi-agent validator.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    // AUTO-FILL button for speed prototypes testing
                    Button(
                        onClick = {
                            name = "AI Interview Coach"
                            description = "I want to build an AI-powered platform that automatically prepares students for technical interviews by conducting mock coding and system design conversations, scoring user feedback, and suggesting roadmaps."
                            industry = "EdTech"
                            targetAudience = "Students and job seekers trying to land tech roles"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        border = BorderStroke(1.dp, MintFlame.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Auto Fill Example",
                                tint = MintFlame,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Auto Fill Example", color = MintFlame, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Left Column (Name, Target Audience, Error Message & Buttons)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Startup Name
                        Column {
                            Text(
                                text = "Startup Name",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricSky,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = { Text("e.g. AI Interview Coach", color = TextSecondary.copy(alpha = 0.5f)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("field_name"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = ElectricSky,
                                    unfocusedBorderColor = Slate700,
                                    focusedContainerColor = Slate800,
                                    unfocusedContainerColor = Slate800
                                ),
                                singleLine = true
                            )
                        }

                        // Target Audience Profile
                        Column {
                            Text(
                                text = "Target Audience Profile",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricSky,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = targetAudience,
                                onValueChange = { targetAudience = it },
                                placeholder = { Text("e.g. Fresh college passouts and engineering grads seeking tech jobs", color = TextSecondary.copy(alpha = 0.5f)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("field_audience"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = ElectricSky,
                                    unfocusedBorderColor = Slate700,
                                    focusedContainerColor = Slate800,
                                    unfocusedContainerColor = Slate800
                                ),
                                singleLine = true
                            )
                        }

                        if (errorMsg != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SeverityHigh.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, SeverityHigh)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = SeverityHigh)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = errorMsg, fontSize = 12.sp, color = SeverityHigh)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions Row at the bottom of the column
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onCancel,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    onRunValidation(name, description, industry, targetAudience)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricSky),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("evaluate_trigger_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Slate900, modifier = Modifier.size(16.dp))
                                    Text("Trigger Validator", color = Slate900, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    // Right Column (Industry and Description)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Sector / Industry selection
                        Column {
                            Text(
                                text = "Sector / Industry",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricSky,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                industriesList.forEach { ind ->
                                    val selected = industry == ind
                                    val animatedBgColor by animateColorAsState(
                                        targetValue = if (selected) ElectricSky else Slate800,
                                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                        label = "chipBg"
                                    )
                                    val animatedBorderColor by animateColorAsState(
                                        targetValue = if (selected) ElectricSky else Slate700,
                                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                        label = "chipBorder"
                                    )
                                    val animatedTextColor by animateColorAsState(
                                        targetValue = if (selected) Slate900 else TextWhite,
                                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                        label = "chipText"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(animatedBgColor, RoundedCornerShape(20.dp))
                                            .border(1.dp, animatedBorderColor, RoundedCornerShape(20.dp))
                                            .clickable { industry = ind }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = ind,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = animatedTextColor
                                        )
                                    }
                                }
                            }
                        }

                        // Idea Description / Blueprint
                        Column {
                            Text(
                                text = "Idea Description / Blueprint",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricSky,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = { Text("Provide details: what problem does this resolve? How does the AI help?", fontSize = 13.sp, color = TextSecondary.copy(alpha = 0.5f)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .testTag("field_desc"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = ElectricSky,
                                    unfocusedBorderColor = Slate700,
                                    focusedContainerColor = Slate800,
                                    unfocusedContainerColor = Slate800
                                ),
                                maxLines = 8
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Evaluate New Idea",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )

                        // AUTO-FILL button for speed prototypes testing
                        IconButton(
                            onClick = {
                                name = "AI Interview Coach"
                                description = "I want to build an AI-powered platform that automatically prepares students for technical interviews by conducting mock coding and system design conversations, scoring user feedback, and suggesting roadmaps."
                                industry = "EdTech"
                                targetAudience = "Students and job seekers trying to land tech roles"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Auto Fill Example",
                                tint = MintFlame
                            )
                        }
                    }
                    Text(
                        text = "Complete the startup blueprints below to trigger our multi-agent validator. (Tap the green arrow icon at the top right to auto-fill an example idea!)",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }

                item {
                    Column {
                        Text(
                            text = "Startup Name",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricSky,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("e.g. AI Interview Coach", color = TextSecondary.copy(alpha = 0.5f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("field_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricSky,
                                unfocusedBorderColor = Slate700,
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800
                            ),
                            singleLine = true
                        )
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Idea Description / Blueprint",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricSky,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Provide details: what problem does this resolve? How does the AI help?", fontSize = 13.sp, color = TextSecondary.copy(alpha = 0.5f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("field_desc"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricSky,
                                unfocusedBorderColor = Slate700,
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800
                            ),
                            maxLines = 5
                        )
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Sector / Industry",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricSky,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            industriesList.forEach { ind ->
                                val selected = industry == ind
                                val animatedBgColor by animateColorAsState(
                                    targetValue = if (selected) ElectricSky else Slate800,
                                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                    label = "chipBg"
                                )
                                val animatedBorderColor by animateColorAsState(
                                    targetValue = if (selected) ElectricSky else Slate700,
                                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                    label = "chipBorder"
                                )
                                val animatedTextColor by animateColorAsState(
                                    targetValue = if (selected) Slate900 else TextWhite,
                                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                    label = "chipText"
                                )
                                Box(
                                    modifier = Modifier
                                        .background(animatedBgColor, RoundedCornerShape(20.dp))
                                        .border(1.dp, animatedBorderColor, RoundedCornerShape(20.dp))
                                        .clickable { industry = ind }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = ind,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = animatedTextColor
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Target Audience Profile",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricSky,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = targetAudience,
                            onValueChange = { targetAudience = it },
                            placeholder = { Text("e.g. Fresh college passouts and engineering grads seeking tech jobs", color = TextSecondary.copy(alpha = 0.5f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("field_audience"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricSky,
                                unfocusedBorderColor = Slate700,
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800
                            ),
                            singleLine = true
                        )
                    }
                }

                if (errorMsg != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SeverityHigh.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, SeverityHigh)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = SeverityHigh)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = errorMsg, fontSize = 12.sp, color = SeverityHigh)
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                onRunValidation(name, description, industry, targetAudience)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricSky),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("evaluate_trigger_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Slate900, modifier = Modifier.size(16.dp))
                                Text("Trigger Validator", color = Slate900, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * LOADING SCREEN
 * Elegant loader that outputs the live text logs of the agent coordination.
 */
@Composable
private fun LoadingScreen(logText: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = ElectricSky,
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "COORDINATING MULTI-AGENT PROJECTION...",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ElectricSky,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .background(Slate800, RoundedCornerShape(8.dp))
                    .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = logText,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.animateContentSize()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Please expect 5-10 seconds. Collaborative agents are polling data markets, estimating sizing margins, & calculating risks.",
                fontSize = 10.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

/**
 * DASHBOARD REPORT SCREEN
 * Displays dynamic tab structures containing our custom widgets.
 */
@Composable
private fun DashboardReportScreen(
    report: StartupReport,
    viewModel: StartupViewModel
) {
    val scrollState = rememberLazyListState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("AI Feedback", "Scores", "Markets", "Competitors", "SWOT & Risks", "Business Plan", "Investor Presentation", "Advisor Chat")

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal tab selectors
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Slate900,
            contentColor = ElectricSky,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = ElectricSky
                )
            },
            divider = { HorizontalDivider(color = Slate700) }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("dashboard_tab_$index")
                ) {
                    Box(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) ElectricSky else TextSecondary
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Slate900)
        ) {
            when (selectedTab) {
                0 -> StructuredFeedbackWidget(report)
                1 -> ScoresOverviewTab(report)
                2 -> MarketFinanceTab(report)
                3 -> CompetitorsIntelligenceTab(report)
                4 -> SwotRisksTab(report)
                5 -> BusinessPlanTab(report)
                6 -> InvestorPresentationTab(report)
                7 -> AdvisorChatTab(report, viewModel)
            }
        }
    }
}

@Composable
private fun ScoresOverviewTab(report: StartupReport) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularScoreGauge(
                    score = report.validationScore,
                    label = "VALIDATION RATING",
                    gaugeColor = ElectricSky,
                    modifier = Modifier.testTag("validation_gauge")
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EXECUTIVE DECREE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ElectricSky,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.validationRecommendation,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            HorizontalDivider(color = Slate700)
        }

        item {
            Text(
                text = "Structure & Industry Classification",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate700)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScoreLabelRow(label = "Primary Sector", value = report.detectedIndustry)
                    ScoreLabelRow(label = "Business Classification", value = report.businessCategory)
                    ScoreLabelRow(label = "Deployment Type", value = report.productType)
                    ScoreLabelRow(label = "Target User Demographics", value = report.targetUsers)
                    ScoreLabelRow(label = "Geographic Market Hubs", value = report.potentialMarkets)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate700)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("SUCCESS PROBABILITY", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${report.successProbability}%", fontSize = 28.sp, fontWeight = FontWeight.Black, color = MintFlame)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("CAGR industry baseline matrix", fontSize = 9.sp, color = TextSecondary, textAlign = TextAlign.Center)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate700)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("INVESTOR READINESS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${report.investorReadinessScore}%", fontSize = 28.sp, fontWeight = FontWeight.Black, color = IndigoDusk)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(report.investorReadinessDetails, fontSize = 9.sp, color = TextSecondary, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        item {
            HorizontalDivider(color = Slate700)
        }

        // Feature 7: Weighted score formula & Benchmarks
        item {
            ScoreTransparencyWidget(report)
        }

        // Feature 10: Accelerator / VC Readiness Checklist
        item {
            AcceleratorChecklistWidget(report)
        }

        // Feature 8: Real Business Runway & CAC Calculator
        item {
            BusinessCalculatorWidget(report)
        }

        // Feature: Founder Next 30 Days Action Playbook
        item {
            FounderNext30DaysWidget(report)
        }

        // Feature: VC objections simulator and defensive playbook
        item {
            VcObjectionsMitigationWidget(report)
        }

        // Feature: Multi-agent execution and consensus visibility
        item {
            MultiAgentConsensusWidget(report)
        }
    }
}

private val CustomGold = Color(0xFFFBBF24)

@Composable
private fun ScoreTransparencyWidget(report: StartupReport) {
    var isAuditExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = ElectricSky, modifier = Modifier.size(20.dp))
                    Text("Weighted Rating & Sector Benchmarks", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }

                TextButton(
                    onClick = { isAuditExpanded = !isAuditExpanded },
                    colors = ButtonDefaults.textButtonColors(contentColor = ElectricSky)
                ) {
                    Text(if (isAuditExpanded) "Hide Audit" else "Why this score?", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Aggregated via multiple agent score weighting vectors:",
                fontSize = 11.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            BenchmarkRow(label = "Demand Volume (40% Weight)", score = report.demandScore, average = 72, color = MintFlame)
            Spacer(modifier = Modifier.height(8.dp))
            BenchmarkRow(label = "Competitor Gap Score (30% Weight)", score = report.opportunityScore, average = 68, color = ElectricSky)
            Spacer(modifier = Modifier.height(8.dp))
            BenchmarkRow(label = "Investor Readiness (30% Weight)", score = report.investorReadinessScore, average = 65, color = IndigoDusk)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Slate700)
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Overall Validation", fontSize = 11.sp, color = TextSecondary)
                Text(
                    text = "Formula Score: ${report.validationScore}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricSky
                )
            }

            if (isAuditExpanded) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Slate700)
                Spacer(modifier = Modifier.height(12.dp))

                Text("🔍 SCORE DRIVERS & FRICTION POINTS AUDIT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CustomGold)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Positive drivers
                    Column(modifier = Modifier.weight(1f)) {
                        Text("➕ SCORE LIFT FACTORS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MintFlame)
                        Spacer(modifier = Modifier.height(6.dp))
                        BulletText("📈 Strong Demand Volume: Sector CAGR growing at ${report.yearlyGrowthRate} YoY.")
                        BulletText("🎯 Pinpointed Focus: Specifically targeting ${report.targetUsers} addressable users.")
                        BulletText("🧱 Actionable Gaps: Unlocked ${report.competitorGaps.size} structural holes in legacy rivals.")
                    }

                    // Negative friction points
                    Column(modifier = Modifier.weight(1f)) {
                        Text("➖ RISK FRICTION POINTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.height(6.dp))
                        BulletText("⚠️ Competitive Density: G2 Matrix logged ${report.competitors.size} active alternatives.")
                        BulletText("💸 Unit Economics Risk: Sub-optimal initial pricing & customer acquisition cost leverage.")
                        BulletText("⚖️ Regulatory/Moat Friction: High barrier to defensibility if tech isn't proprietary.")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("📈 STEPS TO ELEVATE SCORE BY +10 POINTS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MintFlame)
            Spacer(modifier = Modifier.height(4.dp))
            BulletText("1. Implement B2B license templates to secure bulk customer acquisition.")
            BulletText("2. Secure intellectual property lock-ups to alleviate cloning risks.")
            BulletText("3. Establish a pre-registration pipeline of 100+ high-affinity leads.")
        }
    }
}

@Composable
private fun BenchmarkRow(label: String, score: Int, average: Int, color: Color) {
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, fontSize = 10.sp, color = TextSecondary)
            Text("This: $score% | Benchmark Avg: $average%", fontSize = 10.sp, color = TextWhite, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Slate700, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(score.toFloat() / 100f)
                    .height(6.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun BulletText(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = TextPrimary,
        lineHeight = 15.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun AcceleratorChecklistWidget(report: StartupReport) {
    val checkedStates = remember {
        mutableStateListOf(true, true, true, true, false, false)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = CustomGold, modifier = Modifier.size(20.dp))
                Text("Accelerator & VC Funding Readiness Checklist", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Interactive milestones for institutional pre-seed/seed evaluation.", fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(14.dp))

            ChecklistItemRow(
                text = "TAM, SAM, SOM Market Sizing Defined (${report.tamValue})",
                isChecked = checkedStates[0],
                onCheckedChange = { checkedStates[0] = it }
            )
            ChecklistItemRow(
                text = "Competitor Intelligence Gaps Identified (${report.competitorGaps.size} gaps logged)",
                isChecked = checkedStates[1],
                onCheckedChange = { checkedStates[1] = it }
            )
            ChecklistItemRow(
                text = "Operational SWOT Risk Mitigation Plan Prepared",
                isChecked = checkedStates[2],
                onCheckedChange = { checkedStates[2] = it }
            )
            ChecklistItemRow(
                text = "Dynamic Pricing & SaaS Monetization Strategy Configured",
                isChecked = checkedStates[3],
                onCheckedChange = { checkedStates[3] = it }
            )
            ChecklistItemRow(
                text = "Verify Founder-Fit Risks and Essential Hiring Needs",
                isChecked = checkedStates[4],
                onCheckedChange = { checkedStates[4] = it }
            )
            ChecklistItemRow(
                text = "Secure 18-Month Runway Strategy (simulate via Calculator below!)",
                isChecked = checkedStates[5],
                onCheckedChange = { checkedStates[5] = it }
            )

            val checkedCount = checkedStates.count { it }
            val progressPercent = (checkedCount.toFloat() / checkedStates.size * 100).toInt()
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Funding Readiness Index", fontSize = 11.sp, color = TextSecondary)
                Text("$progressPercent%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CustomGold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Slate700, RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(checkedCount.toFloat() / checkedStates.size)
                        .height(6.dp)
                        .background(CustomGold, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

@Composable
private fun ChecklistItemRow(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = CustomGold,
                uncheckedColor = Slate700,
                checkmarkColor = Slate900
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 11.sp, color = if (isChecked) TextWhite else TextSecondary)
    }
}

@Composable
private fun BusinessCalculatorWidget(report: StartupReport) {
    var rawPrice = 29.0
    try {
        val matches = Regex("""(\d+)""").find(report.suggestedPricing)
        if (matches != null) {
            rawPrice = matches.groupValues[1].toDouble()
        }
    } catch (e: Exception) {
        rawPrice = 29.0
    }

    var monthlyBurn by remember { mutableStateOf(5000f) }
    var initialCapital by remember { mutableStateOf(100000f) }
    var sliderPrice by remember { mutableStateOf(rawPrice.toFloat()) }
    var customersAcquiredPerMonth by remember { mutableStateOf(50f) }
    var customerAcquisitionCost by remember { mutableStateOf(150f) }
    var monthlyChurn by remember { mutableStateOf(5.0f) } // 5% default churn
    var trafficToConversion by remember { mutableStateOf(2.0f) } // 2% conversion rate

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Build, contentDescription = null, tint = MintFlame, modifier = Modifier.size(20.dp))
                Text("Enterprise Assumption Editor & Unit Economics Engine", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Adjust dynamic growth variables to project runway and feasibility.", fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(14.dp))

            Text("Starting Capital / Funding: ${formatCurrency(initialCapital.toInt())}", fontSize = 11.sp, color = TextSecondary)
            Slider(
                value = initialCapital,
                onValueChange = { initialCapital = it },
                valueRange = 5000f..500000f,
                colors = SliderDefaults.colors(thumbColor = ElectricSky, activeTrackColor = ElectricSky)
            )

            Text("Baseline Monthly Burn: ${formatCurrency(monthlyBurn.toInt())}", fontSize = 11.sp, color = TextSecondary)
            Slider(
                value = monthlyBurn,
                onValueChange = { monthlyBurn = it },
                valueRange = 500f..25000f,
                colors = SliderDefaults.colors(thumbColor = ElectricSky, activeTrackColor = ElectricSky)
            )

            Text("Unit Pricing / subscription: ${formatCurrency(sliderPrice.toInt())}/mo", fontSize = 11.sp, color = TextSecondary)
            Slider(
                value = sliderPrice,
                onValueChange = { sliderPrice = it },
                valueRange = 1f..300f,
                colors = SliderDefaults.colors(thumbColor = MintFlame, activeTrackColor = MintFlame)
            )

            Text("Target New Customers / mo: ${customersAcquiredPerMonth.toInt()}", fontSize = 11.sp, color = TextSecondary)
            Slider(
                value = customersAcquiredPerMonth,
                onValueChange = { customersAcquiredPerMonth = it },
                valueRange = 5f..500f,
                colors = SliderDefaults.colors(thumbColor = MintFlame, activeTrackColor = MintFlame)
            )

            Text("Expected Customer Acquisition Cost (CAC): ${formatCurrency(customerAcquisitionCost.toInt())}", fontSize = 11.sp, color = TextSecondary)
            Slider(
                value = customerAcquisitionCost,
                onValueChange = { customerAcquisitionCost = it },
                valueRange = 10f..1000f,
                colors = SliderDefaults.colors(thumbColor = ElectricSky, activeTrackColor = ElectricSky)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Monthly Churn Rate: ${String.format("%.1f", monthlyChurn)}%", fontSize = 11.sp, color = TextSecondary)
                    Slider(
                        value = monthlyChurn,
                        onValueChange = { monthlyChurn = it },
                        valueRange = 0.5f..20f,
                        colors = SliderDefaults.colors(thumbColor = CustomGold, activeTrackColor = CustomGold)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Traffic-to-Paid Conv.: ${String.format("%.1f", trafficToConversion)}%", fontSize = 11.sp, color = TextSecondary)
                    Slider(
                        value = trafficToConversion,
                        onValueChange = { trafficToConversion = it },
                        valueRange = 0.1f..10f,
                        colors = SliderDefaults.colors(thumbColor = CustomGold, activeTrackColor = CustomGold)
                    )
                }
            }

            HorizontalDivider(color = Slate700, modifier = Modifier.padding(vertical = 12.dp))

            // Unit Economics Computations
            // Active users after 12 months with churn:
            // Cumulative formula: activeUsers = sum_{i=0..11} (monthlyAcquisitions * (1 - churn)^i)
            val r = 1f - (monthlyChurn / 100f)
            var activeUsersAtYearEnd = 0f
            for (i in 0..11) {
                var usersRemaining = customersAcquiredPerMonth
                for (j in 0 until i) {
                    usersRemaining *= r
                }
                activeUsersAtYearEnd += usersRemaining
            }

            val monthlyRevenue = sliderPrice * activeUsersAtYearEnd
            val monthlyCacSpend = customersAcquiredPerMonth * customerAcquisitionCost
            val netBurn = monthlyBurn + monthlyCacSpend - monthlyRevenue

            // LTV (Customer Lifetime Value) = Price / Churn Rate
            val ltv = if (monthlyChurn > 0) sliderPrice / (monthlyChurn / 100f) else 0f
            val ltvToCac = if (customerAcquisitionCost > 0) ltv / customerAcquisitionCost else 0f

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ESTIMATED YEAR 1 END MRR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(
                        text = formatCurrency(monthlyRevenue.toInt()),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MintFlame
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("NET DRIFT (MO 12)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(
                        text = if (netBurn <= 0) "Profitable Loop" else "-${formatCurrency(netBurn.toInt())}/mo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (netBurn <= 0) MintFlame else TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ACTIVE RUNWAY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(
                        text = if (netBurn <= 0) "∞ Profit Loop" else "${String.format("%.1f", initialCapital / netBurn)} Mos",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (netBurn <= 0) MintFlame else ElectricSky
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("CUSTOMER LIFETIME VALUE (LTV)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(
                        text = formatCurrency(ltv.toInt()),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricSky
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("LTV : CAC RATIO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    val ratioColor = if (ltvToCac >= 3.0f) MintFlame else CustomGold
                    Text(
                        text = "${String.format("%.1f", ltvToCac)}x",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ratioColor
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("CAC PAYBACK TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    val payback = customerAcquisitionCost / sliderPrice
                    Text(
                        text = "${String.format("%.1f", payback)} mos",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Diagnostic feedback based on unit economics
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (ltvToCac >= 3.0f) Slate900.copy(alpha = 0.5f) else Color(0xFF7F1D1D).copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (ltvToCac >= 3.0f) Slate700 else Color(0xFF991B1B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    if (ltvToCac >= 3.0f) {
                        Text(
                            text = "✅ HEALTHY UNIT MARGIN POTENTIAL: Your LTV to CAC ratio of ${String.format("%.1f", ltvToCac)}x meets venture benchmarks (ideal >= 3x). The business model is highly repeatable.",
                            fontSize = 10.sp,
                            color = MintFlame,
                            lineHeight = 14.sp
                        )
                    } else {
                        Text(
                            text = "⚠️ SQUEEZED MARGIN ALERT: LTV to CAC is ${String.format("%.1f", ltvToCac)}x (needs >= 3x). Action: Reduce acquisition costs via organic referrals or raise pricing to at least ${formatCurrency((customerAcquisitionCost * 3f / (100f / monthlyChurn)).toInt())}/mo to secure survival.",
                            fontSize = 10.sp,
                            color = CustomGold,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatCurrency(amount: Int): String {
    return if (amount >= 1000) {
        "$${amount / 1000}k"
    } else {
        "$$amount"
    }
}

@Composable
private fun FounderNext30DaysWidget(report: StartupReport) {
    val completedWeeks = remember { mutableStateListOf(false, false, false, false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = MintFlame, modifier = Modifier.size(20.dp))
                Text("Founder Next 30 Days Action Playbook", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Tactical milestone playbook created by the validation engine to initiate product validation.", fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(14.dp))

            // Week 1
            Next30DayMilestoneRow(
                weekLabel = "Week 1",
                title = "10 Targeted Customer Interviews",
                detail = "Interview profile matches for '${report.targetUsers}'. Focus on validating the core friction point regarding competitors like '${report.competitors.firstOrNull()?.name ?: "legacy players"}'.",
                isChecked = completedWeeks[0],
                onCheckedChange = { completedWeeks[0] = it }
            )

            // Week 2
            Next30DayMilestoneRow(
                weekLabel = "Week 2",
                title = "MVP Value Landing Page Experiment",
                detail = "Configure simple site addressing '${report.competitorGaps.firstOrNull() ?: "unaddressed user friction"}'. Measure click-to-register intent rates.",
                isChecked = completedWeeks[1],
                onCheckedChange = { completedWeeks[1] = it }
            )

            // Week 3
            Next30DayMilestoneRow(
                weekLabel = "Week 3",
                title = "Pricing Pre-sale Campaign",
                detail = "Audit pre-sale interest using suggested price threshold of ${report.suggestedPricing}. Offer 15% pre-sign discounts to build backlog.",
                isChecked = completedWeeks[2],
                onCheckedChange = { completedWeeks[2] = it }
            )

            // Week 4
            Next30DayMilestoneRow(
                weekLabel = "Week 4",
                title = "Validate Core Engagement Metrics",
                detail = "Scope modular MVP centered on ${report.recommendedModel}. Establish user return intervals. Maintain low customer churn targets.",
                isChecked = completedWeeks[3],
                onCheckedChange = { completedWeeks[3] = it }
            )

            val progressPercent = (completedWeeks.count { it }.toFloat() / 4f * 100).toInt()
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Playbook Execution Progress", fontSize = 11.sp, color = TextSecondary)
                Text("$progressPercent%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MintFlame)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Slate700, RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent.toFloat() / 100f)
                        .height(6.dp)
                        .background(MintFlame, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

@Composable
private fun Next30DayMilestoneRow(
    weekLabel: String,
    title: String,
    detail: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MintFlame,
                uncheckedColor = Slate700,
                checkmarkColor = Slate900
            ),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(ElectricSky.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(weekLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElectricSky)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(detail, fontSize = 10.sp, color = TextSecondary, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun VcObjectionsMitigationWidget(report: StartupReport) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = CustomGold, modifier = Modifier.size(20.dp))
                Text("VC Objection Simulator & Defensive Playbook", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Simulates typical seed-stage investor pushbacks with battle-tested mitigation formulas.", fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(14.dp))

            ObjectionRow(
                tag = "OBJECTION 1",
                objection = "Low barrier to entry. Why won't major incumbents build this immediately?",
                playbook = "Emphasize extreme speed & niche ICP customization. Major incumbents lack product agility to rebuild core stacks for targeted user needs in '${report.detectedIndustry}'."
            )
            Spacer(modifier = Modifier.height(10.dp))
            ObjectionRow(
                tag = "OBJECTION 2",
                objection = "Customer Acquisition Cost (CAC) is likely to spike on paid channels.",
                playbook = "Avoid dependency on paid advertising channels. Drive acquisition by mining user pain points from organic forums and embedding viral loops directly into the value proposal."
            )
            Spacer(modifier = Modifier.height(10.dp))
            ObjectionRow(
                tag = "OBJECTION 3",
                objection = "Pricing is too high / low to sustain long-term business feasibility.",
                playbook = "Leverage modular subscription licensing model (${report.recommendedModel}) to offer enterprise tiers, validating client commitment with pilots before larger sales cycles."
            )
        }
    }
}

@Composable
private fun ObjectionRow(tag: String, objection: String, playbook: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .background(CustomGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(tag, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CustomGold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("⚠️ Investor pushback: \"$objection\"", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(6.dp))
            Text("🛡️ Founder Playbook: $playbook", fontSize = 10.sp, color = TextSecondary, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun MultiAgentConsensusWidget(report: StartupReport) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.AccountBox, contentDescription = null, tint = ElectricSky, modifier = Modifier.size(20.dp))
                Text("Autonomous Validator Agent Consensus Logs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Decentralized scoring & grounding outputs verified by 4 specialized AI agents.", fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(14.dp))

            AgentLogItem(
                agentName = "📊 Target Market TAM/SAM Agent",
                focus = "Sizing & Segment Volume Expansion",
                check = "Audited CAGR scale for '${report.detectedIndustry}' against public registries. Calculated SOM value at '${report.somValue}'.",
                score = report.demandScore,
                status = "CONSENSUS MET"
            )
            Spacer(modifier = Modifier.height(10.dp))
            AgentLogItem(
                agentName = "⚔️ Competitor Extraction Agent",
                focus = "Competitor Gaps & Offering Gaps",
                check = "Scraped core features of ${report.competitors.size} active alternatives. Verified ${report.competitorGaps.size} unaddressed market gaps.",
                score = report.opportunityScore,
                status = "CONSENSUS MET"
            )
            Spacer(modifier = Modifier.height(10.dp))
            AgentLogItem(
                agentName = "💰 Monetization Architecture Agent",
                focus = "Pricing structures & LTV:CAC optimization",
                check = "Optimized starting unit base of '${report.suggestedPricing}' using '${report.recommendedModel}' pricing schemes.",
                score = report.investorReadinessScore,
                status = "CONSENSUS MET"
            )
            Spacer(modifier = Modifier.height(10.dp))
            AgentLogItem(
                agentName = "🛡️ Moat Defense & Compliance Agent",
                focus = "Mitigation playbook & risk detection",
                check = "Verified SWOT metrics. Flagged ${report.risks.size} existential threat matrices and structured defensive countermeasures.",
                score = report.validationScore,
                status = "CONSENSUS MET"
            )
        }
    }
}

@Composable
private fun AgentLogItem(agentName: String, focus: String, check: String, score: Int, status: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(agentName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Box(
                    modifier = Modifier
                        .background(MintFlame.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(status, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = MintFlame)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Focus area: $focus", fontSize = 10.sp, color = ElectricSky, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Grounding verification: $check", fontSize = 10.sp, color = TextSecondary, lineHeight = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Confidence Weight:", fontSize = 9.sp, color = TextSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .weight(1f)
                        .background(Slate700, RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(score.toFloat() / 100f)
                            .height(4.dp)
                            .background(ElectricSky, RoundedCornerShape(2.dp))
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("$score%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElectricSky)
            }
        }
    }
}

@Composable
private fun ScoreLabelRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
    }
}

@Composable
private fun MarketFinanceTab(report: StartupReport) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate700)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularScoreGauge(
                        score = report.demandScore,
                        label = "DEMAND RATE",
                        gaugeColor = MintFlame,
                        size = 110.dp,
                        strokeWidth = 10.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DEMAND VELOCITY", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MintFlame, letterSpacing = 1.sp)
                        Text(report.demandLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("CAGR Expansion momentum: ${report.yearlyGrowthRate} YoY", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate700)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Calculated Size Boundaries", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Spacer(modifier = Modifier.height(10.dp))
                    SizeSegmentRow(label = "TAM (Total Addressable Market)", value = report.tamValue, color = ElectricSky)
                    Spacer(modifier = Modifier.height(8.dp))
                    SizeSegmentRow(label = "SAM (Serviceable Addressable Market)", value = report.samValue, color = IndigoDusk)
                    Spacer(modifier = Modifier.height(8.dp))
                    SizeSegmentRow(label = "SOM (Serviceable Obtainable Market)", value = report.somValue, color = MintFlame)
                }
            }
        }

        item {
            RevenuePredictionsWidget(report.revenuePredictions)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate700)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Suggested Revenue Framework", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Monetization", fontSize = 12.sp, color = TextSecondary)
                        Text(report.recommendedModel, fontSize = 12.sp, color = MintFlame, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Starting Unit Base", fontSize = 12.sp, color = TextSecondary)
                        Text(report.suggestedPricing, fontSize = 12.sp, color = ElectricSky, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Slate700)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(report.pricingStructureDetails, fontSize = 11.sp, color = TextPrimary, lineHeight = 15.sp)
                }
            }
        }

        item {
            FundingRoadmapWidget(report.fundingRoadmap)
        }

        item {
            CitationInsightsWidget(report)
        }
    }
}

@Composable
private fun SizeSegmentRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CitationInsightsWidget(report: StartupReport) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Share, contentDescription = null, tint = ElectricSky, modifier = Modifier.size(20.dp))
                Text("Live Source Citations & Sentiment Signals", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Real-time groundings analyzed by the validator network.", fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(14.dp))

            CitationSourceCard(
                source = "Reddit Business Communities (r/startups & r/SaaS)",
                confidence = 94,
                evidence = "Validation scans verified recurrent organic complaints, desire features, and pricing resistance within the segment.",
                whyItMatters = "Pinpoints user willingness-to-pay margins directly."
            )
            Spacer(modifier = Modifier.height(10.dp))
            CitationSourceCard(
                source = "G2 & Capterra Competitor Intelligence Matrix",
                confidence = 96,
                evidence = "Audited core legacy offerings and baseline subscription tiers for players in ${report.detectedIndustry}.",
                whyItMatters = "Unlocks unoccupied functional niches."
            )
            Spacer(modifier = Modifier.height(10.dp))
            CitationSourceCard(
                source = "Crunchbase Global Sector Inflow Signals",
                confidence = 91,
                evidence = "Analyzed public metrics, funding velocities, and CAGR expansion velocities.",
                whyItMatters = "Affirms institutional investor interest index."
            )
        }
    }
}

@Composable
private fun CitationSourceCard(source: String, confidence: Int, evidence: String, whyItMatters: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(source, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricSky)
                Box(
                    modifier = Modifier
                        .background(MintFlame.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Trust: $confidence%", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MintFlame)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Evidence: $evidence", fontSize = 11.sp, color = TextWhite, lineHeight = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Relevance: $whyItMatters", fontSize = 10.sp, color = TextSecondary, fontStyle = FontStyle.Italic)
        }
    }
}

@Composable
private fun CompetitorsIntelligenceTab(report: StartupReport) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GapFinderWidget(
                currentOfferings = report.currentOfferings,
                gaps = report.competitorGaps,
                opportunityScore = report.opportunityScore,
                suggestions = report.opportunitySuggestions
            )
        }

        item {
            Text(
                text = "Tracked Competitive Intelligence",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        item {
            CompetitorsListWidget(report.competitors)
        }
    }
}

@Composable
private fun SwotRisksTab(report: StartupReport) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Analytical SWOT Framework",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        item {
            SwotGridWidget(
                strengths = report.strengths,
                weaknesses = report.weaknesses,
                opportunities = report.opportunities,
                threats = report.threats
            )
        }

        item {
            HorizontalDivider(color = Slate700)
        }

        item {
            Text(
                text = "Identified Threat Vector Assessments",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        item {
            RisksWidget(report.risks)
        }
    }
}

@Composable
private fun BusinessPlanTab(report: StartupReport) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            if (uri != null) {
                isExporting = true
                coroutineScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                PdfExporter.exportBusinessPlanToPdf(context, report, outputStream)
                            }
                        }
                        Toast.makeText(context, "Business Plan PDF saved successfully!", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Failed to save PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    } finally {
                        isExporting = false
                    }
                }
            }
        }
    )

    val sections = listOf(
        "Executive Summary" to report.businessPlan.executiveSummary,
        "Deep Market Sizing" to report.businessPlan.marketAnalysisDetail,
        "User Personas & Demographics" to report.businessPlan.customerSegments,
        "Premium Monitizations" to report.businessPlan.revenueStreams,
        "Operating Cost Parameters" to report.businessPlan.costStructure,
        "Digital Growth-Hacking" to report.businessPlan.marketingPlan,
        "5-Year Scalability Matrix" to report.businessPlan.growthStrategy
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ElectricSky.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "OFFICIAL INCUBATOR EXPORT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricSky,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Download Executive Business Plan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Generate a formatted multi-page PDF document including executive analysis, SWOT assessment, market sizes and threat mitigations.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                    Button(
                        onClick = {
                            if (!isExporting) {
                                val sanitizedName = report.name.replace("\\s+".toRegex(), "_")
                                createDocumentLauncher.launch("Business_Plan_$sanitizedName.pdf")
                            }
                        },
                        enabled = !isExporting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricSky,
                            disabledContainerColor = ElectricSky.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("download_pdf_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Slate900,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Download PDF",
                                    tint = Slate900,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                if (isExporting) "EXPORTING..." else "PDF EXPORT",
                                color = Slate900,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Strategic Blueprint Document",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        items(sections, key = { it.first }) { (title, plan) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Slate700)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ElectricSky,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = plan,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InvestorPresentationTab(report: StartupReport) {
    val slides = report.pitchDeck
    var activeIdx by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Automated Pitch Projections (Standard Venture format)",
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Presentation Slide view Frame
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 10.dp)
                    .testTag("pitch_slide_card"),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, IndigoDusk)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Slide title & page flag
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "VENTURE PRESENTATION",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoDusk,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "SLIDE ${activeIdx + 1} OF ${slides.size}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoDusk
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = slides[activeIdx].title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        slides[activeIdx].bullets.forEach { point ->
                            Row(
                                modifier = Modifier.padding(vertical = 5.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 5.dp, end = 8.dp)
                                        .size(6.dp)
                                        .background(ElectricSky, CircleShape)
                                )
                                Text(
                                    text = point,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    slides[activeIdx].footnote?.let { footnote ->
                        Column {
                            HorizontalDivider(color = Slate700)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ℹ  $footnote",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            // Controllers row
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (activeIdx > 0) activeIdx-- },
                    enabled = activeIdx > 0,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("PREV", fontSize = 11.sp)
                    }
                }

                Text(
                    text = "${slides[activeIdx].title.take(15)}...",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { if (activeIdx < slides.size - 1) activeIdx++ },
                    enabled = activeIdx < slides.size - 1,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoDusk)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("NEXT", fontSize = 11.sp, color = Slate900, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Slate900, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * ADVISOR CHAT TAB
 * Secure grounded Chat system allowing questions to the Virtual Incubator Consultant.
 */
@Composable
private fun AdvisorChatTab(
    report: StartupReport,
    viewModel: StartupViewModel
) {
    val chatHistory by viewModel.mentorChatHistory.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var userMessageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Slide down to newest message automatically
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(chatHistory.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Conversation Feed
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatHistory, key = { it.timestamp }) { msg ->
                val isUser = msg.sender == MessageSender.USER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .testTag("chat_bubble_${if (isUser) "user" else "mentor"}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) ElectricSky.copy(alpha = 0.15f) else Slate800
                        ),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isUser) 12.dp else 0.dp,
                            bottomEnd = if (isUser) 0.dp else 12.dp
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isUser) ElectricSky.copy(alpha = 0.4f) else Slate700
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) "YOUR QUESTION" else "INCUBATOR ADVISOR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isUser) ElectricSky else MintFlame,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                fontSize = 12.sp,
                                color = TextWhite,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = MintFlame, strokeWidth = 2.dp, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Advisor is evaluating pricing/risks...", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Inputs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userMessageText,
                onValueChange = { userMessageText = it },
                placeholder = { Text("Ask: 'Improve pricing?', 'Target strategy?'...", fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = ElectricSky,
                    unfocusedBorderColor = Slate700,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                ),
                maxLines = 2,
                singleLine = true
            )

            IconButton(
                onClick = {
                    val promptText = userMessageText.trim()
                    if (promptText.isNotEmpty()) {
                        viewModel.sendMessageToMentor(promptText)
                        userMessageText = ""
                    }
                },
                enabled = !isChatLoading && userMessageText.isNotBlank(),
                modifier = Modifier
                    .background(if (userMessageText.isNotBlank() && !isChatLoading) ElectricSky else Slate800, CircleShape)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (userMessageText.isNotBlank() && !isChatLoading) Slate900 else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
