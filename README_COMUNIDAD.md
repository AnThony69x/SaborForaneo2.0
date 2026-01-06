# 🍽️ Funcionalidad de Comunidad - SaborForáneo

## 📝 Descripción

La funcionalidad de Comunidad permite a los usuarios de SaborForáneo compartir sus propias recetas con la comunidad. Los usuarios pueden:

- ✅ Ver recetas compartidas por otros usuarios
- ✅ Crear y publicar sus propias recetas con fotos
- ✅ Editar sus recetas creadas
- ✅ Eliminar sus recetas
- ❤️ Dar "like" a recetas de otros usuarios
- 💬 Ver número de comentarios (preparado para futura implementación)

## 🚀 Características Implementadas

### 1. **Pantalla de Comunidad**
   - Vista de "Todas las recetas" de la comunidad
   - Vista de "Mis Recetas" para gestionar las propias
   - Botón flotante para crear nueva receta
   - Sistema de likes en tiempo real
   - Información del autor con foto de perfil

### 2. **Crear Receta**
   - Formulario completo con validación
   - Subir foto desde galería
   - Campos: nombre, descripción, categoría, tiempo, porciones, dificultad
   - Ingredientes dinámicos (agregar/eliminar)
   - Pasos de preparación dinámicos (agregar/eliminar)
   - Categorías predefinidas con emojis

### 3. **Gestión de Recetas**
   - Editar recetas propias (preparado)
   - Eliminar recetas propias con confirmación
   - Solo el autor puede modificar/eliminar sus recetas

### 4. **Integración**
   - Botón en pantalla de búsqueda para acceder a la comunidad
   - Navegación fluida entre pantallas
   - Almacenamiento en Firebase Firestore
   - Imágenes en Firebase Storage

## 🗂️ Archivos Creados

### Modelos
- `RecetaComunidad.kt` - Modelo de datos de receta de comunidad
- `ComentarioReceta.kt` - Modelo para comentarios (preparado)

### Repositorios
- `ComunidadRepository.kt` - Lógica de negocio y acceso a Firestore
- Métodos agregados en `StorageRepository.kt` para imágenes

### ViewModels
- `ComunidadViewModel.kt` - Gestión de estado de la comunidad

### UI
- `PantallaComunidad.kt` - Pantalla principal de comunidad
- `PantallaCrearReceta.kt` - Formulario de creación/edición
- `TarjetaRecetaComunidad.kt` - Componente de tarjeta de receta

### Navegación
- Rutas agregadas en `Rutas.kt`
- Navegación configurada en `NavGraph.kt`
- Botón en `PantallaBusqueda.kt`

## 📋 Configuración Requerida

### Firebase Firestore

1. **Reglas de seguridad**: Ver `CONFIGURACION_COMUNIDAD.md`
2. **Índices compuestos**: Crear los índices especificados en el archivo de configuración

### Firebase Storage

1. **Reglas de seguridad**: Ver `CONFIGURACION_COMUNIDAD.md`

## 🔄 Flujo de Usuario

1. Usuario va a la pantalla de **Búsqueda**
2. Click en botón flotante **"Comunidad"**
3. Ve todas las recetas de la comunidad
4. Puede dar like a cualquier receta
5. Cambia a **"Mis Recetas"** para ver/gestionar sus recetas
6. Click en botón **"+"** para crear nueva receta
7. Llena el formulario y publica
8. La receta aparece en la comunidad inmediatamente

## 🎨 Características de UI/UX

- **Diseño Material 3**: Siguiendo las directrices de Material Design
- **Animaciones suaves**: Transiciones entre pantallas
- **Tarjetas atractivas**: Diseño visual agradable para recetas
- **Feedback inmediato**: Snackbars para confirmar acciones
- **Validación de formularios**: Previene datos incorrectos
- **Imágenes optimizadas**: Carga eficiente con Coil
- **Tiempo relativo**: Muestra "Hace 5 min", "Hace 2 días", etc.

## 🔮 Funcionalidades Futuras (Preparadas)

### Comentarios
- El modelo y repositorio están listos
- Solo falta implementar la UI

### Edición de Recetas
- La navegación está preparada
- Reutilizar `PantallaCrearReceta` con datos precargados

### Filtros y Búsqueda
- Filtrar por categoría
- Buscar recetas en la comunidad
- Ordenar por popularidad

### Moderación (Admin)
- Aprobar/rechazar recetas antes de publicar
- Reportar recetas inapropiadas

## 🐛 Notas Técnicas

### Seguridad
- Todas las operaciones validan el UID del usuario
- Las reglas de Firestore impiden modificar recetas ajenas
- Las imágenes se organizan por carpetas de usuario

### Rendimiento
- Paginación lista para implementar
- Caché de imágenes con Coil
- Observación en tiempo real con Flow

### Escalabilidad
- Estructura lista para miles de recetas
- Índices optimizados para consultas rápidas
- Separación de concerns (Repository pattern)

## ✅ Testing

Para probar la funcionalidad:

1. Inicia sesión con dos cuentas diferentes
2. Crea una receta con la cuenta 1
3. Verifica que aparezca en "Comunidad"
4. Cambia a cuenta 2
5. Ve la receta de cuenta 1 en "Comunidad"
6. Da like a la receta
7. Verifica que el contador aumente
8. Intenta editar (no debería poder)
9. La cuenta 1 puede editar/eliminar su propia receta

## 📱 Capturas de Pantalla

_Próximamente: Agregar capturas de las pantallas implementadas_

## 🤝 Contribuciones

Para agregar nuevas funcionalidades:
1. Seguir la estructura existente
2. Mantener la separación de capas (UI, ViewModel, Repository)
3. Agregar validaciones apropiadas
4. Documentar cambios en reglas de Firebase

---

**¡La comunidad de SaborForáneo está lista para que los usuarios compartan sus mejores recetas! 🎉**

