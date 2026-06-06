package com.example.ui

import android.speech.tts.TextToSpeech
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.*
import java.util.*

fun android.content.Context.findActivity(): android.app.Activity? {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is android.app.Activity) return context
        context = context.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduGuideApp(viewModel: EduViewModel) {
    val context = LocalContext.current
    val currentScreen = viewModel.navigationStack.lastOrNull() ?: Screen.ClassGrid
    val studyPoints by viewModel.studyPoints.collectAsState()
    val isDevConsoleVisible by viewModel.isDeveloperConsoleVisible.collectAsState()
    val interstitialAdState by viewModel.interstitialAdState.collectAsState()
    val showAppOpenAd by viewModel.showAppOpenAd.collectAsState()
    val rewardedAdState by viewModel.rewardedAdState.collectAsState()
    
    // Core event logs from framework
    val rawLogs = remember { mutableStateListOf<AnalyticsEvent>() }
    LaunchedEffect(Unit) {
        AnalyticsAndAdFramework.eventLogs.collect { event ->
            rawLogs.add(event)
        }
    }

    // Handle back presses
    BackHandler(enabled = viewModel.navigationStack.size > 1) {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (currentScreen) {
                        is Screen.ClassGrid -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "EduGuide",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Class 6-12",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        is Screen.SubjectList -> {
                            Column {
                                Text(
                                    text = "Syllabus Units",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = when (currentScreen.classLevel.classNum) {
                                        9 -> "Class 9-10 • English Curriculum"
                                        11 -> "Class 11-12 • English Curriculum"
                                        else -> "Class ${currentScreen.classLevel.classNum} • English Curriculum"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        is Screen.LessonList -> {
                            Column {
                                Text(
                                    text = currentScreen.unit.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = when (currentScreen.classLevel.classNum) {
                                        9 -> "Class 9-10 • ${currentScreen.subject.name}"
                                        11 -> "Class 11-12 • ${currentScreen.subject.name}"
                                        else -> "Class ${currentScreen.classLevel.classNum} • ${currentScreen.subject.name}"
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        is Screen.LessonDetails -> {
                            Column {
                                Text(
                                    text = currentScreen.lesson.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentScreen.unit.name,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        is Screen.BookmarksList -> {
                            Text(
                                text = "My Watchlist Bookmarks",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        is Screen.Settings -> {
                            Text(
                                text = "Preferences & Settings",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        is Screen.PrivacyPolicy -> {
                            Text(
                                text = "Privacy Policy",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        is Screen.TermsOfService -> {
                            Text(
                                text = "Terms of Service",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (viewModel.navigationStack.size > 1) {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back Navigation"
                            )
                        }
                    }
                },
                actions = {
                    if (currentScreen is Screen.LessonDetails) {
                        val bookmarks by viewModel.allBookmarks.collectAsState()
                        val isBookmarked = bookmarks.any { it.lessonId == currentScreen.lesson.id }
                        IconButton(
                            onClick = {
                                viewModel.toggleBookmark(
                                    lessonId = currentScreen.lesson.id,
                                    lessonName = currentScreen.lesson.name,
                                    classNum = currentScreen.classLevel.classNum,
                                    subjectId = currentScreen.subject.id,
                                    subjectName = currentScreen.subject.name
                                )
                            },
                            modifier = Modifier.testTag("bookmark_toggle_details_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Bookmark this lesson",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.BookmarksList) },
                            modifier = Modifier.testTag("bookmarks_nav_button")
                        ) {
                            Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Watchlist Bookmarks")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Settings) },
                        modifier = Modifier.testTag("settings_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "App Settings",
                            tint = if (currentScreen is Screen.Settings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            )
        },
        bottomBar = {
            AdBannerComponent(onAdClicked = {
                viewModel.simulateAdBonusPoints()
                Toast.makeText(context, "🏆 +100 Ad Bonus Points added to checklist rewards!", Toast.LENGTH_LONG).show()
            })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content area with smooth animations
            AnimatedContent(
                targetState = currentScreen,
                label = "ScreenTransition",
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                }
            ) { screen ->
                when (screen) {
                    is Screen.ClassGrid -> ClassGridScreen(viewModel = viewModel, studyPoints = studyPoints)
                    is Screen.SubjectList -> SubjectListScreen(viewModel = viewModel, screen = screen)
                    is Screen.LessonList -> LessonListScreen(viewModel = viewModel, screen = screen)
                    is Screen.LessonDetails -> LessonDetailsScreen(viewModel = viewModel, screen = screen)
                    is Screen.PrivacyPolicy -> PrivacyPolicyScreen(viewModel = viewModel)
                    is Screen.TermsOfService -> TermsOfServiceScreen(viewModel = viewModel)
                    is Screen.BookmarksList -> BookmarksScreen(viewModel = viewModel)
                    is Screen.Settings -> SettingsScreen(viewModel = viewModel)
                }
            }

            // Developer Diagnostics Live Feed Panel (BottomSheet Overlay style)
            AnimatedVisibility(
                visible = isDevConsoleVisible && com.example.BuildConfig.DEBUG,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter)
            ) {
                DeveloperConsoleOverlay(
                    logs = rawLogs,
                    onClose = { viewModel.toggleDeveloperConsole() },
                    onTriggerCrash = {
                        viewModel.triggerSimulatedCrash()
                        Toast.makeText(context, "💥 Custom exception logged in Diagnostics terminal!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Fullscreen real Google AdMob Interstitial Ad placements with fallback
            when (val state = interstitialAdState) {
                is InterstitialState.Loading -> {
                    val activity = context.findActivity()
                    LaunchedEffect(state) {
                        try {
                            com.google.android.gms.ads.interstitial.InterstitialAd.load(
                                context,
                                "ca-app-pub-3940256099942544/1033173712",
                                com.google.android.gms.ads.AdRequest.Builder().build(),
                                object : com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                                    override fun onAdLoaded(ad: com.google.android.gms.ads.interstitial.InterstitialAd) {
                                        super.onAdLoaded(ad)
                                        AnalyticsAndAdFramework.logEvent("AdMob", "InterstitialLoadSuccess", "Real Google Test Interstitial loaded successfully.")
                                        if (activity != null) {
                                            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                                                override fun onAdDismissedFullScreenContent() {
                                                    super.onAdDismissedFullScreenContent()
                                                    viewModel.dismissInterstitialAd()
                                                    viewModel.simulateAdBonusPoints()
                                                }
                                                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                                    super.onAdFailedToShowFullScreenContent(error)
                                                    viewModel.setInterstitialAdVisible()
                                                }
                                            }
                                            ad.show(activity)
                                        } else {
                                            viewModel.setInterstitialAdVisible()
                                        }
                                    }

                                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                                        super.onAdFailedToLoad(error)
                                        AnalyticsAndAdFramework.logEvent("AdMob", "InterstitialLoadFailed", "Code: ${error.code} - ${error.message}")
                                        viewModel.setInterstitialAdVisible()
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            AnalyticsAndAdFramework.logEvent("AdMob", "InterstitialLoadException", e.message ?: "Unknown Thread Error")
                            viewModel.setInterstitialAdVisible()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is InterstitialState.Visible -> {
                    SimulatedInterstitialAdDialog(
                        onDismissRequest = { viewModel.dismissInterstitialAd() },
                        onClaimBonus = {
                            viewModel.simulateAdBonusPoints()
                            Toast.makeText(context, "💎 Bonus Study points claimed!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                InterstitialState.Idle -> {}
            }

            // Fullscreen real Google AdMob Rewarded Ad placement with fallback
            when (val state = rewardedAdState) {
                is RewardedState.Loading -> {
                    val activity = context.findActivity()
                    LaunchedEffect(state) {
                        try {
                            com.google.android.gms.ads.rewarded.RewardedAd.load(
                                context,
                                "ca-app-pub-3940256099942544/5224354917",
                                com.google.android.gms.ads.AdRequest.Builder().build(),
                                object : com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {
                                    override fun onAdLoaded(ad: com.google.android.gms.ads.rewarded.RewardedAd) {
                                        super.onAdLoaded(ad)
                                        AnalyticsAndAdFramework.logEvent("AdMob", "RewardedLoadSuccess", "Real Google Test Rewarded loaded successfully.")
                                        if (activity != null) {
                                            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                                                override fun onAdDismissedFullScreenContent() {
                                                    super.onAdDismissedFullScreenContent()
                                                    // User manually dismissed ad - verify rewarded action complete in onUserEarnedReward callback
                                                }
                                                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                                    super.onAdFailedToShowFullScreenContent(error)
                                                    viewModel.setRewardedAdVisible()
                                                }
                                            }
                                            ad.show(activity) { rewardItem ->
                                                AnalyticsAndAdFramework.logEvent("AdMob", "RewardEarned", "Reward: ${rewardItem.amount} ${rewardItem.type}")
                                                viewModel.dismissRewardedAd(true)
                                            }
                                        } else {
                                            viewModel.setRewardedAdVisible()
                                        }
                                    }

                                    override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                                        super.onAdFailedToLoad(error)
                                        AnalyticsAndAdFramework.logEvent("AdMob", "RewardedLoadFailed", "Code: ${error.code} - ${error.message}")
                                        viewModel.setRewardedAdVisible()
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            AnalyticsAndAdFramework.logEvent("AdMob", "RewardedLoadException", e.message ?: "Unknown Thread Error")
                            viewModel.setRewardedAdVisible()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                }
                is RewardedState.Visible -> {
                    SimulatedRewardedAdDialog(
                        onDismissRequest = { completed ->
                            viewModel.dismissRewardedAd(completed)
                            if (completed) {
                                Toast.makeText(context, "🏆 +150 Points & +3 Stars claimed successfully!", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
                RewardedState.Idle -> {}
            }

            // Real Google AdMob App Open Ad overlay with fallback
            if (showAppOpenAd) {
                var showSimulatedAppOpen by remember { mutableStateOf(false) }
                val activity = context.findActivity()
                LaunchedEffect(Unit) {
                    try {
                        com.google.android.gms.ads.appopen.AppOpenAd.load(
                            context,
                            "ca-app-pub-3940256099942544/9257395921",
                            com.google.android.gms.ads.AdRequest.Builder().build(),
                            object : com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                                override fun onAdLoaded(ad: com.google.android.gms.ads.appopen.AppOpenAd) {
                                    super.onAdLoaded(ad)
                                    AnalyticsAndAdFramework.logEvent("AdMob", "AppOpenLoadSuccess", "Real Google Test App Open loaded successfully.")
                                    if (activity != null) {
                                        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                                            override fun onAdDismissedFullScreenContent() {
                                                super.onAdDismissedFullScreenContent()
                                                viewModel.dismissAppOpenAd()
                                            }
                                            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                                super.onAdFailedToShowFullScreenContent(error)
                                                showSimulatedAppOpen = true
                                            }
                                        }
                                        ad.show(activity)
                                    } else {
                                        showSimulatedAppOpen = true
                                    }
                                }

                                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                                    super.onAdFailedToLoad(error)
                                    AnalyticsAndAdFramework.logEvent("AdMob", "AppOpenLoadFailed", "Code: ${error.code} - ${error.message}")
                                    showSimulatedAppOpen = true
                                }
                            }
                        )
                    } catch (e: Exception) {
                        AnalyticsAndAdFramework.logEvent("AdMob", "AppOpenException", e.message ?: "Unknown Exception")
                        showSimulatedAppOpen = true
                    }
                }

                if (showSimulatedAppOpen) {
                    SimulatedAppOpenAd(
                        onDismiss = { viewModel.dismissAppOpenAd() }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// 1. GRID SCREEN (Class 6 to 12 selector)
@Composable
fun ClassGridScreen(viewModel: EduViewModel, studyPoints: StudyPoints) {
    val classes by viewModel.classLevels.collectAsState()
    val progressList by viewModel.allProgress.collectAsState()
    val bookmarks by viewModel.allBookmarks.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("class_grid_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hi Study Buddy! 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Select your Class guidebook to begin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            StudyPointsWidget(studyPoints = studyPoints)
        }

        item {
            NativeAdvancedAdCard()
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Checklist Study Analytics",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(text = "✓ Completed: ${progressList.size}", fontSize = 12.sp)
                            Text(text = "★ Bookmarked: ${bookmarks.size}", fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = { viewModel.navigateTo(Screen.BookmarksList) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("Bookmarks", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Text(
                text = "Academic Guidebooks (6 to 12)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Beautiful Grid of class levels
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .height(380.dp)
                    .fillMaxWidth()
            ) {
                items(classes) { classLevel ->
                    val gradient = getClassGradient(classLevel.classNum)
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .testTag("class_card_${classLevel.classNum}")
                            .clickable {
                                viewModel.navigateTo(Screen.SubjectList(classLevel))
                            },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(gradient)
                                .padding(12.dp)
                        ) {
                            // Bottom right background emblem
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (classLevel.classNum) {
                                        9 -> "9-10"
                                        11 -> "11-12"
                                        else -> "${classLevel.classNum}"
                                    },
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }

                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = classLevel.className,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = classLevel.description,
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { viewModel.navigateTo(Screen.PrivacyPolicy) }) {
                    Text("Privacy Policy", fontSize = 12.sp)
                }
                Text(text = "•", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                TextButton(onClick = { viewModel.navigateTo(Screen.TermsOfService) }) {
                    Text("Terms of Service", fontSize = 12.sp)
                }
            }
        }
    }
}

// 2. SUBJECT & UNIT LIST SCREEN (Syllabus Units Guidebook)
@Composable
fun SubjectListScreen(viewModel: EduViewModel, screen: Screen.SubjectList) {
    val progressList by viewModel.allProgress.collectAsState()
    val englishSubject = screen.classLevel.subjects.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("subject_list_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (englishSubject == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "No English syllabus content loaded for this class.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        } else {
            items(englishSubject.units) { unit ->
                // Calculate progress checklist percentage for THIS specific Unit
                val unitLessons = unit.lessons
                val completedInUnit = unitLessons.count { lesson ->
                    progressList.any { it.lessonId == lesson.id && it.completed }
                }
                val percent = if (unitLessons.isNotEmpty()) {
                    (completedInUnit.toFloat() / unitLessons.size)
                } else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unit_card_${unit.id}")
                        .clickable {
                            viewModel.navigateTo(Screen.LessonList(screen.classLevel, englishSubject, unit))
                        },
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(45.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Unit",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = unit.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${unitLessons.size} English Lessons",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Open Lesson list"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Unit-level Progress indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Unit Checklist progress: $completedInUnit/${unitLessons.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${(percent * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { percent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

// 3. LESSON LIST SCREEN
@Composable
fun LessonListScreen(viewModel: EduViewModel, screen: Screen.LessonList) {
    val progressList by viewModel.allProgress.collectAsState()
    val bookmarks by viewModel.allBookmarks.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("lesson_list_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Lessons in this syllabus unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column {
                    screen.unit.lessons.forEachIndexed { index, lesson ->
                        val isCompleted = progressList.any { it.lessonId == lesson.id && it.completed }
                        val isBookmarked = bookmarks.any { it.lessonId == lesson.id }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.navigateTo(
                                        Screen.LessonDetails(
                                            classLevel = screen.classLevel,
                                            subject = screen.subject,
                                            unit = screen.unit,
                                            lesson = lesson
                                        )
                                    )
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox to complete checklist directly
                            Checkbox(
                                checked = isCompleted,
                                onCheckedChange = {
                                    viewModel.toggleLessonProgress(lesson.id, screen.classLevel.classNum, screen.subject.id)
                                },
                                modifier = Modifier.testTag("checkbox_${lesson.id}")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lesson.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap to load interactive guidebook WebView",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }

                            // Bookmark trigger
                            IconButton(onClick = {
                                viewModel.toggleBookmark(
                                    lessonId = lesson.id,
                                    lessonName = lesson.name,
                                    classNum = screen.classLevel.classNum,
                                    subjectId = screen.subject.id,
                                    subjectName = screen.subject.name
                                )
                            }) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Save Lesson",
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        if (index < screen.unit.lessons.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

// 4. LESSON VIEW SCREEN (Featuring custom interactive Android Webview & Local Speech Engine)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonDetailsScreen(viewModel: EduViewModel, screen: Screen.LessonDetails) {
    val progressList by viewModel.allProgress.collectAsState()
    val bookmarks by viewModel.allBookmarks.collectAsState()

    val context = LocalContext.current
    val isCompleted = progressList.any { it.lessonId == screen.lesson.id && it.completed }
    val isBookmarked = bookmarks.any { it.lessonId == screen.lesson.id }

    var fontSizeMultiplier by remember { mutableStateOf(1.0f) }
    val isDarkTheme = isSystemInDarkTheme()

    // Setup Text-to-Speech Accessibility feature!
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    var showFabMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("lesson_webview_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Custom guidebook actions (FontSize, Text-To-Speech)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Text Size:", style = MaterialTheme.typography.labelSmall)
                    TextButton(
                        onClick = { fontSizeMultiplier = 0.85f },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("A-", fontSize = 11.sp, fontWeight = if (fontSizeMultiplier == 0.85f) FontWeight.Bold else FontWeight.Normal)
                    }
                    TextButton(
                        onClick = { fontSizeMultiplier = 1.0f },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("A", fontSize = 13.sp, fontWeight = if (fontSizeMultiplier == 1.0f) FontWeight.Bold else FontWeight.Normal)
                    }
                    TextButton(
                        onClick = { fontSizeMultiplier = 1.3f },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("A+", fontSize = 16.sp, fontWeight = if (fontSizeMultiplier == 1.3f) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                // Speak Lesson button
                FilledTonalButton(
                    onClick = {
                        if (isSpeaking) {
                            tts?.stop()
                            isSpeaking = false
                            AnalyticsAndAdFramework.logEvent("Accessibility", "TTSStop", "Voice feedback paused")
                        } else {
                            // Extract plain text tags from simple HTML
                            val cleanText = screen.lesson.content
                                .replace(Regex("<[^>]*>"), "")
                                .replace("&rarr;", " yields ")
                                .replace("&sigma;", " sigma ")
                                .replace("&sum;", " sum of ")
                                .replace("<sub>", " ")
                                .replace("</sub>", " ")
                            
                            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
                            isSpeaking = true
                            AnalyticsAndAdFramework.logEvent("Accessibility", "TTSSpeak", "Speech reader started for: '${screen.lesson.name}'")
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = "Speak screen",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isSpeaking) "Pause Voice" else "Read Aloud", fontSize = 11.sp)
                }
            }

            // Active Interactive WebView component
            Box(modifier = Modifier.weight(1f)) {
                EduWebView(
                    htmlContent = screen.lesson.content,
                    fontSizeMultiplier = fontSizeMultiplier,
                    isDarkEnabled = isDarkTheme,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Floating Action Button with study options panel
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = showFabMenu,
                    enter = fadeIn(animationSpec = tween(150)) + expandVertically(expandFrom = Alignment.Bottom),
                    exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(shrinkTowards = Alignment.Bottom)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .width(220.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Study Progress",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            
                            // "Mark as Read" action option inside floating menu
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        showFabMenu = false
                                        viewModel.toggleLessonProgress(screen.lesson.id, screen.classLevel.classNum, screen.subject.id)
                                        Toast.makeText(
                                            context,
                                            if (!isCompleted) "🎉 Awesome! +50 Points & 1 Star added to checklist!" else "Lesson progress reset.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .background(if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Done,
                                    contentDescription = "Status check icon",
                                    tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isCompleted) "Completed" else "Mark as Read",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Watchlist Bookmark item inside floating options menu
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        showFabMenu = false
                                        viewModel.toggleBookmark(
                                            lessonId = screen.lesson.id,
                                            lessonName = screen.lesson.name,
                                            classNum = screen.classLevel.classNum,
                                            subjectId = screen.subject.id,
                                            subjectName = screen.subject.name
                                        )
                                    }
                                    .background(if (isBookmarked) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Watchlist status icon",
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isBookmarked) "Saved to Watch" else "Add Bookmark",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Primary Floating Action Button
                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    contentColor = if (isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.testTag("complete_lesson_action_fab")
                ) {
                    Icon(
                        imageVector = if (showFabMenu) Icons.Default.Close else if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = "Options menu controller"
                    )
                }
            }
        }
    }
}

// Interactive WebView Composable using custom css bridging for optimal responsive typography layout
@Composable
fun EduWebView(
    htmlContent: String,
    fontSizeMultiplier: Float,
    isDarkEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                setBackgroundColor(0)
            }
        },
        update = { webView ->
            val textThemeColor = if (isDarkEnabled) "#e3e3e3" else "#1c1b1f"
            val bgThemeColor = if (isDarkEnabled) "#121212" else "#fafdf6"
            val linkThemeColor = if (isDarkEnabled) "#60a5fa" else "#2563eb"
            val blockBackground = if (isDarkEnabled) "#1e293b" else "#f1f5f9"
            val accentCardBg = if (isDarkEnabled) "#1e1b4b" else "#e0e7ff"
            val accentCardText = if (isDarkEnabled) "#c7d2fe" else "#312e81"

            val fullyStyledHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: system-ui, -apple-system, sans-serif;
                        font-size: ${16 * fontSizeMultiplier}px;
                        line-height: 1.6;
                        color: $textThemeColor;
                        background-color: $bgThemeColor;
                        padding: 16px;
                        margin: 0;
                    }
                    h2 {
                        font-size: 1.4rem;
                        font-weight: 800;
                        color: $linkThemeColor;
                        margin-top: 0;
                        margin-bottom: 12px;
                    }
                    h3 {
                        font-size: 1.15rem;
                        font-weight: 700;
                        margin-top: 20px;
                        margin-bottom: 8px;
                    }
                    p, li {
                        margin-bottom: 12px;
                    }
                    .tip {
                        background-color: $accentCardBg;
                        color: $accentCardText;
                        border-left: 4px solid $linkThemeColor;
                        padding: 14px;
                        border-radius: 8px;
                        margin: 16px 0;
                        font-size: 0.95rem;
                    }
                    .formula {
                        background-color: $blockBackground;
                        font-family: "Courier New", Courier, monospace;
                        font-size: 1.1rem;
                        font-weight: bold;
                        padding: 14px;
                        text-align: center;
                        border-radius: 8px;
                        margin: 16px 0;
                        border: 1px solid rgba(128,128,128,0.2);
                        overflow-x: auto;
                    }
                    pre {
                        background-color: $blockBackground;
                        padding: 12px;
                        border-radius: 8px;
                        overflow-x: auto;
                        font-size: 0.9rem;
                    }
                    code {
                        font-family: inherit;
                        font-weight: bold;
                    }
                    ul {
                        padding-left: 20px;
                    }
                </style>
                </head>
                <body>
                    $htmlContent
                </body>
                </html>
            """.trimIndent()
            
            webView.loadDataWithBaseURL(null, fullyStyledHtml, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

// 5. BOOKMARKS/WATCHLIST SCREEN
@Composable
fun BookmarksScreen(viewModel: EduViewModel) {
    val bookmarks by viewModel.allBookmarks.collectAsState()

    val bookmarksContext = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bookmarks_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.triggerRewardedAd {
                            Toast.makeText(bookmarksContext, "🎉 You have been credited +150 Points and +3 Stars booster!", Toast.LENGTH_LONG).show()
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Points Booster", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CLAIM REWARDS BOOSTER!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Play a quick 5-second AdMob sponsor video to claim +150 points and +3 stars balance booster instantly!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (bookmarks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Empty Bookmarks",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No saved lessons yet!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap the bookmark icon in the guidebook while reading to save it here for offline revision.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
        } else {
            items(bookmarks) { bookmark ->
                val currentClasses by viewModel.classLevels.collectAsState()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Find matching elements in the preloaded syllabus structural list and navigate directly there!
                            val targetClass = currentClasses.find { it.classNum == bookmark.classNum }
                            val targetSub = targetClass?.subjects?.find { it.id == bookmark.subjectId }
                            val targetUnit = targetSub?.units?.find { uu -> uu.lessons.any { ll -> ll.id == bookmark.lessonId } }
                            val targetLesson = targetUnit?.lessons?.find { it.id == bookmark.lessonId }

                            if (targetClass != null && targetSub != null && targetUnit != null && targetLesson != null) {
                                viewModel.navigateTo(
                                    Screen.LessonDetails(
                                        classLevel = targetClass,
                                        subject = targetSub,
                                        unit = targetUnit,
                                        lesson = targetLesson
                                    )
                                )
                            } else {
                                LogEventToFramework("NavigationError", "Failed to resolve references for Bookmark: ${bookmark.lessonName}")
                            }
                        },
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Bookmark Active",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bookmark.lessonName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (bookmark.classNum) {
                                    9 -> "Class 9-10 • ${bookmark.subjectName}"
                                    11 -> "Class 11-12 • ${bookmark.subjectName}"
                                    else -> "Class ${bookmark.classNum} • ${bookmark.subjectName}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        IconButton(onClick = {
                            viewModel.toggleBookmark(
                                lessonId = bookmark.lessonId,
                                lessonName = bookmark.lessonName,
                                classNum = bookmark.classNum,
                                subjectId = bookmark.subjectId,
                                subjectName = bookmark.subjectName
                            )
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete bookmark")
                        }
                    }
                }
            }
        }
    }
}

// 6. PRIVACY POLICY COMPLIANCE VIEW
@Composable
fun PrivacyPolicyScreen(viewModel: EduViewModel) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("privacy_policy_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GDPR, COPPA & Play Store Compliance Policy",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Last Updated: June 2026. This Privacy Policy details the data management protocols used by EduGuide for students in Classes 6 through 12.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text(
                        text = "1. Student Data Protection Law (COPPA/GDPR)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "EduGuide is dedicated protecting children's privacy online. We do NOT harvest or collect personal identifiable information (PII) from schools, students or individual devices. All bookmarks, point progression matrices, and completions are stored strictly offline on your physical hardware.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "2. Ads Placements & Google AdMob SDK",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "To fund this free workbook curriculum, we render Material-engineered sponsored contextual ad banners and interstitial breaks. These ad mechanisms utilize safe frameworks complying with COPPA's targeted kid-safe directory restrictions.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "3. Facebook SDK Promotion & SDK Hooks",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "We utilize Facebook SDK triggers to safely compile app promotion campaigns. None of the curriculum results, reading text or bookmarks are reported. Events compiled include app startup metrics and course lesson selection metrics.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Button(
                onClick = { uriHandler.openUri("https://khairulkabir.dev/eduguide/privacy") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Web Link")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Complete Web Version")
            }
        }
    }
}

// 7. TERMS OF SERVICE VIEW
@Composable
fun TermsOfServiceScreen(viewModel: EduViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tos_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Usage Guidelines",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "By accessing any syllabus, textbooks, questions, or checklists on EduGuide, kids and parents agree to utilize information solely for supportive studying. No commercial distribution is permitted without official authority.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Play Credits & Rewards",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "All study points, level checkmarks, and custom reward elements are offline virtual entities. They possess no financial value and are kept to encourage positive academic habits on the device.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// Custom decorative Canvas drawings mapping educational vectors to resolve core material lacks safely
@Composable
fun CustomSubjectIcon(iconKey: String) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        
        when (iconKey) {
            "Calculate" -> {
                // Draw a Math Plus and Equal sign cleanly
                val p = Path().apply {
                    // Plus sign
                    moveTo(w*0.35f, h*0.25f)
                    lineTo(w*0.35f, h*0.45f)
                    moveTo(w*0.25f, h*0.35f)
                    lineTo(w*0.45f, h*0.35f)
                    // Equals sign
                    moveTo(w*0.55f, h*0.3f)
                    lineTo(w*0.75f, h*0.3f)
                    moveTo(w*0.55f, h*0.4f)
                    lineTo(w*0.75f, h*0.4f)
                }
                drawPath(p, Color(0xFFE91E63), style = Stroke(width = 3.dp.toPx()))
            }
            "Science", "Biotech" -> {
                // Chemical Erlenmeyer Flask
                val p = Path().apply {
                    moveTo(w*0.4f, h*0.2f)
                    lineTo(w*0.6f, h*0.2f)
                    moveTo(w*0.5f, h*0.2f)
                    lineTo(w*0.5f, h*0.4f)
                    lineTo(w*0.25f, h*0.8f)
                    lineTo(w*0.75f, h*0.8f)
                    lineTo(w*0.5f, h*0.4f)
                }
                drawPath(p, Color(0xFF009688), style = Stroke(width = 2.5.dp.toPx()))
            }
            "Functions" -> {
                // Curves representing function coordinates
                val p = Path().apply {
                    moveTo(w*0.15f, h*0.8f)
                    quadraticTo(w*0.5f, h*0.15f, w*0.85f, h*0.8f)
                }
                drawPath(p, Color(0xFF9C27B0), style = Stroke(width = 3.dp.toPx()))
            }
            "Park" -> {
                // Plant Leaves silhouette representation
                val p = Path().apply {
                    moveTo(w*0.5f, h*0.8f)
                    lineTo(w*0.5f, h*0.25f)
                    quadraticTo(w*0.3f, h*0.4f, w*0.5f, h*0.6f)
                    quadraticTo(w*0.7f, h*0.4f, w*0.5f, h*0.6f)
                }
                drawPath(p, Color(0xFF4CAF50), style = Stroke(width = 3.dp.toPx()))
            }
            "Terminal" -> {
                // Prompt > symbol
                val p = Path().apply {
                    moveTo(w*0.3f, h*0.3f)
                    lineTo(w*0.6f, h*0.5f)
                    lineTo(w*0.3f, h*0.7f)
                }
                drawPath(p, Color(0xFF3F51B5), style = Stroke(width = 3.dp.toPx()))
            }
            "Memory", "Bolt" -> {
                // Electron lightning bolt
                val p = Path().apply {
                    moveTo(w*0.55f, h*0.15f)
                    lineTo(w*0.3f, h*0.55f)
                    lineTo(w*0.55f, h*0.55f)
                    lineTo(w*0.45f, h*0.85f)
                }
                drawPath(p, Color(0xFFFF9800), style = Stroke(width = 3.dp.toPx()))
            }
            "English" -> {
                // Book line sketch default in beautiful Slate/Accent color
                val p = Path().apply {
                    moveTo(w*0.2f, h*0.2f)
                    lineTo(w*0.8f, h*0.2f)
                    lineTo(w*0.8f, h*0.8f)
                    lineTo(w*0.2f, h*0.8f)
                    close()
                    moveTo(w*0.35f, h*0.4f)
                    lineTo(w*0.65f, h*0.4f)
                    moveTo(w*0.35f, h*0.55f)
                    lineTo(w*0.65f, h*0.55f)
                }
                drawPath(p, Color(0xFF6366F1), style = Stroke(width = 3.dp.toPx()))
            }
            else -> {
                // Book line sketch default
                val p = Path().apply {
                    moveTo(w*0.2f, h*0.2f)
                    lineTo(w*0.8f, h*0.2f)
                    lineTo(w*0.8f, h*0.8f)
                    lineTo(w*0.2f, h*0.8f)
                    close()
                    moveTo(w*0.35f, h*0.5f)
                    lineTo(w*0.65f, h*0.5f)
                }
                drawPath(p, Color(0xFF607D8B), style = Stroke(width = 2.5.dp.toPx()))
            }
        }
    }
}

// Visual color coordination for Class cards
fun getClassGradient(classNum: Int): Brush {
    val colors = when (classNum) {
        6 -> listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)) // Blue
        7 -> listOf(Color(0xFF10B981), Color(0xFF047857)) // Emerald Green
        8 -> listOf(Color(0xFFF59E0B), Color(0xFFB45309)) // Warm Amber
        9 -> listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)) // Purple
        10 -> listOf(Color(0xFFEC4899), Color(0xFFBE185D)) // Pink
        11 -> listOf(Color(0xFF06B6D4), Color(0xFF0891B2)) // Cyan
        12 -> listOf(Color(0xFF3D52D5), Color(0xFF090C9B)) // Royal Indigo
        else -> listOf(Color(0xFF6B7280), Color(0xFF374151)) // Gray
    }
    return Brush.linearGradient(colors)
}

fun LogEventToFramework(name: String, msg: String) {
    AnalyticsAndAdFramework.logEvent("EduError", name, msg)
}

// 8. APP SETTINGS SCREEN
@Composable
fun SettingsScreen(viewModel: EduViewModel) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isDevConsoleVisible by viewModel.isDeveloperConsoleVisible.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    var ratingState by remember { mutableStateOf(0) }
    var updateState by remember { mutableStateOf<String?>(null) } // null, "checking", "latest"

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About EduGuide", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "EduGuide is a comprehensive personal high-school guidebook built with Material Design 3. " +
                    "It offers rapid, high-retention offline learning checklists, prime diagnostics, and syllabus structures designed by education professionals.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }

    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("Rate EduGuide App", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("We'd love to hear your feedback! Tap to rate:", textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { ratingState = star }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star $star",
                                    tint = if (star <= ratingState) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRateDialog = false
                        Toast.makeText(context, "Thank you for rating us $ratingState stars!", Toast.LENGTH_SHORT).show()
                        AnalyticsAndAdFramework.logEvent("Settings", "AppRated", "Rated: $ratingState stars")
                    },
                    enabled = ratingState > 0
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (updateState != null) {
        AlertDialog(
            onDismissRequest = { if (updateState != "checking") updateState = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (updateState == "checking") Icons.Default.Refresh else Icons.Default.CheckCircle,
                        contentDescription = "Status",
                        tint = if (updateState == "checking") MaterialTheme.colorScheme.primary else Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (updateState == "checking") "Checking for updates" else "System is Up to Date")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (updateState == "checking") {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Connecting to Play Store servers...", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    } else {
                        Text("You are currently running the latest official version of EduGuide (v1.4.2).\nNo updates available at this time.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                if (updateState == "latest") {
                    TextButton(onClick = { updateState = null }) { Text("Done") }
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Preferences & Diagnostics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Night vs Dark theme Toggle (Requested: "in settings/sesstion use day and dark mode")
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Theme Icon",
                            tint = if (isDarkTheme) Color(0xFF818CF8) else Color(0xFFFFB300)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Theme Selection", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(if (isDarkTheme) "Dark Space Theme" else "Day Light Theme", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.toggleTheme() }
                    )
                }
            }
        }

        // Diagnostics Panel toggle (moving it here so it remains fully available!)
        if (com.example.BuildConfig.DEBUG) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Events Console",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Real-time Diagnostics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("Monitor Firebase/AdMob events", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                            }
                        }
                        Switch(
                            checked = isDevConsoleVisible,
                            onCheckedChange = { viewModel.toggleDeveloperConsole() }
                        )
                    }
                }
            }

            item {
                var showAdMobRef by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "AdMob Reference",
                                    tint = Color(0xFFFFB300)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("AdMob Demo Code Guide", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text("Official test IDs & code structures", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                }
                            }
                            TextButton(onClick = { showAdMobRef = !showAdMobRef }) {
                                Text(if (showAdMobRef) "Hide" else "Show Code")
                            }
                        }
                        
                        if (showAdMobRef) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("Google Test Ad Unit IDs:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.foundation.text.selection.SelectionContainer {
                                Column(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(8.dp).fillMaxWidth()) {
                                    Text("Banner ID:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    Text("ca-app-pub-3940256099942544/6300978111", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Interstitial ID:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    Text("ca-app-pub-3940256099942544/1033173712", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Rewarded Video ID:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    Text("ca-app-pub-3940256099942544/5224354917", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("1. Initialize Mobile Ads:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.foundation.text.selection.SelectionContainer {
                                Text(
                                    text = "MobileAds.initialize(context) { status ->\n    Log.d(\"AdMob\", \"AdMob SDK Initialized successfully!\")\n}",
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(8.dp).fillMaxWidth(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("2. Programmatic Banner Layout:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.foundation.text.selection.SelectionContainer {
                                Text(
                                    text = "val adView = AdView(context).apply {\n    adUnitId = \"ca-app-pub-3940256099942544/6300978111\"\n    setAdSize(AdSize.BANNER)\n}\nadView.loadAd(AdRequest.Builder().build())",
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(8.dp).fillMaxWidth(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Application Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // About EduGuide
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAboutDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "About icon", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("About EduGuide", fontWeight = FontWeight.Bold)
                        Text("General description & educational vision", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // Privacy Policy
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.PrivacyPolicy) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Privacy icon", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Privacy Policy", fontWeight = FontWeight.Bold)
                        Text("Information protection & GDPR consent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // Update App
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        updateState = "checking"
                        AnalyticsAndAdFramework.logEvent("Settings", "UpdateChecked", "Initiated update check simulation.")
                        scope.launch {
                            delay(1200)
                            updateState = "latest"
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Update icon", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Update Application", fontWeight = FontWeight.Bold)
                        Text("Check official servers for updates", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // Rate App
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRateDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Rate icon", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Rate Application", fontWeight = FontWeight.Bold)
                        Text("Tell us how much you like EduGuide!", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // More App for Same Dev (Requested: "more app for same dev")
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Companion Apps from Same Developer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple("GyanSathi Board Prep", "Matric Board exams complete questions & syllabus guide.", "Board Exams"),
                    Triple("GrammarHub Expert", "Deep dive into language syntax, nouns, and composition rules.", "Language Prep"),
                    Triple("PyCode Master", "Learn Python, object oriented structures and logic flows offline.", "Computer Science")
                ).forEach { partner ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            Toast.makeText(context, "Opening Play Store for ${partner.first}...", Toast.LENGTH_SHORT).show()
                            AnalyticsAndAdFramework.logEvent("Companion", "AppOpened", "Opened details for: ${partner.first}")
                            uriHandler.openUri("https://khairulkabir.dev/eduguide/apps")
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(partner.third.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(partner.first, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(partner.second, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                Toast.makeText(context, "Installing ${partner.first}...", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Install")
                            }
                        }
                    }
                }
            }
        }
    }
}

// 9. ADMOB NATIVE ADVANCED CARD COMPONENT
@Composable
fun NativeAdvancedAdCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showRealAd by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Ad", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AdMob Native Advanced Sponsor", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Icon(imageVector = Icons.Default.Info, contentDescription = "Ad Info", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp, max = 260.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.ui.viewinterop.AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        com.google.android.gms.ads.AdView(ctx).apply {
                            adUnitId = "ca-app-pub-3940256099942544/6300978111"
                            setAdSize(com.google.android.gms.ads.AdSize.MEDIUM_RECTANGLE)
                            adListener = object : com.google.android.gms.ads.AdListener() {
                                override fun onAdLoaded() {
                                    super.onAdLoaded()
                                    showRealAd = true
                                    AnalyticsAndAdFramework.logEvent("AdMob", "NativeBannerLoaded", "Real Google Test Medium Banner loaded inside Native Card")
                                }
                                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                                    super.onAdFailedToLoad(error)
                                    showRealAd = false
                                    AnalyticsAndAdFramework.logEvent("AdMob", "NativeBannerFailed", "Error Code: ${error.code} - ${error.message}")
                                }
                            }
                            val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
                            loadAd(adRequest)
                        }
                    }
                )
            }
            
            if (!showRealAd) {
                Spacer(modifier = Modifier.height(10.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Sponsor Logo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Codex IDE - Premium Editor on Mobile", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Write clean apps on-the-go with full autocompletion.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "No subscription required. Offline-ready.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            Toast.makeText(context, "Redirecting to Codex Play Store details...", Toast.LENGTH_SHORT).show()
                            AnalyticsAndAdFramework.logAdMobEvent("ActiveScreen", "NativeAdvanced", "Clicked")
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("Install Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 10. ADMOB REWARDED INTERSTITIAL DIALOG
@Composable
fun SimulatedRewardedAdDialog(
    onDismissRequest: (completed: Boolean) -> Unit
) {
    var timerSeconds by remember { mutableStateOf(5) }
    
    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            timerSeconds--
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (timerSeconds == 0) {
                onDismissRequest(true)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "AdMob Rewarded Interstitial",
                    tint = Color(0xFF009688),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("AdMob Rewarded Video Break")
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF3B82F6),
                                    Color(0xFF818CF8),
                                    Color(0xFF0F172A)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Diamond Reward",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rewarding in $timerSeconds seconds...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Course Sponsor: Peak Prep Academy",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (timerSeconds > 0) "Please watch the entire ad to claim your +150 Points and +3 Gold Stars bonus!" else "Video complete! You have earned your study rewards!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismissRequest(true) },
                enabled = timerSeconds == 0
            ) {
                Text("Claim +150 Points Reward 🎉")
            }
        },
        dismissButton = {
            if (timerSeconds > 0) {
                TextButton(onClick = { onDismissRequest(false) }) {
                    Text("Close (Lose Rewards)", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

// 11. ADMOB APP OPEN AD COMPONENT
@Composable
fun SimulatedAppOpenAd(onDismiss: () -> Unit) {
    var timerSeconds by remember { mutableStateOf(3) }
    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            timerSeconds--
        }
        onDismiss()
    }
    
    Card(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_open_ad_card"),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Ad", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                
                TextButton(onClick = onDismiss, modifier = Modifier.testTag("skip_app_open_ad")) {
                    Text(if (timerSeconds > 0) "Skip in ${timerSeconds}s" else "Skip Ad ➔", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "AdMob App Open Ad",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Google AdMob App Open Ad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Welcome back to EduGuide! This card loads live whenever the app enters focus.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text("SPONSOR PROMOTION", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Learn 10x Faster with Duolingo Plus", fontWeight = FontWeight.Black, fontSize = 18.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("AdMob standard App Open frame representation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.weight(1.5f))
            
            Text(
                text = "EduGuide Offline Guidebook",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
