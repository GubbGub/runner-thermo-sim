#!/bin/bash
# Runner Thermo build script.
#   ./build.sh          compile
#   ./build.sh test     compile and run the physics tests
#   ./build.sh cli      run the terminal version
#   ./build.sh gui      run the window version
#   ./build.sh app      package "Runner Thermo.app" and install it in /Applications
set -e
cd "$(dirname "$0")"

compile() {
  rm -rf out && mkdir -p out/classes
  javac --release 17 -d out/classes $(find src -name "*.java")
}

case "${1:-}" in
  test)
    compile
    javac --release 17 -cp out/classes -d out/test $(find test -name "*.java")
    java -cp out/classes:out/test runnersim.PhysicsTests
    ;;
  cli)
    compile
    java -cp out/classes runnersim.App --cli
    ;;
  gui)
    compile
    java -cp out/classes runnersim.App
    ;;
  app)
    compile
    mkdir -p out/jar
    jar --create --file out/jar/runner-thermo-sim.jar --main-class runnersim.App -C out/classes .
    rm -rf dist
    jpackage --type app-image \
      --name "Runner Thermo" \
      --input out/jar \
      --main-jar runner-thermo-sim.jar \
      --main-class runnersim.App \
      --icon assets/AppIcon.icns \
      --mac-package-identifier com.gubbgub.runnerthermo \
      --dest dist
    rm -rf "/Applications/Runner Thermo.app"
    cp -R "dist/Runner Thermo.app" /Applications/
    echo "Installed to /Applications/Runner Thermo.app"
    ;;
  *)
    compile
    echo "Compiled to out/classes. Try: ./build.sh test, cli, gui, or app"
    ;;
esac
