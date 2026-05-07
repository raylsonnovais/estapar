#!/bin/bash
BASE_URL="https://estapar-backend.onrender.com"
DATE=$(date +%Y-%m-%d)
ENTRY_TIME=$(date -u +"%Y-%m-%dT%H:%M:%S.000Z")
EXIT_TIME=$(date -u -d "+2 hours" +"%Y-%m-%dT%H:%M:%S.000Z" 2>/dev/null || date -u -v+2H +"%Y-%m-%dT%H:%M:%S.000Z")
PLATE="TST0001"

echo "=== 1. Health Check ==="
curl -s $BASE_URL/actuator/health | jq .

echo -e "\n=== 2. ENTRY event ==="
curl -s -X POST $BASE_URL/webhook \
  -H "Content-Type: application/json" \
  -d "{\"license_plate\":\"$PLATE\",\"entry_time\":\"$ENTRY_TIME\",\"event_type\":\"ENTRY\"}" \
  -w "\nHTTP %{http_code}\n"

echo -e "\n=== 3. PARKED event ==="
curl -s -X POST $BASE_URL/webhook \
  -H "Content-Type: application/json" \
  -d "{\"license_plate\":\"$PLATE\",\"lat\":-23.561684,\"lng\":-46.655981,\"event_type\":\"PARKED\"}" \
  -w "\nHTTP %{http_code}\n"

echo -e "\n=== 4. EXIT event ==="
curl -s -X POST $BASE_URL/webhook \
  -H "Content-Type: application/json" \
  -d "{\"license_plate\":\"$PLATE\",\"exit_time\":\"$EXIT_TIME\",\"event_type\":\"EXIT\"}" \
  -w "\nHTTP %{http_code}\n"

echo -e "\n=== 5. Revenue ==="
curl -s "$BASE_URL/revenue?date=$DATE&sector=A" | jq .

echo -e "\n=== 6. Prometheus metrics ==="
curl -s $BASE_URL/actuator/prometheus | grep "parking_"
