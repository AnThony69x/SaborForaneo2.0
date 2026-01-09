# Guía de Pruebas Unitarias - SaborForaneo

## 📋 ¿Qué son las pruebas unitarias?

Las **pruebas unitarias** son fragmentos de código que verifican el comportamiento de una unidad específica de código (función, método o clase) de forma aislada. Se ejecutan automáticamente y de manera rápida, sin necesidad de la UI o dispositivos Android.

---

## 🎯 ¿Qué se puede probar?

### 1. Funciones puras
- Lógica de validación
- Cálculos matemáticos
- Transformaciones de datos
- Utilidades

### 2. ViewModels
- Estados de UI
- Flujos de datos (StateFlow, LiveData)
- Manejo de casos de uso
- Lógica de negocio

### 3. Repositorios
- Operaciones CRUD
- Transformación de datos de Firebase
- Manejo de errores

### 4. Validaciones
- Campos de formularios
- Reglas de negocio
- Autenticación

---

## ✅ Ventajas de las pruebas unitarias

| Ventaja | Descripción |
|---------|-------------|
| 🐛 **Detección temprana de errores** | Encuentra bugs antes de que lleguen a producción |
| 🔄 **Refactorización segura** | Puedes cambiar código con confianza sabiendo que las pruebas te avisarán si algo se rompe |
| 📚 **Documentación viva** | Las pruebas documentan cómo debe comportarse el código |
| 🚀 **Menos bugs en producción** | Reduce la cantidad de errores que experimentan los usuarios |
| 💎 **Mejor calidad de código** | Código más limpio, modular y mantenible |
| ⚡ **Velocidad** | Se ejecutan en segundos sin necesidad de emulador |

---

## 🧪 Aplicación al proyecto SaborForaneo

### ViewModel identificado: `DetalleRecetaViewModel`

Este ViewModel maneja la lógica de mostrar el detalle de una receta y la funcionalidad de favoritos.

### Función a probar: `alternarFavorito()`

```kotlin
fun alternarFavorito() {
    viewModelScope.launch {
        _uiState.value.receta?.let { receta ->
            val esFavorito = receta.esFavorito
            
            val resultado = if (esFavorito) {
                firestoreRepository.quitarFavorito(receta.id)
            } else {
                firestoreRepository.agregarFavorito(receta.id)
            }
            
            if (resultado.isSuccess) {
                cargarReceta()
            }
        }
    }
}
```

### ¿Qué se está probando?

1. ✅ **Agregar a favoritos**: Verifica que cuando una receta NO es favorita, se llame a `agregarFavorito()`
2. ✅ **Quitar de favoritos**: Verifica que cuando una receta SI es favorita, se llame a `quitarFavorito()`
3. ✅ **Recarga de receta**: Confirma que `cargarReceta()` se ejecute después de cambiar el estado
4. ✅ **Manejo de null**: Verifica que no falle si la receta no existe
5. ✅ **Estado inicial**: Valida que el estado inicial sea consistente
6. ✅ **Carga exitosa**: Asegura que los datos se cargan correctamente

### ¿Por qué es importante probar esto?

| Razón | Impacto |
|-------|---------|
| **Funcionalidad crítica** | Los favoritos son una característica importante para los usuarios |
| **Interacción con Firebase** | Previene errores al guardar/eliminar favoritos en Firestore |
| **Experiencia de usuario** | Asegura que la UI se actualice correctamente después de cada acción |
| **Casos edge** | Detecta problemas cuando la receta no existe o hay errores de conexión |

---

## 🚀 Cómo ejecutar las pruebas

### Opción 1: Desde Android Studio (Recomendado)

1. Abre el archivo `DetalleRecetaViewModelTest.kt`
2. Click derecho en el nombre de la clase
3. Selecciona **"Run 'DetalleRecetaViewModelTest'"**
4. Verás los resultados en la ventana "Run" en la parte inferior

### Opción 2: Ejecutar una prueba individual

1. Ubica el método de prueba que quieres ejecutar (ej: `cuando la receta NO es favorita...`)
2. Click en el ícono de "play" verde al lado del método
3. Se ejecutará solo esa prueba

