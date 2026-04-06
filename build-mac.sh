#!/bin/bash
echo "Building JAR..."
mvn clean package

echo "Building DMG..."
rm -rf output/
jpackage \
  --input target/ \
  --name WwlcFX \
  --main-jar wwlc-fx-1.0.jar \
  --main-class com.wwlc.Main \
  --type dmg \
  --dest output/ \
  --module-path javafx-sdk-21.0.2/lib \
  --add-modules javafx.controls,javafx.fxml,javafx.media \
  --java-options "--enable-native-access=javafx.graphics"

echo "Installing DMG..."
hdiutil attach output/WwlcFX-1.0.dmg
cp -f /Volumes/WwlcFX/WwlcFX.app /Applications/ 2>/dev/null || true
sudo cp javafx-sdk-21.0.2/lib/*.dylib /Applications/WwlcFX.app/Contents/runtime/Contents/Home/lib/
hdiutil detach /Volumes/WwlcFX

echo "Done! Launching app..."
/Applications/WwlcFX.app/Contents/MacOS/WwlcFX
