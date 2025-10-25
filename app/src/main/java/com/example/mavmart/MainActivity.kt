package com.example.mavmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mavmart.ui.theme.MavMartTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MavMartTheme {
                val brandPrimary = Color(0xFF0A2647)
                val backgroundColor = Color(0xFFF8F9FA)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor,
                    contentColor = brandPrimary
                ) {
                    val nav = rememberNavController()

                    // track the logged-in user id (null when logged out)
                    var currentUserId by rememberSaveable { mutableStateOf<Long?>(null) }

                    NavHost(navController = nav, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onUser = { nav.navigate("login/user") },
                                onAdmin = { nav.navigate("login/admin") },
                                onRegister = { nav.navigate("register") }
                            )
                        }

                        // User login
                        composable("login/user") {
                            UserLoginScreen(
                                onBack = { nav.popBackStack() },
                                onSuccess = { userId ->           // capture userId
                                    currentUserId = userId
                                    nav.navigate("listings") {
                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("login/admin") {
                            AdminLoginScreen(
                                onBack = { nav.popBackStack() },
                                onSuccess = {
                                    nav.navigate("admin") {
                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                onBack = { nav.popBackStack() }
                            )
                        }

                        composable("listings") {
                            ListingsScreen(
                                currentUserId = currentUserId,     // pass it in
                                onLogout = {
                                    currentUserId = null
                                    nav.navigate("login") {
                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        // Admin dashboard with tabs
                        composable("admin") {
                            AdminDashboardScreen(
                                onLogout = {
                                    nav.navigate("login") {
                                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}