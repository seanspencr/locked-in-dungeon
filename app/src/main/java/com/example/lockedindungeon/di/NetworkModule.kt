package com.example.lockedindungeon.di

import com.example.lockedindungeon.data.remote.SingletonApiClientWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.openapitools.client.infrastructure.ApiClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiClient(wrapper: SingletonApiClientWrapper): ApiClient {
        return wrapper.authenticatedApiClient
    }
}
