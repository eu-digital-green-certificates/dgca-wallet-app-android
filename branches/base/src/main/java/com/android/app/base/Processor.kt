package com.android.app.base

import android.content.Intent

interface Processor {
    fun id(): String

    fun prefetchData()

    fun isApplicable(input: String): Intent?

    suspend fun getItemCards(): List<ProcessorItemCard>?

    fun getSettingsIntent(): Pair<String, Intent>?

    suspend fun deleteItem(itemCard: Int)
}