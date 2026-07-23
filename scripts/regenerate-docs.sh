#!/usr/bin/env bash
# Regenerate BankOne docs (DOCX + PDF) from markdown sources in docs/_source
# Usage: ./scripts/regenerate-docs.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$ROOT/docs/_source"
DEST="$ROOT/docs"
ASSETS="$DEST/_assets"
REF="$ASSETS/reference.docx"
CSS="$ASSETS/print.css"
CHROME="${CHROME:-/Applications/Google Chrome.app/Contents/MacOS/Google Chrome}"

if [[ ! -d "$SRC" ]]; then
  echo "Missing $SRC — keep markdown working copies under docs/_source for rebuilds."
  exit 1
fi
if [[ ! -x "$CHROME" ]]; then
  echo "Google Chrome required for PDF export at: $CHROME"
  exit 1
fi

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

polish_docx() {
  local venv="/tmp/bankone-docs-venv"
  if [[ ! -x "$venv/bin/python" ]]; then
    python3 -m venv "$venv"
    "$venv/bin/pip" install -q python-docx
  fi
  "$venv/bin/python" "$ROOT/scripts/_polish_docx.py" "$DEST"
}

convert_one() {
  local md="$1"
  local rel="${md#$SRC/}"
  local base="${rel%.md}"
  local title
  title=$(basename "$base")
  mkdir -p "$DEST/$(dirname "$rel")"

  pandoc "$md" -f markdown -t docx --reference-doc="$REF" \
    -o "$DEST/${base}.docx" --metadata title="$title"

  local body="$TMP/body.html"
  local full="$TMP/full.html"
  pandoc "$md" -f markdown -t html5 -o "$body"
  {
    echo '<!DOCTYPE html><html lang="en"><head><meta charset="utf-8"/>'
    echo "<title>${title}</title><style>"
    cat "$CSS"
    echo '</style></head><body><div class="doc-wrap">'
    echo '<div class="doc-header"><div class="brand">BankOne Documentation</div>'
    echo "<div class=\"meta\">${title}</div></div>"
    cat "$body"
    echo '</div></body></html>'
  } > "$full"

  "$CHROME" --headless --disable-gpu --no-pdf-header-footer \
    --print-to-pdf="$DEST/${base}.pdf" "file://${full}" 2>/dev/null
  echo "OK $base"
}

shopt -s nullglob
for md in "$SRC"/*.md; do convert_one "$md"; done
for md in "$SRC/MODULES"/*.md; do convert_one "$md"; done
polish_docx
echo "Regenerated docs in $DEST"
