#!/usr/bin/env sh
# Starts the local Docker backend and checks its readiness and health endpoints.
# Requires docker compose and curl.
set -eu

base_url="${BINGEBOX_SMOKE_BASE_URL:-http://localhost:8080}"
compose_was_running=false

cleanup() {
  if [ "$compose_was_running" = false ]; then
    docker compose down --remove-orphans
  fi
}
trap cleanup EXIT INT TERM

if [ -n "$(docker compose ps --all --quiet)" ]; then
  compose_was_running=true
fi
docker compose up --build -d
echo "Waiting for the backend to become ready (up to 6 minutes)..."

ready_attempt=0
until curl --fail --silent "$base_url/ready" >/dev/null; do
  ready_attempt=$((ready_attempt + 1))
  if [ "$ready_attempt" -ge 180 ]; then
    echo "Backend did not become ready within 6 minutes." >&2
    exit 1
  fi
  if [ $((ready_attempt % 15)) -eq 0 ]; then
    echo "Still waiting for the backend..."
  fi
  sleep 2
done

echo "Backend is ready. Checking health..."
curl --fail --silent --show-error --output /dev/null "$base_url/health"
echo "Backend smoke check passed: /ready and /health returned successful responses."