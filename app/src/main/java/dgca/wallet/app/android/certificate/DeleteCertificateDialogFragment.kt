package dgca.wallet.app.android.certificate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.R
import dgca.wallet.app.android.base.BindingDialogFragment
import dgca.wallet.app.android.databinding.DialogFragmentDeleteCertificateBinding
import dgca.wallet.app.android.formatWith

const val DELETE_CERTIFICATE_REQUEST_KEY = "DELETE_CERTIFICATE_REQUEST"
const val DELETE_CERTIFICATE_ITEM_POSITION_RESULT_PARAM = "DELETE_CERTIFICATE_ITEM_POSITION_RESULT_PARAM"
const val DELETE_CERTIFICATE_ITEM_CARD_RESULT_PARAM = "DELETE_CERTIFICATE_ITEM_CARD_RESULT_PARAM"

@AndroidEntryPoint
class DeleteCertificateDialogFragment : BindingDialogFragment<DialogFragmentDeleteCertificateBinding>() {

    private val args: DeleteCertificateDialogFragmentArgs by navArgs()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogFragmentDeleteCertificateBinding =
        DialogFragmentDeleteCertificateBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.cancel.setOnClickListener { findNavController().navigateUp() }
        binding.delete.setOnClickListener {
            findNavController().navigateUp()
            setFragmentResult(
                DELETE_CERTIFICATE_REQUEST_KEY,
                bundleOf(
                    DELETE_CERTIFICATE_ITEM_POSITION_RESULT_PARAM to args.position,
                    DELETE_CERTIFICATE_ITEM_CARD_RESULT_PARAM to args.itemCard
                )
            )
        }

        val certificateCard: CertificatesCard.CertificateCard = args.itemCard
        binding.typeTitle.text = when {
            certificateCard.certificate.vaccinations?.first() != null -> binding.root.resources.getString(R.string.vaccination_only)
            certificateCard.certificate.recoveryStatements?.isNotEmpty() == true -> binding.root.resources.getString(R.string.recovery            )
            certificateCard.certificate.tests?.isNotEmpty() == true -> binding.root.resources.getString(R.string.test)
            else -> ""
        }
        binding.typeValue.text = if (certificateCard.certificate.vaccinations?.first() != null)
            "${certificateCard.certificate.vaccinations.first().doseNumber}/${certificateCard.certificate.vaccinations.first().totalSeriesOfDoses}" else ""
        binding.dateValue.text = getCertificateDate(certificateCard)
        binding.fromValue.text = certificateCard.certificate.getFullName()
    }

    protected fun getCertificateDate(certificateCard: CertificatesCard.CertificateCard): String {
        return when {
            certificateCard.certificate.vaccinations?.first() != null ->
                certificateCard.certificate.vaccinations.first().dateOfVaccination
            certificateCard.certificate.recoveryStatements?.first() != null ->
                certificateCard.certificate.recoveryStatements.first().certificateValidFrom
            certificateCard.certificate.tests?.first() != null ->
                certificateCard.certificate.tests.first().dateTimeOfCollection.split("T").first()
            else -> certificateCard.dateTaken.formatWith(dgca.wallet.app.android.YEAR_MONTH_DAY)
        }
    }
}
