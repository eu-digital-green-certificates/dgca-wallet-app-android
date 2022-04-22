package dgca.wallet.app.android.dcc.ui.wallet.certificates.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.android.app.dcc.R
import com.android.app.dcc.databinding.ActivityDccViewCertificateBinding
import dagger.hilt.android.AndroidEntryPoint

const val CERTIFICATE_ID_PARAM_KEY = "CERTIFICATE_ID_PARAM"

@AndroidEntryPoint
class DccViewCertificateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDccViewCertificateBinding

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDccViewCertificateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(),
            fallbackOnNavigateUpListener = ::onSupportNavigateUp
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        setSupportActionBar(binding.toolbar)

        navController.setGraph(
            R.navigation.dcc_view_certificate_nav_graph,
            DccViewCertificateFragmentArgs(intent.getIntExtra(CERTIFICATE_ID_PARAM_KEY, -1)).toBundle()
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            navController.navigate(R.id.settingsFragment)
        } else if (item.itemId != android.R.id.home || !navController.navigateUp()) {
            super.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
