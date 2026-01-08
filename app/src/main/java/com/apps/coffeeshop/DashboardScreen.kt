package com.apps.coffeeshop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.max
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apps.coffeeshop.data.Product
import com.apps.coffeeshop.data.Transaction
import com.apps.coffeeshop.ui.components.EmptyState
import com.apps.coffeeshop.ui.theme.*
import com.apps.coffeeshop.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(onMenuClick: () -> Unit, viewModel: ProductViewModel = viewModel()) {
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalSales by viewModel.totalSales.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val balance by viewModel.currentBalance.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val topSelling by viewModel.topSellingProducts.collectAsState()
    
    // Trend Data
    val salesTrendDataState by viewModel.salesTrendData.collectAsState()
    val (incomeTrend, quantityTrend, totalPeriodIncome) = salesTrendDataState
    val trendRange by viewModel.trendRange.collectAsState()
    val profile by viewModel.storeProfile.collectAsState()
    
    // Logic for Stock Alert
    val lowStockProducts = products.filter { it.stock < 10 }
    val context = androidx.compose.ui.platform.LocalContext.current

    // State for Chart Mode
    var chartType by remember { mutableStateOf("Bar") } // "Bar" or "Line"
    
    // State for Data Source (Pendapatan vs Terjual)
    var isIncomeMode by remember { mutableStateOf(true) } // true = Income, false = Qty
    
    // Derived Data
    val salesTrend = if (isIncomeMode) incomeTrend else quantityTrend
    val safeMax = if (salesTrend.isNotEmpty()) salesTrend.maxOf { it.second }.coerceAtLeast(1f) else 1f
    
    // Dialog State
    val showStatDetail = remember { mutableStateOf<Pair<String, String>?>(null) }

    Scaffold(
        topBar = { 
            DashboardTopBar(
                onMenuClick = onMenuClick,
                onExportClick = { 
                    com.apps.coffeeshop.ui.utils.ReportUtils.generateReport(
                        context,
                        allTransactions,
                        totalIncome,
                        totalExpense,
                        profile?.logoUri
                    )
                }
            ) 
        }
    ) { paddingValues ->
        val isWideScreen = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp > 600.dp
        
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (isWideScreen) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(0.6f)) {
                        StatsSection(totalIncome, totalExpense, balance) { label, value ->
                             showStatDetail.value = label to value
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (lowStockProducts.isNotEmpty()) {
                            StockWarningSection(lowStockProducts)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        SalesChartSection(
                            totalIncome = totalPeriodIncome, 
                            salesTrend = salesTrend, 
                            currentRange = trendRange, 
                            onRangeSelected = { viewModel.setTrendRange(it) },
                            chartType = chartType,
                            onChartTypeChange = { chartType = it },
                            isIncomeMode = isIncomeMode,
                            onModeChange = { isIncomeMode = it },
                            safeMax = safeMax
                        )
                    }
                    Column(modifier = Modifier.weight(0.4f)) {
                         RecentTransactionsSection(allTransactions.take(8))
                         Spacer(modifier = Modifier.height(16.dp))
                         TopSellingMenuSection(topSelling)
                    }
                }
            } else {
                StatsSection(totalIncome, totalExpense, balance) { label, value ->
                     showStatDetail.value = label to value
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (lowStockProducts.isNotEmpty()) {
                    StockWarningSection(lowStockProducts)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                SalesChartSection(
                    totalIncome = totalPeriodIncome, 
                    salesTrend = salesTrend, 
                    currentRange = trendRange, 
                    onRangeSelected = { viewModel.setTrendRange(it) },
                    chartType = chartType,
                    onChartTypeChange = { chartType = it },
                    isIncomeMode = isIncomeMode,
                    onModeChange = { isIncomeMode = it },
                    safeMax = safeMax
                )
                Spacer(modifier = Modifier.height(16.dp))
                TopSellingMenuSection(topSelling)
                Spacer(modifier = Modifier.height(16.dp))
                RecentTransactionsSection(allTransactions.take(5))
            }
        }
        
        // ... Dialog ...
        if (showStatDetail.value != null) {
            val (title, value) = showStatDetail.value!!
            AlertDialog(
                onDismissRequest = { showStatDetail.value = null },
                confirmButton = { TextButton(onClick = { showStatDetail.value = null }) { Text("Tutup") } },
                title = { Text(title) },
                text = { 
                    Text(
                        value, 
                        style = MaterialTheme.typography.displaySmall, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun DashboardTopBar(onMenuClick: () -> Unit, onExportClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f), // Transparent background matches theme
        shadowElevation = 4.dp, // Consistent shadow
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Hamburger + Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) {
                     Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Right: Export Button
            IconButton(onClick = onExportClick) {
                Icon(
                    imageVector = Icons.Default.Share, 
                    contentDescription = "Cetak Laporan",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ... TopSellingMenuSection & TopSellingItem unchanged ...
@Composable
fun TopSellingMenuSection(topSelling: List<Pair<String, Int>>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = "Menu Terlaris",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )
            
            if (topSelling.isEmpty()) {
                 EmptyState(
                     message = "Belum ada data penjualan",
                     description = "Lakukan transaksi untuk melihat menu terlaris.",
                     icon = Icons.Default.EmojiFoodBeverage
                 )
            } else {
                topSelling.forEachIndexed { index, (name, count) ->
                    TopSellingItem("${index + 1}.", name, "${count}x terjual")
                    if (index < topSelling.lastIndex) Divider()
                }
            }
        }
    }
}

@Composable
fun TopSellingItem(rank: String, name: String, count: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(rank, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.width(28.dp))
            Text(name, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(count, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun StatsSection(income: Double, expense: Double, balance: Double, onStatClick: (String, String) -> Unit) {
    // Animation specific
    val animatedBalance = remember { androidx.compose.animation.core.Animatable(0f) }
    
    LaunchedEffect(balance) {
        animatedBalance.animateTo(
            targetValue = balance.toFloat(),
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
             Box(modifier = Modifier.weight(1f)) { StatCard("Pemasukan", CurrencyUtils.toRupiah(income), Color(0xFF4CAF50), onClick = { onStatClick("Pemasukan", CurrencyUtils.toRupiah(income)) }) }
             Box(modifier = Modifier.weight(1f)) { StatCard("Pengeluaran", CurrencyUtils.toRupiah(expense), Color(0xFFF44336), onClick = { onStatClick("Pengeluaran", CurrencyUtils.toRupiah(expense)) }) }
        }
        StatCard(
            "Saldo Saat Ini", 
            CurrencyUtils.toRupiah(animatedBalance.value.toDouble()), 
            Color(0xFF2196F3),
            isPrimary = true,
            onClick = { onStatClick("Saldo Saat Ini", CurrencyUtils.toRupiah(balance)) }
        )
    }
}

@Composable
fun StatCard(label: String, value: String, valueColor: Color, isPrimary: Boolean = false, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPrimary) 4.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label, 
                style = MaterialTheme.typography.labelLarge, 
                color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Fix: Use ViewBox-like logic or maxLines with scaling. For now, we allow wrapping but line height controlled.
            Text(
                text = value, 
                style = if (value.length > 15) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall, 
                color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else valueColor, 
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

// ... StockWarningSection unchanged ...
@Composable
fun StockWarningSection(lowStockProducts: List<Product>) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF3E0),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF6C00),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Stok Menipis!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = "${lowStockProducts.size} produk perlu restock.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            lowStockProducts.take(3).forEach { product ->
               Text("• ${product.name} (Sisa: ${product.stock})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start=36.dp, bottom=4.dp))
            }
        }
    }
}

@Composable
fun SalesChartSection(
    totalIncome: Double,
    salesTrend: List<Pair<String, Float>>,
    currentRange: ProductViewModel.TrendRange,
    onRangeSelected: (ProductViewModel.TrendRange) -> Unit,
    chartType: String,
    onChartTypeChange: (String) -> Unit,
    isIncomeMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    safeMax: Float
) {
    var isChartDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Title with Dropdown
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isChartDropdownExpanded = true }) {
                        Text("Trend Penjualan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Icon(Icons.Default.ArrowDropDown, "Select Chart")
                        
                        DropdownMenu(
                            expanded = isChartDropdownExpanded,
                            onDismissRequest = { isChartDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("Grafik Batang") }, onClick = { onChartTypeChange("Bar"); isChartDropdownExpanded = false })
                            DropdownMenuItem(text = { Text("Grafik Garis") }, onClick = { onChartTypeChange("Line"); isChartDropdownExpanded = false })
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    
                    // Total Display (Animates?)
                    Text(
                        "Total: ${CurrencyUtils.toRupiah(totalIncome)}", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }
                
                // Toggle Buttons (7/30 Days)
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrendToggleButton(text = "7 Hari", isSelected = currentRange == ProductViewModel.TrendRange.DAYS_7, onClick = { onRangeSelected(ProductViewModel.TrendRange.DAYS_7) })
                    Spacer(modifier = Modifier.width(4.dp))
                    TrendToggleButton(text = "30 Hari", isSelected = currentRange == ProductViewModel.TrendRange.DAYS_30, onClick = { onRangeSelected(ProductViewModel.TrendRange.DAYS_30) })
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Data Type Toggle (Income vs Qty)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha=0.3f), RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    DataTypeButton("Pendapatan", isIncomeMode) { onModeChange(true) }
                    DataTypeButton("Terjual", !isIncomeMode) { onModeChange(false) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Chart Content
            if (salesTrend.isEmpty() || salesTrend.all { it.second == 0f }) {
                 Box(modifier = Modifier.fillMaxSize().height(200.dp), contentAlignment = Alignment.Center) {
                     Text("Belum ada data penjualan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                 }
            } else {
                if (chartType == "Bar") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp) // Increased height more for padding
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 12.dp), // Padding Bottom Container
                        horizontalArrangement = if (currentRange == ProductViewModel.TrendRange.DAYS_7) Arrangement.SpaceEvenly else Arrangement.spacedBy(16.dp), // More space between bars
                        verticalAlignment = Alignment.Bottom
                    ) {
                        salesTrend.forEachIndexed { index, (label, value) ->
                            val animatedProgress = remember { androidx.compose.animation.core.Animatable(0f) }
                            LaunchedEffect(value) {
                                 animatedProgress.animateTo(
                                     targetValue = value / safeMax,
                                     animationSpec = androidx.compose.animation.core.tween(durationMillis = 800, delayMillis = index * 30)
                                 )
                            }
                             ChartBar(
                                 label = label,
                                 value = value,
                                 heightFill = if (animatedProgress.value < 0.05f && value > 0) 0.05f else animatedProgress.value,
                                 isHighlight = label == salesTrend.last().first,
                                 isIncomeMode = isIncomeMode // Pass mode for formatter
                             )
                        }
                    }
                } else {
                    LineChart(
                        data = salesTrend,
                        maxVal = safeMax,
                        isIncomeMode = isIncomeMode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(bottom = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DataTypeButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text, 
            style = MaterialTheme.typography.labelSmall, 
            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TrendToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50)) // Pill shape matching container
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp) // Larger touch target
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = textColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ChartBar(label: String, value: Float, heightFill: Float, isHighlight: Boolean = false, isIncomeMode: Boolean = true, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxHeight()
            .width(40.dp)
            .padding(top = 8.dp), // Add padding top to push down if needed, but important is padding inside column
        verticalArrangement = Arrangement.Bottom
    ) {
        // Value Text
        if (value > 0) {
            val displayText = if (isIncomeMode) {
                CurrencyUtils.toSimpleString(value.toDouble()) 
            } else {
                value.toInt().toString()
            }
            
            Text(
                text = displayText, 
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp, 
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        Box(
            modifier = Modifier
                .width(20.dp) 
                .fillMaxHeight(if (heightFill <= 0) 0.01f else heightFill)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.height(8.dp)) // Increased Padding Bottom
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            maxLines = 1,
            fontSize = 9.sp
        )
    }
}

@Composable
fun RecentTransactionsSection(transactions: List<Transaction>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transaksi Terakhir", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                
                Text("Lihat Semua", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
            }
            
            if (transactions.isEmpty()) {
                 EmptyState(
                     message = "Belum ada transaksi",
                     description = "Transaksi baru akan muncul di sini.",
                     icon = Icons.Default.LocalCafe
                 )
            } else {
                transactions.forEachIndexed { index, trx ->
                    TransactionItem(trx)
                    if (index < transactions.lastIndex) Divider()
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val date = Date(transaction.date)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(date)
    
    val isIncome = transaction.type == "IN" || transaction.type == "CAPITAL"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if(isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if(isIncome) Icons.Default.LocalCafe else Icons.Default.Warning, 
                    contentDescription = null, 
                    tint = if(isIncome) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                val name = if (transaction.itemsJson.isNotEmpty()) {
                    if (transaction.itemsJson.length > 25) transaction.itemsJson.take(25) + "..." else transaction.itemsJson
                } else transaction.note.ifEmpty { "Transaksi" }
                
                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
        Text(
            text = (if (isIncome) "+ " else "- ") + CurrencyUtils.toRupiah(transaction.totalAmount), 
            fontWeight = FontWeight.Bold, 
            style = MaterialTheme.typography.bodyMedium,
            color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

// ... ChartBar unchanged ...

@Composable
fun LineChart(
    data: List<Pair<String, Float>>,
    maxVal: Float,
    isIncomeMode: Boolean = true, // Added param
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    
    val itemWidth = 60.dp
    val totalWidth = itemWidth * data.size
    val minWidth = 350.dp 
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val valueStyle = MaterialTheme.typography.labelSmall
    
    Box(
        modifier = modifier
            .horizontalScroll(scrollState)
            .width(max(totalWidth, minWidth))
            .padding(horizontal = 16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Padding for labels
            val bottomPadding = 36.dp.toPx() // Increased bottom padding to 36dp
            val topPadding = 24.dp.toPx()
            val chartH = h - bottomPadding - topPadding
            
            val step = w / data.size
            val halfStep = step / 2
            
            val points = data.mapIndexed { i, (_, value) ->
                val x = (i * step) + halfStep
                val y = topPadding + (chartH - ((value / maxVal) * chartH))
                androidx.compose.ui.geometry.Offset(x, y)
            }
            
            // Draw Line Path
            if (points.isNotEmpty()) {
                val path = androidx.compose.ui.graphics.Path()
                path.moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) path.lineTo(points[i].x, points[i].y)
                
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
            
            // Draw Points and Labels
            points.forEachIndexed { i, point ->
                // Dot
                drawCircle(Color.White, radius = 6.dp.toPx(), center = point)
                drawCircle(primaryColor, radius = 4.dp.toPx(), center = point)
                
                // Value Label (Top)
                val valueRaw = data[i].second
                val valueText = if (isIncomeMode) CurrencyUtils.toSimpleString(valueRaw.toDouble()) else valueRaw.toInt().toString()
                
                val valueLayout = textMeasurer.measure(
                    text = androidx.compose.ui.text.AnnotatedString(valueText),
                    style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = labelColor)
                )
                drawText(
                    textLayoutResult = valueLayout,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        point.x - (valueLayout.size.width / 2),
                        point.y - valueLayout.size.height - 8.dp.toPx()
                    )
                )
                
                // X-Axis Label (Bottom)
                val labelText = data[i].first
                val labelLayout = textMeasurer.measure(
                    text = androidx.compose.ui.text.AnnotatedString(labelText),
                    style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = labelColor)
                )
                drawText(
                    textLayoutResult = labelLayout,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        point.x - (labelLayout.size.width / 2),
                        h - bottomPadding + 12.dp.toPx() // Push text slightly down from graph area
                    )
                )
            }
        }
    }
}
