# 🍽️ Sabor Foráneo
Aplicación móvil de recetas ecuatorianas e internacionales con **Firebase Backend**.

## 📱 Características
- 🔐 **Autenticación**: Login, Registro, Recuperación de contraseña
- 👑 **Panel Admin**: Gestión de usuarios (saborforaneo@gmail.com)
- 🌓 **Temas por Usuario**: Modo claro/oscuro guardado en Firestore
- 📸 **Fotos de Perfil**: Subida a Firebase Storage con Coil
- 🔔 **Notificaciones**: Permisos Android 13+
- 📍 **Ubicación**: Permisos GPS
- ⭐ **Favoritos**: Guardados en Firestore por usuario
- 🎨 **5 Paletas de Color**: Verde, Rojo, Azul, Naranja, Morado

## 🛠️ Tecnologías
| Categoría        | Tecnología                  |
|------------------|-----------------------------|
| Lenguaje         | Kotlin                      |
| UI               | Jetpack Compose + Material3 |
| Arquitectura     | MVVM + Clean Architecture   |
| Backend          | Firebase (Auth, Firestore, Storage) |
| Navegación       | Navigation Compose          |
| Imágenes         | Coil 2.6.0                  |
| Async            | Kotlin Coroutines + Flow    |
| Dependencias     | Gradle Version Catalogs     |

## 📂 Estructura del Proyecto
```
app/src/main/java/com/example/saborforaneo/
├── data/
│   ├── local/mock/              # Datos mock (recetas)
│   ├── remote/firebase/         # Servicios Firebase
│   │   ├── FirebaseAuthService.kt
│   │   ├── FirestoreService.kt
│   │   └── FirebaseStorageService.kt
│   └── repository/              # Repositorios
├── ui/
│   ├── components/              # Componentes reutilizables
│   ├── navigation/              # NavGraph
│   ├── screens/
│   │   ├── auth/                # Login, Registro
│   │   ├── admin/               # Panel Admin
│   │   ├── profile/             # Perfil + Configuración
│   │   ├── home/                # Pantalla principal
│   │   └── ...
│   └── theme/                   # Temas dinámicos
├── viewmodel/                   # AuthViewModel
└── MainActivity.kt
```

## 🚀 Instalación

### 1. Clonar el repositorio
```bash
git clone https://github.com/AnThony69x/SaborForaneo.git
cd SaborForaneo
```

### 2. Configurar Firebase
1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com)
2. Descarga `google-services.json` y colócalo en `app/`
3. Configura **Authentication** (Email/Password habilitado)
4. Crea base de datos **Firestore** y **Storage**
5. Aplica las reglas de `firestore.rules` y `storage.rules`

### 3. Compilar y ejecutar
```bash
./gradlew assembleDebug
```
- **Requisitos**: Android Studio Hedgehog+ | Gradle 8.7 | Android 8.0+ (API 26)

## 🔐 Firebase Configuration

### Archivos importantes (NO incluidos en el repositorio)
- `app/google-services.json` - Configuración de Firebase **(debes crear el tuyo)**
- `local.properties` - Rutas del SDK de Android

### Reglas de Seguridad
#### Firestore (`firestore.rules`)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Función para verificar si es admin
    function esAdmin() {
      return request.auth != null && 
             exists(/databases/$(database)/documents/usuarios/$(request.auth.uid)) &&
             get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.rol == 'admin';
    }
    
    // Usuarios
    match /usuarios/{userId} {
      allow read: if request.auth != null && 
                     (request.auth.uid == userId || esAdmin());
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Recetas
    match /recetas/{recetaId} {
      allow read: if true; // Lectura pública
      allow create: if request.auth != null && esAdmin();
      allow update: if request.auth != null && esAdmin();
      allow delete: if request.auth != null && esAdmin();
    }
  }
}
```

#### Índices Requeridos en Firestore
Debes crear estos índices manualmente en Firebase Console → Firestore → Índices:

**Índice 1: Consulta de recetas activas ordenadas por fecha**
- Colección: `recetas`
- Campos:
  - `activa` - Ascendente
  - `fechaCreacion` - Descendente

**Índice 2: Consulta de recetas por categoría**
- Colección: `recetas`
- Campos:
  - `categoria` - Ascendente
  - `activa` - Ascendente
  - `fechaCreacion` - Descendente

> **Nota**: Firebase te sugerirá crear estos índices automáticamente cuando ejecutes las consultas. Simplemente haz clic en el enlace que aparece en el error.

#### Storage (`storage.rules`)
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /usuarios/{userId}/perfil/{fileName} {
      allow read: if true;
      allow write: if request.auth.uid == userId
        && request.resource.size < 5 * 1024 * 1024;
    }
  }
}
```

## 📋 Pantallas
| Pantalla          | Descripción                          |
|-------------------|--------------------------------------|
| Splash            | Carga inicial + verificación sesión  |
| Onboarding        | Introducción (primera vez)           |
| Login             | Autenticación con Firebase           |
| Registro          | Crear cuenta + rol automático        |
| Recuperar         | Envío de email para resetear         |
| Home              | Recetas desde Firestore ordenadas por fecha |
| Búsqueda          | Filtros avanzados                    |
| Detalle           | Vista completa de receta             |
| Favoritos         | Guardados en Firestore               |
| Perfil            | Foto, nombre, tema, configuración    |
| Admin Panel       | Solo para saborforaneo@gmail.com     |
| Gestión Recetas   | CRUD de recetas (solo admin)         |

## 🔑 Credenciales Admin
- **Email**: `saborforaneo@gmail.com`
- **Rol**: `admin` (asignado automáticamente en Firestore)

## 📌 Permisos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" /> <!-- Android 13+ -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## 📦 Archivos que NO se suben a GitHub (.gitignore)
```
google-services.json         # Contiene claves de API de Firebase
local.properties             # Rutas locales del SDK
*.apk / *.aab                # Binarios compilados
/build/ y /app/build/        # Archivos generados
.gradle/ y .idea/            # Configuración IDE
*.log                        # Logs
```

## 🚨 Antes de Subir a GitHub
1. ✅ Verifica que `google-services.json` NO esté en el repo
2. ✅ Revisa que `.gitignore` esté configurado
3. ✅ Cambia las reglas de Firebase de "modo prueba" a producción
4. ✅ Documenta las variables de entorno necesarias

## 🌟 Características Implementadas
- [x] Firebase Authentication con manejo de errores
- [x] Persistencia de sesión
- [x] Roles de usuario (admin/usuario)
- [x] Perfil conectado a Firestore
- [x] Subida de fotos de perfil
- [x] Tema oscuro por usuario
- [x] Limpieza de estado al cerrar sesión
- [x] Animaciones de navegación
- [x] Notificaciones locales
- [x] **Todas las recetas almacenadas en Firestore**
- [x] **CRUD completo de recetas para admin**
- [x] **Sistema de búsqueda y filtros (16 categorías)**
- [x] **Sistema de favoritos sincronizado**
- [x] **Categorías 100% sincronizadas (usuario = admin)**

## 👨‍💻 Autor
**AnThony69x**

## 📄 Licencia
MIT License
