#!/bin/bash
# Turns a 1024x1024 PNG into assets/AppIcon.icns using macOS's built-in sips and iconutil.
# Usage: ./make_icon.sh path/to/your-icon-1024.png
set -e
cd "$(dirname "$0")"

SRC="${1:-assets/icon-1024.png}"
SET="AppIcon.iconset"
rm -rf "$SET" && mkdir "$SET"

for size in 16 32 128 256 512; do
  sips -z $size $size "$SRC" --out "$SET/icon_${size}x${size}.png" >/dev/null
  double=$((size * 2))
  sips -z $double $double "$SRC" --out "$SET/icon_${size}x${size}@2x.png" >/dev/null
done

iconutil -c icns "$SET" -o assets/AppIcon.icns
rm -rf "$SET"
echo "Created assets/AppIcon.icns"
