#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DIST_DIR="${ROOT_DIR}/dist"
OUTPUT_FILE="${DIST_DIR}/count_it_up_single_file.html"

mkdir -p "${DIST_DIR}"

perl -0pe '
BEGIN {
  chomp(my $mime = qx(file --brief --mime-type "'"${ROOT_DIR}"'/Count_It_Up.png"));
  chomp(my $encoded = qx(base64 -w 0 "'"${ROOT_DIR}"'/Count_It_Up.png"));
  $image_data_url = "data:$mime;base64,$encoded";
}

s{src="Count_It_Up\.png"}{src="$image_data_url"}g;
' "${ROOT_DIR}/Count_It_Up.html" > "${OUTPUT_FILE}"

echo "Packaged single-file app at ${OUTPUT_FILE}"
