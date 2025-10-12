package com.example.mavmart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

enum class Role { Buyer, Seller, Admin }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onSubmit: (role: Role, first: String, last: String, email: String, password: String) -> Unit,
) {
    var role by remember { mutableStateOf(Role.Buyer) }
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Submit button pinned to bottom
            Button(
                onClick = { onSubmit(role, first, last, email, password) },
                enabled = first.isNotBlank() && last.isNotBlank()
                        && email.isNotBlank() && password.length >= 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
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
            // Role selector
            Text("Role")
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = role == Role.Buyer,
                    onClick = { role = Role.Buyer },
                    label = { Text("Buyer") }
                )
                FilterChip(
                    selected = role == Role.Seller,
                    onClick = { role = Role.Seller },
                    label = { Text("Seller") }
                )
            }

            // First Name
            OutlinedTextField(
                value = first,
                onValueChange = { first = it },
                label = { Text("First Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            // Last Name
            OutlinedTextField(
                value = last,
                onValueChange = { last = it },
                label = { Text("Last Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                keyboardOptions = KeyboardOptions.Default,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f)) // push fields up if there’s extra space
        }
    }
}