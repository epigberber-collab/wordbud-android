#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${WORDBUD_ANDROID_JAR:?Set WORDBUD_ANDROID_JAR to an Android 35 android.jar}"
: "${WORDBUD_BUILD_TOOLS:?Set WORDBUD_BUILD_TOOLS to Android build-tools 35.0.0 directory}"
mkdir -p out/classes out/dex
find app/src/main/java -name '*.java' > out/java-sources.txt
if command -v javac >/dev/null; then
  javac -encoding UTF-8 -source 8 -target 8 -bootclasspath "$WORDBUD_ANDROID_JAR:$WORDBUD_BUILD_TOOLS/core-lambda-stubs.jar" -d out/classes @out/java-sources.txt
else
  : "${WORDBUD_ECJ_JAR:?Set WORDBUD_ECJ_JAR to an Eclipse ECJ compiler jar if javac is absent}"
  java -jar "$WORDBUD_ECJ_JAR" -encoding UTF-8 -8 -bootclasspath "$WORDBUD_ANDROID_JAR:$WORDBUD_BUILD_TOOLS/core-lambda-stubs.jar" -d out/classes @out/java-sources.txt
fi
(cd out/classes && zip -qr ../classes.jar .)
"$WORDBUD_BUILD_TOOLS/d8" --lib "$WORDBUD_ANDROID_JAR" --min-api 28 --output out/dex out/classes.jar
"$WORDBUD_BUILD_TOOLS/aapt2" compile --dir app/src/main/res -o out/resources.zip
sed 's/<manifest /<manifest package="app.wordbud.ime" /' app/src/main/AndroidManifest.xml > out/AndroidManifest.xml
"$WORDBUD_BUILD_TOOLS/aapt2" link -o out/unsigned.apk --manifest out/AndroidManifest.xml -I "$WORDBUD_ANDROID_JAR" -A app/src/main/assets --version-code 1 --version-name 0.1.0 out/resources.zip
(cd out/dex && zip -q ../unsigned.apk classes.dex)
"$WORDBUD_BUILD_TOOLS/zipalign" -f 4 out/unsigned.apk out/aligned.apk
if [ ! -f out/development.jks ]; then
  keytool -genkeypair -keystore out/development.jks -storepass android -keypass android -alias wordbud-dev -keyalg RSA -keysize 2048 -validity 3650 -dname 'CN=WordBud Development'
fi
"$WORDBUD_BUILD_TOOLS/apksigner" sign --ks out/development.jks --ks-pass pass:android --key-pass pass:android --out out/wordbud-0.1.0.apk out/aligned.apk
"$WORDBUD_BUILD_TOOLS/apksigner" verify --verbose out/wordbud-0.1.0.apk
