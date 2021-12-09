package dgca.wallet.app.android.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class BindingDialogFragment<T : ViewBinding> : DialogFragment() {

    private var _binding: T? = null
    val binding get() = _binding!!

    abstract fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): T

    open fun onDestroyBinding(binding: T) {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val innerBinding = onCreateBinding(inflater, container)
        _binding = innerBinding
        return innerBinding.root
    }

    override fun onDestroyView() {
        val innerBinding = _binding
        if (innerBinding != null) {
            onDestroyBinding(innerBinding)
        }

        _binding = null

        super.onDestroyView()
    }
}