/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
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
 *  Created by osarapulov on 5/12/21 2:55 PM
 */

package dgca.wallet.app.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.text.toSpannable
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.base.BindingFragment
import dgca.wallet.app.android.data.local.Preferences
import dgca.wallet.app.android.databinding.FragmentSettingsBinding
import dgca.wallet.app.android.util.applyStyle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BindingFragment<FragmentSettingsBinding>() {

    @Inject
    lateinit var preferences: Preferences

    private val viewModel by viewModels<SettingsViewModel>()

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingsBinding =
        FragmentSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        viewModel.lastRevocationStateUpdateTimeStamp.observe(viewLifecycleOwner) {
            setUpUpdateRevocationStateButton(it)
        }

        binding.privacyInformation.setOnClickListener { launchWebIntent() }
        binding.licenses.setOnClickListener { openLicenses() }
        binding.version.text =
            getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.settings).isVisible = false
    }

    private fun setUpUpdateRevocationStateButton(revocationUpdateResult: RevocationUpdateResult) {
        val subString = if (revocationUpdateResult.timestamp > 0) {
            val localDateTime =
                Instant.ofEpochMilli(revocationUpdateResult.timestamp / 1000)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            getString(R.string.last_successfully_updated_at, localDateTime)
        } else {
            getString(R.string.wasnt_updated_yet)
        }

        val context = requireContext()
        val spannable = SpannableStringBuilder()
            .append(
                getString(R.string.update_revocation_state).toSpannable()
                    .applyStyle(context, R.style.TextAppearance_Dgca_SettingsButtonHeader)
            )
            .append("\n")
            .append(
                subString.toSpannable()
                    .applyStyle(context, R.style.TextAppearance_Dgca_SettingsButtonSubHeader)
            ).apply {
                if (revocationUpdateResult.isSuccessful?.not() == true) {
                    append("\n\n")
                    append(
                        getString(R.string.last_update_failed).toSpannable()
                            .applyStyle(
                                context,
                                R.style.TextAppearance_Dgca_SettingsButtonSubHeader
                            )
                    )
                }
            }

        binding.updateRevocationState.text = spannable

        binding.updateRevocationState.setOnClickListener {
            viewModel.updateRevocationState()
        }
    }

    private fun launchWebIntent() {
        val page = Uri.parse(PRIVACY_POLICY)
        val intent = Intent(Intent.ACTION_VIEW, page)

        if (intent.resolveActivity(requireContext().packageManager) == null) {
            return
        }
        requireContext().startActivity(intent)
    }

    private fun openLicenses() {
        val intent = Intent(requireContext(), LicensesActivity::class.java)
        requireContext().apply {
            startActivity(intent)
        }
    }

    companion object {
        const val PRIVACY_POLICY =
            "https://op.europa.eu/en/web/about-us/legal-notices/eu-mobile-apps"
    }
}