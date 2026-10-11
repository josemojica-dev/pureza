# Pureza – app Android + widget

## Opción A: compilar el APK sin instalar nada (GitHub Actions)
1. Crea un repositorio nuevo (privado) en github.com y sube TODO el contenido de esta carpeta.
2. Pestaña **Actions** → "Build APK" (corre solo al subir) → cuando termine, descarga el artefacto **Pureza-APK**.
3. Descomprime, pasa `app-debug.apk` a tu teléfono e instálalo (permite "instalar apps desconocidas").

## Opción B: Android Studio
1. `npm install` y `npx cap sync android`
2. `npx cap open android` → Run ▶ en tu teléfono (o Build > Build APK).

## Widget
Mantén presionado un espacio vacío de la pantalla de inicio → Widgets → **Pureza**.
Muestra tu racha y un botón "Marcar hoy limpio" que registra el día sin abrir la app.
Tocar el widget abre la app.

Los datos se guardan solo en el teléfono (nada sale a internet).
