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
 *  Created by osarapulov on 8/23/21 1:54 PM
 */

package dgca.wallet.app.android.ui.file

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dgca.wallet.app.android.base.BindingFragment
import dagger.hilt.android.AndroidEntryPoint
import dgca.wallet.app.android.R
import dgca.wallet.app.android.databinding.FragmentFileViewBinding
import dgca.wallet.app.android.dcc.ui.wallet.certificates.view.ShareImageIntentProvider
import javax.inject.Inject

@AndroidEntryPoint
class ViewFileFragment : BindingFragment<FragmentFileViewBinding>() {

    private val args by navArgs<ViewFileFragmentArgs>()
    private val viewModel by viewModels<ViewFileViewModel>()

    @Inject
    lateinit var shareImageIntentProvider: ShareImageIntentProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val minEdge = displayMetrics.widthPixels * 0.9
        viewModel.init(args.file, minEdge)
    }

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFileViewBinding =
        FragmentFileViewBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { onViewModelEvent(it) }
        }
        viewModel.image.observe(viewLifecycleOwner) { bitmap ->
            bitmap?.let { binding.fileImage.setImageBitmap(it) }
            binding.progressView.visibility = View.GONE
        }
        binding.share.setOnClickListener { launchSharing() }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.findItem(R.id.delete).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.delete) {
            viewModel.deleteFile(args.file)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun onViewModelEvent(event: ViewFileViewModel.ViewFileEvent) {
        when (event) {
            is ViewFileViewModel.ViewFileEvent.OnFileDeleted -> findNavController().popBackStack()
        }
    }

    private fun launchSharing() {
        startActivity(
            Intent.createChooser(
                shareImageIntentProvider.getShareImageIntent(args.file),
                getString(R.string.share)
            )
        )
    }
}