### Opción 3: Desde la terminal

```bash
# Windows
gradlew test

# Ver el reporte HTML generado
start app\build\reports\tests\testDebugUnitTest\index.html
```

---

## 📊 Interpretando los resultados

### ✅ Prueba exitosa
```
✓ cuando la receta NO es favorita, alternarFavorito la agrega a favoritos
✓ cuando la receta SI es favorita, alternarFavorito la quita de favoritos
✓ alternarFavorito recarga la receta despues de cambiar el estado
```

### ❌ Prueba fallida
```
✗ cuando la receta NO es favorita, alternarFavorito la agrega a favoritos
  Expected: agregarFavorito() to be called once
  Actual: never called
```

---

## 🔧 Tecnologías utilizadas en las pruebas

| Librería | Propósito |
|----------|-----------|
| **JUnit 4** | Framework base para pruebas unitarias |
| **Mockito** | Crear mocks (simulaciones) de dependencias |
| **Mockito-Kotlin** | Extensiones de Mockito para Kotlin |
| **Coroutines Test** | Probar código asíncrono con coroutines |
| **Kotlin Test** | Assertions más expresivas en Kotlin |

---

## 📝 Estructura de una prueba

Cada prueba sigue el patrón **AAA** (Arrange-Act-Assert):

```kotlin
@Test
fun `cuando la receta NO es favorita, alternarFavorito la agrega a favoritos`() = runTest {
    // Given (Arrange) - Configurar el escenario
    whenever(recetaRepository.obtenerRecetaPorId("123"))
        .thenReturn(Result.success(recetaTest))
    
    // When (Act) - Ejecutar la acción
    viewModel.alternarFavorito()
    
    // Then (Assert) - Verificar los resultados
    verify(firestoreRepository).agregarFavorito("123")
}
```

---

## 🎓 Conceptos clave

### Mock
Un **mock** es un objeto simulado que imita el comportamiento de un objeto real. Permite probar código sin depender de bases de datos, APIs o servicios externos.

```kotlin
// Crear un mock
private lateinit var firestoreRepository: FirestoreRepository
firestoreRepository = mock()

// Configurar su comportamiento
whenever(firestoreRepository.agregarFavorito("123"))
    .thenReturn(Result.success(Unit))
```

### Verify
`verify()` comprueba que un método fue llamado con los parámetros esperados:

```kotlin
// Verificar que se llamó una vez
verify(firestoreRepository, times(1)).agregarFavorito("123")

// Verificar que nunca se llamó
verify(firestoreRepository, never()).quitarFavorito(any())
```

### Test Dispatcher
Un `TestDispatcher` permite controlar la ejecución de coroutines en las pruebas:

```kotlin
private val testDispatcher = StandardTestDispatcher()

@Before
fun setup() {
    Dispatchers.setMain(testDispatcher)
}

// Esperar a que todas las coroutines terminen
advanceUntilIdle()
```

---

## 📦 Archivos modificados/creados

### ✏️ Modificados
- `app/src/main/java/com/example/saborforaneo/viewmodel/DetalleRecetaViewModel.kt`
  - Refactorizado para inyección de dependencias
  - Código original comentado para referencia

- `app/build.gradle.kts`
  - Agregadas dependencias de testing

### ✨ Creados
- `app/src/test/java/com/example/saborforaneo/viewmodel/DetalleRecetaViewModelTest.kt`
  - 6 pruebas unitarias completas
  - Documentación en español

---

## 🎯 Próximos pasos

1. **Ejecutar las pruebas** para verificar que todo funciona correctamente
2. **Agregar más pruebas** para otros ViewModels (HomeViewModel, ComunidadViewModel, etc.)
3. **Crear pruebas de integración** para probar flujos completos
4. **Configurar CI/CD** para ejecutar pruebas automáticamente en cada commit

---

## 📚 Referencias

- [JUnit Documentation](https://junit.org/junit4/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Kotlin Coroutines Testing](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Android Testing Guide](https://developer.android.com/training/testing)

---

**Creado para:** SaborForaneo 2.0  
**Fecha:** Enero 2026  
**Autor:** Equipo de Desarrollo

