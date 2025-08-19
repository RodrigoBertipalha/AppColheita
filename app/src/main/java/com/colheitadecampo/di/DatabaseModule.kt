package com.colheitadecampo.di

import android.content.Context
import com.colheitadecampo.data.local.ColheitaDatabase
import com.colheitadecampo.data.local.FieldDao
import com.colheitadecampo.data.local.ImportSessionDao
import com.colheitadecampo.data.local.PlotDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ColheitaDatabase {
        return ColheitaDatabase.getDatabase(context)
    }

    @Provides
    fun provideFieldDao(database: ColheitaDatabase): FieldDao {
        return database.fieldDao()
    }

    @Provides
    fun providePlotDao(database: ColheitaDatabase): PlotDao {
        return database.plotDao()
    }

    @Provides
    fun provideImportSessionDao(database: ColheitaDatabase): ImportSessionDao {
        return database.importSessionDao()
    }
}
