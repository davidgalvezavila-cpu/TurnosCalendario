# Calendario de Turnos Laborales (Android)

App nativa en **Kotlin + Jetpack Compose** para gestionar turnos de trabajo en un calendario mensual, con persistencia local mediante **Room**.

## Funcionalidades
- Vista de calendario mensual con navegación entre meses (◀ ▶) y botón "hoy" (toca el nombre del mes).
- Al tocar un día se abre un diálogo para asignar un turno: **Mañana, Tarde, Noche, Libre, Vacaciones** (colores y horarios predefinidos, editables en el código).
- Cada día pintado con el color de su turno y las iniciales del tipo.
- Notas opcionales por turno.
- Eliminar turno de un día.
- Los datos persisten en una base de datos local (Room/SQLite), por lo que sobreviven a reinicios de la app.
- Leyenda de colores en la parte inferior de la pantalla.

## Estructura del proyecto
```
TurnosCalendario/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/turnoscalendario/
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   ├── Turno.kt              # Entidad Room + tipos de turno predefinidos
│       │   │   ├── TurnoDao.kt           # Consultas SQL
│       │   │   ├── TurnoDatabase.kt      # Base de datos Room
│       │   │   └── TurnoRepository.kt    # Capa de acceso a datos
│       │   └── ui/
│       │       ├── CalendarViewModel.kt  # Estado y lógica del calendario
│       │       ├── CalendarScreen.kt     # UI en Jetpack Compose
│       │       └── theme/Theme.kt
│       └── res/values/ (strings, themes)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Cómo abrir y ejecutar
1. Abre **Android Studio** (versión Koala o superior recomendada).
2. `File > Open` y selecciona la carpeta `TurnosCalendario`.
3. Deja que Gradle sincronice (descargará las dependencias de Compose, Room, etc.).
4. Conecta un dispositivo o inicia un emulador con **API 24+**.
5. Pulsa "Run" ▶.

> Nota: este proyecto no incluye el Gradle Wrapper (`gradlew`/`gradle-wrapper.jar`) binario. Android Studio lo generará automáticamente al abrir el proyecto, o puedes ejecutar `gradle wrapper` si tienes Gradle instalado localmente.

## Compilar desde tu propio smartphone Android (sin PC)

Este proyecto usa Jetpack Compose + Room (con KSP), lo que hace la compilación más pesada que un proyecto Android básico. Hay dos caminos:

### ✅ Opción recomendada: que la nube compile por ti (GitHub Actions)
Solo necesitas el navegador del móvil y, opcionalmente, Termux para subir el código.

1. Instala **Termux** (desde F-Droid, no la versión de Play Store, que está desactualizada) y ejecuta `pkg install git`. Solo hace falta git, nada de JDK ni Android SDK.
2. Crea una cuenta y un repositorio **nuevo y vacío** en [github.com](https://github.com) desde el navegador del móvil.
3. Da acceso a Termux al almacenamiento (`termux-setup-storage`) y sube el proyecto:
   ```bash
   cd /sdcard/Download/TurnosCalendario   # donde hayas descomprimido el ZIP
   git init && git branch -M main
   git remote add origin https://github.com/TU_USUARIO/TU_REPO.git
   git add .
   git commit -m "Proyecto inicial"
   git push -u origin main
   ```
   (Te pedirá autenticarte: usa un [token de acceso personal](https://github.com/settings/tokens) como contraseña, no la contraseña normal de tu cuenta).
4. Este proyecto ya incluye `.github/workflows/build-debug-apk.yml`, así que en cuanto el push termine, GitHub compilará el APK automáticamente.
5. En el navegador: tu repo → pestaña **Actions** → abre la ejecución (círculo verde ✓) → descarga el artefacto `TurnosCalendario-debug` → dentro está el `.apk`.
6. Abre el `.apk` descargado desde el gestor de archivos del móvil (puede pedirte permitir "instalar apps de origen desconocido" para tu navegador).

### ⚙️ Opción avanzada: compilar 100% en el teléfono con Termux (sin nube)
El proyecto incluye `termux-build.sh`, que automatiza todo el proceso. Requisitos: Termux desde **F-Droid** (la versión de Play Store no trae `openjdk-17`), varios GB libres y conexión a internet para las descargas.

```bash
termux-setup-storage
cd /sdcard/Download/TurnosCalendario   # o donde hayas descomprimido el proyecto
bash termux-build.sh
```

El script instala Java 17 + `aapt`/`aapt2` + Android SDK command-line tools + Gradle 8.7 (la versión que necesita el AGP 8.5.0 de este proyecto), y compila. Dos cosas a saber:

- **El primer intento de compilación va a fallar "a propósito"**: Gradle descarga un `aapt2` compilado para x86-64 que no corre en un móvil ARM64. El script lo detecta, sustituye ese binario por la versión ARM64 de Termux, y vuelve a compilar — es el paso conocido como "parche de aapt2", documentado por la comunidad de Termux.
- El enlace de los *command-line tools* de Android incluye un número de versión que Google cambia de vez en cuando. Si el script falla al descargarlo (error 404), abre https://developer.android.com/studio en el navegador → sección "Command line tools only" → copia el enlace actual → sustitúyelo en la variable `CMDLINE_TOOLS_URL` al principio de `termux-build.sh`.

Si todo va bien, el APK queda en tu carpeta de Descargas como `TurnosCalendario-debug.apk`, listo para abrir e instalar.

## Personalización rápida
- **Tipos de turno y colores**: edita la lista `TIPOS_TURNO_PREDEFINIDOS` en `data/Turno.kt`.
- **Un turno por día vs. varios turnos por día**: actualmente el repositorio reemplaza el turno existente de un día al guardar uno nuevo (`TurnoRepository.guardarTurno`). Si necesitas permitir múltiples turnos por día (ej. turnos partidos), se puede adaptar el DAO para no filtrar por fecha única.
- **Idioma**: los textos están en español; para internacionalizar, mover las cadenas a `res/values/strings.xml` y crear `res/values-en/strings.xml`, etc.
- **Exportar/backup**: se podría añadir exportación a `.ics` o copia de seguridad en la nube como siguiente paso.

## Posibles mejoras futuras
- Notificaciones/recordatorios antes de empezar un turno.
- Vista semanal o de lista además de la mensual.
- Múltiples turnos por día (turnos partidos).
- Sincronización con Google Calendar.
- Estadísticas (horas trabajadas por mes, por tipo de turno).

