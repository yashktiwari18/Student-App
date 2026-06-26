package com.studentprofile.app.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.studentprofile.app.domain.models.ParentAccount
import com.studentprofile.app.domain.models.StudentDetails
import com.studentprofile.app.domain.models.StudentProfile
import com.studentprofile.app.data.repository.ParentRepository
import com.studentprofile.app.domain.models.SubjectPerformance
import com.studentprofile.app.R

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PREFS_NAME = "UserPrefs"
        private const val KEY_SUBDOMAIN = "subdomain"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_LOGGED_IN_STUDENT_ID = "loggedInStudentId"
        private const val KEY_LOGGED_IN_PARENT_ID = "loggedInParentId"
        
        private const val PREFS_MPIN = "MpinPrefs"
    }

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mpinPrefs = application.getSharedPreferences(PREFS_MPIN, Context.MODE_PRIVATE)
    private val parentRepo = ParentRepository(application.applicationContext)

    private val _authState = MutableStateFlow<AuthState>(AuthState.SubdomainRequired)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _selectedStudentDetails = MutableStateFlow<StudentDetails?>(null)
    val selectedStudentDetails: StateFlow<StudentDetails?> = _selectedStudentDetails.asStateFlow()

    private val _subdomain = MutableStateFlow<String?>(null)
    val subdomain: StateFlow<String?> = _subdomain.asStateFlow()

    private val _apiUrl = MutableStateFlow<String?>(null)
    val apiUrl: StateFlow<String?> = _apiUrl.asStateFlow()

    init {
        seedMockData()
        restoreSession()
    }

    private fun seedMockData() {
        if (parentRepo.getParents().isEmpty()) {
            parentRepo.addOrUpdateParent(
                ParentAccount(
                    parentId = "parent@example.com",
                    password = "password123",
                    children = listOf(
                        StudentProfile("student_1", "Aryan Sharma", "Class 10 - A", "A", "ADM001"),
                        StudentProfile("student_2", "Isha Sharma", "Class 8 - B", "B", "ADM002")
                    )
                )
            )
            parentRepo.addOrUpdateParent(
                ParentAccount(
                    parentId = "test@example.com",
                    password = "password123",
                    children = listOf(
                        StudentProfile("student_3", "Rahul Kumar", "Class 9 - C", "C", "ADM003")
                    )
                )
            )
        }
    }

    private fun restoreSession() {
        val savedSubdomain = prefs.getString(KEY_SUBDOMAIN, null)
        _subdomain.value = savedSubdomain
        if (savedSubdomain != null) {
            _apiUrl.value = "https://$savedSubdomain.localtest.me:8002/api/v1"
        }

        if (savedSubdomain == null) {
            _authState.value = AuthState.SubdomainRequired
            return
        }

        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val studentId = prefs.getString(KEY_LOGGED_IN_STUDENT_ID, null)

        if (isLoggedIn && !studentId.isNullOrBlank()) {
            _authState.value = AuthState.Authenticated(studentId)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun setSubdomain(domain: String) {
        val formattedDomain = domain.trim().lowercase()
        prefs.edit().putString(KEY_SUBDOMAIN, formattedDomain).apply()
        _subdomain.value = formattedDomain
        
        // Assign API URL based on subdomain
        _apiUrl.value = "https://$formattedDomain.localtest.me:8002/api/v1"
        
        _authState.value = AuthState.Unauthenticated
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields.")
            return
        }

        // Role is "student" as per requirements
        val role = "student"

        val parent = parentRepo.getParent(email)
        if (parent != null && parent.password == password) {
            val children = parent.children
            if (children.isEmpty()) {
                _authState.value = AuthState.Error("No students linked to this account.")
            } else {
                prefs.edit().putString(KEY_LOGGED_IN_PARENT_ID, email).apply()
                _authState.value = AuthState.StudentSelectionRequired(email, children)
            }
        } else {
            _authState.value = AuthState.Error("Invalid email or password.")
        }
    }

    fun selectStudent(studentId: String) {
        val isMpinRegistered = mpinPrefs.contains(studentId)
        if (isMpinRegistered) {
            _authState.value = AuthState.MPINLoginRequired(studentId)
        } else {
            _authState.value = AuthState.MPINRegistrationRequired(studentId)
        }
    }

    fun registerMPIN(studentId: String, mpin: String) {
        if (mpin.length < 4) {
            _authState.value = AuthState.Error("MPIN must be 4 digits.")
            return
        }
        mpinPrefs.edit().putString(studentId, mpin).apply()
        completeLogin(studentId)
    }

    fun verifyMPIN(studentId: String, mpin: String) {
        val savedMpin = mpinPrefs.getString(studentId, null)
        if (savedMpin == mpin) {
            completeLogin(studentId)
        } else {
            _authState.value = AuthState.Error("Incorrect MPIN.")
        }
    }

    private fun completeLogin(studentId: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_LOGGED_IN_STUDENT_ID, studentId)
            .apply()
        
        val student = parentRepo.findStudent(studentId)
        if (student != null) {
            _selectedStudentDetails.value = buildStudentDetails(student)
        }
        
        _authState.value = AuthState.Authenticated(studentId)
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_LOGGED_IN_STUDENT_ID)
            .remove(KEY_LOGGED_IN_PARENT_ID)
            .apply()
        _authState.value = AuthState.Unauthenticated
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

    fun changeSubdomain() {
        prefs.edit().remove(KEY_SUBDOMAIN).apply()
        _subdomain.value = null
        _apiUrl.value = null
        logout()
        _authState.value = AuthState.SubdomainRequired
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

    fun getSelectedStudentDetails(): StudentDetails? = _selectedStudentDetails.value

    private fun buildStudentDetails(student: StudentProfile): StudentDetails {
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
            numRemarks = if (attendance > 90f) 1 else 2,
            subjectPerformances = subjectPerformances
        )
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}
