package com.studentprofile.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.studentprofile.app.models.StudentProfile
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ──────────────────────────────────────────────
// IntroScreen — Entry point with Login / Sign Up options
// ──────────────────────────────────────────────

@Composable
fun IntroScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome to Student App", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { navController.navigate(R.id.nav_login) }) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate(R.id.nav_signup) }) {
            Text("Sign Up")
        }
    }
}

// ──────────────────────────────────────────────
// SignupScreen — Registers credentials via AuthViewModel
// ──────────────────────────────────────────────

@Composable
fun SignupScreen(navController: NavController, authViewModel: AuthViewModel) {
    var parentId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var registrationErrorMessage by remember { mutableStateOf<String?>(null) }

    // Simple form container for multiple students. Each entry holds name, classInfo, admissionId
    data class StudentForm(
        val name: MutableState<String>,
        val classInfo: MutableState<String>,
        val admissionId: MutableState<String>
    )

    val students = remember {
        mutableStateListOf(
            StudentForm(mutableStateOf(""), mutableStateOf(""), mutableStateOf(""))
        )
    }

    // Observe auth state for validation errors from the ViewModel
    val authState by authViewModel.authState.collectAsState()

    // Surface ViewModel errors as the registration message
    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            registrationErrorMessage = (authState as AuthState.Error).message
            authViewModel.clearError()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Parent Account", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = parentId,
            onValueChange = { parentId = it },
            label = { Text("Parent Login ID (email)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Set Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // Show feedback message (error or success)
        registrationErrorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = if (message.contains("success", ignoreCase = true))
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Student entries ---
        Spacer(modifier = Modifier.height(12.dp))
        Text("Students", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        students.forEachIndexed { index, form ->
            OutlinedTextField(
                value = form.name.value,
                onValueChange = { form.name.value = it },
                label = { Text("Student Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = form.classInfo.value,
                onValueChange = { form.classInfo.value = it },
                label = { Text("Class (e.g. Class 10 - A)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = form.admissionId.value,
                onValueChange = { form.admissionId.value = it },
                label = { Text("Admission ID (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        TextButton(onClick = {
            students.add(StudentForm(mutableStateOf(""), mutableStateOf(""), mutableStateOf("")))
        }) {
            Text("Add Another Student")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // Client-side confirm-password check
                if (password != confirmPassword) {
                    registrationErrorMessage = "Passwords do not match."
                    return@Button
                }

                // Validate parent + students
                val pId = parentId.trim()
                if (pId.isBlank()) {
                    registrationErrorMessage = "Please enter parent login ID."
                    return@Button
                }

                val childProfiles = mutableListOf<StudentProfile>()
                students.forEachIndexed { idx, f ->
                    val name = f.name.value.trim()
                    val cls = f.classInfo.value.trim()
                    val admission = f.admissionId.value.trim().ifEmpty { null }
                    if (name.isBlank() || cls.isBlank()) {
                        registrationErrorMessage = "Each student must have a name and class."
                        return@Button
                    }
                    // Generate a lightweight studentId derived from name + index
                    val genId = name.lowercase()
                        .replace("\\s+".toRegex(), "_")
                        .replace("[^a-z0-9_]", "") + "_" + (idx + 1)
                    childProfiles.add(StudentProfile(studentId = genId, displayName = name, classInfo = cls, admissionId = admission))
                }

                // Delegate to ViewModel — new API to register parent + children
                val success = authViewModel.registerParent(
                    parentId = pId,
                    password = password.trim(),
                    children = childProfiles
                )

                if (success) {
                    registrationErrorMessage = "Registration successful! Redirecting to Login..."
                    // Navigate to Login after successful registration
                    navController.navigate(R.id.nav_login) {
                        popUpTo(R.id.nav_signup) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register & Go to Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = {
            navController.navigate(R.id.nav_login) {
                popUpTo(R.id.nav_signup) { inclusive = true }
            }
        }) {
            Text("Already have an account? Log in")
        }
    }
}

// ──────────────────────────────────────────────
// LoginScreen — Validates credentials via AuthViewModel
// ──────────────────────────────────────────────

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    var studentId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    // Observe auth state reactively
    val authState by authViewModel.authState.collectAsState()

    // React to error state changes from the ViewModel
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Error -> {
                loginErrorMessage = (authState as AuthState.Error).message
                authViewModel.clearError()
            }
            else -> { /* Authenticated is handled by MainActivity switching to XML dashboard */ }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Student Login", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = studentId,
            onValueChange = {
                studentId = it
                loginErrorMessage = null // Clear error on new input
            },
            label = { Text("Login ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                loginErrorMessage = null // Clear error on new input
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // Show login error
        loginErrorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Delegate entirely to the ViewModel
                authViewModel.login(
                    studentId = studentId.trim(),
                    password = password.trim()
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = {
            navController.navigate(R.id.nav_signup)
        }) {
            Text("Don't have an account? Sign up")
        }
    }
}