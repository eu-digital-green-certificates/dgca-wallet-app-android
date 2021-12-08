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
import dgca.wallet.app.android.base.BindingDialogFragment
import dgca.wallet.app.android.databinding.DialogFragmentDeleteFileBinding

const val DELETE_FILE_REQUEST_KEY = "DELETE_FILE_REQUEST"
const val DELETE_FILE_POSITION_RESULT_PARAM = "DELETE_FILE_POSITION_RESULT_PARAM"
const val DELETE_FILE_FILE_RESULT_PARAM = "DELETE_FILE_FILE_RESULT_PARAM"

@AndroidEntryPoint
class DeleteFileDialogFragment : BindingDialogFragment<DialogFragmentDeleteFileBinding>() {

    private val args: DeleteFileDialogFragmentArgs by navArgs()

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogFragmentDeleteFileBinding =
        DialogFragmentDeleteFileBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.cancel.setOnClickListener { findNavController().navigateUp() }
        binding.delete.setOnClickListener {
            findNavController().navigateUp()
            setFragmentResult(
                DELETE_FILE_REQUEST_KEY,
                bundleOf(
                    DELETE_FILE_POSITION_RESULT_PARAM to args.position,
                    DELETE_FILE_FILE_RESULT_PARAM to args.file
                )
            )
        }
    }
}
