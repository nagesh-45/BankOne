#!/usr/bin/env bash
# Rebuild BankOne WAR and redeploy to local Open Liberty (server: bankone).
set -euo pipefail

# Backend Maven root is BankOne-BackEnd (sibling of scripts/)
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../BankOne-BackEnd" && pwd)"
WLP_HOME="${WLP_HOME:-$HOME/tools/wlp}"
SERVER_NAME="${SERVER_NAME:-bankone}"
WAR_NAME="bankone-0.0.1-SNAPSHOT.war"
APPS_DIR="$WLP_HOME/usr/servers/$SERVER_NAME/apps"

echo "==> Using JAVA_HOME (JDK 21)..."
export JAVA_HOME
JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export PATH="$JAVA_HOME/bin:$PATH"
java -version

if [[ ! -x "$WLP_HOME/bin/server" ]]; then
  echo "ERROR: Liberty not found at $WLP_HOME"
  echo "Set WLP_HOME if your install is elsewhere."
  exit 1
fi

echo "==> Building WAR in $ROOT_DIR ..."
cd "$ROOT_DIR"
mvn -DskipTests clean package

WAR_PATH="$ROOT_DIR/target/$WAR_NAME"
if [[ ! -f "$WAR_PATH" ]]; then
  echo "ERROR: WAR not found at $WAR_PATH"
  exit 1
fi

echo "==> Stopping Liberty server '$SERVER_NAME' (if running)..."
"$WLP_HOME/bin/server" stop "$SERVER_NAME" || true

echo "==> Copying WAR to $APPS_DIR ..."
mkdir -p "$APPS_DIR"
# Remove exploded leftovers so Liberty picks up the new WAR cleanly
rm -rf "$APPS_DIR/expanded" "$APPS_DIR/$WAR_NAME"
cp "$WAR_PATH" "$APPS_DIR/$WAR_NAME"
ls -lh "$APPS_DIR/$WAR_NAME"

echo "==> Starting Liberty server '$SERVER_NAME'..."
"$WLP_HOME/bin/server" start "$SERVER_NAME"

echo "==> Waiting for HTTP 9080..."
for i in {1..40}; do
  if curl -sS -o /dev/null --connect-timeout 1 http://localhost:9080/ 2>/dev/null; then
    break
  fi
  sleep 1
done

echo "==> Smoke test: POST /auth/login"
HTTP_CODE="$(curl -sS -o /tmp/bankone-login.json -w "%{http_code}" \
  -X POST http://localhost:9080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}' || true)"

echo "HTTP $HTTP_CODE"
if [[ "$HTTP_CODE" == "200" ]]; then
  echo "Redeploy OK — API on http://localhost:9080"
  echo "UI: keep ng serve on http://localhost:4200 (API base should be 9080)"
else
  echo "WARNING: login smoke test did not return 200."
  echo "Check: $WLP_HOME/usr/servers/$SERVER_NAME/logs/messages.log"
  exit 1
fi
