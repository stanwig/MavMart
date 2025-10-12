package com.example.mavmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mavmart.ui.theme.MavMartTheme
import androidx.compose.foundation.layout.*        
import androidx.compose.material3.*               
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment              
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MavMartTheme {
                Surface(Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground) {

                    val nav = rememberNavController()

                    NavHost(navController = nav, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onBuyer = { nav.navigate("login/buyer") },
                                onSeller = { nav.navigate("login/seller") },
                                onAdmin  = { nav.navigate("login/admin") },
                                onRegister = { nav.navigate("register") }
                            )
                        }
                        composable("login/buyer") {
                            AuthLoginScreen(
                                role = Role.Buyer,
                                onBack = { nav.popBackStack() },
                                onSubmit = { email, password ->
                                    // TODO: auth Buyer here
                                    nav.popBackStack() // return to login
                                }
                            )
                        }
                        composable("login/seller") {
                            AuthLoginScreen(
                                role = Role.Seller,
                                onBack = { nav.popBackStack() },
                                onSubmit = { email, password ->
                                    // TODO: auth Seller here
                                    nav.popBackStack() // return to login
                                }
                            )
                        }
                        composable("login/admin") {
                            AuthLoginScreen(
                                role = Role.Admin,
                                onBack = { nav.popBackStack() },
                                onSubmit = { email, password ->
                                    // TODO: auth Admin here
                                    nav.popBackStack() // return to login
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onBack = { nav.popBackStack() },
                                onSubmit = { role, first, last, email, password ->
                                    // TODO: send to server / save locally
                                    nav.popBackStack() // return to login
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onBuyer: () -> Unit,
    onSeller: () -> Unit,
    onAdmin: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Centered block
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "MavMart",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onBuyer,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Login as Buyer") }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onSeller,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Login as Seller") }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onAdmin,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Login as Admin") }
            }

            // Bottom button
            Button(
                onClick = onRegister,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)      // gap from the bottom
                    .navigationBarsPadding()      // avoids gesture bar
            ) { Text("Register") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MavMartTheme { LoginScreen({}, {}, {}, {}) }
}
