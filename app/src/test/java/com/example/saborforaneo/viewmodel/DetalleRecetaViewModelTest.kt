package com.example.saborforaneo.viewmodel

import com.example.saborforaneo.data.model.Receta
import com.example.saborforaneo.data.repository.FirestoreRepository
import com.example.saborforaneo.data.repository.RecetaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pruebas unitarias para DetalleRecetaViewModel
 *
 * Estas pruebas verifican que:
 * 1. Cuando una receta NO es favorita, alternarFavorito() la agrega a favoritos
 * 2. Cuando una receta SI es favorita, alternarFavorito() la quita de favoritos
 * 3. La receta se recarga después de cambiar el estado de favorito
 */

// Indica que esta clase usa funciones experimentales de coroutines
@OptIn(ExperimentalCoroutinesApi::class)
// Clase que contiene todas las pruebas del ViewModel
class DetalleRecetaViewModelTest {

    // Variable para simular el repositorio de recetas
    private lateinit var recetaRepository:  RecetaRepository
    // Variable para simular el repositorio de Firestore
    private lateinit var firestoreRepository: FirestoreRepository
    // Variable del ViewModel que vamos a probar
    private lateinit var viewModel: DetalleRecetaViewModel
    // Dispatcher especial para controlar las coroutines en los tests
    private val testDispatcher = StandardTestDispatcher()

    // Receta de ejemplo para usar en las pruebas
    private val recetaTest = Receta(
        id = "123", // ID único de la receta
        nombre = "Tacos al Pastor", // Nombre de la receta
        descripcion = "Deliciosos tacos mexicanos", // Descripción breve
        // Lista de ingredientes necesarios
        ingredientes = listOf("Carne de cerdo", "Piña", "Cebolla", "Cilantro", "Tortillas"),
        // Lista de pasos para preparar
        pasos = listOf(
            "Marinar la carne",
            "Asar la carne",
            "Calentar tortillas",
            "Servir con piña y cebolla"
        ),
        categoria = "🌮 Comida Mexicana", // Categoría de la receta
        imagenUrl = "https://example.com/tacos.jpg", // URL de la imagen
        esFavorito = false // Por defecto NO es favorita
    )






    // Este método se ejecuta ANTES de cada prueba
    @Before
    fun setup() {
        // Reemplaza el dispatcher principal por uno de prueba
        Dispatchers.setMain(testDispatcher)
        // Crea un objeto falso del repositorio de recetas
        recetaRepository = mock()
        // Crea un objeto falso del repositorio de Firestore
        firestoreRepository = mock()
    }






    // Este método se ejecuta DESPUÉS de cada prueba
    @After
    fun tearDown() {
        // Restaura el dispatcher original
        Dispatchers.resetMain()
    }







    // Prueba:  Agregar una receta a favoritos
    @Test
    fun `cuando la receta NO es favorita, alternarFavorito la agrega a favoritos`() = runTest {
        // Cuando se pida la receta con ID "123"
        whenever(recetaRepository.obtenerRecetaPorId("123"))
            // Devuelve una receta que NO es favorita
            .thenReturn(Result.success(recetaTest.copy(esFavorito = false)))

        // Cuando se pregunte si es favorita
        whenever(firestoreRepository.esFavorito("123"))
            .thenReturn(Result.success(false))  // Primera vez: NO es favorito
            .thenReturn(Result.success(true))   // Segunda vez: SÍ es favorito

        // Cuando se agregue a favoritos
        whenever(firestoreRepository.agregarFavorito("123"))
            // Devuelve éxito
            .thenReturn(Result.success(Unit))

        // Crea el ViewModel (esto carga la receta automáticamente)
        viewModel = DetalleRecetaViewModel(recetaRepository, firestoreRepository, "123")
        // Espera a que termine de cargar
        advanceUntilIdle()

        // Ejecuta el método que alterna el favorito
        viewModel.alternarFavorito()
        // Espera a que termine
        advanceUntilIdle()

        // Verifica que se llamó 1 vez a agregar favorito
        verify(firestoreRepository, times(1)).agregarFavorito("123")
        // Verifica que NUNCA se llamó a quitar favorito
        verify(firestoreRepository, never()).quitarFavorito(any())
        // Verifica que ahora la receta SÍ es favorita
        assertTrue(viewModel.uiState.value.receta?.esFavorito == true)
    }






