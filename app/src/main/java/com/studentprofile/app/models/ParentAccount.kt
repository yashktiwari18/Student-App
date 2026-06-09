package com.studentprofile.app.models

/**
 * Represents a parent account which may own one or more student profiles.
 * Stored locally as part of the app's lightweight persistence schema.
 */
data class ParentAccount(
    val parentId: String,                 // Login ID (email or username)
    val password: String,                 // Local password (kept for compatibility with existing auth)
    val children: List<StudentProfile> = emptyList() // All student profiles belonging to this parent
)
