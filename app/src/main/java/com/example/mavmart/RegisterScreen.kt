package com.example.mavmart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

enum class Role { User, Admin }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onSubmit: (role: Role, first: String, last: String, email: String, password: String) -> Unit,
) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // --- Color palette ---
    val brandPrimary = Color(0xFF0A2647)   // deep navy
    val brandAccent = Color(0xFFFF8C00)    // vibrant orange
    val textColor = Color(0xFF111827)      // near-black
    val outlineColor = Color(0xFFE5E7EB)   // light gray

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Register", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = brandPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Button(
                onClick = { onSubmit(Role.User, first, last, email, password) }, // always User
                enabled = first.isNotBlank() && last.isNotBlank()
                        && email.isNotBlank() && password.length >= 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandAccent,
                    contentColor = Color.White,
                    disabledContainerColor = brandAccent.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text("Create Account")
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = first,
                onValueChange = { first = it },
                label = { Text("First Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandPrimary,
                    unfocusedBorderColor = outlineColor,
                    focusedLabelColor = brandPrimary,
                    cursorColor = brandPrimary
                )
            )
            OutlinedTextField(
                value = last,
                onValueChange = { last = it },
                label = { Text("Last Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandPrimary,
                    unfocusedBorderColor = outlineColor,
                    focusedLabelColor = brandPrimary,
                    cursorColor = brandPrimary
                )
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandPrimary,
                    unfocusedBorderColor = outlineColor,
                    focusedLabelColor = brandPrimary,
                    cursorColor = brandPrimary
                )
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandPrimary,
                    unfocusedBorderColor = outlineColor,
                    focusedLabelColor = brandPrimary,
                    cursorColor = brandPrimary
                )
            )
            Spacer(Modifier.weight(1f))
        }
    }
}
