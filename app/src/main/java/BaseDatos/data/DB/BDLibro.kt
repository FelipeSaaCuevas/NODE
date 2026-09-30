package BaseDatos.data.DB

import BaseDatos.data.dao.entityDao
import BaseDatos.data.entity.nombre
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [nombre::class], version = 2)
abstract class BDLibro : RoomDatabase() {

    abstract fun enDao(): entityDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE nombre ADD COLUMN fecha INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getDatabase(ctx: Context): BDLibro {
            return Room.databaseBuilder(
                ctx,
                BDLibro::class.java,
                "libro"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}
// segunda base de datos para una pagina que registra la lista de compra
