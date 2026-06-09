package com.studentprofile.app

/**
 * Sealed interface representing all possible authentication states.
 * Used as the single source of truth for the UI layer to decide which screen to render.
 */
sealed interface AuthState {

    /** Initial / logged-out state — user sees the Intro/Login/Signup flow. */
    object Unauthenticated : AuthState

    /** A login or signup request is being processed (for future async calls). */
    object Loading : AuthState

    /**
     * User has successfully authenticated.
     * @param studentId The authenticated student's login ID.
     */
    data class Authenticated(val studentId: String) : AuthState

    /**
     * Parent credentials validated but parent must select which child profile to enter.
     * @param parentId the authenticated parent's login id
     */
    data class ParentAuthenticated(val parentId: String) : AuthState

    /**
     * Authentication attempt failed.
     * @param message A user-facing error message explaining the failure.
     */
    data class Error(val message: String) : AuthState
}
