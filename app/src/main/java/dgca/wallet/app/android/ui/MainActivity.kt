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
 *  Created by osarapulov on 8/25/21 4:40 PM
 */

package dgca.wallet.app.android.ui

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import dgca.wallet.app.android.inputrecognizer.navigateToSpecificModule
import dgca.wallet.app.android.inputrecognizer.nfc.NdefParser
import dgca.wallet.app.android.protocolhandler.ProtocolHandlerViewModel
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.BuildConfig
import dgca.wallet.app.android.R
import dgca.wallet.app.android.databinding.ActivityMainBinding
import dgca.wallet.app.android.ui.dashboard.DashboardFragmentDirections
import timber.log.Timber

const val INTENT_ACTION = "dgca.wallet.app.android.INTENT"
const val DATA_PARAM_KEY = "DATA_PARAM"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    private val viewModel by viewModels<MainViewModel>()
    private val protocolViewModel by viewModels<ProtocolHandlerViewModel>()

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

        handleIntent(intent)
        viewModel.init()

        protocolViewModel.protocolHandlerResult.observe(this) {
            if (it is ProtocolHandlerViewModel.ProtocolHandlerResult.Applicable) {
                navigateToSpecificModule(it.intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
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

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            INTENT_ACTION -> {
                val action =
                    DashboardFragmentDirections.actionCertificatesFragmentToIntentFragment(intent.getStringExtra(
                        DATA_PARAM_KEY
                    )!!)
                navController.navigate(action)
            }
            NfcAdapter.ACTION_NDEF_DISCOVERED -> checkNdefMessage(intent)
        }
    }

    private fun checkNdefMessage(intent: Intent) {
        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
            val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
            parseNdefMessages(messages)
            intent.removeExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
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
            protocolViewModel.init(qrCodeText)
        } else {
            Timber.d("Received empty NDEFMessage")
        }
    }
}
