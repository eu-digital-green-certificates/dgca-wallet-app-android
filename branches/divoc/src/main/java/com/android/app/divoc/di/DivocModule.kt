package com.android.app.divoc.di

import com.android.app.base.Processor
import com.android.app.base.ProcessorMarker
import com.android.app.divoc.DivocProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@InstallIn(SingletonComponent::class)
@Module
class DivocModule {

    @Provides
    @IntoSet
    @ProcessorMarker
    fun provideDivocProcessor(): Processor = DivocProcessor()
}