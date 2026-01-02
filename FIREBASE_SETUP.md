# 🔥 Configuración de Firebase para Sabor Foráneo

Esta guía te ayudará a configurar Firebase desde cero para ejecutar la aplicación.

---

## 📋 Requisitos Previos
- Cuenta de Google
- Proyecto clonado de GitHub
- Android Studio instalado

---

## 🚀 Paso 1: Crear Proyecto en Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Clic en **"Agregar proyecto"**
3. Nombre del proyecto: `SaborForaneo` (o el que prefieras)
4. Desactiva Google Analytics (opcional para desarrollo)
5. Clic en **"Crear proyecto"**

---

## 📱 Paso 2: Registrar App Android

1. En el proyecto, clic en el ícono de **Android**
2. **Nombre del paquete**: `com.example.saborforaneo`
   - ⚠️ Debe coincidir exactamente con el paquete de la app
3. **Apodo de la app** (opcional): `Sabor Foráneo`
4. **SHA-1** (opcional para desarrollo): Puedes agregarlo después
5. Clic en **"Registrar app"**

---

## 📥 Paso 3: Descargar google-services.json

1. Descarga el archivo `google-services.json`
2. Colócalo en la carpeta **`app/`** del proyecto:
   ```
   SaborForaneo/
   └── app/
       └── google-services.json  ← Aquí
   ```
3. ⚠️ **IMPORTANTE**: Este archivo NO debe subirse a GitHub (ya está en `.gitignore`)

---

## 🔐 Paso 4: Configurar Authentication

1. En Firebase Console, ve a **Authentication**
2. Clic en **"Comenzar"**
3. Habilita **"Correo electrónico/contraseña"**
4. **NO** habilites el enlace de correo (solo la contraseña)
5. Guarda los cambios

### Crear Usuario Administrador

**Opción A: Desde Firebase Console**
1. Ve a **Authentication > Users**
2. Clic en **"Agregar usuario"**
3. Email: `saborforaneo@gmail.com`
4. Contraseña: (elige una segura)
5. Clic en **"Agregar usuario"**

**Opción B: Desde la App**
1. Ejecuta la app
2. Regístrate con `saborforaneo@gmail.com`
3. La app detectará el email y asignará rol `admin` automáticamente

---

## 📊 Paso 5: Configurar Firestore Database

1. En Firebase Console, ve a **Firestore Database**
2. Clic en **"Crear base de datos"**
3. Selecciona **"Iniciar en modo de prueba"**
4. Ubicación: `us-central` (o tu región)
5. Clic en **"Habilitar"**

### Aplicar Reglas de Seguridad

1. En Firestore, ve a la pestaña **"Reglas"**
2. Copia el contenido de `firestore.rules` (archivo en la raíz del proyecto)
3. Pega en el editor de reglas
4. Clic en **"Publicar"**

### Estructura de Colecciones (se crean automáticamente)

```
firestore/
└── usuarios/
    └── {uid}/
        ├── uid: "abc123"
        ├── nombre: "Usuario"
        ├── email: "user@example.com"
        ├── fotoPerfil: "https://..."
        ├── rol: "usuario" | "admin"
        ├── temaOscuro: true | false
        ├── notificacionesActivas: true
        ├── ubicacionActiva: false
        └── recetasFavoritas: []
```

---

## 🗂️ Paso 6: Configurar Storage

1. En Firebase Console, ve a **Storage**
2. Clic en **"Comenzar"**
3. Selecciona **"Iniciar en modo de prueba"**
4. Ubicación: Misma que Firestore
5. Clic en **"Listo"**

### Aplicar Reglas de Seguridad

1. En Storage, ve a la pestaña **"Reglas"**
2. Copia el contenido de `storage.rules` (archivo en la raíz del proyecto)
3. Pega en el editor de reglas
4. Clic en **"Publicar"**

### Estructura de Carpetas (se crean automáticamente)

```
storage/
└── usuarios/
    └── {uid}/
        └── perfil/
            └── perfil_1234567890.jpg
```

---

## ✅ Paso 7: Verificar Configuración

### En Firebase Console:
- ✅ Authentication habilitado con Email/Password
- ✅ Usuario admin `saborforaneo@gmail.com` creado
- ✅ Firestore Database creado con reglas aplicadas
- ✅ Storage habilitado con reglas aplicadas

### En Android Studio:
- ✅ `google-services.json` en `app/`
- ✅ Sincronización de Gradle exitosa
- ✅ Sin errores de compilación

---

## 🧪 Paso 8: Probar la App

### Compilar
```bash
./gradlew assembleDebug
```

### Ejecutar en emulador/dispositivo
1. Abre Android Studio
2. Selecciona un dispositivo (API 26+)
3. Clic en **Run ▶️**

### Flujo de prueba:
1. **Splash** → Detecta que no hay sesión
2. **Onboarding** → Primera vez
3. **Login** → Registra un nuevo usuario
4. **Home** → Ve recetas mock
5. **Perfil** → Cambia tema, sube foto
6. **Cerrar sesión** → Vuelve a Login
7. **Login Admin** → Usa `saborforaneo@gmail.com`
8. **Panel Admin** → Ve estadísticas

---

## 🚨 Problemas Comunes

### Error: "google-services.json not found"
- ✅ Verifica que el archivo esté en `app/`
- ✅ Sincroniza Gradle: `File > Sync Project with Gradle Files`

### Error: "FirebaseApp initialization unsuccessful"
- ✅ Verifica el nombre del paquete: `com.example.saborforaneo`
- ✅ Regenera `google-services.json` con el paquete correcto

### Error: "Permission denied" en Firestore/Storage
- ✅ Verifica que las reglas estén publicadas
- ✅ En desarrollo, usa "modo de prueba" (expira en 30 días)

### No aparece el usuario en Firestore
- ✅ Espera 2-3 segundos después del registro
- ✅ Revisa la consola de Firebase en tiempo real
- ✅ Verifica conexión a internet

---

## 🔒 Seguridad para Producción

### Cambiar Reglas de "Modo Prueba" a Producción

**Firestore** (ver `firestore.rules` completo):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /usuarios/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
  }
}
```

**Storage** (ver `storage.rules` completo):
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

---

## 📚 Recursos Adicionales

- [Documentación Firebase](https://firebase.google.com/docs)
- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Storage Security Rules](https://firebase.google.com/docs/storage/security)

---

## ✅ Checklist Final

Antes de iniciar desarrollo:
- [ ] Proyecto Firebase creado
- [ ] App Android registrada
- [ ] `google-services.json` descargado y colocado
- [ ] Authentication habilitado
- [ ] Usuario admin creado
- [ ] Firestore creado con reglas
- [ ] Storage creado con reglas
- [ ] App compila sin errores
- [ ] Login funciona correctamente
- [ ] Perfil carga datos de Firestore
- [ ] Subida de fotos funciona

---

**Última actualización**: Enero 2026
