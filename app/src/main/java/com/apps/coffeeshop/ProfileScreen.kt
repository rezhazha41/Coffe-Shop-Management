package com.apps.coffeeshop

import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.apps.coffeeshop.viewmodel.ProductViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProductViewModel) {
    val profile by viewModel.storeProfile.collectAsState()
    val currentBalance by viewModel.currentBalance.collectAsState()
    
    var storeName by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }
    var printerMac by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cashierPassword by remember { mutableStateOf("") }
    var isDarkMode by remember { mutableStateOf(false) }
    
    var showCapitalDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    
    // Load initial data
    LaunchedEffect(profile) {
        profile?.let {
            storeName = it.storeName
            storeAddress = it.storeAddress
            printerMac = it.printerMacAddress
            username = it.username
            password = it.password
            cashierPassword = it.cashierPassword
            isDarkMode = it.isDarkMode
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Store Settings ---
            SettingsSection("Pengaturan Toko") {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Nama Toko") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = storeAddress,
                    onValueChange = { storeAddress = it },
                    label = { Text("Alamat Toko") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            
            // --- Appearance ---
            SettingsSection("Tampilan") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mode Gelap (Dark Mode)")
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { checked ->
                            isDarkMode = checked
                            // Auto-save to trigger immediate theme change in MainActivity
                            viewModel.saveProfile(
                                storeName, storeAddress, printerMac, 
                                username, password, cashierPassword, 
                                checked, profile?.logoUri
                            )
                        }
                    )
                }
                
                // Logo Picker
                Spacer(Modifier.height(8.dp))
                Text("Logo Toko", style = MaterialTheme.typography.bodyMedium)
                
                val context = androidx.compose.ui.platform.LocalContext.current
                val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    uri?.let { viewModel.updateLogo(context, it) }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .border(1.dp, Color.Gray, androidx.compose.foundation.shape.CircleShape)
                            .clip(androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile?.logoUri != null) {
                            coil.compose.AsyncImage(
                                model = java.io.File(profile!!.logoUri!!),
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Print, "Default Logo", tint = Color.Gray)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(onClick = { 
                        launcher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Text("Ganti Logo")
                    }
                }
            }

            // --- Security ---
            SettingsSection("Keamanan (Login)") {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username Admin") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password Admin") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                 OutlinedTextField(
                    value = cashierPassword,
                    onValueChange = { cashierPassword = it },
                    label = { Text("Password Kasir (Username: kasir)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
            
            // --- Balance Management ---
            SettingsSection("Manajemen Saldo") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Saldo Saat Ini", style = MaterialTheme.typography.labelMedium)
                        Text(
                            CurrencyUtils.toRupiah(currentBalance), 
                            style = MaterialTheme.typography.headlineMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showCapitalDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.AttachMoney, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Input Modal")
                    }
                    Button(
                        onClick = { showWithdrawDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Icon(Icons.Default.MoneyOff, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tarik Saldo")
                    }
                }
            }

            // --- Printer Settings ---
            SettingsSection("Pengaturan Printer") {
                OutlinedTextField(
                    value = printerMac,
                    onValueChange = { printerMac = it }, 
                    label = { Text("MAC Address Printer") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.Print, "Printer") },
                    supportingText = { Text("Format: 00:11:22:33:44:55") }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { 
                    viewModel.saveProfile(storeName, storeAddress, printerMac, username, password, cashierPassword, isDarkMode, profile?.logoUri)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Simpan Pengaturan")
            }
        }
    }
    
    if (showCapitalDialog) {
        AmountDialog(
            title = "Input Modal Awal",
            onDismiss = { showCapitalDialog = false },
            onConfirm = { amount ->
                viewModel.addTransaction("CAPITAL", amount, note = "Modal Awal")
                showCapitalDialog = false
            }
        )
    }
    
    if (showWithdrawDialog) {
        AmountDialog(
            title = "Tarik Saldo",
            onDismiss = { showWithdrawDialog = false },
            onConfirm = { amount ->
                viewModel.addTransaction("WITHDRAW", amount, note = "Penarikan Saldo")
                showWithdrawDialog = false
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

@Composable
fun AmountDialog(title: String, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }

    fun formatNumber(input: String): String {
        val clean = input.filter { it.isDigit() }
        if (clean.isEmpty()) return ""
        return try {
            val number = clean.toLong()
            java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(number)
        } catch (e: Exception) {
            clean
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { 
                    amount = formatNumber(it)
                },
                label = { Text("Nominal") },
                prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val cleanAmount = amount.filter { it.isDigit() }.toDoubleOrNull()
                if (cleanAmount != null && cleanAmount > 0) {
                    onConfirm(cleanAmount)
                }
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
