package com.apps.coffeeshop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.apps.coffeeshop.data.Product
import com.apps.coffeeshop.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductScreen(navController: NavController, viewModel: ProductViewModel) {
    val products by viewModel.allProducts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToRestock by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Produk") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(products) { product ->
                ListItem(
                    headlineContent = { Text(product.name) },
                    supportingContent = { Text("${CurrencyUtils.toRupiah(product.price)} - ${product.category} (Stok: ${product.stock})") },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { 
                                // Trigger Restock Dialog
                                productToRestock = product 
                            }) {
                                Icon(Icons.Default.ShoppingCart, "Restock", tint = Color(0xFF4CAF50))
                            }
                            IconButton(onClick = { productToEdit = product }) {
                                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteProduct(product) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                )
                Divider()
            }
        }
    }

    if (showAddDialog) {
        ProductDialog(
            title = "Tambah Produk",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, price, category, imageUrl, stock ->
                viewModel.addProduct(name, price, category, imageUrl, stock)
                showAddDialog = false
            }
        )
    }

    if (productToEdit != null) {
        ProductDialog(
            title = "Edit Produk",
            initialName = productToEdit!!.name,
            initialPrice = productToEdit!!.price,
            initialCategory = productToEdit!!.category,
            initialImageUrl = productToEdit!!.imageUrl,
            initialStock = productToEdit!!.stock,
            onDismiss = { productToEdit = null },
            onConfirm = { name, price, category, imageUrl, stock ->
                val updatedProduct = productToEdit!!.copy(
                    name = name, 
                    price = price, 
                    category = category, 
                    imageUrl = imageUrl, 
                    stock = stock
                )
                viewModel.updateProduct(updatedProduct)
                productToEdit = null
            }
        )
    }
    
    if (productToRestock != null) {
        RestockDialog(
            productName = productToRestock!!.name,
            onDismiss = { productToRestock = null },
            onConfirm = { qty, cost ->
                viewModel.restockProduct(productToRestock!!, qty, cost)
                productToRestock = null
            }
        )
    }
}

@Composable
fun RestockDialog(productName: String, onDismiss: () -> Unit, onConfirm: (Int, Double) -> Unit) {
    var qty by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restock: $productName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Masukkan jumlah barang yang dibeli dan total biayanya.", style = MaterialTheme.typography.bodyMedium)
                
                OutlinedTextField(
                    value = qty,
                    onValueChange = { if (it.all { char -> char.isDigit() }) qty = it },
                    label = { Text("Jumlah (Qty)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = cost,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) cost = input
                    },
                    label = { Text("Total Biaya Belanja (Rp)") },
                    visualTransformation = com.apps.coffeeshop.ui.utils.CurrencyVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Biaya akan dicatat sebagai Pengeluaran.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        },
        confirmButton = {
            Button(onClick = {
                val q = qty.toIntOrNull() ?: 0
                val c = cost.toDoubleOrNull() ?: 0.0
                if (q > 0) onConfirm(q, c)
            }) {
                Text("Simpan Stok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ProductDialog(
    title: String,
    initialName: String = "",
    initialPrice: Double = 0.0,
    initialCategory: String = "Kopi",
    initialImageUrl: String = "",
    initialStock: Int = 0,
    onDismiss: () -> Unit, 
    onConfirm: (String, Double, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    // Initial state: Raw digits string
    var price by remember { mutableStateOf(if (initialPrice > 0) initialPrice.toLong().toString() else "") }
    var category by remember { mutableStateOf(initialCategory) }
    var imageUrl by remember { mutableStateOf(initialImageUrl) }
    var stock by remember { mutableStateOf(if (initialStock > 0) initialStock.toString() else "") }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val pickMedia = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val flag = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flag)
            imageUrl = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Produk") })
                
                // Price Input with Auto-Formatting (VisualTransformation)
                OutlinedTextField(
                    value = price, 
                    onValueChange = { input ->
                        // Store only digits in the state
                        if (input.all { it.isDigit() }) {
                            price = input
                        }
                    }, 
                    label = { Text("Harga") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    visualTransformation = com.apps.coffeeshop.ui.utils.CurrencyVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stock, 
                    onValueChange = { if (it.all { char -> char.isDigit() }) stock = it }, 
                    label = { Text("Stok") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") })
                
                Text("Gambar Produk:", style = MaterialTheme.typography.bodyMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable { 
                            pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = imageUrl,
                            contentDescription = "Selected Image",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Pilih Gambar")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // Price is already raw string
                val p = price.toDoubleOrNull() ?: 0.0
                val s = stock.toIntOrNull() ?: 0
                onConfirm(name, p, category, imageUrl, s)
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
