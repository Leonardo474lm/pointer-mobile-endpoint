package com.example.scan.navigation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.scan.view.HomeScreen
import com.example.scan.view.SplashScreen
@Composable
fun AppNavigation() {
    val modifier = Modifier
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "",
        modifier = modifier.fillMaxSize()
    ) {


    }
}