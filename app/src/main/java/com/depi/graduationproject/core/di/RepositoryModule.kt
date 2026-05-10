package com.depi.graduationproject.core.di

import com.depi.graduationproject.data.repository.ParkingRepositoryImpl
import com.depi.graduationproject.data.repository.SettingsRepositoryImpl
import com.depi.graduationproject.domain.repository.IParkingRepository
import com.depi.graduationproject.domain.repository.ISettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindParkingRepository(
        parkingRepositoryImpl: ParkingRepositoryImpl
    ): IParkingRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): ISettingsRepository
}
