package com.example.note

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import BaseDatos.data.DB.BDLibro
import BaseDatos.data.entity.nombre
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GuardarActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_guardar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val t2 = findViewById<EditText>(R.id.t2) // Nombre
        val t3 = findViewById<EditText>(R.id.t3) // Asunto
        val t4 = findViewById<EditText>(R.id.t4) // Monto
        val b1 = findViewById<Button>(R.id.b1) // boton de guardar
        val b2 = findViewById<Button>(R.id.b2) // boton de salida
        val check = findViewById<Chip>(R.id.check) // Chip para gastos

        // Instancia de la base de datos
        val db = BDLibro.getDatabase(this)
        val dao = db.enDao()

        // Variable para almacenar el último nombre guardado
        var ultimoNombreGuardado = ""

        // Cargar el último nombre guardado al iniciar la actividad
        lifecycleScope.launch {
            ultimoNombreGuardado = withContext(Dispatchers.IO) {
                dao.obtenerUltimoNombre() ?: ""
            }
            // Establecer el último nombre en el EditText si existe
            if (ultimoNombreGuardado.isNotEmpty()) {
                t2.setText(ultimoNombreGuardado)
            }
        }

        // Limpiar el campo nombre al hacer clic
        t2.setOnClickListener {
            if (t2.text.isNotEmpty()){
                t2.text.clear()
            }
        }

        // Configurar el Chip para mostrar el monto negativo cuando está activado
        check.setOnCheckedChangeListener { _, isChecked ->
            val textoMonto = t4.text.toString()
            if (textoMonto.isNotEmpty()) {
                val monto = textoMonto.toIntOrNull()
                if (monto != null && monto > 0) {
                    if (isChecked) {
                        // Si el chip está activado, mostrar el número negativo
                        t4.setText("-$monto")
                        Toast.makeText(this, "Gasto activado", Toast.LENGTH_SHORT).show()
                    } else {
                        // Si el chip está desactivado, mostrar el número positivo
                        t4.setText(monto.toString())
                        Toast.makeText(this, "Ingreso activado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Botón Guardar
        b1.setOnClickListener {
            val nombreTexto = t2.text.toString()
            val asuntoTexto = t3.text.toString()
            val montoTexto = t4.text.toString()

            if (nombreTexto.isNotEmpty() && asuntoTexto.isNotEmpty() && montoTexto.isNotEmpty()) {

                // Obtener el monto como entero
                var montoInt = montoTexto.toIntOrNull()

                if (montoInt == null) {
                    Toast.makeText(this@GuardarActivity, "Ingrese un número válido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Validar que el monto sea positivo (para evitar que el usuario ingrese números negativos manualmente)
                if (montoInt < 0) {
                    Toast.makeText(this@GuardarActivity, "Solo se permiten números positivos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Si el chip está marcado, el monto se guarda como negativo
                val montoFinal = if (check.isChecked) -montoInt else montoInt

                val nuevoRegistro = nombre(
                    Nombre = nombreTexto,
                    asunto = asuntoTexto,
                    Monto = montoFinal
                )

                // Guardar en Room
                lifecycleScope.launch {
                    dao.insert(nuevoRegistro)
                    // Actualizar el último nombre guardado
                    ultimoNombreGuardado = nombreTexto
                }

                Toast.makeText(this@GuardarActivity, "Registro guardado exitosamente", Toast.LENGTH_SHORT).show()

                // Limpiar campos
                t3.text.clear()
                t4.text.clear()
                check.isChecked = false

            } else {
                Toast.makeText(this@GuardarActivity, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón Salir
        b2.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Salir")
                .setMessage("¿Seguro que quieres salir?")
                .setPositiveButton("Aceptar") { _, _ ->
                    startActivity(Intent(this, inicioActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}