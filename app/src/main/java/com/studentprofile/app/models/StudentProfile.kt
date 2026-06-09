package com.studentprofile.app.models

/**
 * Represents an individual student profile.
 * This is a lightweight model intended for local storage and UI binding.
 */
data class StudentProfile(
    val studentId: String,        // Unique identifier for the student (used as session target)
    val displayName: String,      // Full name shown in UI
    val classInfo: String,        // e.g. "Class 10 - A"
    val section: String? = null,  // Optional section (e.g., "A")
    val admissionId: String? = null, // Optional admission number
    val avatarResId: Int? = null     // Optional drawable resource id for avatar
)