    // Prueba: Quitar una receta de favoritos
    @Test
    fun `cuando la receta SI es favorita, alternarFavorito la quita de favoritos`() = runTest {
        // Cuando se pida la receta con ID "123"
        whenever(recetaRepository.obtenerRecetaPorId("123"))
            // Devuelve una receta que SÍ es favorita
            .thenReturn(Result.success(recetaTest.copy(esFavorito = true)))

        // Cuando se pregunte si es favorita
        whenever(firestoreRepository.esFavorito("123"))
            .thenReturn(Result.success(true))   // Primera vez: SÍ es favorito
            .thenReturn(Result.success(false))  // Segunda vez: NO es favorito

        // Cuando se quite de favoritos
        whenever(firestoreRepository.quitarFavorito("123"))
            // Devuelve éxito
            .thenReturn(Result.success(Unit))

        // Crea el ViewModel
        viewModel = DetalleRecetaViewModel(recetaRepository, firestoreRepository, "123")
        // Espera a que termine de cargar
        advanceUntilIdle()

        // Ejecuta el método que alterna el favorito
        viewModel.alternarFavorito()
        // Espera a que termine
        advanceUntilIdle()

        // Verifica que se llamó 1 vez a quitar favorito
        verify(firestoreRepository, times(1)).quitarFavorito("123")
        // Verifica que NUNCA se llamó a agregar favorito
        verify(firestoreRepository, never()).agregarFavorito(any())
        // Verifica que ahora la receta NO es favorita
        assertFalse(viewModel.uiState.value.receta?.esFavorito == true)
    }






    // Prueba: La receta se recarga después de alternar favorito
    @Test
    fun `alternarFavorito recarga la receta despues de cambiar el estado`() = runTest {
        // Cuando se pida la receta
        whenever(recetaRepository.obtenerRecetaPorId("123"))
            // Devuelve la receta de prueba
            .thenReturn(Result.success(recetaTest))

        // Cuando se pregunte si es favorita
        whenever(firestoreRepository.esFavorito("123"))
            // Devuelve falso
            .thenReturn(Result.success(false))

        // Cuando se agregue a favoritos
        whenever(firestoreRepository.agregarFavorito("123"))
            // Devuelve éxito
            .thenReturn(Result.success(Unit))

        // Crea el ViewModel (1ra carga de receta)
        viewModel = DetalleRecetaViewModel(recetaRepository, firestoreRepository, "123")
        // Espera a que termine
        advanceUntilIdle()

        // Ejecuta alternar favorito (2da carga de receta)
        viewModel.alternarFavorito()
        // Espera a que termine
        advanceUntilIdle()

        // Verifica que se cargó la receta 2 veces
        verify(recetaRepository, times(2)).obtenerRecetaPorId("123")
    }






    // Prueba: No hace nada si la receta es null
    @Test
    fun `alternarFavorito no hace nada si la receta es null`() = runTest {
        // Cuando se pida la receta
        whenever(recetaRepository.obtenerRecetaPorId("123"))
            // Devuelve un error (receta no encontrada)
            .thenReturn(Result.failure(Exception("Receta no encontrada")))

        // Crea el ViewModel (falla al cargar)
        viewModel = DetalleRecetaViewModel(recetaRepository, firestoreRepository, "123")
        // Espera a que termine
        advanceUntilIdle()

        // Intenta alternar favorito (pero la receta es null)
        viewModel.alternarFavorito()
        // Espera a que termine
        advanceUntilIdle()

        // Verifica que NUNCA se llamó a agregar favorito
        verify(firestoreRepository, never()).agregarFavorito(any())
        // Verifica que NUNCA se llamó a quitar favorito
        verify(firestoreRepository, never()).quitarFavorito(any())
        // Verifica que la receta es null
        assertNull(viewModel.uiState.value.receta)
        // Verifica que hay un mensaje de error
        assertTrue(viewModel.uiState.value.error?.isNotEmpty() == true)
    }






    // Prueba: El estado inicial es correcto
    @Test
    fun `el estado inicial debe tener cargando en true`() {
        // Crea un estado inicial vacío
        val estado = DetalleRecetaUiState()

        // Verifica que está en modo "cargando"
        assertTrue(estado.cargando)
        // Verifica que no hay receta cargada
        assertNull(estado.receta)
        // Verifica que no hay errores
        assertNull(estado.error)
    }






    // Prueba: La carga exitosa actualiza el estado
    @Test
    fun `cargarReceta actualiza el estado correctamente cuando es exitosa`() = runTest {
        // Cuando se pida la receta
        whenever(recetaRepository.obtenerRecetaPorId("123"))
            // Devuelve la receta de prueba exitosamente
            .thenReturn(Result.success(recetaTest))

        // Cuando se pregunte si es favorita
        whenever(firestoreRepository.esFavorito("123"))
            // Devuelve falso
            .thenReturn(Result.success(false))

        // Crea el ViewModel (carga la receta automáticamente)
        viewModel = DetalleRecetaViewModel(recetaRepository, firestoreRepository, "123")
        // Espera a que termine de cargar
        advanceUntilIdle()

        // Obtiene el estado actual
        val estado = viewModel.uiState.value
        // Verifica que ya NO está cargando
        assertFalse(estado.cargando)
        // Verifica que el nombre de la receta es correcto
        assertEquals("Tacos al Pastor", estado.receta?.nombre)
        // Verifica que no hay errores
        assertNull(estado.error)
    }
}