#!/bin/bash
WGET="wget --quiet --show-progress"
LWJGL_VERSION=3.3.3
DIST_JARS=(
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl-opengl/lwjgl-opengl.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl-glfw/lwjgl-glfw.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl/lwjgl.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl-opengl/lwjgl-opengl-natives-linux.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl-glfw/lwjgl-glfw-natives-linux.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl/lwjgl-natives-linux.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl-opengl/lwjgl-opengl-natives-windows.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl-glfw/lwjgl-glfw-natives-windows.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl/lwjgl-natives-windows.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl-opengl/lwjgl-opengl-natives-macos.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl-glfw/lwjgl-glfw-natives-macos.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl/lwjgl-natives-macos.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl-opengl/lwjgl-opengl-natives-macos-arm64.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl-glfw/lwjgl-glfw-natives-macos-arm64.jar"
  "https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/lwjgl/lwjgl-natives-macos-arm64.jar"
)

if [ -z "$1" ]; then
  echo "Usage: $0 <version>"
  exit 1
fi

# Build the current version
mvn package

if [ ! -f target/noodel-"$1".jar ]; then
  echo "Error: target/noodel-$1.jar does not exist"
  exit 1
fi

# Copy in files to dist
cp target/noodel-"$1".jar dist/yodi.jar
cp LICENSE dist/LICENSE
echo 'java -cp "yodi.jar:lib/*" org.appledash.noodel.YodiGame' > dist/run.sh
echo 'java -cp "yodi.jar;lib/*" org.appledash.noodel.YodiGame' > dist/run.bat

# Get the LWJGL jars
for jar in "${DIST_JARS[@]}"; do
  $WGET -P dist/lib/ -nc "$jar"
done

# Get the LWJGL license
$WGET -O dist/lib/LICENSE.lwjgl -nc https://build.lwjgl.org/release/${LWJGL_VERSION}/bin/LICENSE

# Make the dist zip
rm -f yodi-"$1".zip
zip -9 -r yodi-"$1".zip dist
