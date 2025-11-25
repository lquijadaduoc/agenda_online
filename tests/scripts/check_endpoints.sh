#!/usr/bin/env bash
set -euo pipefail

APP_URL="${APP_URL:-http://app:8080}"

echo "Using APP_URL=$APP_URL"

wait_for_health(){
  echo "Waiting for app health..."
  for i in $(seq 1 60); do
    if curl -s "$APP_URL/health" | grep -q "OK"; then
      echo "App healthy"
      return 0
    fi
    echo "  still waiting... ($i)"
    sleep 2
  done
  echo "App did not become healthy in time" >&2
  return 1
}

json_post(){
  local url="$1"; shift
  local data="$1"; shift
  curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST -H "Content-Type: application/json" -d "$data" "$url"
}

wait_for_health

echo "1) GET /health"
curl -s -i "$APP_URL/health" || true

TS=$(date +%s)
EMAIL="test+${TS}@example.com"
PASSWORD="Password123"
SLUG="test-slug-${TS}"

echo "2) POST /auth/register -> $EMAIL"
REG_PAYLOAD=$(jq -n --arg name "Test User" --arg email "$EMAIL" --arg password "$PASSWORD" --arg publicSlug "$SLUG" --arg businessName "Test Business" '{name:$name,email:$email,password:$password,publicSlug:$publicSlug,businessName:$businessName}')
json_post "$APP_URL/auth/register" "$REG_PAYLOAD" | jq -r '.' || true

echo "3) POST /auth/login"
LOGIN_PAYLOAD=$(jq -n --arg email "$EMAIL" --arg password "$PASSWORD" '{email:$email,password:$password}')
LOGIN_RESP=$(curl -s -X POST -H "Content-Type: application/json" -d "$LOGIN_PAYLOAD" "$APP_URL/auth/login")
echo "$LOGIN_RESP" | jq -r '.'
TOKEN=$(echo "$LOGIN_RESP" | jq -r .accessToken)
if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "Failed to login and get accessToken" >&2
  exit 1
fi
AUTH_HEADER="Authorization: Bearer $TOKEN"

echo "4) GET /services (should be empty or list)"
curl -s -H "$AUTH_HEADER" "$APP_URL/services" | jq -r '.' || true

echo "5) Create a service POST /services"
SERVICE_PAYLOAD=$(jq -n --arg name "Test Service" --arg description "desc" --argjson duration 30 --argjson price 10 --argjson active true '{name:$name,description:$description,durationMinutes:$duration,price:$price,active:$active}')
SERVICE_RESP=$(curl -s -X POST -H "Content-Type: application/json" -H "$AUTH_HEADER" -d "$SERVICE_PAYLOAD" "$APP_URL/services")
echo "$SERVICE_RESP" | jq -r '.'
SERVICE_ID=$(echo "$SERVICE_RESP" | jq -r .id)
echo "  created service id=$SERVICE_ID"

echo "6) Create a client POST /clients"
CLIENT_PAYLOAD=$(jq -n --arg name "Client Name" --arg email "client+${TS}@example.com" --arg phone "123456" --arg notes "test client" '{name:$name,email:$email,phone:$phone,notes:$notes}')
CLIENT_RESP=$(curl -s -X POST -H "Content-Type: application/json" -H "$AUTH_HEADER" -d "$CLIENT_PAYLOAD" "$APP_URL/clients")
echo "$CLIENT_RESP" | jq -r '.'
CLIENT_ID=$(echo "$CLIENT_RESP" | jq -r .id)
echo "  created client id=$CLIENT_ID"

echo "7) Create availability block POST /availability/blocks"
# The DB enforces: if is_recurring = true then weekday must be provided.
BLOCK_PAYLOAD=$(jq -n --arg startTime "09:00:00" --arg endTime "18:00:00" --argjson recurring true --argjson weekday 1 '{startTime:$startTime,endTime:$endTime,recurring:$recurring,weekday:$weekday}')
BLOCK_RESP=$(curl -s -X POST -H "Content-Type: application/json" -H "$AUTH_HEADER" -d "$BLOCK_PAYLOAD" "$APP_URL/availability/blocks")
echo "$BLOCK_RESP" | jq -r '.' || true
BLOCK_ID=$(echo "$BLOCK_RESP" | jq -r .id)
echo "  created block id=$BLOCK_ID"

echo "8) Create appointment POST /appointments"
# Use python3 to compute UTC datetimes (+1 day) to avoid busybox date incompatibilities in Alpine
START_DT=$(python3 - <<'PY'
from datetime import datetime, timedelta
print((datetime.utcnow()+timedelta(days=1)).strftime('%Y-%m-%dT10:00:00Z'))
PY
)
END_DT=$(python3 - <<'PY'
from datetime import datetime, timedelta
print((datetime.utcnow()+timedelta(days=1)).strftime('%Y-%m-%dT10:30:00Z'))
PY
)
APPT_PAYLOAD=$(jq -n --argjson serviceId "$SERVICE_ID" --argjson clientId "$CLIENT_ID" --arg startDateTime "$START_DT" --arg endDateTime "$END_DT" --arg notes "test appointment" '{serviceId:$serviceId,clientId:$clientId,startDateTime:$startDateTime,endDateTime:$endDateTime,notes:$notes}')
APPT_RESP=$(curl -s -X POST -H "Content-Type: application/json" -H "$AUTH_HEADER" -d "$APPT_PAYLOAD" "$APP_URL/appointments")
echo "$APPT_RESP" | jq -r '.' || true
APPT_ID=$(echo "$APPT_RESP" | jq -r .id)
echo "  created appointment id=$APPT_ID"

echo "9) List appointments GET /appointments"
curl -s -H "$AUTH_HEADER" "$APP_URL/appointments" | jq -r '.' || true

echo "10) Public booking POST /public/professionals/$SLUG/appointments (no auth)"
PUB_APPT_PAYLOAD=$(jq -n --argjson serviceId "$SERVICE_ID" --arg startDateTime "$START_DT" --arg endDateTime "$END_DT" --arg name "Public Client" --arg email "pubclient+${TS}@example.com" '{serviceId:$serviceId,startDateTime:$startDateTime,endDateTime:$endDateTime,name:$name,email:$email}')
curl -s -w "\nHTTP_STATUS:%{http_code}\n" -X POST -H "Content-Type: application/json" -d "$PUB_APPT_PAYLOAD" "$APP_URL/public/professionals/$SLUG/appointments" | jq -r '.' || true

echo "All checks finished"
