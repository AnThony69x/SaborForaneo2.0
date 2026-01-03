# Configuración de Google Sign-In con Contraseña

## ✅ Cambios Implementados

He implementado el inicio de sesión con Google en tu aplicación **con solicitud de contraseña**. Los cambios incluyen:

### 1. **Dependencia Agregada** (`app/build.gradle.kts`)
```kotlin
implementation("com.google.android.gms:play-services-auth:21.0.0")
```

### 2. **AuthViewModel Actualizado**
- Se agregó el estado `AuthState.NecesitaContrasena` para manejar el flujo de contraseña
- Nueva función: `iniciarSesionConGoogle(idToken: String)` - Detecta si es usuario nuevo
- Nueva función: `completarRegistroConGoogle(email, nombre, password, idToken)` - Completa el registro con contraseña
- Vincula email/password como método adicional de autenticación

### 3. **Pantallas de Login y Registro Actualizadas**
- Botón "Continuar con Google" agregado en ambas pantallas
- Maneja el flujo completo de autenticación con Google
- Muestra diálogo para establecer contraseña en el primer inicio de sesión

### 4. **Nuevo Componente: DialogoEstablecerContrasena**
- Diálogo elegante que solicita contraseña al usuario
- Validación de contraseña (mínimo 6 caracteres)
- Confirmación de contraseña

### 5. **Archivo strings.xml Actualizado**
- Se agregó el `default_web_client_id` necesario para Google Sign-In

---

## 🔄 Flujo de Autenticación con Google

### Usuario Nuevo (Primera vez):
1. Usuario hace clic en "Continuar con Google"
2. Selecciona su cuenta de Google
3. **Se muestra diálogo para establecer contraseña** 🆕
4. Usuario ingresa y confirma su contraseña
5. Se crea el perfil en Firestore
6. Se vincula email/password como método adicional
7. Redirige a la pantalla correspondiente (Admin/Home)

### Usuario Existente:
1. Usuario hace clic en "Continuar con Google"
2. Selecciona su cuenta de Google
3. **Inicio de sesión directo** (sin solicitar contraseña)
4. Redirige a la pantalla correspondiente

---

## 🎯 Ventajas de este Enfoque

✅ **Doble método de autenticación**: El usuario puede iniciar sesión con:
   - Google Sign-In (rápido y conveniente)
   - Email/Contraseña (alternativa si no tiene acceso a Google)

✅ **Mayor seguridad**: Contraseña establecida desde el inicio

✅ **Mejor experiencia**: Solo se pide contraseña una vez (al registrarse)

✅ **Flexibilidad**: Si el usuario pierde acceso a su cuenta de Google, puede usar email/contraseña

---

## 🔧 Pasos para Completar la Configuración

### Paso 1: Sincronizar Gradle
1. Abre el proyecto en **Android Studio**
2. Haz clic en **File > Sync Project with Gradle Files**
3. Espera a que termine la sincronización

### Paso 2: Verificar la Configuración en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto **saborforaneo-aa2c5**
3. Ve a **Authentication > Sign-in method**
4. Verifica que **Google** esté habilitado (✅ ya lo hiciste)

### Paso 3: Obtener el SHA-1 de tu App

Para que Google Sign-In funcione, necesitas agregar el SHA-1 de tu app a Firebase:

#### Opción A: Obtener SHA-1 con Gradle (Recomendado)
1. Abre la terminal en Android Studio
2. Ejecuta:
   ```bash
   cd C:\Users\emili\SaborForaneo2.0-main
   gradlew signingReport
   ```
3. Busca la sección **debug** y copia el valor de **SHA1**

