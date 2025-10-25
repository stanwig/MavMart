package com.example.mavmart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onSubmit: (role: Role, first: String, last: String, email: String, password: String) -> Unit = { _,_,_,_,_ -> }
) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val brandPrimary = Color(0xFF0A2647)
    val brandAccent  = Color(0xFFFF8C00)
    val outlineColor = Color(0xFFE5E7EB)

    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        snackbarHost = { SnackbarHost(hostState = snackbar) },
        bottomBar = {
            Button(
                onClick = {
                    val user = User(
                        first = first.trim(),
                        last = last.trim(),
                        email = email.trim().lowercase(),
                        password = password,
                        role = Role.User
                    )
                    val id = db.insertUser(user)
                    if (id > 0) {
                        onSubmit(Role.User, first, last, email, password)
                        onBack()
                    } else {
                        scope.launch {
                            snackbar.showSnackbar(
                                message = "Email already exists.",
                                actionLabel = "Dismiss",
                                withDismissAction = true,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                enabled = first.isNotBlank() && last.isNotBlank() && email.isNotBlank() && password.length >= 6,
                modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandAccent,
                    contentColor = Color.White,
                    disabledContainerColor = brandAccent.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) { Text("Create Account") }
        }
    ) { inner ->
        Column(
            modifier = Modifier.padding(inner).padding(horizontal = 20.dp, vertical = 12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = first, onValueChange = { first = it },
                label = { Text("First Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandPrimary, unfocusedBorderColor = outlineColor,
                    focusedLabelColor = brandPrimary, cursorColor = brandPrimary
                )
            )
            OutlinedTextField(
                value = last, onValueChange = { last = it },
                label = { Text("Last Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandPrimary, unfocusedBorderColor = outlineColor,
                    focusedLabelColor = brandPrimary, cursorColor = brandPrimary
                )
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandPrimary, unfocusedBorderColor = outlineColor,
                    focusedLabelColor = brandPrimary, cursorColor = brandPrimary
                )
            )
            Text(
                text = "Tip: Use your UTA email if you are a current student.",
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.05.sp, fontSize = 12.sp),
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            )
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                visualTransformation = VisualTransformation.None,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = brandPrimary, unfocusedBorderColor = outlineColor,
                    focusedLabelColor = brandPrimary, cursorColor = brandPrimary
                )
            )
            Spacer(Modifier.weight(1f))
        }
    }
}