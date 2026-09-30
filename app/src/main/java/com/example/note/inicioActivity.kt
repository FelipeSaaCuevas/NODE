package com.example.note

import androidx.core.content.ContextCompat
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import BaseDatos.data.DB.BDLibro
import android.annotation.SuppressLint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class inicioActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.inicio_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val b1 = findViewById<Button>(R.id.b1)
        val b2 = findViewById<Button>(R.id.b2)
        val contenedor = findViewById<LinearLayout>(R.id.TDato)
        val tResultado = findViewById<TextView>(R.id.TResultados)

        val db = BDLibro.getDatabase(this)
        val dao = db.enDao()

        // Configurar el contenedor para centrar las tarjetas
        contenedor.gravity = Gravity.CENTER_HORIZONTAL

        dao.getAll().observe(this) { lista ->
            contenedor.removeAllViews()

            for (item in lista) {

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(32, 32, 32, 32)
                    background = ContextCompat.getDrawable(
                        this@inicioActivity,
                        R.drawable.imagen_card
                    )

                    val screenWidth = resources.displayMetrics.widthPixels
                    layoutParams = LinearLayout.LayoutParams(
                        (screenWidth * 0.8).toInt(),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(24, 16, 24, 16)
                    }
                }

                val fechaTexto = obtenerTiempoRelativo(item.Fecha)

                val tv = TextView(this)
                tv.text =
                    "Nombre: ${item.Nombre}\nAsunto: ${item.asunto}\nMonto: ${item.Monto}\nFecha: $fechaTexto"
                tv.textSize = 16f

                val btnBorrar = Button(this)
                btnBorrar.text = "Borrar"
                btnBorrar.setOnClickListener {
                    mostrarDialogoBorrar(item, dao)
                }

                val btnCopiar = Button(this)
                btnCopiar.text = "Copiar monto"
                btnCopiar.setOnClickListener {

                    val texto = item.Monto.toString()
                    val clipboard =
                        getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip =
                        android.content.ClipData.newPlainText("Resultado", texto)

                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(this, "monto copiado", Toast.LENGTH_SHORT).show()
                }

                card.addView(tv)
                card.addView(btnBorrar)
                card.addView(btnCopiar)

                contenedor.addView(card)
            }
        }

        // copiar total
        tResultado.setOnClickListener {
            val textoCompleto = tResultado.text.toString()
            val soloNumero = textoCompleto.replace("El total: ", "").trim()

            val clipboard =
                getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip =
                android.content.ClipData.newPlainText("Resultado", soloNumero)

            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Monto total copiado", Toast.LENGTH_SHORT).show()
        }

        dao.getTotalMonto().observe(this) { total ->
            tResultado.text = "El total: ${total ?: 0}"
        }

        // menu
        b1.setOnClickListener {

            val opciones = arrayOf(
                "Agregar nota",
                "Ver notas",
                "Eliminar todo"
            )

            AlertDialog.Builder(this)
                .setTitle("¿Qué quiere hacer?")
                .setItems(opciones) { _, which ->
                    when (which) {
                        0 -> {
                            startActivity(Intent(this, GuardarActivity::class.java))
                            finish()
                        }
                        1 -> contenedor.visibility = View.VISIBLE
                        2 -> mostrarDialogoEliminarTodo(dao)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        b2.setOnClickListener {
            contenedor.visibility = View.VISIBLE
        }
    }

    // 🔥 FUNCIÓN NUEVA: tiempo relativo
    private fun obtenerTiempoRelativo(fecha: Long): String {
        val ahora = System.currentTimeMillis()
        val diferencia = ahora - fecha

        val segundos = diferencia / 1000
        val minutos = segundos / 60
        val horas = minutos / 60
        val dias = horas / 24

        return when {
            segundos < 60 -> "hace un momento"
            minutos < 60 -> "hace $minutos minuto(s)"
            horas < 24 -> "hace $horas hora(s)"
            dias == 1L -> "ayer"
            dias < 7 -> "hace $dias día(s)"
            else -> {
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formato.format(Date(fecha))
            }
        }
    }

    private fun mostrarDialogoBorrar(
        item: BaseDatos.data.entity.nombre,
        dao: BaseDatos.data.dao.entityDao
    ) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar registro")
            .setMessage("¿Seguro que deseas eliminar este registro?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    dao.delete(item)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEliminarTodo(
        dao: BaseDatos.data.dao.entityDao
    ) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar TODO")
            .setMessage("¿Seguro que deseas borrar todos los registros?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                lifecycleScope.launch {
                    dao.deleteAll()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}