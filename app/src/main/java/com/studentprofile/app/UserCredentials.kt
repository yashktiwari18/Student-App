package com.studentprofile.app

/**
 * Simple data class holding a registered user's credentials.
 * Used for local-only validation during the frontend prototyping phase.
 * Will be replaced by a proper backend / Room entity in production.
 */
data class UserCredentials(
    val studentId: String,
    val password: String
)
