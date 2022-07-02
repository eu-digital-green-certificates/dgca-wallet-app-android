package com.android.app.icao

import android.content.Intent
import android.net.Uri
import com.android.app.base.Processor
import com.android.app.base.ProcessorItemCard
import javax.inject.Inject

class IcaoProcessor @Inject constructor() : Processor {
    override fun id(): String {
        return ICAO_PROCESSOR_ID
    }

    override fun prefetchData() {

    }

    override fun isApplicable(input: String): Intent? {
        //        if applicable
        //        return Intent("com.android.app.icao.View", Uri.parse("verifier://icao")).apply {
        //            putExtra("result_key", "Result: icao QR valid")
        //        }
        return null
    }

    override suspend fun getItemCards(): List<ProcessorItemCard> {
        return emptyList()
    }

    override fun getSettingsIntent(): Pair<String, Intent> {
        return Pair("Icao", Intent("com.android.app.dcc.View", Uri.parse("settings://icao")))
    }

    override suspend fun deleteItem(itemCard: Int) {
        TODO("Not yet implemented")
    }

    companion object {
        private const val ICAO_PROCESSOR_ID = "ICAO"
    }
}
