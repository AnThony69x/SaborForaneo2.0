package com.example.saborforaneo.data.mock

import android.content.Context
import com.example.saborforaneo.data.model.*
import org.json.JSONObject

object DatosMock {

    private var _recetasCache: List<Receta>? = null

    fun cargarRecetas(context: Context) {
        if (_recetasCache != null) return

        val todasLasRecetas = mutableListOf<Receta>()

        todasLasRecetas.addAll(cargarDesdeJson(context, "recetas_ecuatorianas.json"))
        todasLasRecetas.addAll(cargarDesdeJson(context, "recetas_internacionales.json"))

        _recetasCache = todasLasRecetas
    }

    private fun cargarDesdeJson(context: Context, nombreArchivo: String): List<Receta> {
        val recetas = mutableListOf<Receta>()

        try {
            val jsonString = context.assets.open(nombreArchivo)
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("recetas")

            for (i in 0 until jsonArray.length()) {
                val recetaJson = jsonArray.getJSONObject(i)

                val ingredientes = mutableListOf<String>()
                val ingredientesArray = recetaJson.getJSONArray("ingredientes")
                for (j in 0 until ingredientesArray.length()) {
                    ingredientes.add(ingredientesArray.getString(j))
                }

                val pasos = mutableListOf<String>()
                val pasosArray = recetaJson.getJSONArray("pasos")
                for (j in 0 until pasosArray.length()) {
                    pasos.add(pasosArray.getString(j))
                }

                val receta = Receta(
                    id = recetaJson.getString("id"),
                    nombre = recetaJson.getString("nombre"),
                    descripcion = recetaJson.getString("descripcion"),
                    imagenUrl = recetaJson.getString("imagenUrl"),
                    tiempoPreparacion = recetaJson.getInt("tiempoPreparacion"),
                    dificultad = Dificultad.valueOf(recetaJson.getString("dificultad")),
                    porciones = recetaJson.getInt("porciones"),
                    categoria = recetaJson.getString("categoria"),
                    pais = recetaJson.getString("pais"),
                    ingredientes = ingredientes,
                    pasos = pasos,
                    esFavorito = recetaJson.getBoolean("esFavorito"),
                    esVegetariana = recetaJson.getBoolean("esVegetariana"),
                    esVegana = recetaJson.getBoolean("esVegana"),
                    precio = Precio.valueOf(recetaJson.getString("precio"))
                )

                recetas.add(receta)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return recetas
    }

    val recetasDestacadas: List<Receta>
        get() = _recetasCache ?: emptyList()

    val categorias = listOf(
        Categoria("1", "Ecuatoriana", "🇪🇨", 0xFFFF7043),
        Categoria("2", "Mexicana", "🌮", 0xFF81C784),
        Categoria("3", "Italiana", "🍝", 0xFFFFAB91),
        Categoria("4", "Asiática", "🍜", 0xFFFFEB3B),
        Categoria("5", "Desayuno", "🥞", 0xFFFF9800),
        Categoria("6", "Almuerzo", "🍱", 0xFF4CAF50),
        Categoria("7", "Cena", "🍽️", 0xFFBCAAA4),
        Categoria("8", "Rápidas", "⚡", 0xFFC8E6C9),
        Categoria("9", "Vegetariana", "🥗", 0xFF8BC34A),
        Categoria("10", "Económica", "💰", 0xFF8D6E63),
        Categoria("11", "Ensaladas", "🥙", 0xFF9CCC65)
    )

    fun obtenerRecetaPorId(id: String): Receta? {
        return recetasDestacadas.find { it.id == id }
    }

    fun obtenerRecetasFavoritas(): List<Receta> {
        return recetasDestacadas.filter { it.esFavorito }
    }

    fun buscarRecetas(consulta: String): List<Receta> {
        return recetasDestacadas.filter {
            it.nombre.contains(consulta, ignoreCase = true) ||
                    it.descripcion.contains(consulta, ignoreCase = true) ||
                    it.categoria.contains(consulta, ignoreCase = true) ||
                    it.ingredientes.any { ing -> ing.contains(consulta, ignoreCase = true) }
        }
    }
}