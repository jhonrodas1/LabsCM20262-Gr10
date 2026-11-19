package co.edu.udea.compumovil.gr10_20262.lab2.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RallyRepository {
    val accounts = listOf(
        Account("Checking", 1234, 2215.13f, 0xFF004940),
        Account("Home Savings", 5678, 8676.88f, 0xFF005D57),
        Account("Car Savings", 9012, 987.48f, 0xFF04B97F),
        Account("Vacation", 3456, 253.0f, 0xFF37EFBA)
    )

    val bills = listOf(
        Bill("RedPay Credit", "Jan 29", 45.36f, 0xFFFFDC78),
        Bill("Rent", "Feb 9", 1200.0f, 0xFFFF6951),
        Bill("TabFine Credit", "Feb 22", 87.33f, 0xFFFFD7D0),
        Bill("ABC Loans", "Feb 29", 400.0f, 0xFFFFAC12)
    )

    private val _copRate = MutableStateFlow(4200.0)
    val copRate: StateFlow<Double> = _copRate.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("--:--")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    fun updateExchangeRate(rate: Double, time: String) {
        _copRate.value = rate
        _lastSyncTime.value = time
    }

    fun getAccount(name: String?): Account? {
        if (name.isNullOrEmpty()) return null
        return accounts.find { it.name.equals(name, ignoreCase = true) }
    }

    fun getBill(name: String?): Bill? {
        if (name.isNullOrEmpty()) return null
        return bills.find { it.name.equals(name, ignoreCase = true) }
    }
}
