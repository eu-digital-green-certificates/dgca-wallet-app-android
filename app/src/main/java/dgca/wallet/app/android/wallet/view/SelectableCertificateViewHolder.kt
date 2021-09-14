package dgca.wallet.app.android.wallet.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.wallet.app.android.R
import dgca.wallet.app.android.databinding.ItemSelectableCertificateBinding

class SelectableCertificateViewHolder(
    private val binding: ItemSelectableCertificateBinding,
    private val viewModel: CertificateSelectorViewModel
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup, model: CertificateSelectorViewModel) =
            SelectableCertificateViewHolder(ItemSelectableCertificateBinding.inflate(inflater, parent, false), model)
    }

    init {
        itemView.setOnClickListener {
            viewModel.onCertificateSelected(bindingAdapterPosition)
        }
    }

    fun bind(model: SelectableCertificateModel) {
        binding.title.text = model.title
        binding.description.text = itemView.context.getString(R.string.valid_until, model.validUntil)
        binding.radioButton.isChecked = model.selected
    }
}