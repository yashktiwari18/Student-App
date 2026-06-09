package com.studentprofile.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.studentprofile.app.models.ParentAccount
import com.studentprofile.app.models.StudentDetails
import com.studentprofile.app.models.StudentProfile
import com.studentprofile.app.storage.ParentRepository

/**
 * Central ViewModel managing all authentication state for the app.
 *
 * Responsibilities:
 * - Holds a reactive [StateFlow] of [AuthState] that the UI observes.
 * - Manages local user registration (stores credentials in SharedPreferences).
 * - Validates login attempts against locally registered credentials.
 * - Persists session state across app restarts via SharedPreferences.
 * - Provides a clean logout mechanism.
 *
 * This ViewModel uses [AndroidViewModel] to access application context for
 * SharedPreferences without leaking Activity references.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PREFS_NAME = "UserPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_LOGGED_IN_STUDENT_ID = "loggedInStudentId"
        private const val KEY_LOGGED_IN_PARENT_ID = "loggedInParentId"
        private const val KEY_REGISTERED_STUDENT_ID = "registeredStudentId"
        private const val KEY_REGISTERED_PASSWORD = "registeredPassword"
    }

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val parentRepo = ParentRepository(application.applicationContext)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    /** Observable auth state — the UI collects this to decide what to render. */
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _selectedStudentDetails = MutableStateFlow<StudentDetails?>(null)
    val selectedStudentDetails: StateFlow<StudentDetails?> = _selectedStudentDetails.asStateFlow()

    init {
        // Restore session from disk on ViewModel creation
        restoreSession()
    }

    // ──────────────────────────────────────────────
    // Session Persistence
    // ──────────────────────────────────────────────

    /**
     * Checks SharedPreferences for an existing session and restores it.
     * Called once during ViewModel init.
     */
    private fun restoreSession() {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val studentId = prefs.getString(KEY_LOGGED_IN_STUDENT_ID, null)
        val parentId = prefs.getString(KEY_LOGGED_IN_PARENT_ID, null)

        if (isLoggedIn && !studentId.isNullOrBlank()) {
            _authState.value = AuthState.Authenticated(studentId)
        } else if (!parentId.isNullOrBlank()) {
            // Parent previously authenticated but student selection pending
            _authState.value = AuthState.ParentAuthenticated(parentId)
        } else {
            // Clean up any inconsistent state
            clearSession()
        }
    }

    /**
     * Persists the authenticated session to SharedPreferences.
     */
    private fun saveSession(studentId: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_LOGGED_IN_STUDENT_ID, studentId)
            .apply()
    }

    private fun saveParentSession(parentId: String) {
        prefs.edit()
            .putString(KEY_LOGGED_IN_PARENT_ID, parentId)
            .apply()
    }

    /**
     * Removes all session data from SharedPreferences.
     */
    private fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_LOGGED_IN_STUDENT_ID)
            .remove(KEY_LOGGED_IN_PARENT_ID)
            .apply()
        _authState.value = AuthState.Unauthenticated
    }

    // ──────────────────────────────────────────────
    // Registration
    // ──────────────────────────────────────────────

    /**
     * Registers a new student account locally.
     *
     * Validation rules:
     * - Student ID must not be blank.
     * - Password must be at least 4 characters.
     *
     * On success, credentials are saved to SharedPreferences and
     * [AuthState] remains [AuthState.Unauthenticated] (user must log in after registering).
     *
     * @param studentId The desired login ID.
     * @param password The desired password.
     * @return `true` if registration succeeded, `false` otherwise (error state is set).
     */
    fun register(studentId: String, password: String): Boolean {
        // --- Input validation ---
        if (studentId.isBlank()) {
            _authState.value = AuthState.Error("Student ID cannot be empty.")
            return false
        }
        if (password.length < 4) {
            _authState.value = AuthState.Error("Password must be at least 4 characters.")
            return false
        }

        // --- Check for duplicate registration ---
        val existingId = prefs.getString(KEY_REGISTERED_STUDENT_ID, null)
        if (existingId != null && existingId == studentId) {
            _authState.value = AuthState.Error("This Student ID is already registered. Please log in.")
            return false
        }

        // --- Save credentials locally ---
        prefs.edit()
            .putString(KEY_REGISTERED_STUDENT_ID, studentId)
            .putString(KEY_REGISTERED_PASSWORD, password)
            .apply()

        // Reset state to unauthenticated (user should now proceed to login)
        _authState.value = AuthState.Unauthenticated
        return true
    }

    /**
     * Registers a parent account with one or more child student profiles.
     * This method preserves the legacy single-student login by storing the first
     * child's studentId and the password in the legacy keys so existing login
     * flow continues to work unchanged.
     */
    fun registerParent(parentId: String, password: String, children: List<StudentProfile>): Boolean {
        if (parentId.isBlank()) {
            _authState.value = AuthState.Error("Parent ID cannot be empty.")
            return false
        }
        if (password.length < 4) {
            _authState.value = AuthState.Error("Password must be at least 4 characters.")
            return false
        }
        if (children.isEmpty()) {
            _authState.value = AuthState.Error("Please add at least one student.")
            return false
        }

        // Persist parent and children using ParentRepository
        val parent = ParentAccount(parentId = parentId, password = password, children = children)
        parentRepo.addOrUpdateParent(parent)

        // Write legacy single-student credentials for backward compatibility
        val firstStudentId = children.first().studentId
        prefs.edit()
            .putString(KEY_REGISTERED_STUDENT_ID, firstStudentId)
            .putString(KEY_REGISTERED_PASSWORD, password)
            .apply()

        _authState.value = AuthState.Unauthenticated
        return true
    }

    // ──────────────────────────────────────────────
    // Login
    // ──────────────────────────────────────────────

    /**
     * Attempts to log in with the provided credentials.
     *
     * Validates against locally stored registered credentials.
     * On success, persists the session and transitions to [AuthState.Authenticated].
     *
     * @param studentId The login ID entered by the user.
     * @param password The password entered by the user.
     */
    fun login(studentId: String, password: String) {
        // --- Input validation ---
        if (studentId.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields.")
            return
        }
        // --- Credential matching ---
        // First, check if credentials match a parent account stored in ParentRepository
        val parent = parentRepo.getParent(studentId)
        if (parent != null) {
            if (password != parent.password) {
                _authState.value = AuthState.Error("Invalid login or password.")
                return
            }

            // Parent authenticated — decide selection or auto-select
            val children = parent.children
            if (children.isEmpty()) {
                _authState.value = AuthState.Error("No students found for this parent account.")
                return
            }

            if (children.size == 1) {
                // Auto-select single child to preserve legacy flow
                val childId = children.first().studentId
                saveSession(childId)
                _authState.value = AuthState.Authenticated(childId)
            } else {
                // Multiple children — save parent session and request selection
                saveParentSession(parent.parentId)
                _authState.value = AuthState.ParentAuthenticated(parent.parentId)
            }
            return
        }

        // Fallback: legacy single-student credential store
        val registeredId = prefs.getString(KEY_REGISTERED_STUDENT_ID, null)
        val registeredPassword = prefs.getString(KEY_REGISTERED_PASSWORD, null)

        if (registeredId == null || registeredPassword == null) {
            _authState.value = AuthState.Error("No account found. Please sign up first.")
            return
        }

        if (studentId != registeredId || password != registeredPassword) {
            _authState.value = AuthState.Error("Invalid Student ID or Password.")
            return
        }

        // --- Authentication success (legacy) ---
        saveSession(studentId)
        _authState.value = AuthState.Authenticated(studentId)
    }

    // ──────────────────────────────────────────────
    // Logout
    // ──────────────────────────────────────────────

    /**
     * Logs the user out by clearing the persisted session.
     * Transitions state to [AuthState.Unauthenticated].
     */
    fun logout() {
        clearSession()
    }

    /**
     * Called after parent selects a student from the selection UI.
     */
    fun selectStudent(studentId: String) {
        val student = parentRepo.findStudent(studentId)
        if (student != null) {
            _selectedStudentDetails.value = buildStudentDetails(student)
        }
        saveSession(studentId)
        _authState.value = AuthState.Authenticated(studentId)
    }

    fun refreshSelectedStudentFromSession() {
        val currentStudentId = prefs.getString(KEY_LOGGED_IN_STUDENT_ID, null)
        if (!currentStudentId.isNullOrBlank()) {
            val student = parentRepo.findStudent(currentStudentId)
            if (student != null) {
                _selectedStudentDetails.value = buildStudentDetails(student)
            }
        }
    }

    fun getCurrentParentId(): String? {
        return prefs.getString(KEY_LOGGED_IN_PARENT_ID, null)
    }

    fun getLinkedStudentsForCurrentSession(): List<StudentProfile> {
        val parentId = getCurrentParentId()
        if (!parentId.isNullOrBlank()) {
            return parentRepo.getChildrenForParent(parentId)
        }

        val currentStudentId = prefs.getString(KEY_LOGGED_IN_STUDENT_ID, null)
        if (!currentStudentId.isNullOrBlank()) {
            return parentRepo.findParentByStudentId(currentStudentId)?.children ?: emptyList()
        }

        return emptyList()
    }

    /**
     * Convenience accessor for children of a parent account.
     */
    fun getChildrenForParent(parentId: String): List<StudentProfile> = parentRepo.getChildrenForParent(parentId)

    fun getSelectedStudentDetails(): StudentDetails? = _selectedStudentDetails.value

    private fun buildStudentDetails(student: StudentProfile): StudentDetails {
        // Local demo data derived from class/student identity; can be replaced later with real storage.
        val classDigit = student.classInfo.filter { it.isDigit() }.toIntOrNull() ?: 0
        val attendance = when (classDigit % 3) {
            0 -> 92.3f
            1 -> 88.5f
            else -> 81.0f
        }
        val avgScore = when (classDigit % 3) {
            0 -> 78.6f
            1 -> 82.0f
            else -> 74.0f
        }

        val subjectPerformances = listOf(
            SubjectPerformance("English", avgScore, if (avgScore >= 80f) "A" else "B+", R.drawable.ic_subject_english, R.drawable.bg_subject_icon_english, android.graphics.Color.parseColor("#28C76F")),
            SubjectPerformance("Mathematics", avgScore - 4f, "B", R.drawable.ic_subject_math, R.drawable.bg_subject_icon_math, android.graphics.Color.parseColor("#FF9F43")),
            SubjectPerformance("Science", avgScore - 1f, "A", R.drawable.ic_subject_science, R.drawable.bg_subject_icon_science, android.graphics.Color.parseColor("#28C76F"))
        )

        return StudentDetails(
            student = student,
            fatherName = "Vikram Sharma",
            motherName = "Neha Sharma",
            attendancePercent = attendance,
            presentCount = if (attendance > 90f) 115 else 108,
            absentCount = if (attendance > 90f) 15 else 22,
            avgScore = avgScore,
            grade = if (avgScore >= 80f) "A" else "B+",
            rank = if (avgScore >= 80f) "8 / 42" else "12 / 42",
            homeworkSubmitted = if (attendance > 90f) 24 else 22,
            homeworkPending = if (attendance > 90f) 2 else 4,
            behaviour = if (attendance > 90f) "Good" else "Satisfactory",
            numRemarks = if (attendance > 90f) 1 else 2,
            subjectPerformances = subjectPerformances
        )
    }

    // ──────────────────────────────────────────────
    // Error Handling
    // ──────────────────────────────────────────────

    /**
     * Clears any current error state, returning to [AuthState.Unauthenticated].
     * Call this after the UI has displayed the error to the user.
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}
