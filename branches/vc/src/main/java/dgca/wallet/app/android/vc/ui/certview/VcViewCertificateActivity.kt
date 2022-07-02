package dgca.wallet.app.android.vc.ui.certview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.android.app.vc.R
import com.android.app.vc.databinding.ActivityVcViewCertificateBinding
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.vc.model.VcCard

@AndroidEntryPoint
class VcViewCertificateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVcViewCertificateBinding

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVcViewCertificateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.setGraph(
            R.navigation.vc_view_certificate_nav_graph,
            VcViewCertificateFragmentArgs(intent.getIntExtra(VcCard.VC_CERTIFICATE_ID_PARAM_KEY, -1)).toBundle()
        )
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp() || super.onSupportNavigateUp()
}
