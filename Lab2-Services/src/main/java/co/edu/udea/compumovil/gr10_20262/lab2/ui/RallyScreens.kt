package co.edu.udea.compumovil.gr10_20262.lab2.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.udea.compumovil.gr10_20262.lab2.R
import co.edu.udea.compumovil.gr10_20262.lab2.data.Account
import co.edu.udea.compumovil.gr10_20262.lab2.data.Bill
import co.edu.udea.compumovil.gr10_20262.lab2.data.RallyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

val RallyBackground = Color(0xFF33333D)
val RallyCard = Color(0xFF373740)
val RallyGreen = Color(0xFF1EB980)
val RallyOrange = Color(0xFFFF6859)

fun formatUsd(usd: Float): String {
    return String.format(Locale.US, "$%,.2f USD", usd)
}

fun formatCop(usd: Float, rate: Double): String {
    val cop = usd * rate
    return String.format(Locale.US, "$%,.0f COP", cop)
}

fun formatExchangeRate(rate: Double): String {
    return String.format(Locale.US, "$%,.2f COP", rate)
}

@Composable
fun RallyAnimatedCircle(
    proportions: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        var startAngle = -90f
        for (i in proportions.indices) {
            val sweep = proportions[i] * 360f
            if (sweep > 0) {
                drawArc(
                    color = colors[i],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
                startAngle += sweep
            }
        }
    }
}

@Composable
fun OverviewScreen(
    onNavigateToAccounts: () -> Unit,
    onNavigateToBills: () -> Unit,
    onSyncNow: () -> Unit,
    onAccountClick: (String) -> Unit = {},
    onBillClick: (String) -> Unit = {}
) {
    val copRate by RallyRepository.copRate.collectAsState()
    val lastSync by RallyRepository.lastSyncTime.collectAsState()

    val totalAccountsUsd = RallyRepository.accounts.map { it.balance }.sum()
    val totalBillsUsd = RallyRepository.bills.map { it.amount }.sum()

    var isSyncing by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RallyBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RallyCard)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.exchange_rate),
                        color = Color.LightGray,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1 USD = ${formatExchangeRate(copRate)}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.last_sync_format, stringResource(R.string.last_sync), lastSync),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = RallyGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    isSyncing = true
                                    onSyncNow()
                                    coroutineScope.launch {
                                        delay(1000)
                                        isSyncing = false
                                        showSuccess = true
                                        delay(2000)
                                        showSuccess = false
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.sync_now),
                                    tint = RallyGreen
                                )
                            }
                        }
                    }
                    if (showSuccess) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ Sincronizado / Synced",
                            color = RallyGreen,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        item {
            OverviewSummaryCard(
                title = stringResource(R.string.accounts),
                amountUsd = totalAccountsUsd,
                rate = copRate,
                onSeeAllClick = onNavigateToAccounts,
                itemsContent = {
                    RallyRepository.accounts.take(2).forEach { account ->
                        AccountRow(account = account, rate = copRate, onClick = { onAccountClick(account.name) })
                        HorizontalDivider(color = RallyBackground, thickness = 1.dp)
                    }
                }
            )
        }

        item {
            OverviewSummaryCard(
                title = stringResource(R.string.bills),
                amountUsd = totalBillsUsd,
                rate = copRate,
                onSeeAllClick = onNavigateToBills,
                itemsContent = {
                    RallyRepository.bills.take(2).forEach { bill ->
                        BillRow(bill = bill, rate = copRate, onClick = { onBillClick(bill.name) })
                        HorizontalDivider(color = RallyBackground, thickness = 1.dp)
                    }
                }
            )
        }
    }
}

@Composable
fun OverviewSummaryCard(
    title: String,
    amountUsd: Float,
    rate: Double,
    onSeeAllClick: () -> Unit,
    itemsContent: @Composable ColumnScope.() -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RallyCard)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onSeeAllClick) {
                    Text(text = stringResource(R.string.see_all), color = RallyGreen)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatUsd(amountUsd),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "≈ ${formatCop(amountUsd, rate)}",
                color = RallyGreen,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = RallyBackground, thickness = 1.dp)
            itemsContent()
        }
    }
}

@Composable
fun AccountsScreen(
    onAccountClick: (String) -> Unit = {}
) {
    val copRate by RallyRepository.copRate.collectAsState()
    val amounts = RallyRepository.accounts.map { it.balance }
    val totalAccountsUsd = amounts.sum()
    val proportions = amounts.map { if (totalAccountsUsd > 0) it / totalAccountsUsd else 0f }
    val colors = RallyRepository.accounts.map { Color(it.colorHex) }

    Column(modifier = Modifier.fillMaxSize().background(RallyBackground)) {
        TotalHeader(
            title = stringResource(R.string.accounts),
            amountUsd = totalAccountsUsd,
            rate = copRate,
            proportions = proportions,
            colors = colors
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(RallyRepository.accounts) { account ->
                AccountRow(account = account, rate = copRate, onClick = { onAccountClick(account.name) })
                HorizontalDivider(color = RallyCard, thickness = 1.dp)
            }
        }
    }
}

