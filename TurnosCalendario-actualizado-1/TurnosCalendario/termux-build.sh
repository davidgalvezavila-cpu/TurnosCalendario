#!/data/data/com.termux/files/usr/bin/bash
# Compila el APK de depuración directamente en Termux, sin PC ni Android Studio.
# Uso: colócate dentro de la carpeta del proyecto y ejecuta:  bash termux-build.sh
set -e

if [ -z "$PREFIX" ] || [[ "$PREFIX" != *com.termux* ]]; then
  echo "❌ Este script está pensado para ejecutarse dentro de Termux (Android)."
  exit 1
fi

RUTA_PROYECTO="$(cd "$(dirname "$0")" && pwd)"
cd "$RUTA_PROYECTO"

GRADLE_VERSION="8.7"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"

echo "== 1/6: Paquetes base (Java 17, aapt2, herramientas) =="
pkg update -y && pkg upgrade -y
pkg install -y openjdk-17 wget unzip zip git aapt aapt2

echo "== 2/6: Variables de entorno =="
JAVAC_BIN="$(which javac || true)"
if [ -z "$JAVAC_BIN" ]; then
  echo "❌ No se encontró javac tras instalar openjdk-17. Revisa el paso anterior."
  exit 1
fi
export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$JAVAC_BIN")")")"
export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$HOME/gradle-$GRADLE_VERSION/bin"

echo "== 3/6: Android SDK command-line tools =="
if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  cd "$ANDROID_HOME/cmdline-tools"
  echo "Descargando cmdline-tools... si esto falla con 404, el número de versión"
  echo "cambió: consigue el enlace actual en https://developer.android.com/studio"
  echo "(sección 'Command line tools only') y vuelve a intentarlo."
  wget -O tools.zip "$CMDLINE_TOOLS_URL"
  unzip -q tools.zip
  mv cmdline-tools latest
  rm tools.zip
  cd "$RUTA_PROYECTO"
fi

echo "== 4/6: Gradle $GRADLE_VERSION (compatible con el AGP 8.5.0 de este proyecto) =="
if [ ! -d "$HOME/gradle-$GRADLE_VERSION" ]; then
  cd "$HOME"
  wget -O gradle.zip "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  unzip -q gradle.zip
  rm gradle.zip
  cd "$RUTA_PROYECTO"
fi

echo "== 5/6: Licencias y paquetes del SDK (platform 34, build-tools 34.0.0) =="
yes | sdkmanager --licenses > /dev/null
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo "== 6/6: Compilar =="
echo "sdk.dir=$ANDROID_HOME" > local.properties

echo ""
echo "Primer intento (se espera que falle justo en el paso de aapt2 — es normal,"
echo "solo sirve para que Gradle descargue y guarde en caché el aapt2 x86-64)..."
gradle assembleDebug || true

echo ""
echo "Sustituyendo aapt2 x86-64 por la versión ARM64 de Termux..."
AAPT2_BIN="$(which aapt2 || true)"
if [ -z "$AAPT2_BIN" ]; then
  echo "❌ No se encontró aapt2 (¿falló 'pkg install aapt2' en el paso 1?)."
  exit 1
fi
find "$HOME/.gradle" -name 'aapt2-*-linux.jar' -type f | xargs -I{} jar -u -f {} -C "$(dirname "$AAPT2_BIN")" aapt2

echo ""
echo "Compilando de verdad..."
gradle assembleDebug --no-daemon

APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK" ]; then
  mkdir -p "$HOME/storage/downloads"
  cp "$APK" "$HOME/storage/downloads/TurnosCalendario-debug.apk"
  echo ""
  echo "✅ Listo. Busca TurnosCalendario-debug.apk en tu carpeta de Descargas."
else
  echo ""
  echo "❌ La compilación falló. Revisa el log de arriba para ver el error exacto."
fi
