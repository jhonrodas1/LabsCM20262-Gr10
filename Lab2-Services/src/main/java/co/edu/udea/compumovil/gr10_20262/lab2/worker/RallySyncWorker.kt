package co.edu.udea.compumovil.gr10_20262.lab2.worker

import android.content.Context
import androidx.annotation.Keep
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.edu.udea.compumovil.gr10_20262.lab2.data.RallyRepository
import co.edu.udea.compumovil.gr10_20262.lab2.data.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Keep
class RallySyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val response = RetrofitClient.api.getLatestRates()
            
            // Extraer la tasa para COP, y usar un valor por defecto en caso de no encontrarse
            val copRate = response.rates?.get("COP") ?: 4200.0
            
            // Formatear la hora actual en formato HH:mm:ss
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            
            // Actualizar el repositorio con los nuevos datos
            RallyRepository.updateExchangeRate(copRate, currentTime)
            
            Result.success()
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            // En caso de que falle la petición (ej. sin internet), indicamos a WorkManager que reintente
            Result.retry()
        }
    }
}
