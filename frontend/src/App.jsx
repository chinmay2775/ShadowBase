import { useEffect, useState } from 'react'

// Day 1 frontend deliverable: prove the React app runs AND can reach the backend.
// This calls GET /health on the Spring Boot backend and shows the result.
const BACKEND_URL = 'http://localhost:8080'

export default function App() {
  const [status, setStatus] = useState('checking…')
  const [detail, setDetail] = useState(null)

  useEffect(() => {
    fetch(`${BACKEND_URL}/health`)
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then((data) => {
        setStatus('connected')
        setDetail(data)
      })
      .catch((err) => {
        setStatus('offline')
        setDetail({ error: err.message })
      })
  }, [])

  return (
    <div className="app">
      <h1>ShadowBase</h1>
      <p className="subtitle">Zero-Downtime Schema Migration Sandbox</p>

      <div className={`status-card ${status}`}>
        <span className="dot" />
        <div>
          <strong>Backend: {status}</strong>
          <pre>{detail ? JSON.stringify(detail, null, 2) : ''}</pre>
        </div>
      </div>
    </div>
  )
}
