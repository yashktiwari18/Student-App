package com.studentprofile.app

sealed class Screen(val route: String) {
    object Intro : Screen("intro")
    object Login : Screen("login")
    object Signup : Screen("signup")
}