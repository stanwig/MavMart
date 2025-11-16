package com.example.mavmart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(currentUserId: Long) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val cs = MaterialTheme.colorScheme

    var user by remember { mutableStateOf<User?>(null) }
    var showEdit by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        user = db.getUserById(currentUserId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(20.dp)
    ) {
        if (user == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("User not found", color = cs.onSurface)
            }
        } else {
            val u = user!!
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("USER ID: ${u.id}",  color = cs.primary, style = MaterialTheme.typography.titleMedium)
                Text("NAME: ${u.first} ${u.last}", color = cs.primary, style = MaterialTheme.typography.titleMedium)
                Text("EMAIL: ${u.email}", color = cs.primary, style = MaterialTheme.typography.titleMedium)
                Text("ROLE: ${u.role.name}", color = cs.primary, style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { showEdit = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.primary,
                        contentColor = cs.onPrimary
                    )
                ) { Text("EDIT PROFILE") }
            }
        }
    }

    if (showEdit && user != null) {
        EditProfileDialog(
            user = user!!,
            onDismiss = { showEdit = false },
            onSave = { updated ->
                db.updateUser(updated)
                user = db.getUserById(currentUserId)
                showEdit = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var first by remember { mutableStateOf(user.first) }
    var last by remember { mutableStateOf(user.last) }
    var email by remember { mutableStateOf(user.email) }
    var password by remember { mutableStateOf(user.password) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", color = cs.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = first, onValueChange = { first = it },
                    label = { Text("First name") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = last, onValueChange = { last = it },
                    label = { Text("Last name") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password") },
                    textStyle = LocalTextStyle.current.copy(color = cs.onSurface),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        focusedLabelColor = cs.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        user.copy(
                            first = first.trim(),
                            last = last.trim(),
                            email = email.trim().lowercase(),
                            password = password
                        )
                    )
                },
                enabled = first.isNotBlank() && last.isNotBlank() && email.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(contentColor = cs.primary)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = cs.primary)
            ) { Text("Cancel") }
        }
    )
}
