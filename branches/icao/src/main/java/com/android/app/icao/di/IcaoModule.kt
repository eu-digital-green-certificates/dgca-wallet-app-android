package com.android.app.icao.di

import com.android.app.base.Processor
import com.android.app.base.ProcessorMarker
import com.android.app.icao.IcaoProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@InstallIn(SingletonComponent::class)
@Module
class IcaoModule {

    @Provides
    @IntoSet
    @ProcessorMarker
    fun provideIcaoProcessor(): Processor = IcaoProcessor()
}