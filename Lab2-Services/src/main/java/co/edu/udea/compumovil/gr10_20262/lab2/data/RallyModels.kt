package co.edu.udea.compumovil.gr10_20262.lab2.data

data class Account(val name: String, val number: Int, val balance: Float, val colorHex: Long)

data class Bill(val name: String, val due: String, val amount: Float, val colorHex: Long)

data class ExchangeRateResponse(val result: String, val base_code: String, val rates: Map<String, Double>)
