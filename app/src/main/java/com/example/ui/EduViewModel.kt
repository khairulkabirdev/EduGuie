package com.example.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface Screen {
    object ClassGrid : Screen
    data class SubjectList(val classLevel: ClassLevel) : Screen
    data class LessonList(val classLevel: ClassLevel, val subject: Subject, val unit: UnitClass) : Screen
    data class LessonDetails(
        val classLevel: ClassLevel,
        val subject: Subject,
        val unit: UnitClass,
        val lesson: Lesson
    ) : Screen
    
    // Auxiliary screens for GDPR / Privacy compliance
    object PrivacyPolicy : Screen
    object TermsOfService : Screen
    object BookmarksList : Screen
    object Settings : Screen
}

class EduViewModel(private val repository: EduRepository) : ViewModel() {

    // Custom Backstack navigation stack
    val navigationStack = mutableStateListOf<Screen>(Screen.ClassGrid)

    // Loaded Local Knowledgebase Guidebook Data
    private val _classLevels = MutableStateFlow<List<ClassLevel>>(emptyList())
    val classLevels: StateFlow<List<ClassLevel>> = _classLevels.asStateFlow()

    // Room DB Subscriptions
    val allProgress: StateFlow<List<LessonProgress>> = repository.getAllProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookmarks: StateFlow<List<Bookmark>> = repository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyPoints: StateFlow<StudyPoints> = repository.getStudyPoints()
        .map { it ?: StudyPoints() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudyPoints())

    // UI Interactive States
    private val _isDeveloperConsoleVisible = MutableStateFlow(false)
    val isDeveloperConsoleVisible: StateFlow<Boolean> = _isDeveloperConsoleVisible.asStateFlow()

