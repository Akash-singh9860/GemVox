package com.app.gemvox.core.di

import com.app.gemvox.data.repository.VoiceRepositoryImpl
import com.app.gemvox.domain.repository.VoiceRepository
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
    abstract fun bindVoiceRepository(
        impl: VoiceRepositoryImpl
    ): VoiceRepository
}