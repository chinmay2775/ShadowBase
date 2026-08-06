import { useEffect, useState } from 'react'
import SqlEditor from './components/SqlEditor.jsx'
import SandboxControls from './components/SandboxControls.jsx'
import MetricsDashboard from './components/MetricsDashboard.jsx'
import ReplayResults from './components/ReplayResults.jsx'
import SchemaCheck from './components/SchemaCheck.jsx'
import Sidebar from './components/Sidebar.jsx'
import ProductionConsole from './components/ProductionConsole.jsx'
import './App.css'

const BACKEND_URL = 'http://localhost:8080'

export default function App() {
  const [status, setStatus] = useState('checking…')
  const [detail, setDetail] = useState(null)
  const [dbId, setDbId] = useState(null)
  const [activeSection, setActiveSection] = useState('sandbox')

  useEffect(() => {
    fetch(`${BACKEND_URL}/health`)
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then((data) => {
        setStatus('Connected')
        setDetail(data)
      })
      .catch((err) => {
        setStatus('Offline')
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

      <div className="app-body">
        <Sidebar activeSection={activeSection} onSelect={setActiveSection} />

        <main className="app-main">
          <section className={`app-section ${activeSection === 'sandbox' ? '' : 'section-hidden'}`}>
            <h2 className="section-title">Sandbox</h2>
            <p className="section-desc">Create a disposable database and run SQL against it.</p>
            <SandboxControls dbId={dbId} setDbId={setDbId} />
            <SqlEditor dbId={dbId} />
          </section>

          <section className={`app-section ${activeSection === 'pipeline' ? '' : 'section-hidden'}`}>
            <h2 className="section-title">Production Pipeline</h2>
            <p className="section-desc">Live change-capture metrics from the mock production database.</p>

            <div className="section-panel">
              <MetricsDashboard />
            </div>

            <div className="section-panel">
              <ProductionConsole />
            </div>
          </section>

          <section className={`app-section ${activeSection === 'migration' ? '' : 'section-hidden'}`}>
            <h2 className="section-title">Migration Testing</h2>
            <p className="section-desc">Check whether a proposed schema change is safe, then replay real traffic against it.</p>
            <SchemaCheck />
            <ReplayResults />
            
          </section>
        </main>
      </div>
    </div>
  )
}
