package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.StartupIdea
import com.example.data.model.StartupReport
import com.example.data.remote.GeminiClient
import com.example.data.repository.StartupRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ValidationState {
    IDLE,
    ANALYZING,
    SUCCESS,
    ERROR
}

class StartupViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ai_idea_validator_prefs", Context.MODE_PRIVATE)

    private val _isUserLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _loggedInEmail = MutableStateFlow(prefs.getString("logged_in_email", "") ?: "")
    val loggedInEmail: StateFlow<String> = _loggedInEmail.asStateFlow()

    private val _loggedInName = MutableStateFlow(prefs.getString("logged_in_name", "") ?: "")
    val loggedInName: StateFlow<String> = _loggedInName.asStateFlow()

    private val _isSplashActive = MutableStateFlow(true)
    val isSplashActive: StateFlow<Boolean> = _isSplashActive.asStateFlow()

    init {
        viewModelScope.launch {
            delay(2200) // 2.2 seconds for realistic aesthetic splash loading
            _isSplashActive.value = false
        }
    }

    fun login(email: String, name: String) {
        viewModelScope.launch {
            _isChatLoading.value = true
            com.example.data.remote.FirebaseService.loginOrCreateUser(email, name) { success, _ ->
                _isChatLoading.value = false
                prefs.edit().apply {
                    putBoolean("is_logged_in", true)
                    putString("logged_in_email", email)
                    putString("logged_in_name", name)
                    apply()
                }
                _isUserLoggedIn.value = true
                _loggedInEmail.value = email
                _loggedInName.value = name
                
                // Sync any previous local ideas to Firebase in real-time
                if (com.example.data.remote.FirebaseService.isFirebaseReady()) {
                    com.example.data.remote.FirebaseService.syncAllLocalIdeasToCloud(startupIdeas.value)
                }
            }
        }
    }

    fun logout() {
        com.example.data.remote.FirebaseService.logout()
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
            putString("logged_in_email", "")
            putString("logged_in_name", "")
            apply()
        }
        _isUserLoggedIn.value = false
        _loggedInEmail.value = ""
        _loggedInName.value = ""
    }

    private val database = AppDatabase.getDatabase(application)
    private val repository = StartupRepository(database.startupIdeaDao())

    // 1. Reactive stream of all previous validated ideas ordered from Room
    val startupIdeas: StateFlow<List<StartupIdea>> = repository.allStartupIdeas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _validationState = MutableStateFlow(ValidationState.IDLE)
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()

    private val _currentAgentLog = MutableStateFlow("")
    val currentAgentLog: StateFlow<String> = _currentAgentLog.asStateFlow()

    private val _activeReport = MutableStateFlow<StartupReport?>(null)
    val activeReport: StateFlow<StartupReport?> = _activeReport.asStateFlow()

    private val _activeIdeaId = MutableStateFlow<Int?>(null)
    val activeIdeaId: StateFlow<Int?> = _activeIdeaId.asStateFlow()

    private val _mentorChatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val mentorChatHistory: StateFlow<List<ChatMessage>> = _mentorChatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun selectIdea(idea: StartupIdea) {
        _activeReport.value = idea.report
        _activeIdeaId.value = idea.id
        resetMentorChat(idea.report)
    }

    fun deselectIdea() {
        _activeReport.value = null
        _activeIdeaId.value = null
        _validationState.value = ValidationState.IDLE
    }

    fun setIdleState() {
        _validationState.value = ValidationState.IDLE
    }

    private fun resetMentorChat(report: StartupReport?) {
        if (report == null) {
            _mentorChatHistory.value = emptyList()
            return
        }
        val welcomeMessage = ChatMessage(
            sender = MessageSender.MENTOR,
            text = "Hello! I am your AI Startup Mentor. I have thoroughly analyzed the startup strategy for **${report.name}**.\n\nI'm particularly excited about your high Opportunity Score of **${report.opportunityScore}%** and our calculated Success Probability of **${report.successProbability}%**!\n\nShould we discuss how to address the **${report.risks.size} risks** we detected, optimize your pricing structure of **${report.suggestedPricing}**, or walk through your competitive advantage?"
        )
        _mentorChatHistory.value = listOf(welcomeMessage)
    }

    /**
     * Executes the Multi-agent analysis system.
     * We stream progress logs to the UI visually before pulling the completed response.
     */
    fun validateNewIdea(
        name: String,
        description: String,
        industry: String,
        targetAudience: String
    ) {
        if (name.isBlank() || description.isBlank()) {
            _errorMessage.value = "Startup name and details cannot be empty."
            _validationState.value = ValidationState.ERROR
            return
        }

        viewModelScope.launch {
            _validationState.value = ValidationState.ANALYZING
            _errorMessage.value = null

            // Stream premium agent sequence to make analysis transparent and satisfying
            val agentSteps = listOf(
                "Initializing Multi-Agent Startup Validator...",
                "[Market Agent] Researching global industry growth projections...",
                "[Market Agent] Evaluating potential sizing (TAM, SAM, SOM)...",
                "[Competitor Agent] Scoping existing industry benchmarks...",
                "[Competitor Agent] Running intelligence gap matrices on competitors...",
                "[Financial Agent] Running Monte Carlo models on conservative/optimistic revenue...",
                "[Risk Agent] Scanning operational, technical, and regulatory threats...",
                "[Plan Agent] Generating executive and digital marketing schedules...",
                "[Pitch Agent] Organizing narrative slides and funding roadmap parameters..."
            )

            for (step in agentSteps) {
                _currentAgentLog.value = step
                delay(1200) // Aesthetic delay for realistic simulation
            }

            try {
                // Call actual Gemini API (or gracefully fallback to mock if no key)
                val reportResult = GeminiClient.validateStartupIdea(name, description, industry, targetAudience)

                // Save report into Room database
                val newIdea = StartupIdea(
                    name = name,
                    description = description,
                    industry = industry,
                    targetAudience = targetAudience,
                    timestamp = System.currentTimeMillis(),
                    report = reportResult
                )
                repository.insertIdea(newIdea)

                // Push report to Firestore if connected
                if (com.example.data.remote.FirebaseService.isFirebaseReady()) {
                    com.example.data.remote.FirebaseService.saveStartupIdeaToFirestore(newIdea) { _, _ -> }
                }

                // Set this report as active report immediately
                _activeReport.value = reportResult
                resetMentorChat(reportResult)

                // Retrieve the saved entity id (handled cleanly as Room propagates new list)
                _validationState.value = ValidationState.SUCCESS
            } catch (e: Exception) {
                _errorMessage.value = "Failed to evaluate idea: ${e.localizedMessage}"
                _validationState.value = ValidationState.ERROR
            }
        }
    }

    fun deleteIdea(id: Int) {
        viewModelScope.launch {
            repository.deleteIdeaById(id)
            if (_activeIdeaId.value == id) {
                deselectIdea()
            }
        }
    }

    fun sendMessageToMentor(text: String) {
        val report = _activeReport.value ?: return
        if (text.isBlank()) return

        val userMessage = ChatMessage(sender = MessageSender.USER, text = text)
        val currentHistory = _mentorChatHistory.value.toMutableList()
        currentHistory.add(userMessage)
        _mentorChatHistory.value = currentHistory

        viewModelScope.launch {
            _isChatLoading.value = true
            try {
                val reply = GeminiClient.chatWithMentor(report, currentHistory, text)
                val mentorMessage = ChatMessage(sender = MessageSender.MENTOR, text = reply)
                val updatedHistory = _mentorChatHistory.value.toMutableList()
                updatedHistory.add(mentorMessage)
                _mentorChatHistory.value = updatedHistory
            } catch (e: Exception) {
                val errMessage = ChatMessage(
                    sender = MessageSender.MENTOR,
                    text = "I apologize, my consulting channels are receiving an error: ${e.localizedMessage}. What other strategic elements of ${report.name} would you like to review?"
                )
                val updatedHistory = _mentorChatHistory.value.toMutableList()
                updatedHistory.add(errMessage)
                _mentorChatHistory.value = updatedHistory
            } finally {
                _isChatLoading.value = false
            }
        }
    }
}
