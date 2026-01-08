package com.apps.coffeeshop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import com.apps.coffeeshop.R

@Composable
fun LoginScreen(navController: NavController, viewModel: com.apps.coffeeshop.viewmodel.ProductViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val profile by viewModel.storeProfile.collectAsState()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Modern Logo Asset
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color.White)
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.2f), androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val logoUri = profile?.logoUri
                        if (logoUri != null) {
                            coil.compose.AsyncImage(
                                model = logoUri,
                                contentDescription = "Logo",
                                modifier = Modifier.size(110.dp).clip(androidx.compose.foundation.shape.CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(110.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        "Selamat Datang", 
                        style = MaterialTheme.typography.headlineSmall, 
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Silakan login untuk melanjutkan", 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                            }
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Go
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onGo = {
                                focusManager.clearFocus()
                                val currentProfile = profile
                                
                                val isAdmin = if (currentProfile != null) {
                                    username == currentProfile.username && password == currentProfile.password
                                } else {
                                    username == "admin" && password == "admin123"
                                }
                                
                                val isCashier = if (currentProfile != null) {
                                    username == "kasir" && password == currentProfile.cashierPassword
                                } else {
                                    username == "kasir" && password == "kasir123"
                                }

                                if (isAdmin) {
                                    viewModel.setSession(com.apps.coffeeshop.viewmodel.ProductViewModel.UserRole.ADMIN)
                                    navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                                } else if (isCashier) {
                                    viewModel.setSession(com.apps.coffeeshop.viewmodel.ProductViewModel.UserRole.CASHIER)
                                    navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                                } else {
                                    error = "Username atau Password salah!"
                                    scope.launch { snackbarHostState.showSnackbar(error) }
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (error.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = {
                            val currentProfile = profile
                            
                            val isAdmin = if (currentProfile != null) {
                                username == currentProfile.username && password == currentProfile.password
                            } else {
                                username == "admin" && password == "admin123"
                            }
                            
                            val isCashier = if (currentProfile != null) {
                                username == "kasir" && password == currentProfile.cashierPassword
                            } else {
                                username == "kasir" && password == "kasir123"
                            }

                            if (isAdmin) {
                                viewModel.setSession(com.apps.coffeeshop.viewmodel.ProductViewModel.UserRole.ADMIN)
                                navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                            } else if (isCashier) {
                                viewModel.setSession(com.apps.coffeeshop.viewmodel.ProductViewModel.UserRole.CASHIER)
                                navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                            } else {
                                error = "Username atau Password salah!"
                                scope.launch { snackbarHostState.showSnackbar(error) }
                            }
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Masuk", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
