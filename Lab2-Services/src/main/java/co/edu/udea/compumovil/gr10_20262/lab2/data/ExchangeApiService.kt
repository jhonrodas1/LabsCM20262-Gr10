package co.edu.udea.compumovil.gr10_20262.lab2.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ExchangeApiService {
    @GET("v6/latest/USD")
    suspend fun getLatestRates(): ExchangeRateResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://open.er-api.com/"

    val api: ExchangeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeApiService::class.java)
    }
}
