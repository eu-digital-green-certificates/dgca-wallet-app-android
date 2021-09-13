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
 *  Created by osarapulov on 5/7/21 10:10 AM
 */

package dgca.wallet.app.android

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.databinding.ActivityMainBinding
import dgca.wallet.app.android.model.BookingSystemModel
import dgca.wallet.app.android.nfc.NdefParser
import dgca.wallet.app.android.wallet.CertificatesFragmentDirections
import dgca.wallet.app.android.wallet.scan_import.qr.BOOKING_SYSTEM_MODEL_RESULT_KEY
import dgca.wallet.app.android.wallet.scan_import.qr.CLAIM_GREEN_CERTIFICATE_RESULT_KEY
import dgca.wallet.app.android.wallet.scan_import.qr.FETCH_MODEL_REQUEST_KEY
import dgca.wallet.app.android.wallet.scan_import.qr.certificate.ClaimGreenCertificateModel
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!BuildConfig.DEBUG) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(),
            fallbackOnNavigateUpListener = ::onSupportNavigateUp
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        setSupportActionBar(binding.toolbar)

        navController.addOnDestinationChangedListener(this)

        navHostFragment.childFragmentManager.setFragmentResultListener(
            FETCH_MODEL_REQUEST_KEY, this
        ) { _, bundle ->
            navController.navigateUp()
            val claimGreenCertificateModel: ClaimGreenCertificateModel? =
                bundle.getParcelable(CLAIM_GREEN_CERTIFICATE_RESULT_KEY)
            if (claimGreenCertificateModel != null) {
                navigateToClaimCertificatePage(claimGreenCertificateModel)
            } else {
                val bookingSystemModel: BookingSystemModel? =
                    bundle.getParcelable(BOOKING_SYSTEM_MODEL_RESULT_KEY)
                if (bookingSystemModel != null) {
                    navigateToBookingSystemModelConsentPage(bookingSystemModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        checkNdefMessage(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                navController.navigate(R.id.settingsFragment)
                true
            }
            android.R.id.home -> navController.navigateUp()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun clearBackground() {
        window.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.white))
    }

    fun disableBackButton() {
        binding.toolbar.navigationIcon = null
    }

    private fun checkNdefMessage(intent: Intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                parseNdefMessages(messages)
                intent.removeExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            }
        }
    }

    private fun parseNdefMessages(messages: List<NdefMessage>) {
        if (messages.isEmpty()) {
            return
        }

        val builder = StringBuilder()
        val records = NdefParser.parse(messages[0])
        val size = records.size

        for (i in 0 until size) {
            val record = records[i]
            val str = record.str()
            builder.append(str)
        }

        val qrCodeText = builder.toString()
        if (qrCodeText.isNotEmpty()) {
            val action = CertificatesFragmentDirections.actionCertificatesFragmentToModelFetcherDialogFragment(qrCodeText)
            navController.navigate(action)
        } else {
            Timber.d("Received empty NDEFMessage")
        }
    }

    private fun navigateToClaimCertificatePage(claimGreenCertificateModel: ClaimGreenCertificateModel) {
        navController.removeOnDestinationChangedListener(this)
        val action =
            CertificatesFragmentDirections.actionCertificatesFragmentToClaimCertificateFragment(claimGreenCertificateModel)
        navController.navigate(action)
        navController.addOnDestinationChangedListener(this)
    }

    private fun navigateToBookingSystemModelConsentPage(bookingSystemModel: BookingSystemModel) {
        navController.removeOnDestinationChangedListener(this)
        val action =
            CertificatesFragmentDirections.actionCertificatesFragmentToBookingSystemConsentFragment(bookingSystemModel)
        navController.navigate(action)
        navController.addOnDestinationChangedListener(this)
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        if (destination.id == R.id.certificatesFragment) {
            checkNdefMessage(intent)
        }
    }
}