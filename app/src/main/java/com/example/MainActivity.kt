package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.DhabaViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.home.HomeScreen
import com.example.ui.menu.MenuScreen
import com.example.ui.cart.CartScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.order.OrderTrackingScreen
import com.example.ui.theme.LucknowDhabaTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LucknowDhabaTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    val navController = rememberNavController()
    val viewModel: DhabaViewModel = viewModel()

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(onAuthSuccess = { currentUser = Firebase.auth.currentUser })
    } else {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") { 
                HomeScreen(
                    navController = navController, 
                    viewModel = viewModel
                ) 
            }
            composable("menu") { 
                MenuScreen(
                    navController = navController, 
                    viewModel = viewModel
                ) 
            }
            composable("cart") { 
                CartScreen(
                    navController = navController, 
                    viewModel = viewModel
                ) 
            }
            composable("chat") {
                ChatScreen(navController = navController)
            }
            composable("order_tracking/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderTrackingScreen(
                    orderId = orderId,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}
