package com.apps.coffeeshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apps.coffeeshop.ui.theme.CoffeeShopTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExitToApp

class MainActivity : ComponentActivity() {
    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as CoffeeShopApplication
        val viewModelFactory = com.apps.coffeeshop.viewmodel.ProductViewModelFactory(app.repository)
        
        setContent {
            val viewModel: com.apps.coffeeshop.viewmodel.ProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = viewModelFactory)
            val navController = rememberNavController()
            val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            
            // Collect Profile & Role
            val profile = viewModel.storeProfile.collectAsState(initial = null).value
            val userRole = viewModel.userRole.collectAsState().value
            val isDarkMode = profile?.isDarkMode ?: false
            
            CoffeeShopTheme(darkTheme = isDarkMode) {
                androidx.compose.material3.ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = userRole != com.apps.coffeeshop.viewmodel.ProductViewModel.UserRole.NONE, // Disable swipe on login
                    drawerContent = {
                        if (userRole != com.apps.coffeeshop.viewmodel.ProductViewModel.UserRole.NONE) {
                             androidx.compose.material3.ModalDrawerSheet {
                                // Drawer Header
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(MaterialTheme.colorScheme.primary), // Match Login Button
                                    contentAlignment = androidx.compose.ui.Alignment.CenterStart
                                ) {
                                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(24.dp)) {
                                        androidx.compose.foundation.layout.Box(
                                             modifier = Modifier
                                                 .size(64.dp)
                                                 .clip(androidx.compose.foundation.shape.CircleShape)
                                                 .background(Color.White),
                                             contentAlignment = androidx.compose.ui.Alignment.Center
                                        ) {
                                            val logoUri = profile?.logoUri
                                            if (logoUri != null) {
                                                coil.compose.AsyncImage(
                                                    model = logoUri,
                                                    contentDescription = "User",
                                                    modifier = Modifier.fillMaxSize().clip(androidx.compose.foundation.shape.CircleShape),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                            } else {
                                                androidx.compose.foundation.Image(
                                                     painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo),
                                                     contentDescription = "User",
                                                     modifier = Modifier.size(56.dp)
                                                )
                                            }
                                        }
                                        androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                                        androidx.compose.material3.Text(
                                            text = if (userRole == com.apps.coffeeshop.viewmodel.ProductViewModel.UserRole.ADMIN) profile?.storeName ?: "Admin" else "Kasir",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary // Contrast text
                                        )
                                        androidx.compose.material3.Text(
                                            text = if (userRole == com.apps.coffeeshop.viewmodel.ProductViewModel.UserRole.ADMIN) "Owner / Administrator" else "Staff / Cashier",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) // Contrast text
                                        )
                                    }
                                }
                                
                                androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                                
                                // Menu Items (Optimized imports omitted for brevity, keeping logic)
                                // ...
                                
                                // ... (Rest of drawer content)
                                
                                androidx.compose.material3.NavigationDrawerItem(
                                    label = { androidx.compose.material3.Text("Dashboard") },
                                    selected = false,
                                    icon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Home, null) },
                                    modifier = Modifier.padding(horizontal=12.dp),
                                    onClick = { 
                                        scope.launch { drawerState.close() }
                                        navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } }
                                    }
                                )

                                androidx.compose.material3.NavigationDrawerItem(
                                    label = { androidx.compose.material3.Text("Kasir (POS)") },
                                    selected = false,
                                    icon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.ShoppingCart, null) },
                                    modifier = Modifier.padding(horizontal=12.dp),
                                    onClick = { 
                                        scope.launch { drawerState.close() }
                                        navController.navigate("pos") 
                                    }
                                )
                                
                                if (userRole == com.apps.coffeeshop.viewmodel.ProductViewModel.UserRole.ADMIN) {
                                    androidx.compose.material3.NavigationDrawerItem(
                                        label = { androidx.compose.material3.Text("Kelola Produk") },
                                        selected = false,
                                        icon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.List, null) },
                                        modifier = Modifier.padding(horizontal=12.dp),
                                        onClick = { 
                                            scope.launch { drawerState.close() }
                                            navController.navigate("manage") 
                                        }
                                    )
                                    androidx.compose.material3.NavigationDrawerItem(
                                        label = { androidx.compose.material3.Text("Profil & Pengaturan") },
                                        selected = false,
                                        icon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Settings, null) },
                                        modifier = Modifier.padding(horizontal=12.dp),
                                        onClick = { 
                                            scope.launch { drawerState.close() }
                                            navController.navigate("profile") 
                                        }
                                    )
                                }
                                
                                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                                androidx.compose.material3.Divider(modifier = Modifier.padding(horizontal = 16.dp))
                                androidx.compose.material3.NavigationDrawerItem(
                                    label = { androidx.compose.material3.Text("Keluar (Logout)", color = MaterialTheme.colorScheme.error) },
                                    selected = false,
                                    icon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
                                    modifier = Modifier.padding(horizontal=12.dp, vertical=16.dp),
                                    onClick = { 
                                        scope.launch { drawerState.close() }
                                        viewModel.logout()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true } // Clear back stack
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController, 
                            startDestination = "login",
                            // Optimized Transitions: Fade only (lighter on low-end GPU)
                            enterTransition = { 
                                androidx.compose.animation.fadeIn(animationSpec = tween(200)) 
                            },
                            exitTransition = { 
                                androidx.compose.animation.fadeOut(animationSpec = tween(200))
                            },
                            popEnterTransition = { 
                                androidx.compose.animation.fadeIn(animationSpec = tween(200)) 
                            },
                            popExitTransition = { 
                                androidx.compose.animation.fadeOut(animationSpec = tween(200))
                            }
                        ) {
                            composable("login") {
                                LoginScreen(navController, viewModel)
                            }
                            composable("dashboard") {
                                DashboardScreen(
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    viewModel = viewModel
                                )
                            }
                            composable("pos") {
                                POSScreen(
                                    navController = navController, 
                                    viewModel = viewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("cart") {
                                CheckoutScreen(navController, viewModel)
                            }
                            composable("manage") {
                                ManageProductScreen(navController, viewModel)
                            }
                            composable("profile") {
                                ProfileScreen(navController, viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
