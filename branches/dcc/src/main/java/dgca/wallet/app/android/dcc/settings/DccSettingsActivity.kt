package dgca.wallet.app.android.dcc.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.android.app.dcc.R
import com.android.app.dcc.databinding.ActivityDccSettingsMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DccSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDccSettingsMainBinding

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDccSettingsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setSupportActionBar(binding.toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != android.R.id.home || !navController.navigateUp()) {
            super.onBackPressed()
        }
        return true
    }
}
