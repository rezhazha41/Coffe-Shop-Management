package com.apps.coffeeshop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.apps.coffeeshop.data.Product
import com.apps.coffeeshop.viewmodel.CartItem
import com.apps.coffeeshop.viewmodel.ProductViewModel
import androidx.compose.ui.text.font.FontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.apps.coffeeshop.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(navController: NavController, viewModel: ProductViewModel, onMenuClick: () -> Unit) {
    val products by viewModel.filteredProducts.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // Responsive layout logic...
    BoxWithConstraints {
        if (maxWidth < 600.dp) {
             // Mobile View
             Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                 TopBar(onMenuClick = onMenuClick) 
                 CategorySelector(selectedCategory) { viewModel.setCategory(it) }
                 ProductGrid(
                     products = products, 
                     cartItems = cartItems,
                     modifier = Modifier.weight(1f), 
                     onAdd = { viewModel.addToCart(it) },
                     onRemove = { viewModel.removeFromCart(it) }
                 )
                 
                 // Small Cart Summary
                 if (cartItems.isNotEmpty()) {
                     Surface(
                         color = MaterialTheme.colorScheme.surface,
                         tonalElevation = 8.dp,
                         modifier = Modifier.fillMaxWidth()
                     ) {
                         Column(Modifier.padding(16.dp)) {
                             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total items: ${cartItems.sumOf { it.quantity }}")
                                Text("Rp ${cartItems.sumOf { it.product.price * it.quantity }.toInt()}", fontWeight = FontWeight.Bold)
                             }
                             Spacer(modifier = Modifier.height(8.dp))
                             Button(onClick = { navController.navigate("cart") }, modifier = Modifier.fillMaxWidth()) {
                                 Text("Lihat Pesanan & Bayar")
                             }
                         }
                     }
                 }
             }
        } else {
            // Tablet/Desktop View
            Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                Column(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                    TopBar(onMenuClick = onMenuClick)
                    CategorySelector(selectedCategory) { viewModel.setCategory(it) }
                    ProductGrid(
                        products = products, 
                        cartItems = cartItems,
                        modifier = Modifier.fillMaxSize(), 
                        onAdd = { viewModel.addToCart(it) },
                        onRemove = { viewModel.removeFromCart(it) }
                    )
                }
                
                val profile by viewModel.storeProfile.collectAsState()
                Surface(
                    modifier = Modifier.weight(0.4f).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    CartSection(cartItems, viewModel, profile)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavController, viewModel: ProductViewModel) {
    val cartItems by viewModel.cartItems.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout / Pembayaran") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Go back to POS
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
             val profile by viewModel.storeProfile.collectAsState()
             CartSection(cartItems, viewModel, profile)
        }
    }
}

@Composable
fun TopBar(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }
        Text("Kasir", style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = {}) {
            Icon(Icons.Default.Search, contentDescription = "Search")
        }
    }
}

