package co.edu.udea.compumovil.gr10_20262.lab2.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Account(val name: String, val number: Int, val balance: Float, val colorHex: Long)

@Keep
data class Bill(val name: String, val due: String, val amount: Float, val colorHex: Long)

@Keep
data class ExchangeRateResponse(
    @SerializedName("result") val result: String,
    @SerializedName("base_code") val baseCode: String,
    @SerializedName("rates") val rates: Map<String, Double>?
)
