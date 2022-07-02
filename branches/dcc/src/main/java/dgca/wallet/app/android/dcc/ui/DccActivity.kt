package dgca.wallet.app.android.dcc.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewbinding.BuildConfig
import com.android.app.base.RESULT_KEY
import com.android.app.dcc.R
import com.android.app.dcc.databinding.ActivityDccMainBinding
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.dcc.ui.wallet.qr.certificate.ClaimCertificateFragmentArgs
import feature.ticketing.presentation.BookingSystemConsentFragmentArgs

@AndroidEntryPoint
class DccActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var binding: ActivityDccMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!BuildConfig.DEBUG) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }

        binding = ActivityDccMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(),
            fallbackOnNavigateUpListener = ::onSupportNavigateUp
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        setSupportActionBar(binding.toolbar)

        val input = intent.getStringExtra(RESULT_KEY) ?: ""
        viewModel.init(input)

        viewModel.modelFetcherResult.observe(this) { modelFetcherResult ->
            when (modelFetcherResult) {
                is MainViewModel.ModelFetcherResult.GreenCertificateRecognised -> {
                    navController.setGraph(
                        R.navigation.dcc_nav_graph,
                        ClaimCertificateFragmentArgs(modelFetcherResult.certificateModel).toBundle()
                    )
                }

                is MainViewModel.ModelFetcherResult.BookingSystemModelRecognised ->
                    navController.setGraph(
                        R.navigation.ticketing_nav_graph,
                        BookingSystemConsentFragmentArgs(modelFetcherResult.ticketingCheckInParcelable).toBundle()
                    )
                else -> showErrorAndClose()
            }
        }

        viewModel.event.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                onViewModelEvent(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            navController.navigate(R.id.settingsFragment)
        } else if (item.itemId != android.R.id.home || !navController.navigateUp()) {
            super.onBackPressed()
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun onViewModelEvent(it: MainViewModel.MainViewEvent) {
        when (it) {
            MainViewModel.MainViewEvent.InputNotValid -> showErrorAndClose()
        }
    }

    private fun showErrorAndClose() {
        Toast.makeText(this, "Unknown error", Toast.LENGTH_SHORT).show()
        finish()
    }
}
