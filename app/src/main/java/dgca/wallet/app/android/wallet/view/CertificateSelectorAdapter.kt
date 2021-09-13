package dgca.wallet.app.android.wallet.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dgca.wallet.app.android.R

class CertificateSelectorAdapter(
    private val inflater: LayoutInflater,
    private val viewModel: CertificateSelectorViewModel,
) : RecyclerView.Adapter<SelectableCertificateViewHolder>() {

    private var items = emptyList<SelectableCertificateModel>()

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableCertificateViewHolder =
        SelectableCertificateViewHolder.create(inflater, parent, viewModel)

    override fun onBindViewHolder(holder: SelectableCertificateViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_selectable_certificate

    fun update(list: List<SelectableCertificateModel>) {
        notifyChanges(items, list)
        items = list
    }

    fun RecyclerView.Adapter<out RecyclerView.ViewHolder>.notifyChanges(
        oldList: List<SelectableCertificateModel>,
        newList: List<SelectableCertificateModel>
    ) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any {
                return Bundle()
            }
        })

        diff.dispatchUpdatesTo(this)
    }
}
