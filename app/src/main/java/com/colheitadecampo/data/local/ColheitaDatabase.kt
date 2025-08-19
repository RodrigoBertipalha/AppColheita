package com.colheitadecampo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.ImportSession
import com.colheitadecampo.data.model.Plot

@Database(
    entities = [Field::class, Plot::class, ImportSession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ColheitaDatabase : RoomDatabase() {
    abstract fun fieldDao(): FieldDao
    abstract fun plotDao(): PlotDao
    abstract fun importSessionDao(): ImportSessionDao

    companion object {
        @Volatile
        private var INSTANCE: ColheitaDatabase? = null

        fun getDatabase(context: Context): ColheitaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ColheitaDatabase::class.java,
                    "colheita_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
