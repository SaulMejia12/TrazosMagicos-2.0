package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.StickerReward
import com.example.data.local.TraceDatabase
import com.example.data.local.TraceProgress
import com.example.data.repository.TraceRepository
import com.example.utils.GuidePoint
import com.example.utils.SoundSynthesizer
import com.example.utils.TraceCharacter
import com.example.utils.TracePaths
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.random.Random

data class ChildProfile(
    val id: String,
    val name: String,
    val age: Int,
    val avatar: String,
    val bgColor: String
)

class TracingViewModel(
    application: Application,
    private val repository: TraceRepository
) : AndroidViewModel(application) {

    private val soundSynth = SoundSynthesizer()

    // Player Profile States (persisted via SharedPreferences)
    private val sharedPrefs = application.getSharedPreferences("trazos_magicos_prefs", Context.MODE_PRIVATE)

    private val _activeProfileId = MutableStateFlow(sharedPrefs.getString("active_profile_id", "profile_default") ?: "profile_default")
    val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

    private val _playerName = MutableStateFlow("Pequeño Trazador")
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    private val _playerAge = MutableStateFlow(4)
    val playerAge: StateFlow<Int> = _playerAge.asStateFlow()

    private val _playerAvatar = MutableStateFlow("🦊")
    val playerAvatar: StateFlow<String> = _playerAvatar.asStateFlow()

    private val _playerBgColor = MutableStateFlow("Celeste Mágico")
    val playerBgColor: StateFlow<String> = _playerBgColor.asStateFlow()

    private val _childProfiles = MutableStateFlow<List<ChildProfile>>(emptyList())
    val childProfiles: StateFlow<List<ChildProfile>> = _childProfiles.asStateFlow()

    init {
        // Initialize active profile IDs list if not exists
        val idsString = sharedPrefs.getString("profile_ids", "")
        if (idsString.isNullOrBlank()) {
            sharedPrefs.edit()
                .putString("profile_ids", "profile_default")
                .putString("profile_name_profile_default", "Pequeño Trazador")
                .putInt("profile_age_profile_default", 4)
                .putString("profile_avatar_profile_default", "🦊")
                .putString("profile_bg_color_profile_default", "Celeste Mágico")
                .putString("active_profile_id", "profile_default")
                .apply()
        }
        _childProfiles.value = loadProfiles()
        loadActiveProfile()
        generateParentalPuzzle()
    }

    private fun loadProfiles(): List<ChildProfile> {
        val idsString = sharedPrefs.getString("profile_ids", "profile_default") ?: "profile_default"
        val ids = idsString.split(",").filter { it.isNotBlank() }
        return ids.map { id ->
            ChildProfile(
                id = id,
                name = sharedPrefs.getString("profile_name_$id", "Pequeño Trazador") ?: "Pequeño Trazador",
                age = sharedPrefs.getInt("profile_age_$id", 4),
                avatar = sharedPrefs.getString("profile_avatar_$id", "🦊") ?: "🦊",
                bgColor = sharedPrefs.getString("profile_bg_color_$id", "Celeste Mágico") ?: "Celeste Mágico"
            )
        }
    }

    fun loadActiveProfile() {
        val activeId = _activeProfileId.value
        _playerName.value = sharedPrefs.getString("profile_name_$activeId", "Pequeño Trazador") ?: "Pequeño Trazador"
        _playerAge.value = sharedPrefs.getInt("profile_age_$activeId", 4)
        _playerAvatar.value = sharedPrefs.getString("profile_avatar_$activeId", "🦊") ?: "🦊"
        _playerBgColor.value = sharedPrefs.getString("profile_bg_color_$activeId", "Celeste Mágico") ?: "Celeste Mágico"
    }

    fun switchProfile(profileId: String) {
        sharedPrefs.edit().putString("active_profile_id", profileId).apply()
        _activeProfileId.value = profileId
        loadActiveProfile()
    }

    fun updatePlayerProfile(name: String, age: Int, avatar: String) {
        val activeId = _activeProfileId.value
        sharedPrefs.edit()
            .putString("profile_name_$activeId", name.ifBlank { "Pequeño Trazador" })
            .putInt("profile_age_$activeId", age)
            .putString("profile_avatar_$activeId", avatar)
            .apply()
        _playerName.value = name.ifBlank { "Pequeño Trazador" }
        _playerAge.value = age
        _playerAvatar.value = avatar
        _childProfiles.value = loadProfiles()
    }

    fun updatePlayerBgColor(bgColor: String) {
        val activeId = _activeProfileId.value
        sharedPrefs.edit()
            .putString("profile_bg_color_$activeId", bgColor)
            .apply()
        _playerBgColor.value = bgColor
        _childProfiles.value = loadProfiles()
    }

    fun createNewProfile(name: String, age: Int, avatar: String, bgColor: String): String {
        val idsString = sharedPrefs.getString("profile_ids", "profile_default") ?: "profile_default"
        val ids = idsString.split(",").filter { it.isNotBlank() }.toMutableList()
        
        val newId = "profile_${System.currentTimeMillis()}"
        ids.add(newId)
        
        sharedPrefs.edit()
            .putString("profile_ids", ids.joinToString(","))
            .putString("profile_name_$newId", name.ifBlank { "Pequeño Trazador" })
            .putInt("profile_age_$newId", age)
            .putString("profile_avatar_$newId", avatar)
            .putString("profile_bg_color_$newId", bgColor)
            .putString("active_profile_id", newId)
            .apply()
            
        _activeProfileId.value = newId
        _playerName.value = name.ifBlank { "Pequeño Trazador" }
        _playerAge.value = age
        _playerAvatar.value = avatar
        _playerBgColor.value = bgColor
        _childProfiles.value = loadProfiles()
        
        return newId
    }

    fun deleteProfile(profileId: String) {
        val idsString = sharedPrefs.getString("profile_ids", "profile_default") ?: "profile_default"
        val ids = idsString.split(",").filter { it.isNotBlank() }.toMutableList()
        if (ids.size <= 1) return // Do not delete the only profile
        
        ids.remove(profileId)
        
        val editor = sharedPrefs.edit()
            .putString("profile_ids", ids.joinToString(","))
            .remove("profile_name_$profileId")
            .remove("profile_age_$profileId")
            .remove("profile_avatar_$profileId")
            .remove("profile_bg_color_$profileId")
            
        // If the deleted profile was active, switch to another one
        if (_activeProfileId.value == profileId) {
            val fallbackId = ids.first()
            editor.putString("active_profile_id", fallbackId)
            _activeProfileId.value = fallbackId
        }
        editor.apply()
        
        _childProfiles.value = loadProfiles()
        loadActiveProfile()
        
        // Also clear progress from DB for this deleted profile
        resetProfileProgress(profileId)
    }

    fun resetProfileProgress(profileId: String) {
        viewModelScope.launch {
            repository.resetProfileData(profileId)
            if (_activeProfileId.value == profileId) {
                resetTracingState()
            }
        }
    }

    fun editProfileDirect(profileId: String, name: String, age: Int, avatar: String, bgColor: String) {
        sharedPrefs.edit()
            .putString("profile_name_$profileId", name.ifBlank { "Pequeño Trazador" })
            .putInt("profile_age_$profileId", age)
            .putString("profile_avatar_$profileId", avatar)
            .putString("profile_bg_color_$profileId", bgColor)
            .apply()
        
        if (_activeProfileId.value == profileId) {
            _playerName.value = name.ifBlank { "Pequeño Trazador" }
            _playerAge.value = age
            _playerAvatar.value = avatar
            _playerBgColor.value = bgColor
        }
        _childProfiles.value = loadProfiles()
    }

    // UI state flows (profile-specific progress & stickers)
    @OptIn(ExperimentalCoroutinesApi::class)
    val allProgress: StateFlow<List<TraceProgress>> = _activeProfileId
        .flatMapLatest { profileId -> repository.getProgressForProfile(profileId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allStickers: StateFlow<List<StickerReward>> = _activeProfileId
        .flatMapLatest { profileId -> repository.getStickersForProfile(profileId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active character selection
    private val _selectedChar = MutableStateFlow<TraceCharacter?>(null)
    val selectedChar: StateFlow<TraceCharacter?> = _selectedChar.asStateFlow()

    // Tracing interactive state
    private val _currentStrokeIndex = MutableStateFlow(0)
    val currentStrokeIndex: StateFlow<Int> = _currentStrokeIndex.asStateFlow()

    private val _currentPointIndex = MutableStateFlow(0)
    val currentPointIndex: StateFlow<Int> = _currentPointIndex.asStateFlow()

    // Live points drawn by the user for rendering trail
    val userPathPoints = mutableStateListOf<GuidePoint>()
    private var isNewSegmentStarted = true

    // Status flags
    private val _isLevelCompleted = MutableStateFlow(false)
    val isLevelCompleted: StateFlow<Boolean> = _isLevelCompleted.asStateFlow()

    private val _unlockedSticker = MutableStateFlow<String?>(null)
    val unlockedSticker: StateFlow<String?> = _unlockedSticker.asStateFlow()

    // Active Category Filter for learning dashboard: "LETTER" or "NUMBER"
    private val _categoryFilter = MutableStateFlow("NUMBER")
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    // Parental gate verification status
    private val _isParentsUnlocked = MutableStateFlow(false)
    val isParentsUnlocked: StateFlow<Boolean> = _isParentsUnlocked.asStateFlow()

    // Retry hint display state for child guidance
    private val _showRetryHint = MutableStateFlow(false)
    val showRetryHint: StateFlow<Boolean> = _showRetryHint.asStateFlow()

    // Parental Puzzle State
    var num1: Int = 0
    var num2: Int = 0
    var puzzleAnswer: Int = 0

    init {
        generateParentalPuzzle()
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun generateParentalPuzzle() {
        num1 = Random.nextInt(5, 15)
        num2 = Random.nextInt(3, 9)
        puzzleAnswer = num1 + num2
    }

    fun attemptParentalUnlock(answer: Int): Boolean {
        val success = (answer == puzzleAnswer)
        _isParentsUnlocked.value = success
        if (!success) {
            generateParentalPuzzle()
        }
        return success
    }

    fun lockParentsArea() {
        _isParentsUnlocked.value = false
        generateParentalPuzzle()
    }

    fun selectCharacter(characterId: String) {
        val char = TracePaths.characters[characterId]
        _selectedChar.value = char
        resetTracingState()
    }

    fun resetTracingState() {
        _currentStrokeIndex.value = 0
        _currentPointIndex.value = 0
        userPathPoints.clear()
        _isLevelCompleted.value = false
        _unlockedSticker.value = null
        _showRetryHint.value = false
        isNewSegmentStarted = true
    }

    fun onUserDragEnd() {
        isNewSegmentStarted = true
    }

    // Interactive Tracing Pointer Evaluation
    fun onUserTouchMove(x: Float, y: Float) {
        if (_isLevelCompleted.value) return

        val char = _selectedChar.value ?: return
        var strokeIdx = _currentStrokeIndex.value
        var pointIdx = _currentPointIndex.value

        if (strokeIdx >= char.strokes.size) return
        var currentStroke = char.strokes[strokeIdx]
        if (pointIdx >= currentStroke.size) return

        // Record point for current visual drawing trail
        userPathPoints.add(GuidePoint(x, y, strokeIdx, isNewSegmentStarted))
        isNewSegmentStarted = false

        val tolerance = 0.14f
        var advanced = false

        // Loop to advance as many points as are within the tolerance of the current touch point
        while (strokeIdx < char.strokes.size && pointIdx < currentStroke.size) {
            val targetPoint = currentStroke[pointIdx]
            val distance = hypot(x - targetPoint.x, y - targetPoint.y)

            if (distance <= tolerance) {
                advanced = true
                pointIdx++
                if (pointIdx >= currentStroke.size) {
                    // Current stroke completed! Go to next stroke
                    strokeIdx++
                    if (strokeIdx < char.strokes.size) {
                        currentStroke = char.strokes[strokeIdx]
                        pointIdx = 0
                        isNewSegmentStarted = true
                    } else {
                        // All strokes completed!
                        break
                    }
                }
            } else {
                // Not within tolerance of the current target point, stop advancing
                break
            }
        }

        if (advanced) {
            _showRetryHint.value = false
            if (strokeIdx < char.strokes.size) {
                _currentStrokeIndex.value = strokeIdx
                _currentPointIndex.value = pointIdx
            } else {
                // ALL STROKES COMPLETED! LEVEL SUCCESS!
                _currentStrokeIndex.value = strokeIdx
                _currentPointIndex.value = 0
                completeLevel()
            }
        }
    }

    private fun completeLevel() {
        val char = _selectedChar.value ?: return
        _isLevelCompleted.value = true
        soundSynth.playSuccess()

        // Generate and unlock a cute sticker reward!
        val stickersPool = listOf("dino", "rocket", "panda", "sun", "star", "unicorn", "frog", "crab", "lion", "fox", "whale", "koala")
        val randomSticker = stickersPool.random()
        _unlockedSticker.value = randomSticker

        viewModelScope.launch {
            val pId = _activeProfileId.value
            // Save Progress
            val currentProg = repository.getProgressForChar(pId, char.id)
            val newCompletedCount = (currentProg?.completedCount ?: 0) + 1
            // Compute realistic high kid-friendly accuracy score (90 to 100)
            val randomAccuracy = Random.nextInt(92, 101)
            val bestAcc = maxOf(currentProg?.bestAccuracy ?: 0, randomAccuracy)
            
            val stars = when {
                randomAccuracy >= 95 -> 3
                randomAccuracy >= 92 -> 2
                else -> 1
            }
            val bestStars = maxOf(currentProg?.starsEarned ?: 0, stars)

            val newProgress = TraceProgress(
                profileId = pId,
                charId = char.id,
                charType = when {
                    char.isShape -> "SHAPE"
                    char.isLetter -> "LETTER"
                    else -> "NUMBER"
                },
                completedCount = newCompletedCount,
                bestAccuracy = bestAcc,
                starsEarned = bestStars,
                lastCompletedTime = System.currentTimeMillis()
            )
            repository.saveProgress(newProgress)

            // Save Sticker Reward
            repository.saveSticker(StickerReward(profileId = pId, stickerId = randomSticker, unlockTime = System.currentTimeMillis()))
        }
    }

    fun resetAllLearningData() {
        viewModelScope.launch {
            repository.resetAllData()
            resetTracingState()
        }
    }
}

class TracingViewModelFactory(
    private val application: Application,
    private val repository: TraceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TracingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TracingViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
