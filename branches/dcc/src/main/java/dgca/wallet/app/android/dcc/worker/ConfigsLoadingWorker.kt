package dgca.wallet.app.android.dcc.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dgca.wallet.app.android.dcc.data.ConfigRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class ConfigsLoadingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workParams: WorkerParameters,
    private val configRepository: ConfigRepository
) : Worker(context, workParams) {

    override fun doWork(): Result {
        try {
            val config = configRepository.getConfig()
            Timber.d("Config: $config")
        } catch (error: Throwable) {
            Timber.d(error, "Config Loading Error: $error")
            return Result.retry()
        }
        return Result.success()
    }
}