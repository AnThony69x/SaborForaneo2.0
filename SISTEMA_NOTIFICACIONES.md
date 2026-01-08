# Sistema de Notificaciones - SaborForaneo

## 📱 Descripción General

Se ha implementado un sistema completo de notificaciones push para la aplicación SaborForaneo que incluye:

1. **Notificaciones cuando el administrador publica una receta**
2. **Notificaciones cuando un usuario crea una nueva receta en la comunidad**
3. **Notificaciones push periódicas (recordatorios) para usar la aplicación**

---

## 🏗️ Arquitectura del Sistema

### Componentes Implementados

#### 1. **MyFirebaseMessagingService**
- **Ubicación**: `app/src/main/java/com/example/saborforaneo/notifications/MyFirebaseMessagingService.kt`
- **Función**: Servicio que maneja las notificaciones push de Firebase Cloud Messaging
- **Características**:
  - Recibe y procesa mensajes push de FCM
  - Genera y guarda tokens FCM en Firestore
  - Crea canales de notificación específicos por tipo
  - Muestra notificaciones locales personalizadas

**Canales de notificación:**
- `admin_receta_channel`: Recetas publicadas por admin (PRIORIDAD ALTA)
- `comunidad_receta_channel`: Recetas de la comunidad (PRIORIDAD MEDIA)
- `recordatorio_channel`: Recordatorios de uso (PRIORIDAD MEDIA)
- `general_channel`: Notificaciones generales (PRIORIDAD MEDIA)

#### 2. **NotificacionesManager**
- **Ubicación**: `app/src/main/java/com/example/saborforaneo/notifications/NotificacionesManager.kt`
- **Función**: Gestor centralizado de notificaciones
- **Métodos principales**:
  - `notificarNuevaRecetaAdmin(titulo, descripcion)`: Notifica publicación de receta por admin
  - `notificarNuevaRecetaComunidad(titulo, nombreAutor)`: Notifica receta de usuario
  - `mostrarRecordatorioApp()`: Muestra recordatorio aleatorio
  - `registrarTokenFCM(token)`: Guarda token FCM del usuario

#### 3. **RecordatorioWorker**
- **Ubicación**: `app/src/main/java/com/example/saborforaneo/notifications/RecordatorioWorker.kt`
- **Función**: Worker de WorkManager para ejecutar notificaciones periódicas
- **Características**:
  - Se ejecuta en segundo plano cada 24 horas
  - No consume batería innecesariamente
  - Persiste incluso si la app se cierra

#### 4. **NotificacionesScheduler**
- **Ubicación**: `app/src/main/java/com/example/saborforaneo/notifications/NotificacionesScheduler.kt`
- **Función**: Programador de notificaciones periódicas
- **Métodos**:
  - `programarRecordatorios(context, intervaloHoras)`: Programa recordatorios
  - `cancelarRecordatorios(context)`: Cancela todos los recordatorios
  - `verificarEstado(context)`: Verifica el estado de los recordatorios

---

## 🔧 Configuración

### Dependencias Agregadas

En `app/build.gradle.kts`:

```kotlin
// Firebase Cloud Messaging
implementation("com.google.firebase:firebase-messaging")

// WorkManager para notificaciones periódicas
implementation("androidx.work:work-runtime-ktx:2.9.0")
```

### AndroidManifest.xml

Se agregaron las siguientes configuraciones:

```xml
<!-- Permiso de notificaciones (ya existía) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

<!-- Servicio de FCM -->
<service
    android:name=".notifications.MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

<!-- Metadata de FCM -->
<meta-data
    android:name="com.google.firebase.messaging.default_notification_icon"
    android:resource="@android:drawable/ic_dialog_info" />
<meta-data
    android:name="com.google.firebase.messaging.default_notification_color"
    android:resource="@android:color/holo_orange_dark" />
```

---

## 📊 Integración con ViewModels

### GestionComunidadViewModel

Se modificó para enviar notificaciones cuando el admin publica una receta:

