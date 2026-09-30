package BaseDatos.data.dao

import BaseDatos.data.entity.nombre
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface entityDao {

    // INSERTAR
    @Insert
    suspend fun insert(nombre: nombre)

    // ACTUALIZAR
    @Update
    suspend fun update(nombre: nombre)

    // ELIMINAR
    @Delete
    suspend fun delete(nombre: nombre)
    @Query("DELETE FROM nombre")
    suspend fun deleteAll()

    // OBTENER TODOS
    @Query("SELECT * FROM nombre")
    fun getAll(): LiveData<List<nombre>>

    // OBTENER POR ID
    @Query("SELECT * FROM nombre WHERE id = :id")
    fun getByIDAll(id: Long): LiveData<nombre>

    // BUSCAR POR NOMBRE (UNA FILA)
    @Query("SELECT * FROM nombre WHERE Nombre = :nombre LIMIT 1")
    suspend fun todoNombre(nombre: String): nombre?

    @Query("SELECT SUM(Monto) FROM nombre")
    fun getTotalMonto(): LiveData<Int?>

    // En tu interfaz DAO, agrega esta consulta:
    @Query("SELECT Nombre FROM nombre ORDER BY id DESC LIMIT 1")
    suspend fun obtenerUltimoNombre(): String?

}
