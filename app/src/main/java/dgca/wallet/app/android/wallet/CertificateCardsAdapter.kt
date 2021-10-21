/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-wallet-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by osarapulov on 9/10/21 12:58 PM
 */

package dgca.wallet.app.android.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dgca.wallet.app.android.R
import dgca.wallet.app.android.databinding.CertificateCardFileBinding
import dgca.wallet.app.android.databinding.CertificateCardHeaderBinding
import dgca.wallet.app.android.databinding.CertificateCardViewBinding
import dgca.wallet.app.android.formatWith
import java.io.File

class CertificateCardsAdapter(
    private val certificatesCards: List<CertificatesCard>,
    private val certificateCardClickListener: CertificateCardClickListener,
    private val fileCardClickListener: FileCardClickListener
) : RecyclerView.Adapter<CertificateCardsAdapter.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (certificatesCards[position]) {
            is CertificatesCard.CertificatesHeader, is CertificatesCard.ImagesHeader, is CertificatesCard.PdfsHeader -> ViewType.HEADER_TYPE.ordinal
            is CertificatesCard.CertificateCard -> ViewType.CERTIFICATE_TYPE.ordinal
            is CertificatesCard.FileCard -> ViewType.FILE_TYPE.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            ViewType.HEADER_TYPE.ordinal -> ViewHolder.HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.certificate_card_header, parent, false)
            )
            ViewType.CERTIFICATE_TYPE.ordinal -> ViewHolder.CertificateViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.certificate_card_view, parent, false)
            )
            ViewType.FILE_TYPE.ordinal -> ViewHolder.FileViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.certificate_card_file, parent, false)
            )
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.HeaderViewHolder -> holder.bind(certificatesCards[position])
            is ViewHolder.CertificateViewHolder -> holder.bind(
                certificatesCards[position] as CertificatesCard.CertificateCard,
                certificateCardClickListener
            )
            is ViewHolder.FileViewHolder -> holder.bind(
                certificatesCards[position] as CertificatesCard.FileCard,
                fileCardClickListener
            )
        }
    }

    override fun getItemCount(): Int = certificatesCards.size

    interface CertificateCardClickListener {
        fun onCertificateCardClick(certificateId: Int)
    }

    interface FileCardClickListener {
        fun onFileCardClick(file: File)
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            const val YEAR_MONTH_DAY = "yyyy-MM-dd"
        }

        class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
            private val binding = CertificateCardHeaderBinding.bind(itemView)

            fun bind(certificatesCard: CertificatesCard) {
                binding.root.visibility =
                    if (certificatesCard is CertificatesCard.CertificatesHeader || certificatesCard is CertificatesCard.ImagesHeader || certificatesCard is CertificatesCard.PdfsHeader) View.VISIBLE else View.GONE
                when (certificatesCard) {
                    is CertificatesCard.CertificatesHeader -> binding.title.setText(R.string.certificates)
                    is CertificatesCard.ImagesHeader -> binding.title.setText(R.string.images)
                    is CertificatesCard.PdfsHeader -> binding.title.setText(R.string.pdfs)
                    else -> throw IllegalArgumentException()
                }
            }
        }

        class CertificateViewHolder(itemView: View) : ViewHolder(itemView) {
            private val binding = CertificateCardViewBinding.bind(itemView)

            fun bind(
                certificatesCard: CertificatesCard.CertificateCard,
                certificateCardClickListener: CertificateCardClickListener
            ) {
                binding.titleView.text = when {
                    certificatesCard.certificate.vaccinations?.first() != null -> binding.root.resources.getString(
                        R.string.vaccination,
                        certificatesCard.certificate.vaccinations.first().doseNumber.toString(),
                        certificatesCard.certificate.vaccinations.first().totalSeriesOfDoses.toString()
                    )
                    certificatesCard.certificate.recoveryStatements?.isNotEmpty() == true -> binding.root.resources.getString(
                        R.string.recovery
                    )
                    certificatesCard.certificate.tests?.isNotEmpty() == true -> binding.root.resources.getString(R.string.test)
                    else -> ""
                }
                binding.nameView.text = certificatesCard.certificate.getFullName()
                binding.scannedAtDateView.text = certificatesCard.dateTaken.formatWith(YEAR_MONTH_DAY)
                binding.root.setOnClickListener { certificateCardClickListener.onCertificateCardClick(certificatesCard.certificateId) }
            }
        }

        class FileViewHolder(itemView: View) : ViewHolder(itemView) {
            private val binding = CertificateCardFileBinding.bind(itemView)

            fun bind(
                fileCard: CertificatesCard.FileCard,
                fileCardClickListener: FileCardClickListener
            ) {
                binding.titleView.text = fileCard.file.name
                binding.scannedAtDateView.text = fileCard.dateTaken.formatWith(YEAR_MONTH_DAY)
                binding.root.setOnClickListener { fileCardClickListener.onFileCardClick(fileCard.file) }
            }
        }
    }
}

enum class ViewType {
    HEADER_TYPE, CERTIFICATE_TYPE, FILE_TYPE
}