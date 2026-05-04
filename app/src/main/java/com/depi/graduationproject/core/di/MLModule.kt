package com.depi.graduationproject.core.di

import android.content.Context
import com.depi.graduationproject.data.mlkit.TFLitePlateAnalyzer
import com.depi.graduationproject.domain.analyzer.IPlateAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MLModule {

    @Provides
    @Singleton
    fun providePlateAnalyzer(
        @ApplicationContext context: Context
    ): IPlateAnalyzer {
        return TFLitePlateAnalyzer(context)
    }
}
