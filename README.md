# TastyBite Restaurant Assistant - Backend

Spring Boot 3.4 REST backend for the TastyBite AI Restaurant Assistant application.

---

## 1. Health-Check & Keep-Alive Endpoint

A lightweight, unauthenticated health-check endpoint is available for uptime monitoring and automated service reliability.

### Endpoint Details

| Property | Value |
|---|---|
| **Method** | `GET` |
| **Path** | `/health` (also alias `/api/health`) |
| **Authentication** | None (public, permitted via Spring Security) |
| **Response Code** | `200 OK` |
| **Response Content-Type** | `application/json` |

### Sample Response

```json
{
  "status": "UP",
  "timestamp": "2026-10-02T13:45:00.123456Z"
}
```

---

## 2. Deploying to Render

### Service Configuration on Render Dashboard

1. **Environment**: Java / Docker
2. **Build Command**:
   ```bash
   chmod +x gradlew && ./gradlew build -x test
   ```
3. **Start Command**:
   ```bash
   java -jar build/libs/Backend-0.0.1-SNAPSHOT.jar
   ```
4. **Environment Variables**:
   - `PORT`: Automatically provided by Render (Spring Boot reads `${PORT:8080}`)
   - `FIREBASE_SERVICE_ACCOUNT`: Minified JSON string of your Firebase service account key
   - Any custom application secrets (e.g., JWT secret, LLM API keys)

---

## 3. Configuring an External Uptime Monitor (Keep-Alive)

Render's free tier web services spin down automatically after **15 minutes** of inactivity, which causes subsequent requests to experience a 30-50 second cold start delay.

To maintain active status and verify reliability, configure an external monitoring service to ping the health endpoint every **10–14 minutes**.

### Option A: UptimeRobot (Free & Recommended)

1. Sign up or log in at [uptimerobot.com](https://uptimerobot.com).
2. Click **+ Add New Monitor**.
3. Fill in the monitor settings:
   - **Monitor Type**: `HTTP(s)`
   - **Friendly Name**: `TastyBite Backend Health`
   - **URL (or IP)**: `https://YOUR-RENDER-DOMAIN.onrender.com/health`
   - **Monitoring Interval**: `10 minutes` (or `5 minutes`)
   - **Monitor Timeout**: `30 seconds`
4. Click **Create Monitor**.

### Option B: Cron-Job.org (Free HTTP Cron)

1. Sign up at [cron-job.org](https://cron-job.org).
2. Click **Create Cronjob**.
3. Configure:
   - **Title**: `TastyBite Keep-Alive`
   - **URL**: `https://YOUR-RENDER-DOMAIN.onrender.com/health`
   - **Schedule**: Every `10 minutes` (`*/10 * * * *`)
   - **Request Method**: `GET`
4. Save the cronjob.

### Option C: Better Stack (Better Uptime)

1. Sign up at [betterstack.com](https://betterstack.com/uptime).
2. Add monitor for: `https://YOUR-RENDER-DOMAIN.onrender.com/health`.
3. Set alert on non-200 HTTP status code.
4. Set check frequency to `5` or `10` minutes.

---

## 4. Local Development & Verification

### Run Locally

```bash
./gradlew bootRun
```

### Test Health Endpoint

```bash
curl -i http://localhost:8080/health
```

Expected output:
```http
HTTP/1.1 200 OK
Content-Type: application/json

{"status":"UP","timestamp":"..."}
```

### Run Tests

```bash
./gradlew test
```
