import { useEffect, useState } from 'react'
import SqlEditor from './components/SqlEditor.jsx'
import SandboxControls from './components/SandboxControls.jsx'
import MetricsDashboard from './components/MetricsDashboard.jsx'
import ReplayResults from './components/ReplayResults.jsx'
import SchemaCheck from './components/SchemaCheck.jsx'

const BACKEND_URL = 'http://localhost:8080'

export default function App() {
  const [status, setStatus] = useState('checking…')
  const [detail, setDetail] = useState(null)
  const [dbId, SetDbId] = useState(null)

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

      <SandboxControls dbId = {dbId} setDbId={SetDbId}/>
      <SchemaCheck />
      <SqlEditor dbId = {dbId}/>
      <MetricsDashboard />
      <ReplayResults />
      
    </div>
  )
}
