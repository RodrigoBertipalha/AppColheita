package com.colheitadecampo.di

import android.content.Context
import com.colheitadecampo.ColheitaApp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Os módulos necessários serão adicionados aqui conforme necessário
}
