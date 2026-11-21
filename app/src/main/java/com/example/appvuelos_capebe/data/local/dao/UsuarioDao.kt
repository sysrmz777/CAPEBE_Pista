package com.example.appvuelos_capebe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appvuelos_capebe.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDao {

    // insertar usuario en SQLite
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registerUser(user: UsuarioEntity)

    // buscar por correo
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UsuarioEntity?

    // obtener el ultimo usuario guardado en SQLite
    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
    suspend fun getUltimoUsuario(): UsuarioEntity?
}
