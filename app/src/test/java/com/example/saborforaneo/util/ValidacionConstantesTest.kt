package com.example.saborforaneo.util

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests para las funciones de extensión de ValidacionConstantes
 */
class ValidacionConstantesTest {

    // ========== Tests para porcentajeDeUso() ==========
    
    @Test
    fun `porcentajeDeUso devuelve 50 cuando el texto es la mitad del maximo`() {
        val texto = "Hola!"
        val resultado = texto.porcentajeDeUso(10)
        assertEquals(50f, resultado)
    }
    
    @Test
    fun `porcentajeDeUso devuelve 0 cuando el texto esta vacio`() {
        val texto = ""
        val resultado = texto.porcentajeDeUso(100)
        assertEquals(0f, resultado)
    }
    
    @Test
    fun `porcentajeDeUso devuelve 100 cuando el texto alcanza el maximo`() {
        val texto = "1234567890"
        val resultado = texto.porcentajeDeUso(10)
        assertEquals(100f, resultado)
    }

    // ========== Tests para cercaDelLimite() ==========
    
    @Test
    fun `cercaDelLimite devuelve true cuando esta en 80 porciento`() {
        val texto = "12345678" // 8 de 10 = 80%
        val resultado = texto.cercaDelLimite(10)
        assertTrue(resultado)
    }
    
    @Test
    fun `cercaDelLimite devuelve true cuando esta en 90 porciento`() {
        val texto = "123456789" // 9 de 10 = 90%
        val resultado = texto.cercaDelLimite(10)
        assertTrue(resultado)
    }
    
    @Test
    fun `cercaDelLimite devuelve false cuando esta en 70 porciento`() {
        val texto = "1234567" // 7 de 10 = 70%
        val resultado = texto.cercaDelLimite(10)
        assertFalse(resultado)
    }

    // ========== Tests para contarLineasNoVacias() ==========
    
    @Test
    fun `contarLineasNoVacias devuelve 3 cuando hay 3 lineas con texto`() {
        val texto = """
            Línea 1
            Línea 2
            Línea 3
        """.trimIndent()
        
        val resultado = texto.contarLineasNoVacias()
        assertEquals(3, resultado)
    }
    
    @Test
    fun `contarLineasNoVacias ignora lineas vacias`() {
        val texto = """
            Línea 1
            
            Línea 2
            
            
            Línea 3
        """.trimIndent()
        
        val resultado = texto.contarLineasNoVacias()
        assertEquals(3, resultado) // Solo las 3 líneas con texto
    }
    
    @Test
    fun `contarLineasNoVacias devuelve 0 cuando el texto esta vacio`() {
        val texto = ""
        val resultado = texto.contarLineasNoVacias()
        assertEquals(0, resultado)
    }
    
    @Test
    fun `contarLineasNoVacias ignora lineas con solo espacios`() {
        val texto = """
            Línea 1
                
            Línea 2
        """.trimIndent()
        
        val resultado = texto.contarLineasNoVacias()
        assertEquals(2, resultado) // Solo las 2 líneas con texto real
    }
}