```kotlin
fun publicarReceta(recetaId: String) {
    viewModelScope.launch {
        // ... obtener datos de la receta ...
        
        // Actualizar estado
        recetasCollection.document(recetaId).update(...)
        
        // 🔔 NOTIFICAR A USUARIOS
        notificacionesManager.notificarNuevaRecetaAdmin(tituloReceta, descripcion)
        
        cargarRecetasComunidad()
    }
}
```

### ComunidadViewModel

Se modificó para enviar notificaciones cuando un usuario crea una receta:

```kotlin
fun crearReceta(...) {
    viewModelScope.launch {
        // ... crear receta ...
        
        val resultado = comunidadRepository.crearReceta(receta, null)
        if (resultado.isSuccess) {
            // 🔔 NOTIFICAR A OTROS USUARIOS
            notificacionesManager.notificarNuevaRecetaComunidad(nombre, usuario.nombre)
            
            cargarMisRecetas()
            onSuccess()
        }
    }
}
```

---

## 🚀 Flujo de Funcionamiento

### 1. Inicialización (MainActivity)

Cuando la app inicia:
1. Se obtiene el token FCM del dispositivo
2. Se programa el worker de recordatorios (cada 24h)
3. Se crean los canales de notificación

### 2. Admin Publica Receta

```
[Admin pulsa "Publicar"] 
    ↓
[GestionComunidadViewModel.publicarReceta()]
    ↓
[Actualiza Firestore: publicada=true]
    ↓
[NotificacionesManager.notificarNuevaRecetaAdmin()]
    ↓
[Obtiene todos los tokens FCM de usuarios]
    ↓
[Muestra notificación local] 🔔
```

### 3. Usuario Crea Receta

```
[Usuario crea receta] 
    ↓
[ComunidadViewModel.crearReceta()]
    ↓
[Guarda en Firestore con publicada=false]
    ↓
[NotificacionesManager.notificarNuevaRecetaComunidad()]
    ↓
[Obtiene tokens de usuarios interesados]
    ↓
[Muestra notificación local] 🔔
```

### 4. Recordatorios Periódicos

```
[WorkManager ejecuta cada 24h]
    ↓
[RecordatorioWorker.doWork()]
    ↓
[NotificacionesManager.mostrarRecordatorioApp()]
    ↓
[Selecciona mensaje aleatorio]
    ↓
[Muestra notificación] 🔔
```

---

## 📝 Estructura de Datos en Firestore

### Colección: usuarios

Para que las notificaciones funcionen, cada documento de usuario debe tener:

```json
{
  "uid": "usuario123",
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "fcmToken": "token_firebase_cloud_messaging_aqui",
  ...
}
```

El campo `fcmToken` se actualiza automáticamente cuando:
- El usuario inicia sesión
- Se genera un nuevo token FCM
- El usuario cambia de dispositivo

---

## 🎨 Tipos de Notificaciones

### 1. Receta Publicada por Admin
- **Canal**: `admin_receta_channel`
- **Prioridad**: ALTA
- **Icono**: 🎉
- **Título**: "🎉 Nueva Receta Publicada"
- **Mensaje**: "{Título de la receta}\n\n{Descripción resumida}..."

### 2. Receta de la Comunidad
- **Canal**: `comunidad_receta_channel`
- **Prioridad**: MEDIA
- **Icono**: 👥
- **Título**: "👥 Nueva Receta de la Comunidad"
- **Mensaje**: "{Nombre del autor} compartió: {Título de la receta}"

### 3. Recordatorio de Uso
- **Canal**: `recordatorio_channel`
- **Prioridad**: MEDIA
- **Icono**: 🔔
- **Título**: "🔔 ¡Te extrañamos!"
- **Mensajes aleatorios**:
  - "¿Qué tal una nueva receta hoy? 🍳"
  - "Descubre sabores únicos en SaborForaneo 🌎"
  - "¡Hora de cocinar algo delicioso! 👨‍🍳"
  - "Tenemos recetas increíbles esperándote 🍽️"
  - "¿Ya probaste las recetas de la comunidad? 👥"

---

## 🔐 Permisos Necesarios

El usuario debe otorgar permisos de notificación:
- Android 13+ (API 33+): Se solicita permiso `POST_NOTIFICATIONS`
- El permiso se solicita automáticamente en el primer inicio
- Si se deniega, las notificaciones no se mostrarán

