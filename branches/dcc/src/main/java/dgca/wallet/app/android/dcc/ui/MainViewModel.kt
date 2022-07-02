package dgca.wallet.app.android.dcc.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dgca.wallet.app.android.dcc.Event
import dgca.wallet.app.android.dcc.GreenCertificateFetcher
import dgca.wallet.app.android.dcc.model.toCertificateModel
import dgca.wallet.app.android.dcc.ui.wallet.qr.certificate.ClaimGreenCertificateModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dgca.verifier.app.decoder.model.GreenCertificate
import feature.ticketing.domain.checkin.TicketingCheckInModelFetcher
import feature.ticketing.domain.data.checkin.TicketingCheckInRemote
import feature.ticketing.presentation.model.TicketingCheckInParcelable
import feature.ticketing.presentation.model.fromRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val certificateFetcher: GreenCertificateFetcher,
    private val ticketingCheckInModelFetcher: TicketingCheckInModelFetcher
) : ViewModel() {

    private val _event = MutableLiveData<Event<MainViewEvent>>()
    val event: LiveData<Event<MainViewEvent>> = _event

    private val _modelFetcherResult = MutableLiveData<ModelFetcherResult>()
    val modelFetcherResult: LiveData<ModelFetcherResult> = _modelFetcherResult

    fun init(input: String) {
        if (input.isEmpty()) {
            _event.value = Event(MainViewEvent.InputNotValid)
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val greenCertificateRecognised: ModelFetcherResult.GreenCertificateRecognised? =
                    tryToFetchGreenCertificate(input)
                if (greenCertificateRecognised != null) return@withContext greenCertificateRecognised
                val ticketingCheckInRemoteModel: TicketingCheckInRemote? = runCatching {
                    ticketingCheckInModelFetcher.fetchTicketingCheckInModel(input)
                }.getOrElse {
                    Timber.e(it)
                    null
                }
                if (ticketingCheckInRemoteModel != null) return@withContext ModelFetcherResult.BookingSystemModelRecognised(
                    ticketingCheckInRemoteModel.fromRemote()
                )
                return@withContext ModelFetcherResult.NotApplicable
            }.apply {
                _modelFetcherResult.value = this
            }
        }
    }

    private fun tryToFetchGreenCertificate(qrCodeText: String): ModelFetcherResult.GreenCertificateRecognised? {
        val res: Pair<ByteArray?, GreenCertificate?> = certificateFetcher.fetchDataFromQrString(qrCodeText)
        val cose: ByteArray? = res.first
        val greenCertificate: GreenCertificate? = res.second
        return if (cose != null && greenCertificate != null) {
            val certificateModel = greenCertificate.toCertificateModel()
            ModelFetcherResult.GreenCertificateRecognised(
                ClaimGreenCertificateModel(
                    qrCodeText,
                    greenCertificate.getDgci(),
                    cose,
                    certificateModel
                )
            )
        } else {
            null
        }
    }

    sealed class MainViewEvent {
        object InputNotValid : MainViewEvent()
    }

    sealed class ModelFetcherResult {
        class GreenCertificateRecognised(val certificateModel: ClaimGreenCertificateModel) : ModelFetcherResult()
        class BookingSystemModelRecognised(val ticketingCheckInParcelable: TicketingCheckInParcelable) : ModelFetcherResult()
        object NotApplicable : ModelFetcherResult()
    }
}