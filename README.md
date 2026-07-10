# ShadowBase — Zero-Downtime Schema Migration Sandbox

## Run the backend

```bash
cd backend
mvn spring-boot:run
```

Then check it works:

```bash
curl http://localhost:8080/health
```

## Run the frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 — you should see the ShadowBase page with a green
"Backend: connected" card showing the health JSON.

