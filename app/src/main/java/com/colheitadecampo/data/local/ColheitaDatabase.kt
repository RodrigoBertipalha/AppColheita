package com.colheitadecampo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.colheitadecampo.data.model.Field
import com.colheitadecampo.data.model.ImportSession
import com.colheitadecampo.data.model.Plot
import timber.log.Timber

// Migração da versão 1 para a versão 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            // Adicionar coluna 'descartado' à tabela Plot
            database.execSQL("ALTER TABLE plots ADD COLUMN descartado INTEGER NOT NULL DEFAULT 0")
            // Adicionar coluna 'decision' à tabela Plot
            database.execSQL("ALTER TABLE plots ADD COLUMN decision TEXT DEFAULT ''")
            Timber.d("Migração 1->2 concluída com sucesso")
        } catch (e: Exception) {
            Timber.e(e, "Erro durante a migração 1->2")
        }
    }
}

@Database(
    entities = [Field::class, Plot::class, ImportSession::class],
    version = 2,
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
                // Adicionar a migração
                .addMigrations(MIGRATION_1_2)
                // Manter o fallbackToDestructiveMigration como último recurso
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
