#!/usr/bin/env bash
# Inlines the logo into Count_It_Up.html, producing web/count_it_up_vX_Y_Z.html (one self-contained file).
set -euo pipefail

WEB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="$(tr -d '[:space:]' < "${WEB_DIR}/../VERSION")"
OUTPUT_FILE="${WEB_DIR}/count_it_up_v${VERSION//./_}.html"

rm -f "${WEB_DIR}"/count_it_up_v*.html

PNG="${WEB_DIR}/Count_It_Up.png" perl -0pe '
BEGIN {
  chomp(my $encoded = qx(base64 -w 0 "$ENV{PNG}"));
  $image_data_url = "data:image/png;base64,$encoded";
}
s{src="Count_It_Up\.png"}{src="$image_data_url"}g;
' "${WEB_DIR}/Count_It_Up.html" > "${OUTPUT_FILE}"

echo "Packaged single-file app at ${OUTPUT_FILE}"
