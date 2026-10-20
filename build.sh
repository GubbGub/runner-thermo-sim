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
    # Package in a temporary folder outside Desktop/iCloud. Files there pick up Finder
    # metadata (extended attributes) that make macOS code signing fail.
    STAGE="$(mktemp -d /tmp/runner-thermo.XXXXXX)"
    mkdir -p "$STAGE/input"
    jar --create --file "$STAGE/input/runner-thermo-sim.jar" --main-class runnersim.App -C out/classes .
    cp -X assets/AppIcon.icns "$STAGE/AppIcon.icns"
    xattr -cr "$STAGE"
    jpackage --type app-image \
      --name "Runner Thermo" \
      --input "$STAGE/input" \
      --main-jar runner-thermo-sim.jar \
      --main-class runnersim.App \
      --icon "$STAGE/AppIcon.icns" \
      --mac-package-identifier com.gubbgub.runnerthermo \
      --dest "$STAGE/dist"
    rm -rf "/Applications/Runner Thermo.app"
    ditto "$STAGE/dist/Runner Thermo.app" "/Applications/Runner Thermo.app"
    # Re-sign the finished app as one unit (ad-hoc, no Apple developer account needed).
    # Without this, macOS can call the app "damaged" and refuse to open it.
    chmod -R u+w "/Applications/Runner Thermo.app"   # bundled Java ships some files read-only
    xattr -cr "/Applications/Runner Thermo.app"
    codesign --force --deep --sign - "/Applications/Runner Thermo.app"
    codesign --verify --deep --strict "/Applications/Runner Thermo.app"
    rm -rf "$STAGE"
    echo "Installed to /Applications/Runner Thermo.app"
    ;;
  *)
    compile
    echo "Compiled to out/classes. Try: ./build.sh test, cli, gui, or app"
    ;;
esac
