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
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleRecetaViewModelTest {

    // Mocks de las dependencias
    private lateinit var recetaRepository: RecetaRepository
    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var viewModel: DetalleRecetaViewModel

    // Dispatcher para pruebas de coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Receta de prueba
    private val recetaTest = Receta(
        id = "123",
        nombre = "Tacos al Pastor",
        descripcion = "Deliciosos tacos mexicanos",
        ingredientes = listOf("Carne de cerdo", "Piña", "Cebolla", "Cilantro", "Tortillas"),
        pasos = listOf(
            "Marinar la carne",
            "Asar la carne",
            "Calentar tortillas",
            "Servir con piña y cebolla"
        ),
        categoria = "🌮 Comida Mexicana",
        imagenUrl = "https://example.com/tacos.jpg",
        esFavorito = false
    )

    @Before
    fun setup() {
        // Configurar el dispatcher para las coroutines de prueba
        Dispatchers.setMain(testDispatcher)

        // Crear mocks de las dependencias
        recetaRepository = mock()
        firestoreRepository = mock()
    }

    @After
    fun tearDown() {
        // Restaurar el dispatcher original
        Dispatchers.resetMain()
    }

    // ============================================
    // PRUEBA 1: Agregar a favoritos
    // ============================================
    @Test
    fun `cuando la receta NO es favorita, alternarFavorito la agrega a favoritos`() = runTest {
        // Given - Configurar el comportamiento de los mocks
        whenever(recetaRepository.obtenerRecetaPorId("123"))
            .thenReturn(Result.success(recetaTest.copy(esFavorito = false)))

        whenever(firestoreRepository.esFavorito("123"))
            .thenReturn(Result.success(false))  // Primera llamada: no es favorito
            .thenReturn(Result.success(true))   // Segunda llamada: ya es favorito

        whenever(firestoreRepository.agregarFavorito("123"))
            .thenReturn(Result.success(Unit))

        // Crear el ViewModel (esto ejecuta init { cargarReceta() })
        viewModel = DetalleRecetaViewModel(recetaRepository, firestoreRepository, "123")
        advanceUntilIdle() // Esperar a que termine cargarReceta()

        // When - Ejecutar la acción que queremos probar
        viewModel.alternarFavorito()
        advanceUntilIdle() // Esperar a que termine alternarFavorito()

        // Then - Verificar los resultados
        verify(firestoreRepository, times(1)).agregarFavorito("123")
        verify(firestoreRepository, never()).quitarFavorito(any())

        // Verificar que el estado se actualizó correctamente
        assertTrue(viewModel.uiState.value.receta?.esFavorito == true)
    }

    // ============================================
    // PRUEBA 2: Quitar de favoritos
    // ============================================
    @Test
    fun `cuando la receta SI es favorita, alternarFavorito la quita de favoritos`() = runTest {
        // Given
        whenever(recetaRepository.obtenerRecetaPorId("123"))
            .thenReturn(Result.success(recetaTest.copy(esFavorito = true)))

        whenever(firestoreRepository.esFavorito("123"))
            .thenReturn(Result.success(true))   // Primera llamada: es favorito
            .thenReturn(Result.success(false))  // Segunda llamada: ya no es favorito

        whenever(firestoreRepository.quitarFavorito("123"))
            .thenReturn(Result.success(Unit))

        viewModel = DetalleRecetaViewModel(recetaRepository, firestoreRepository, "123")
        advanceUntilIdle()

        // When
        viewModel.alternarFavorito()
        advanceUntilIdle()

        // Then
        verify(firestoreRepository, times(1)).quitarFavorito("123")
        verify(firestoreRepository, never()).agregarFavorito(any())

        // Verificar que el estado se actualizó correctamente
        assertFalse(viewModel.uiState.value.receta?.esFavorito == true)
    }

    // ============================================
    // PRUEBA 3: Recarga de receta
    // ============================================
    @Test
    fun `alternarFavorito recarga la receta despues de cambiar el estado`() = runTest {
        // Given
        whenever(recetaRepository.obtenerRecetaPorId("123"))
            .thenReturn(Result.success(recetaTest))

        whenever(firestoreRepository.esFavorito("123"))
            .thenReturn(Result.success(false))

        whenever(firestoreRepository.agregarFavorito("123"))
            .thenReturn(Result.success(Unit))

        viewModel = DetalleRecetaViewModel(recetaRepository, firestoreRepository, "123")
        advanceUntilIdle()

        // When
        viewModel.alternarFavorito()
        advanceUntilIdle()

        // Then - Verificar que cargarReceta() se ejecutó 2 veces:
        // 1. En el init {} del ViewModel
        // 2. Después de alternarFavorito()
        verify(recetaRepository, times(2)).obtenerRecetaPorId("123")
    }

    // ============================================
    // PRUEBA 4: Manejo de errores
    // ============================================
    @Test
    fun `alternarFavorito no hace nada si la receta es null`() = runTest {
        // Given - Simular error al cargar receta
        whenever(recetaRepository.obtenerRecetaPorId("123"))
            .thenReturn(Result.failure(Exception("Receta no encontrada")))

        viewModel = DetalleRecetaViewModel(recetaRepository, firestoreRepository, "123")
        advanceUntilIdle()

        // When
        viewModel.alternarFavorito()
        advanceUntilIdle()

        // Then - No debe llamar a ningún método de favoritos
        verify(firestoreRepository, never()).agregarFavorito(any())
        verify(firestoreRepository, never()).quitarFavorito(any())

        // El estado debe mostrar error
        assertNull(viewModel.uiState.value.receta)
        assertTrue(viewModel.uiState.value.error?.isNotEmpty() == true)
    }

    // ============================================
    // PRUEBA 5: Estado inicial
    // ============================================
    @Test
    fun `el estado inicial debe tener cargando en true`() {
        // Given
        val estado = DetalleRecetaUiState()

        // Then
        assertTrue(estado.cargando)
        assertNull(estado.receta)
        assertNull(estado.error)
    }

    // ============================================
    // PRUEBA 6: Carga exitosa de receta
    // ============================================
    @Test
    fun `cargarReceta actualiza el estado correctamente cuando es exitosa`() = runTest {
        // Given
        whenever(recetaRepository.obtenerRecetaPorId("123"))
            .thenReturn(Result.success(recetaTest))

        whenever(firestoreRepository.esFavorito("123"))
            .thenReturn(Result.success(false))

        // When
        viewModel = DetalleRecetaViewModel(recetaRepository, firestoreRepository, "123")
        advanceUntilIdle()

        // Then
        val estado = viewModel.uiState.value
        assertFalse(estado.cargando)
        assertEquals("Tacos al Pastor", estado.receta?.nombre)
        assertNull(estado.error)
    }
}

