package com.android.app.divoc

import android.content.Intent
import android.net.Uri
import com.android.app.base.Processor
import com.android.app.base.ProcessorItemCard
import javax.inject.Inject

class DivocProcessor @Inject constructor() : Processor {
    override fun id(): String {
        return DIVOC_PROCESSOR_ID
    }

    override fun prefetchData() {

    }

    override fun isApplicable(input: String): Intent? {
        //        if applicable
        //        return Intent("com.android.app.divoc.View", Uri.parse("verifier://divoc")).apply {
        //            putExtra("result_key", "Result: divoc QR valid")
        //        }
        return null
    }

    override suspend fun getItemCards(): List<ProcessorItemCard> {
        return emptyList()
    }

    override fun getSettingsIntent(): Pair<String, Intent> {
        return Pair("Divoc", Intent("com.android.app.dcc.View", Uri.parse("settings://divoc")))
    }

    override suspend fun deleteItem(itemCard: Int) {
        TODO("Not yet implemented")
    }

    companion object {
        private const val DIVOC_PROCESSOR_ID = "DIVOC"
    }
}