#### Opción B: Obtener SHA-1 con keytool
```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

### Paso 4: Agregar SHA-1 a Firebase

1. En Firebase Console, ve a **Project Settings** (⚙️)
2. Desplázate hasta la sección de tu app Android
3. Haz clic en **"Add fingerprint"**
4. Pega el SHA-1 que obtuviste
5. Haz clic en **Save**

### Paso 5: Descargar el Nuevo google-services.json

1. En Firebase Console, descarga el archivo `google-services.json` actualizado
2. Reemplaza el archivo existente en:
   ```
   C:\Users\emili\SaborForaneo2.0-main\app\google-services.json
   ```

### Paso 6: Rebuild del Proyecto

1. En Android Studio: **Build > Rebuild Project**
2. Espera a que termine

---

## 🧪 Cómo Probar

### Escenario 1: Usuario Nuevo
1. **Ejecuta la aplicación** en un dispositivo físico o emulador
2. En la pantalla de **Login** o **Registro**, haz clic en **"Continuar con Google"**
3. Selecciona una cuenta de Google que **nunca hayas usado** en la app
4. **Aparecerá un diálogo** solicitando establecer contraseña:
   - Ingresa una contraseña (mínimo 6 caracteres)
   - Confirma la contraseña
   - Haz clic en "Confirmar"
5. La app debería:
   - Crear automáticamente un perfil de usuario en Firestore
   - Vincular el email/password como método adicional
   - Redirigir a la pantalla de inicio o admin (según el email)

### Escenario 2: Usuario Existente
1. **Cierra sesión** si estás autenticado
2. Haz clic en **"Continuar con Google"**
3. Selecciona la misma cuenta de Google que usaste antes
4. La app debería:
   - **Iniciar sesión directamente** (sin pedir contraseña)
   - Redirigir a la pantalla correspondiente

### Escenario 3: Iniciar Sesión con Email/Contraseña
1. Después de registrarte con Google y establecer contraseña
2. Cierra sesión
3. En la pantalla de Login, ingresa:
   - **Email**: El de tu cuenta de Google
   - **Contraseña**: La que estableciste en el diálogo
4. Haz clic en "Iniciar Sesión"
5. Deberías poder acceder con email/contraseña

---

## 📱 Notas Importantes

### Roles de Usuario
- Si el email del usuario es `saborforaneo@gmail.com`, se le asignará el rol **admin**
- Cualquier otro email será rol **usuario**

### Primera Vez con Google Sign-In
- La primera vez que un usuario inicia sesión con Google, se crea automáticamente su perfil en Firestore
- El nombre se toma del perfil de Google
- El email se toma de la cuenta de Google

### Debug vs Release
- El SHA-1 es diferente para builds de **debug** y **release**
- Para producción, necesitarás agregar también el SHA-1 de tu keystore de release

---

## ⚠️ Solución de Problemas

### Error: "API key not valid"
- Verifica que el SHA-1 esté correctamente agregado en Firebase
- Descarga nuevamente el `google-services.json` después de agregar el SHA-1

### Error: "Developer error"
- El `default_web_client_id` no coincide con el de Firebase
- Verifica el valor en `app/src/main/res/values/strings.xml`

### La pantalla de Google Sign-In no aparece
- Verifica que Google Sign-In esté habilitado en Firebase Console
- Asegúrate de que las dependencias se hayan sincronizado correctamente

### Error: "10: Developer error"
- Falta agregar el SHA-1 a Firebase
- El `google-services.json` no está actualizado

---

## 🔐 Seguridad

El `default_web_client_id` en `strings.xml` es público y está bien dejarlo en el código. La seguridad viene de:
- Las reglas de Firestore que configuraste
- El SHA-1 registrado en Firebase
- Las restricciones de API en Google Cloud Console

---

## 📚 Recursos Adicionales

- [Documentación oficial de Firebase Auth con Google](https://firebase.google.com/docs/auth/android/google-signin)
- [Configurar Google Sign-In](https://developers.google.com/identity/sign-in/android/start-integrating)

---

## ✨ ¡Listo!

Una vez que completes estos pasos, tu aplicación tendrá inicio de sesión con Google completamente funcional. Los usuarios podrán:
- Iniciar sesión con email/contraseña (método existente)
- Iniciar sesión con Google (nuevo método)
- Ambos métodos crean/usan el mismo perfil de usuario en Firestore