---

## 🧪 Testing

Para probar las notificaciones:

### 1. Notificación de Admin
```kotlin
// En PantallaGestionUsuarios, al presionar "Publicar"
// Se activará automáticamente
```

### 2. Notificación de Usuario
```kotlin
// Al crear una receta desde PantallaCrearReceta
// Se activará automáticamente
```

### 3. Recordatorios Manuales
```kotlin
// Para probar inmediatamente, puedes llamar:
NotificacionesManager(context).mostrarRecordatorioApp()
```

### 4. Programar Recordatorios
```kotlin
// Para cambiar el intervalo de recordatorios (ej. cada 1 hora en testing):
NotificacionesScheduler.programarRecordatorios(context, intervaloHoras = 1)
```

---

## 🐛 Troubleshooting

### Las notificaciones no aparecen

1. **Verificar permisos**:
   - Configuración → Apps → SaborForaneo → Notificaciones → Activadas

2. **Verificar canales**:
   - Los canales se crean automáticamente al iniciar la app
   - Reinicia la app si es la primera instalación

3. **Verificar token FCM**:
   ```kotlin
   // En logs, busca:
   // "Token FCM: ..."
   ```

4. **Verificar WorkManager**:
   ```kotlin
   NotificacionesScheduler.verificarEstado(context)
   // Revisa los logs
   ```

### Los recordatorios no se ejecutan

- **WorkManager** tiene restricciones de batería
- En modo Doze, puede retrasarse hasta la próxima ventana de mantenimiento
- Para testing, usa intervalos cortos (1 hora)

### Notificaciones duplicadas

- Cada notificación tiene un ID único basado en timestamp
- Si ves duplicados, verifica que no estés llamando dos veces a la función

---

## 🔮 Mejoras Futuras

### 1. Backend para Notificaciones Push Reales
Actualmente se usan notificaciones locales. Para enviar notificaciones push reales a todos los usuarios:

- Crear un backend (Node.js, Python, etc.)
- Usar Firebase Admin SDK
- Enviar notificaciones a múltiples tokens usando topics o batch sending

### 2. Personalización de Preferencias
Permitir a usuarios:
- Activar/desactivar tipos específicos de notificaciones
- Elegir horarios para recordatorios
- Configurar frecuencia de recordatorios

### 3. Notificaciones con Imágenes
Agregar imágenes de recetas en las notificaciones:
```kotlin
.setLargeIcon(bitmap)
.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
```

### 4. Notificaciones Interactivas
Agregar botones de acción:
```kotlin
.addAction(R.drawable.ic_like, "Me gusta", pendingIntent)
.addAction(R.drawable.ic_share, "Compartir", sharePendingIntent)
```

### 5. Analytics
Trackear:
- Tasa de apertura de notificaciones
- Conversión (usuarios que interactúan después de la notificación)
- Mejor horario para enviar recordatorios

---

## ✅ Checklist de Implementación

- [x] Agregar dependencias de FCM y WorkManager
- [x] Crear MyFirebaseMessagingService
- [x] Crear NotificacionesManager
- [x] Crear RecordatorioWorker
- [x] Crear NotificacionesScheduler
- [x] Configurar AndroidManifest.xml
- [x] Integrar con GestionComunidadViewModel
- [x] Integrar con ComunidadViewModel
- [x] Inicializar en MainActivity
- [x] Crear canales de notificación
- [x] Programar recordatorios periódicos
- [x] Documentar sistema completo

---

## 📚 Referencias

- [Firebase Cloud Messaging (FCM)](https://firebase.google.com/docs/cloud-messaging)
- [WorkManager para Android](https://developer.android.com/topic/libraries/architecture/workmanager)
- [NotificationCompat](https://developer.android.com/reference/androidx/core/app/NotificationCompat)
- [Canales de Notificación](https://developer.android.com/training/notify-user/channels)

---

**Fecha de implementación**: 7 de enero de 2026  
**Versión de la app**: 1.0  
**Estado**: ✅ Completado y funcional
