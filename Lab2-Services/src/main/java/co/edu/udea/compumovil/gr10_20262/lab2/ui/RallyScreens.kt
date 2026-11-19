package co.edu.udea.compumovil.gr10_20262.lab2.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

fun formatUsdCop(usd: Float, rate: Double): String {
    val cop = usd * rate
    return String.format(Locale.US, "$%,.2f USD  •  $%,.0f COP", usd, cop)
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
    onSyncNow: () -> Unit
) {
    val copRate by RallyRepository.copRate.collectAsState()
    val lastSync by RallyRepository.lastSyncTime.collectAsState()

    val totalAccountsUsd = RallyRepository.accounts.map { it.balance }.sum()
    val totalBillsUsd = RallyRepository.bills.map { it.amount }.sum()

    var isSyncing by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(RallyBackground).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RallyCard)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(text = stringResource(R.string.exchange_rate), color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.exchange_rate_format, copRate),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = RallyGreen, strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = {
                                isSyncing = true
                                onSyncNow()
                                coroutineScope.launch {
                                    delay(1000)
                                    isSyncing = false
                                    showSuccess = true
                                    delay(2000)
                                    showSuccess = false
                                }
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.sync_now), tint = RallyGreen)
                            }
                        }
                    }
                    if (showSuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
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
                onSeeAllClick = onNavigateToAccounts
            )
        }

        item {
            OverviewSummaryCard(
                title = stringResource(R.string.bills),
                amountUsd = totalBillsUsd,
                rate = copRate,
                onSeeAllClick = onNavigateToBills
            )
        }
    }
}

@Composable
fun OverviewSummaryCard(title: String, amountUsd: Float, rate: Double, onSeeAllClick: () -> Unit) {
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
                Text(text = title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onSeeAllClick) {
                    Text(text = stringResource(R.string.see_all), color = RallyGreen)
                }
            }
            Text(
                text = formatUsdCop(amountUsd, rate),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AccountsScreen() {
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
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
            items(RallyRepository.accounts) { account ->
                AccountRow(account, copRate)
                HorizontalDivider(color = RallyCard, thickness = 1.dp)
            }
        }
    }
}

@Composable
fun BillsScreen() {
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
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
            items(RallyRepository.bills) { bill ->
                BillRow(bill, copRate)
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
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.LightGray)
        Spacer(modifier = Modifier.height(24.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            RallyAnimatedCircle(proportions, colors, Modifier.fillMaxSize())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.total), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(
                    text = formatUsdCop(amountUsd, rate),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AccountRow(account: Account, rate: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().height(68.dp).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(4.dp).fillMaxHeight().background(Color(account.colorHex)))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = account.name, color = Color.White, style = MaterialTheme.typography.bodyLarge)
            Text(text = stringResource(R.string.account_mask, account.number), color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = formatUsdCop(account.balance, rate),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun BillRow(bill: Bill, rate: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().height(68.dp).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(4.dp).fillMaxHeight().background(Color(bill.colorHex)))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = bill.name, color = Color.White, style = MaterialTheme.typography.bodyLarge)
            Text(text = "${stringResource(R.string.due)} ${bill.due}", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = formatUsdCop(bill.amount, rate),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
