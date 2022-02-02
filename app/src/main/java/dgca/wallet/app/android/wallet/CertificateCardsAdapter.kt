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
    certificatesCards: List<CertificatesCard>,
    private val certificateCardListener: CertificateCardListener,
    private val fileCardListener: FileCardListener
) : RecyclerView.Adapter<CertificateCardsAdapter.ViewHolder>() {
    private val certificatesCards = certificatesCards.toMutableList()

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
                position,
                certificatesCards[position] as CertificatesCard.CertificateCard,
                certificateCardListener
            )
            is ViewHolder.FileViewHolder -> holder.bind(
                position,
                certificatesCards[position] as CertificatesCard.FileCard,
                fileCardListener
            )
        }
    }

    override fun getItemCount(): Int = certificatesCards.size

    interface CertificateCardListener {
        fun onCertificateCardClick(certificateId: Int)
        fun onCertificateCardDeleted(position: Int, certificateCard: CertificatesCard.CertificateCard)
    }

    interface FileCardListener {
        fun onFileCardClick(file: File)
        fun onFileCardDeleted(position: Int, file: File)
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
                position: Int,
                certificatesCard: CertificatesCard.CertificateCard,
                certificateCardListener: CertificateCardListener
            ) {
                binding.titleView.text = when {
                    certificatesCard.certificate.vaccinations?.first() != null -> binding.root.resources.getString(
                        R.string.vaccination_of,
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
                binding.scannedAtDateView.text = this.getCertificateDate(certificatesCard)
                binding.root.setOnClickListener { certificateCardListener.onCertificateCardClick(certificatesCard.certificateId) }
                binding.root.setOnLongClickListener {
                    certificateCardListener.onCertificateCardDeleted(position, certificatesCard)
                    return@setOnLongClickListener true
                }
            }
        }

        class FileViewHolder(itemView: View) : ViewHolder(itemView) {
            private val binding = CertificateCardFileBinding.bind(itemView)

            fun bind(
                position: Int,
                fileCard: CertificatesCard.FileCard,
                fileCardListener: FileCardListener
            ) {
                binding.titleView.text = fileCard.file.name
                binding.scannedAtDateView.text = fileCard.dateTaken.formatWith(YEAR_MONTH_DAY)
                binding.root.setOnClickListener { fileCardListener.onFileCardClick(fileCard.file) }
                binding.root.setOnLongClickListener {
                    fileCardListener.onFileCardDeleted(position, fileCard.file)
                    return@setOnLongClickListener true
                }
            }
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
}

enum class ViewType {
    HEADER_TYPE, CERTIFICATE_TYPE, FILE_TYPE
}
