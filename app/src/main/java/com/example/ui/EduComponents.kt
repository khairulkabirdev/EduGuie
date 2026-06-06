package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AnalyticsAndAdFramework
import com.example.data.AnalyticsEvent
import com.example.data.StudyPoints
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyPointsWidget(
    studyPoints: StudyPoints,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("study_points_card")
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "My Rewards Balance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "${studyPoints.totalPoints} PTS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                        CircleShape
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Stars",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${studyPoints.starsEarned}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun AdBannerComponent(
    onAdClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var adLoadStatus by remember { mutableStateOf("Requesting real Google AdMob Test Banner...") }
    var showRealAd by remember { mutableStateOf(false) }

    val adSponsors = listOf(
        Pair("Sponsor: Olympiad Math Challenge 2026", "Compete & win scholarships! Click to join."),
        Pair("Sponsor: Brainly Smart Tutor AI", "Need homework help? Get instantly connected."),
        Pair("Sponsor: Learn Kotlin and Jetpack Compose", "Build games and real android apps. FREE courses."),
        Pair("Sponsor: Study Abroad Prep", "Class 10-12 foundation tests are open. Click to explore.")
    )

    var currentAdIndex by remember { mutableStateOf(0) }
    
    // Smoothly rotate fallback ads during session
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(12000)
            currentAdIndex = (currentAdIndex + 1) % adSponsors.size
        }
    }

    val activeAd = adSponsors[currentAdIndex]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner")
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Real google test banner integration
            androidx.compose.ui.viewinterop.AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                factory = { context ->
                    com.google.android.gms.ads.AdView(context).apply {
                        // Official AdMob Banner test placement ID
                        adUnitId = "ca-app-pub-3940256099942544/6300978111"
                        setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                        adListener = object : com.google.android.gms.ads.AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                showRealAd = true
                                adLoadStatus = "Real AdMob test ad loaded successfully!"
                                AnalyticsAndAdFramework.logEvent("AdMob", "BannerLoaded", "Successfully loaded Google Test Banner.")
                            }
                            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                                super.onAdFailedToLoad(error)
                                showRealAd = false
                                adLoadStatus = "AdMob load missed (rendering fallback): ${error.message}"
                                AnalyticsAndAdFramework.logEvent("AdMob", "BannerFailed", "Error Code: ${error.code} - ${error.message}")
                            }
                            override fun onAdClicked() {
                                super.onAdClicked()
                                AnalyticsAndAdFramework.logEvent("AdMob", "BannerClicked", "Navigating Google Test Ad redirect.")
                                onAdClicked()
                            }
                        }
                        loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
                    }
                },
                update = { adView ->
                    // No programmatic update required
                }
            )

            // Dynamic debug line to let the developer verify loader behavior in real-time
            if (com.example.BuildConfig.DEBUG) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Real AdMob: $adLoadStatus",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (showRealAd) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .background(if (showRealAd) Color(0xFF4CAF50) else Color(0xFFFF9800), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (showRealAd) "LIVE TEST AD" else "FALLBACK BANNER",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Fallback display if AdMob package hasn't resolved / finished loading
            if (!showRealAd) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            AnalyticsAndAdFramework.logEvent("AdMob", "FallbackAdClicked", "Clicked: ${activeAd.first}")
                            onAdClicked()
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF9800), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AD",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeAd.first,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = activeAd.second,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Promo Action",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun DeveloperConsoleOverlay(
    logs: List<AnalyticsEvent>,
    onClose: () -> Unit,
    onTriggerCrash: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .testTag("developer_console"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real-time Event Diagnostics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Terminal")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tracked events from Firebase, Facebook SDK, Crashlytics and Google Ads trigger live in the scrollable log below:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTriggerCrash,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Crash")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trigger Non-Fatal Crash", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "No events logged yet. Tap on items inside the guidebook to generate Firebase & Facebook events...",
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    val reversedLogs = remember(logs) { logs.reversed() }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reversedLogs, key = { it.id }) { log ->
                            val color = when (log.service) {
                                "FacebookSDK" -> Color(0xFF1877F2)
                                "FirebaseAnalytics" -> Color(0xFFFFCA28)
                                "FirebaseCrashlytics" -> Color(0xFFEF5350)
                                "AdMob" -> Color(0xFF4CAF50)
                                else -> Color.Green
                            }
                            
                            val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.ROOT).format(Date(log.timestamp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "[$time]",
                                        color = Color.LightGray.copy(alpha = 0.6f),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = log.service,
                                        color = color,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = log.eventName,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = log.description,
                                    color = Color.LightGray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color.DarkGray.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedInterstitialAdDialog(
    onDismissRequest: () -> Unit,
    onClaimBonus: () -> Unit
) {
    var timerSeconds by remember { mutableStateOf(3) }
    
    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            timerSeconds--
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (timerSeconds == 0) {
                onDismissRequest()
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Sponsored",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sponsored Guide Break")
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        )
                        .clickable { onClaimBonus() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Trophy",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Earn Double Rewards!",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Tap this sponsor block to instantly add 100 BONUS Study points to your cashlist checklist!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (timerSeconds > 0) "Closing in $timerSeconds seconds..." else "Ad review complete!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                enabled = timerSeconds == 0
            ) {
                Text("Resume Studies")
            }
        }
    )
}
