package com.example.shivompharmacy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import android.os.Build
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.shivompharmacy.data.model.Medicine
import com.example.shivompharmacy.ui.navigation.Screen
import com.example.shivompharmacy.ui.screens.*
import com.example.shivompharmacy.ui.theme.ShivompharmacyTheme
import com.example.shivompharmacy.viewmodel.PharmacyViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            ShivompharmacyTheme {
                PharmacyApp()
            }
        }
    }
}

@Composable
fun PharmacyApp() {
    val navController = rememberNavController()
    val viewModel: PharmacyViewModel = viewModel()
    val authState by viewModel.authState.collectAsState()
    val lastAddedMedicine by viewModel.lastAddedMedicine.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onNextScreen = {
                    val target = if (authState) Screen.Home.route else Screen.Login.route
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Login.route) {
                LoginScreen(viewModel = viewModel, onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToMedicineListing = { navController.navigate(Screen.MedicineListing.route) },
                    onNavigateToProductDetail = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToPrescription = { navController.navigate(Screen.Prescription.route) },
                    onNavigateToReminder = { navController.navigate(Screen.ReminderList.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                    onNavigateToHealthArticle = { title, category, imageRes -> 
                        navController.navigate(Screen.HealthArticleDetail.createRoute(title, category, imageRes))
                    }
                )
            }
            composable(
                route = Screen.HealthArticleDetail.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType },
                    navArgument("category") { type = NavType.StringType },
                    navArgument("imageRes") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: ""
                val category = backStackEntry.arguments?.getString("category") ?: ""
                val imageRes = backStackEntry.arguments?.getInt("imageRes") ?: 0
                HealthArticleDetailScreen(
                    title = title,
                    category = category,
                    imageRes = imageRes,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Notifications.route) {
                NotificationScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.MedicineListing.route) {
                MedicineListingScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onMedicineClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) }
                )
            }
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.IntType })
            ) { backStackEntry ->
                val medicineId = backStackEntry.arguments?.getInt("medicineId") ?: 0
                ProductDetailScreen(
                    medicineId = medicineId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onCheckoutClick = { navController.navigate(Screen.Checkout.route) }
                )
            }
            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onOrderPlaced = {
                        navController.navigate("order_success") {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
            composable("order_success") {
                OrderSuccessScreen(
                    onContinueShopping = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onViewOrderDetails = {
                        navController.navigate(Screen.OrderHistory.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    },
                    onOrderHistoryClick = { navController.navigate(Screen.OrderHistory.route) },
                    onFavoritesClick = { navController.navigate(Screen.Favorites.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onMedicineClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.OrderHistory.route) {
                OrderHistoryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Prescription.route) {
                PrescriptionScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.ReminderList.route) {
                val reminderViewModel: com.example.shivompharmacy.viewmodel.ReminderViewModel = viewModel()
                com.example.shivompharmacy.ui.screens.reminder.ReminderListScreen(
                    viewModel = reminderViewModel,
                    onBackClick = { navController.popBackStack() },
                    onAddReminderClick = { navController.navigate(Screen.AddReminder.createRoute(-1)) },
                    onEditReminderClick = { id -> navController.navigate(Screen.AddReminder.createRoute(id)) }
                )
            }
            composable(
                route = Screen.AddReminder.route,
                arguments = listOf(navArgument("reminderId") { type = NavType.IntType })
            ) { backStackEntry ->
                val reminderId = backStackEntry.arguments?.getInt("reminderId") ?: -1
                val reminderViewModel: com.example.shivompharmacy.viewmodel.ReminderViewModel = viewModel()
                com.example.shivompharmacy.ui.screens.reminder.AddReminderScreen(
                    viewModel = reminderViewModel,
                    reminderId = reminderId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // Add to Cart Popup Overlay
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            AddToCartPopup(
                medicine = lastAddedMedicine,
                onDismiss = { viewModel.dismissAddToCartPopup() },
                onClick = {
                    navController.navigate(Screen.Cart.route)
                    viewModel.dismissAddToCartPopup()
                }
            )
        }
    }
}

@Composable
fun AddToCartPopup(medicine: Medicine?, onDismiss: () -> Unit, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = medicine != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 50.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (medicine != null) {
                LaunchedEffect(medicine) {
                    delay(2500)
                    onDismiss()
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(80.dp)
                        .clickable { onClick() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White
                        ) {
                            AsyncImage(
                                model = medicine.imageRes,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Added to Cart!",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = medicine.name,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
