package co.edu.udea.compumovil.gr10_20262.lab2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import co.edu.udea.compumovil.gr10_20262.lab2.ui.RallyApp
import co.edu.udea.compumovil.gr10_20262.lab2.ui.theme.Labs20262Gr10Theme
import co.edu.udea.compumovil.gr10_20262.lab2.worker.RallySyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupWorkManager()

        setContent {
            Labs20262Gr10Theme {
                RallyApp(onSyncNow = { triggerManualSync() })
            }
        }
    }

    private fun setupWorkManager() {
        val workManager = WorkManager.getInstance(applicationContext)

        // a) Tarea periódica cada 15 minutos con restricción de red
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<RallySyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "RallySync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )

        // b) One-Time Worker inicial para sincronizar inmediatamente al iniciar la app
        triggerManualSync()
    }

    private fun triggerManualSync() {
        val workManager = WorkManager.getInstance(applicationContext)
        val oneTimeRequest = OneTimeWorkRequestBuilder<RallySyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        workManager.enqueue(oneTimeRequest)
    }
}
