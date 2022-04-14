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

package dgca.wallet.app.android.dcc.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.text.toSpannable
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.app.dcc.BuildConfig
import com.android.app.dcc.R
import com.android.app.dcc.databinding.FragmentDccSettingsBinding
import dgca.wallet.app.android.dcc.ui.BindingFragment
import dgca.wallet.app.android.dcc.utils.applyStyle
import dgca.wallet.app.android.dcc.utils.formatWith
import dgca.wallet.app.android.dcc.utils.toLocalDateTime
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class DccSettingsFragment : BindingFragment<FragmentDccSettingsBinding>() {

    private val viewModel by viewModels<DccSettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        lifecycle.addObserver(viewModel)
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentDccSettingsBinding =
        FragmentDccSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.lastRevocationStateUpdateTimeStamp.observe(viewLifecycleOwner) {
            setUpUpdateRevocationStateButton(it)
        }

        binding.privacyInformation.setOnClickListener { launchWebIntent() }
        binding.licenses.setOnClickListener { openLicenses() }
        binding.version.text = getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        viewModel.inProgress.observe(viewLifecycleOwner, {
            binding.privacyInformation.isClickable = it != true
            binding.licenses.isClickable = it != true
            binding.progressBar.visibility = if (it == true) View.VISIBLE else View.GONE
        })

        viewModel.lastCountriesSyncLiveData.observe(viewLifecycleOwner) {
            setCountriesReloadState(it)
        }
        binding.reloadCountries.setOnClickListener { viewModel.syncCountries() }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        OssLicensesMenuActivity.setActivityTitle(getString(R.string.licenses))
        requireContext().apply {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }
    }

    private fun setCountriesReloadState(lastUpdate: Long) {
        val updateText = if (lastUpdate <= 0) {
            getString(R.string.never)
        } else {
            lastUpdate.toLocalDateTime().formatWith(LAST_UPDATE_DATE_TIME_FORMAT)
        }
        val lastUpdatedText = getString(R.string.last_updated, updateText)
        val context = requireContext()
        val spannable = SpannableStringBuilder()
            .append(
                getString(R.string.reload_countries).toSpannable()
                    .applyStyle(context, R.style.TextAppearance_Dgca_SettingsButtonHeader)
            )
            .append("\n")
            .append(
                lastUpdatedText.toSpannable()
                    .applyStyle(context, R.style.TextAppearance_Dgca_SettingsButtonSubHeader)
            )

        binding.reloadCountries.text = spannable
    }

    companion object {
        private const val PRIVACY_POLICY = "https://op.europa.eu/en/web/about-us/legal-notices/eu-mobile-apps"
        private const val LAST_UPDATE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"
    }
}