@Composable
fun BillsScreen(
    onBillClick: (String) -> Unit = {}
) {
    val copRate by RallyRepository.copRate.collectAsState()
    val amounts = RallyRepository.bills.map { it.amount }
    val totalBillsUsd = amounts.sum()
    val proportions = amounts.map { if (totalBillsUsd > 0) it / totalBillsUsd else 0f }
    val colors = RallyRepository.bills.map { Color(it.colorHex) }

    Column(modifier = Modifier.fillMaxSize().background(RallyBackground)) {
        TotalHeader(
            title = stringResource(R.string.bills),
            amountUsd = totalBillsUsd,
            rate = copRate,
            proportions = proportions,
            colors = colors
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(RallyRepository.bills) { bill ->
                BillRow(bill = bill, rate = copRate, onClick = { onBillClick(bill.name) })
                HorizontalDivider(color = RallyCard, thickness = 1.dp)
            }
        }
    }
}

@Composable
fun TotalHeader(
    title: String,
    amountUsd: Float,
    rate: Double,
    proportions: List<Float>,
    colors: List<Color>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.LightGray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(210.dp)
        ) {
            RallyAnimatedCircle(proportions, colors, Modifier.fillMaxSize())
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.total).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatUsd(amountUsd),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCop(amountUsd, rate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RallyGreen,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AccountRow(account: Account, rate: Double, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(36.dp)
                .background(Color(account.colorHex), shape = RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = account.name,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.account_mask, account.number),
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatUsd(account.balance),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatCop(account.balance, rate),
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun BillRow(bill: Bill, rate: Double, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(36.dp)
                .background(Color(bill.colorHex), shape = RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = bill.name,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${stringResource(R.string.due)} ${bill.due}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatUsd(bill.amount),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatCop(bill.amount, rate),
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountName: String?,
    onBackClick: () -> Unit
) {
    val copRate by RallyRepository.copRate.collectAsState()
    val account = remember(accountName) { RallyRepository.getAccount(accountName) }

    Scaffold(
        containerColor = RallyBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = account?.name ?: stringResource(R.string.account_details),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RallyBackground)
            )
        }
    ) { innerPadding ->
        if (account == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Cuenta no encontrada", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RallyCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(50.dp)
                                    .background(Color(account.colorHex), shape = RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = account.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.account_mask, account.number),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RallyCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.balance_usd),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatUsd(account.balance),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = RallyBackground, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.balance_cop),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatCop(account.balance, copRate),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = RallyGreen
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RallyCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.account_details),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            HorizontalDivider(color = RallyBackground, thickness = 1.dp)
                            DetailRow(label = stringResource(R.string.account_number), value = "•••• ${account.number}")
                            DetailRow(label = stringResource(R.string.status), value = stringResource(R.string.active), valueColor = RallyGreen)
                            DetailRow(label = stringResource(R.string.exchange_rate), value = formatExchangeRate(copRate))
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RallyCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.recent_activity),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            HorizontalDivider(color = RallyBackground, thickness = 1.dp)
                            ActivityRow(title = "Supermercado", date = "Ayer", amount = "-$45.20 USD")
                            ActivityRow(title = "Depósito de nómina", date = "Hace 3 días", amount = "+$1,200.00 USD", isCredit = true)
                            ActivityRow(title = "Cafetería", date = "Hace 5 días", amount = "-$4.50 USD")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDetailScreen(
    billName: String?,
    onBackClick: () -> Unit
) {
    val copRate by RallyRepository.copRate.collectAsState()
    val bill = remember(billName) { RallyRepository.getBill(billName) }

    Scaffold(
        containerColor = RallyBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = bill?.name ?: stringResource(R.string.bill_details),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RallyBackground)
            )
        }
    ) { innerPadding ->
        if (bill == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Factura no encontrada", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RallyCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(50.dp)
                                    .background(Color(bill.colorHex), shape = RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bill.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${stringResource(R.string.due)} ${bill.due}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RallyCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.balance_usd),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatUsd(bill.amount),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = RallyBackground, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.balance_cop),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatCop(bill.amount, copRate),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = RallyGreen
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RallyCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.bill_details),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            HorizontalDivider(color = RallyBackground, thickness = 1.dp)
                            DetailRow(label = stringResource(R.string.due_date), value = bill.due)
                            DetailRow(label = stringResource(R.string.status), value = stringResource(R.string.pending), valueColor = RallyOrange)
                            DetailRow(label = stringResource(R.string.exchange_rate), value = formatExchangeRate(copRate))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ActivityRow(title: String, date: String, amount: String, isCredit: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Text(text = date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCredit) RallyGreen else Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview
@Composable
fun OverviewScreenPreview() {
    OverviewScreen(onNavigateToAccounts = {}, onNavigateToBills = {}, onSyncNow = {})
}

@Preview
@Composable
fun AccountsScreenPreview() {
    AccountsScreen()
}

@Preview
@Composable
fun BillsScreenPreview() {
    BillsScreen()
}

@Preview
@Composable
fun AccountDetailScreenPreview() {
    AccountDetailScreen(accountName = "Checking", onBackClick = {})
}

@Preview
@Composable
fun BillDetailScreenPreview() {
    BillDetailScreen(billName = "Rent", onBackClick = {})
}


