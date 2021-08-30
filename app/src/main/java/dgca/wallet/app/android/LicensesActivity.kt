/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 *  ---license-end
 *
 *  Created by Matthieu De Beule on 11/06/2021, 08:39
 */

package dgca.wallet.app.android

import android.graphics.Typeface
import android.os.Bundle
import android.text.util.Linkify
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dgca.wallet.app.android.databinding.ActivityLicensesBinding
import java.nio.charset.Charset
import java.util.*

//Largely inspired by https://github.com/mozilla-mobile/fenix/pull/13767/
class LicensesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLicensesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLicensesBinding.inflate(layoutInflater)

        setTitle(R.string.licenses)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupLibrariesListView()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupLibrariesListView() {
        val libraries = parseLibraries()
        val listView = binding.licensesListview
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, libraries)
        listView.setOnItemClickListener { _, _, position, _ ->
            showLicenseDialog(libraries[position])
        }
    }

    private fun parseLibraries(): List<LibraryItem> {
        /*
            The gradle plugin "oss-licenses-plugin" creates two "raw" resources:
               - third_party_licenses which is the binary concatenation of all the licenses text for
                 all the libraries. License texts can either be an URL to a license file or just the
                 raw text of the license.
               - third_party_licenses_metadata which contains one dependency per line formatted in
                 the following way: "[start_offset]:[length] [name]"
                 [start_offset]     : first byte in third_party_licenses that contains the license
                                      text for this library.
                 [length]           : length of the license text for this library in
                                      third_party_licenses.
                 [name]             : either the name of the library, or its artifact name.
            See https://github.com/google/play-services-plugins/tree/master/oss-licenses-plugin
        */
        val licensesData = resources
            .openRawResource(R.raw.third_party_licenses)
            .readBytes()
        val licensesMetadataReader = resources
            .openRawResource(R.raw.third_party_license_metadata)
            .bufferedReader()

        return licensesMetadataReader.use { reader -> reader.readLines() }.map { line ->
            val (section, name) = line.split(" ", limit = 2)
            val (startOffset, length) = section.split(":", limit = 2).map(String::toInt)
            val licenseData = licensesData.sliceArray(startOffset until startOffset + length)
            val licenseText = licenseData.toString(Charset.forName("UTF-8"))
            LibraryItem(name, licenseText)
        }.sortedBy { item -> item.name.toLowerCase(Locale.ROOT) }
    }

    private fun showLicenseDialog(libraryItem: LibraryItem) {
        val dialog: AlertDialog = AlertDialog.Builder(this).let {
            it.setTitle(libraryItem.name)
            it.setMessage(libraryItem.license)
            it.show()
        }

        dialog.findViewById<TextView>(android.R.id.message)!!.let {
            Linkify.addLinks(it, Linkify.ALL)
            it.linksClickable = true
            it.textSize = 10F
            it.typeface = Typeface.MONOSPACE
        }
    }
}

private class LibraryItem(val name: String, val license: String) {
    override fun toString(): String {
        return name
    }
}