    // Controlled theme mode: false = Light Theme, true = Dark Theme
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        AnalyticsAndAdFramework.logEvent("Settings", "ThemeChanged", "Toggled Dark Mode to: ${_isDarkTheme.value}")
    }

    // Control Simulated Interstitial overlay
    private val _interstitialAdState = MutableStateFlow<InterstitialState>(InterstitialState.Idle)
    val interstitialAdState: StateFlow<InterstitialState> = _interstitialAdState.asStateFlow()

    // Control Simulated Rewarded Interstitial overlay
    private val _rewardedAdState = MutableStateFlow<RewardedState>(RewardedState.Idle)
    val rewardedAdState: StateFlow<RewardedState> = _rewardedAdState.asStateFlow()

    // Control Web Open Ad
    private val _showAppOpenAd = MutableStateFlow(true)
    val showAppOpenAd: StateFlow<Boolean> = _showAppOpenAd.asStateFlow()

    fun dismissAppOpenAd() {
        _showAppOpenAd.value = false
        AnalyticsAndAdFramework.logAdMobEvent("ActiveScreen", "AppOpenAd", "Dismissed")
    }

    private var actionCounterForAd = 0

    init {
        // Load data from Assets JSON
        _classLevels.value = repository.loadClassLevels()
        AnalyticsAndAdFramework.logEvent("EduGuide", "AppStarted", "GyanSathi Admin/EduGuide Initialised. Sourcing offline models.")
    }

    fun navigateTo(screen: Screen) {
        // Every 3 transitions triggers simulated beautiful AdMob Interstitial
        actionCounterForAd++
        if (actionCounterForAd % 3 == 0) {
            triggerInterstitialAd {
                navigationStack.add(screen)
                logScreenEvent(screen)
            }
        } else {
            navigationStack.add(screen)
            logScreenEvent(screen)
        }
    }

    fun navigateBack(): Boolean {
        return if (navigationStack.size > 1) {
            val departed = navigationStack.removeLast()
            AnalyticsAndAdFramework.logEvent("Navigation", "BackHandled", "Returned from $departed")
            true
        } else {
            false
        }
    }

    private fun logScreenEvent(screen: Screen) {
        when (screen) {
            is Screen.ClassGrid -> {
                AnalyticsAndAdFramework.trackFirebaseAnalytics("ViewClassGrid", "Screen", "ClassGrid")
            }
            is Screen.SubjectList -> {
                AnalyticsAndAdFramework.trackFirebaseAnalytics("ViewSubjects", "Class", screen.classLevel.className)
                AnalyticsAndAdFramework.trackFacebookEvent("ViewClassContent", mapOf("className" to screen.classLevel.className))
            }
            is Screen.LessonList -> {
                AnalyticsAndAdFramework.trackFirebaseAnalytics("ViewLessonList", "Subject", screen.subject.name)
            }
            is Screen.LessonDetails -> {
                AnalyticsAndAdFramework.trackFirebaseAnalytics("ViewLesson", "Lesson", screen.lesson.name)
                AnalyticsAndAdFramework.trackFacebookEvent("ViewTutorial", mapOf("lessonName" to screen.lesson.name, "subject" to screen.subject.name))
            }
            is Screen.PrivacyPolicy -> {
                AnalyticsAndAdFramework.trackFirebaseAnalytics("ViewPrivacy", "Screen", "PrivacyPolicy")
            }
            is Screen.TermsOfService -> {
                AnalyticsAndAdFramework.trackFirebaseAnalytics("ViewToS", "Screen", "TermsOfService")
            }
            is Screen.BookmarksList -> {
                AnalyticsAndAdFramework.trackFirebaseAnalytics("ViewBookmarks", "Screen", "BookmarksList")
            }
            is Screen.Settings -> {
                AnalyticsAndAdFramework.trackFirebaseAnalytics("ViewSettings", "Screen", "Settings")
            }
        }
    }

    // Toggle checklists (Lesson Completion status)
    fun toggleLessonProgress(lessonId: String, classNum: Int, subjectId: String) {
        viewModelScope.launch {
            val exists = allProgress.value.any { it.lessonId == lessonId && it.completed }
            if (exists) {
                repository.deleteProgress(lessonId)
                AnalyticsAndAdFramework.logEvent("Checklist", "LessonReset", "Completed tag removed from $lessonId")
            } else {
                repository.insertProgress(LessonProgress(lessonId = lessonId, classNum = classNum, subjectId = subjectId, completed = true))
                repository.addPoints(50, 1) // Student wins 50 points and 1 gold star!
                AnalyticsAndAdFramework.trackFacebookEvent("AchieveLevel", mapOf("lessonId" to lessonId, "points" to 50))
                AnalyticsAndAdFramework.logEvent("Checklist", "LessonCompleted", "Awarded 50 pts & 1 Gold Star for reading $lessonId!")
            }
        }
    }

    // Toggle Bookmarking Lesson
    fun toggleBookmark(lessonId: String, lessonName: String, classNum: Int, subjectId: String, subjectName: String) {
        viewModelScope.launch {
            val isBookmarked = allBookmarks.value.any { it.lessonId == lessonId }
            if (isBookmarked) {
                repository.deleteBookmark(lessonId)
                AnalyticsAndAdFramework.logEvent("Bookmarks", "BookmarkRemoved", "Removed $lessonName from bookmarks.")
            } else {
                repository.insertBookmark(
                    Bookmark(
                        lessonId = lessonId,
                        lessonName = lessonName,
                        classNum = classNum,
                        subjectId = subjectId,
                        subjectName = subjectName
                    )
                )
                AnalyticsAndAdFramework.logEvent("Bookmarks", "BookmarkAdded", "Saved $lessonName to bookmarks.")
            }
        }
    }

    // Custom developer and analytics control toggles
    fun toggleDeveloperConsole() {
        _isDeveloperConsoleVisible.value = !_isDeveloperConsoleVisible.value
        AnalyticsAndAdFramework.logEvent("Developer", "ConsoleToggled", "Developer diagnostic analytics portal visible: ${_isDeveloperConsoleVisible.value}")
    }

    fun triggerSimulatedCrash() {
        AnalyticsAndAdFramework.recordCrashlyticsError("NullPointerException", "Simulated system diagnostic error triggered on user demand.")
    }

    private fun triggerInterstitialAd(onAdDismissed: () -> Unit) {
        viewModelScope.launch {
            _interstitialAdState.value = InterstitialState.Loading(onAdDismissed)
            AnalyticsAndAdFramework.logAdMobEvent("ActiveScreen", "InterstitialAd", "Loading")
        }
    }

    fun setInterstitialAdVisible() {
        val currentState = _interstitialAdState.value
        if (currentState is InterstitialState.Loading) {
            _interstitialAdState.value = InterstitialState.Visible(currentState.onDismiss)
            AnalyticsAndAdFramework.logAdMobEvent("ActiveScreen", "InterstitialAd", "DisplayedFallback")
        }
    }

    fun dismissInterstitialAd() {
        val currentState = _interstitialAdState.value
        _interstitialAdState.value = InterstitialState.Idle
        if (currentState is InterstitialState.Visible) {
            AnalyticsAndAdFramework.logAdMobEvent("ActiveScreen", "InterstitialAd", "ClosedByUser")
            currentState.onDismiss()
        } else if (currentState is InterstitialState.Loading) {
            AnalyticsAndAdFramework.logAdMobEvent("ActiveScreen", "InterstitialAd", "ClosedThroughSDK")
            currentState.onDismiss()
        }
    }

    fun triggerRewardedAd(onRewarded: () -> Unit) {
        viewModelScope.launch {
            _rewardedAdState.value = RewardedState.Loading(onRewarded)
            AnalyticsAndAdFramework.logAdMobEvent("ActiveScreen", "RewardedAd", "Loading")
        }
    }

    fun setRewardedAdVisible() {
        val currentState = _rewardedAdState.value
        if (currentState is RewardedState.Loading) {
            _rewardedAdState.value = RewardedState.Visible(currentState.onRewarded)
            AnalyticsAndAdFramework.logAdMobEvent("ActiveScreen", "RewardedAd", "DisplayedFallback")
        }
    }

    fun dismissRewardedAd(completed: Boolean) {
        val currentState = _rewardedAdState.value
        _rewardedAdState.value = RewardedState.Idle
        val onRewardedCallback = when (currentState) {
            is RewardedState.Visible -> currentState.onRewarded
            is RewardedState.Loading -> currentState.onRewarded
            else -> null
        }
        if (onRewardedCallback != null) {
            if (completed) {
                AnalyticsAndAdFramework.logAdMobEvent("ActiveScreen", "RewardedAd", "CompletedAndClaimed")
                viewModelScope.launch {
                    repository.addPoints(150, 3) 
                }
                onRewardedCallback()
            } else {
                AnalyticsAndAdFramework.logAdMobEvent("ActiveScreen", "RewardedAd", "ClosedEarly")
            }
        }
    }

    fun simulateAdBonusPoints() {
        viewModelScope.launch {
            repository.addPoints(100, 2) 
            AnalyticsAndAdFramework.logEvent("Rewards", "AdRewardClaimed", "Claimed 100 bonus study points for viewing promotion!")
        }
    }
}

sealed interface InterstitialState {
    object Idle : InterstitialState
    data class Loading(val onDismiss: () -> Unit) : InterstitialState
    data class Visible(val onDismiss: () -> Unit) : InterstitialState
}

sealed interface RewardedState {
    object Idle : RewardedState
    data class Loading(val onRewarded: () -> Unit) : RewardedState
    data class Visible(val onRewarded: () -> Unit) : RewardedState
}

class EduViewModelFactory(private val repository: EduRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EduViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EduViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
