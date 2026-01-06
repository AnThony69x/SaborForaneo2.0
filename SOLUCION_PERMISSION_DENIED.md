# 🔥 Solución: PERMISSION_DENIED al Editar Recetas

## ❌ Problema
Al intentar editar una receta en el panel de admin, aparece el error:
```
PERMISSION_DENIED: Missing or insufficient permissions
```

## 🔍 Causa
Las reglas de Firestore tienen un error de sintaxis (falta `||`) o no están desplegadas correctamente en Firebase Console.

## ✅ Solución en 3 Pasos

### Paso 1: Verificar que el Usuario es Admin

1. Ve a **Firebase Console** → **Firestore Database**
2. Busca la colección `usuarios`
3. Encuentra el documento de tu usuario (el UID)
4. Verifica que tenga: `rol: "admin"`

**Si no dice "admin"**:
- Edita el documento
- Cambia o agrega el campo: `rol` → `admin`
- Guarda

### Paso 2: Desplegar las Reglas Correctas

#### Opción A: Desde Firebase Console (Más Fácil)

1. Ve a **Firebase Console** → **Firestore Database** → **Reglas**

2. **REEMPLAZA TODO** el contenido con esto:

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
    
    // Recetas - Todas gestionadas por admin en Firestore
    match /recetas/{recetaId} {
      allow read: if true; // Lectura pública para todos los usuarios
      allow create: if request.auth != null && esAdmin();
      allow update: if request.auth != null && esAdmin();
      allow delete: if request.auth != null && esAdmin();
    }
    
    // Favoritos
    match /favoritos/{userId} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

3. Haz clic en **"Publicar"**

4. Espera 10-30 segundos para que se propaguen los cambios

#### Opción B: Desde Firebase CLI (Avanzado)

```bash
# Instalar Firebase CLI (si no lo tienes)
npm install -g firebase-tools

# Login
firebase login

# Inicializar (si no está inicializado)
firebase init firestore

# Desplegar reglas
firebase deploy --only firestore:rules
```

### Paso 3: Verificar en la App

1. **Cierra completamente la app** (Force Stop)
2. **Limpia la caché**: Settings → Apps → Sabor Foráneo → Clear Cache
3. **Vuelve a abrir la app**
4. Inicia sesión como admin
5. Intenta editar una receta

**Debería funcionar ahora** ✅

---

## 🔍 Verificación Detallada

### Verificar que las Reglas Están Correctas

1. Ve a Firebase Console → Firestore → Reglas
2. Busca esta línea:
   ```javascript
   (request.auth.uid == userId || esAdmin())
   ```
   ⚠️ **Debe tener `||`** (no solo espacios)

3. Busca la función `esAdmin()`:
   ```javascript
   function esAdmin() {
     return request.auth != null && 
            exists(...) &&
            get(...).data.rol == 'admin';
   }
   ```

### Verificar que el Usuario es Admin

1. Firebase Console → Firestore → Colección `usuarios`
2. Encuentra tu documento (UID del usuario admin)
3. Debe tener:
   ```json
   {
     "uid": "tu-uid-aqui",
     "nombre": "Tu Nombre",
     "email": "admin@example.com",
     "rol": "admin",  ← ESTO ES CRÍTICO
     // ...otros campos
   }
   ```

---

## 🧪 Prueba de Diagnóstico

### Test en la Consola de Firebase (Simulador de Reglas)

1. Ve a Firebase Console → Firestore → Reglas
2. Haz clic en **"Simulador de reglas"** (si está disponible)
3. Configura:
   - **Tipo**: `update`
   - **Ubicación**: `recetas/cualquier-id`
   - **Autenticado**: `Sí`
   - **UID del proveedor**: `tu-uid-de-admin`

4. Resultado esperado: ✅ **Permitido**

---

## ⚠️ Problemas Comunes y Soluciones

### Error: "Property rol is undefined"

**Causa**: El documento del usuario no tiene el campo `rol`.

**Solución**:
1. Ve a Firestore → `usuarios` → tu documento
2. Agrega el campo: `rol: "admin"`
3. Guarda

### Error: "Document does not exist"

**Causa**: El documento del usuario no existe en Firestore.

**Solución**:
1. Crea manualmente el documento en `usuarios/{tu-uid}`
2. Agrega estos campos:
   ```json
   {
     "uid": "tu-uid-aqui",
     "nombre": "Admin",
     "email": "admin@example.com",
     "rol": "admin",
     "fechaCreacion": 1704326400000,
     "recetasFavoritas": [],
     "notificacionesActivas": true,
     "temaOscuro": false
   }
   ```

### Error persiste después de desplegar

**Solución**:
1. Espera 1-2 minutos (propagación de reglas)
2. Cierra la app completamente
3. Limpia caché: `Settings → Apps → Clear Cache`
4. Desinstala e instala de nuevo (último recurso)

---

## 🔐 Reglas Explicadas

```javascript
// ✅ CORRECTA - Con ||
allow read: if request.auth != null && 
               (request.auth.uid == userId || esAdmin());

// ❌ INCORRECTA - Sin ||
allow read: if request.auth != null && 
               (request.auth.uid == userId  esAdmin());
```

### Función esAdmin()

Esta función verifica 3 cosas:
1. **Usuario autenticado**: `request.auth != null`
2. **Documento existe**: `exists(/databases/.../usuarios/$(request.auth.uid))`
3. **Rol es admin**: `get(...).data.rol == 'admin'`

### Reglas de Recetas

```javascript
match /recetas/{recetaId} {
  allow read: if true;  // Todos pueden leer
  allow create: if request.auth != null && esAdmin();  // Solo admin crea
  allow update: if request.auth != null && esAdmin();  // Solo admin edita
  allow delete: if request.auth != null && esAdmin();  // Solo admin elimina
}
```

---

## 📝 Checklist de Verificación

Antes de rendirte, verifica:

- [ ] Las reglas están desplegadas en Firebase Console
- [ ] El usuario tiene campo `rol: "admin"` en Firestore
- [ ] El documento del usuario existe en colección `usuarios`
- [ ] El UID en Firestore coincide con el UID de Authentication
- [ ] Has esperado 30 segundos después de desplegar reglas
- [ ] Has cerrado y reabierto la app
- [ ] Has limpiado la caché de la app
- [ ] Estás autenticado con el usuario correcto

---

## 🚀 Solución Rápida (Desarrollo)

Si solo estás desarrollando y quieres probar rápido (⚠️ **NO PARA PRODUCCIÓN**):

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

Esto permite todo a usuarios autenticados. **Solo para testing local**.

---

## 📞 Contacto de Soporte

Si el error persiste después de seguir todos los pasos:

1. Verifica los logs en Firebase Console → Firestore → Uso
2. Busca errores específicos relacionados con tu UID
3. Copia el mensaje de error completo
4. Revisa que el email de admin sea: `saborforaneo@gmail.com`

---

## ✅ Confirmación Final

Después de aplicar la solución, deberías poder:

- ✅ Crear nuevas recetas
- ✅ Editar recetas existentes
- ✅ Eliminar recetas
- ✅ Ver todas las recetas en el panel admin

**Si todo funciona**: ¡Problema resuelto! 🎉

---

## 🔗 Archivos Relacionados

- `firestore.rules` - Archivo local (debe coincidir con Firebase Console)
- Firebase Console → Firestore → Reglas (fuente de verdad)

---

**Nota Final**: Las reglas en Firebase Console son las que cuentan. El archivo local `firestore.rules` es solo para referencia y despliegue con CLI.