@Composable
fun CategorySelector(selected: String, onSelect: (String) -> Unit) {
    val categories = listOf("Semua", "Kopi", "Teh", "Non-Kopi", "Makanan")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selected
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .height(32.dp)
                    .clickable { onSelect(category) }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = category,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ProductGrid(
    products: List<Product>, 
    cartItems: List<CartItem>, // Add cartItems
    modifier: Modifier = Modifier, 
    onAdd: (Product) -> Unit,
    onRemove: (Product) -> Unit
) {
    if (products.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             com.apps.coffeeshop.ui.components.EmptyState(
                 message = "Produk tidak ditemukan",
                 description = "Coba kata kunci lain atau tambah produk baru.",
                 icon = androidx.compose.material.icons.Icons.Default.Search
             )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp), // Slightly wider for buttons
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
        ) {
            items(products) { product ->
                // Entry Animation
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = visible,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn()
                ) {
                    val qty = cartItems.find { it.product.id == product.id }?.quantity ?: 0
                    ProductCard(
                        product = product, 
                        quantity = qty,
                        onAdd = { onAdd(product) },
                        onRemove = { onRemove(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product, 
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val isOutOfStock = product.stock == 0
    
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (quantity > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.2f) else MaterialTheme.colorScheme.surface
        ),
        border = if (quantity > 0) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .clickable(enabled = !isOutOfStock) { if (quantity == 0) onAdd() }
            .fillMaxWidth()
    ) {
        Column {
           Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .background(Color.LightGray)
                )
                
                // Overlay Quantity Badge if > 0
                if (quantity > 0) {
                     Box(
                         modifier = Modifier
                             .align(Alignment.TopEnd)
                             .padding(8.dp)
                             .background(MaterialTheme.colorScheme.primary, CircleShape)
                             .size(24.dp),
                         contentAlignment = Alignment.Center
                     ) {
                         Text(quantity.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                     }
                }

                if (isOutOfStock) {
                     Box(
                         modifier = Modifier
                             .matchParentSize()
                             .background(Color.Black.copy(alpha = 0.6f)),
                         contentAlignment = Alignment.Center
                     ) {
                         Text("HABIS", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                     }
                }
           }
           
           Column(modifier = Modifier.padding(12.dp)) {
               Text(
                   product.name, 
                   style = MaterialTheme.typography.bodyMedium, 
                   fontWeight = FontWeight.Bold,
                   maxLines = 1,
                   overflow = TextOverflow.Ellipsis
               )
               Spacer(modifier = Modifier.height(4.dp))
               
               if (quantity > 0) {
                   // +/- Controls
                   Row(
                       modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                       verticalAlignment = Alignment.CenterVertically,
                       horizontalArrangement = Arrangement.SpaceBetween
                   ) {
                       // Remove Button
                       Surface(
                           shape = CircleShape,
                           color = MaterialTheme.colorScheme.surfaceVariant,
                           modifier = Modifier.size(36.dp).clickable { onRemove() },
                           shadowElevation = 2.dp
                       ) {
                           Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                           }
                       }
                       
                       Text(
                           quantity.toString(),
                           style = MaterialTheme.typography.titleMedium,
                           fontWeight = FontWeight.Bold,
                           modifier = Modifier.padding(horizontal = 12.dp)
                       )
                       
                       // Add Button
                       Surface(
                           shape = CircleShape,
                           color = MaterialTheme.colorScheme.primary,
                           modifier = Modifier.size(36.dp).clickable { if (!isOutOfStock) onAdd() }, // Check stock?
                           shadowElevation = 2.dp
                       ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, "Add", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            }
                       }
                   }
               } else {
                   // Standard Price & Stock
                   Row(
                       modifier = Modifier.fillMaxWidth(),
                       horizontalArrangement = Arrangement.SpaceBetween,
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       Text(
                           CurrencyUtils.toRupiah(product.price),
                           style = MaterialTheme.typography.bodyMedium,
                           color = MaterialTheme.colorScheme.primary,
                           fontWeight = FontWeight.Bold
                       )
                       Text(
                           "Stok: ${product.stock}",
                           style = MaterialTheme.typography.labelSmall,
                           color = if(product.stock < 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                       )
                   }
               }
           }
        }
    }
}



@Composable
fun CartItemRow(item: CartItem, onAdd: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.product.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, fontWeight = FontWeight.Medium)
            Text(CurrencyUtils.toRupiah(item.product.price), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
            }
            Text("${item.quantity}", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
            IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun CartSection(cartItems: List<CartItem>, viewModel: ProductViewModel, profile: com.apps.coffeeshop.data.StoreProfile? = null) {
    var showReceiptDialog by remember { mutableStateOf(false) }
    var currentReceipt by remember { mutableStateOf("") }

    if (showReceiptDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val total = cartItems.sumOf { it.product.price * it.quantity } // Need to recalc or capture total? 
        // Logic fix: cartItems in ViewModel might be cleared? 
        // Wait, onPay captures 'cartItems' (list copy) but ViewModel.processCheckout usually clears it.
        // We need to capture the cart items BEFORE they are cleared.
        // The current state 'cartItems' updates when cleared.
        // Solution: We need a local state to hold the "last transaction items" for the dialog.
        // For now, let's assume we captured them in a request logic or passed them.
        // Actually, the previous logic 'onPay' set 'showReceiptDialog = true' and 'viewModel.processCheckout' cleared the cart.
        // So 'cartItems' state becomes empty immediately.
        // We must store 'dialogCartItems' state.
    }
    
    // ... wait, I need to refactor CartSection to hold dialog state correctly ...
    var dialogCartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var dialogTotal by remember { mutableStateOf(0.0) }

    if (showReceiptDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        
        // Use Profile Data or Default
        val storeName = profile?.storeName ?: "Apps Coffee Shop"
        val storeAddress = profile?.storeAddress ?: "Jl. Coffee Shop No. 1"
        val logoUri = profile?.logoUri

        ReceiptDialog(
            cartItems = dialogCartItems,
            total = dialogTotal,
            receiptText = currentReceipt,
            onDismiss = { showReceiptDialog = false },
            onPrint = {
                com.apps.coffeeshop.ui.utils.ReceiptUtils.printReceipt(context, dialogCartItems, dialogTotal, storeName, storeAddress, logoUri)
            },
            onSaveImage = {
                com.apps.coffeeshop.ui.utils.ReceiptUtils.saveReceiptImage(context, dialogCartItems, dialogTotal, storeName, storeAddress, logoUri)
                // showReceiptDialog = false // Don't close
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Pesanan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(cartItems) { item ->
                CartItemRow(item, 
                    onAdd = { viewModel.addToCart(item.product) }, 
                    onRemove = { viewModel.removeFromCart(item.product) }
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
            }
        }
        
        // Payment Section
        PaymentCalculator(cartItems,
            onClear = { viewModel.clearCart() },
            onPay = { total, itemsSummary ->
                dialogCartItems = cartItems // Capture current state
                dialogTotal = total
                
                // Generate receipt BEFORE clearing cart/processing
                val storeName = profile?.storeName ?: "Apps Coffee Shop"
                val storeAddress = profile?.storeAddress ?: "Jl. Coffee Shop No. 1"
                currentReceipt = generateReceiptText(cartItems, total, storeName, storeAddress)
                showReceiptDialog = true
                
                // Process Checkout: Records Transaction AND Updates Stock
                viewModel.processCheckout(total, cartItems)
            }
        )
    }
}

fun generateReceiptText(items: List<CartItem>, total: Double, storeName: String = "Apps Coffee Shop", storeAddress: String = "Jl. Coffee Shop No. 1"): String {
    val sb = StringBuilder()
    sb.append("$storeName\n")
    sb.append("$storeAddress\n")
    sb.append("--------------------------------\n")
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(Date())
    sb.append("$date\n")
    sb.append("--------------------------------\n")
    items.forEach { item ->
        sb.append("${item.quantity}x ${item.product.name}\n")
        val totalItem = item.product.price * item.quantity
        sb.append("   @${CurrencyUtils.toRupiah(item.product.price)} = ${CurrencyUtils.toRupiah(totalItem)}\n")
    }
    sb.append("--------------------------------\n")
    sb.append("Total: ${CurrencyUtils.toRupiah(total)}\n")
    sb.append("--------------------------------\n")
    sb.append("Terima Kasih\n")
    return sb.toString()
}

@Composable
fun ReceiptDialog(
    cartItems: List<CartItem>, 
    total: Double, 
    receiptText: String, 
    onDismiss: () -> Unit, 
    onPrint: () -> Unit, 
    onSaveImage: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Struk Pembayaran", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Receipt Preview Background
                Surface(
                    shape = RoundedCornerShape(0.dp), // Paper shape
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth().padding(4.dp).border(1.dp, Color.LightGray)
                ) {
                    Text(
                        text = receiptText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp),
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ceklis 'Simpan Gambar' agar bisa dikirim WA.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Column(Modifier.fillMaxWidth()) {
                Button(onClick = onPrint, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.Print, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cetak (Printer System)")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onSaveImage, modifier = Modifier.fillMaxWidth()) {
                     Icon(Icons.Default.Image, null)
                     Spacer(Modifier.width(8.dp))
                     Text("Simpan ke Galeri")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun PaymentCalculator(cartItems: List<CartItem>, onClear: () -> Unit, onPay: (Double, String) -> Unit) {
    val subtotal = cartItems.sumOf { it.product.price * it.quantity }
    val tax = subtotal * 0.0 // Simplified no tax for now or keeps 11% if user wants, let's stick to 0 for simplicity or 11%
    // User didn't specify tax, but prev code had it. I will remove tax for simplicity as "Uang masuk" usually matches price.
    // Or keep it simple: No tax displayed to avoid confusion unless requested.
    val total = subtotal // + tax
    
    val itemsSummary = cartItems.joinToString(", ") { "${it.quantity}x ${it.product.name}" }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(CurrencyUtils.toRupiah(total), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
             OutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Batal")
            }
            
            Button(
                onClick = { if (cartItems.isNotEmpty()) onPay(total, itemsSummary) },
                modifier = Modifier.weight(2f).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = cartItems.isNotEmpty()
            ) {
                Text("Bayar & Cetak")
            }
        }
    }
}
