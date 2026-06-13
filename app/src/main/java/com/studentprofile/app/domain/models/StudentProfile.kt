package com.studentprofile.app.domain.models

data class StudentProfile(
    val studentId: String,
    val displayName: String,
    val classInfo: String,
    val section: String? = null,
    val admissionId: String? = null,
    val avatarResId: Int? = null
)
