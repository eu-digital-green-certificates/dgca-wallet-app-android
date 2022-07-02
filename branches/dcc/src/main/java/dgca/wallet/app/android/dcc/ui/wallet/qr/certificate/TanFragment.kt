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
 *  Created by mykhailo.nester on 5/12/21 12:27 AM
 */

package dgca.wallet.app.android.dcc.ui.wallet.qr.certificate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.android.app.dcc.R
import com.android.app.dcc.databinding.FragmentCertificateTanBinding
import dgca.wallet.app.android.dcc.ui.BindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TanFragment : BindingFragment<FragmentCertificateTanBinding>() {

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCertificateTanBinding =
        FragmentCertificateTanBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nextBtn.setOnClickListener {
            val tan = binding.tanTextField.editText?.text.toString()
            if (tan.isEmpty()) {
                binding.tanTextField.error = getString(R.string.tan_empty_error)
            } else {
                findNavController().apply {
                    previousBackStackEntry?.savedStateHandle?.set(TAN_KEY, tan)
                    navigateUp()
                }
            }
        }
    }

    companion object {
        const val TAN_KEY = "TAN"
    }
}