import { useState } from 'react'

const BACKEND_URL = 'http://localhost:8080'

export default function SandboxControls({ dbId, setDbId }) {
  const [status, setStatus] = useState('idle')
  const [info, setInfo] = useState(null)

  async function handleCreate() {
    setStatus('creating')
    setInfo(null)
    try {
      const spinRes = await fetch(`${BACKEND_URL}/db/spin-up`, { method: 'POST' })
      if (!spinRes.ok) throw new Error(`spin-up failed: HTTP ${spinRes.status}`)
      const dbInfo = await spinRes.json()

      const seedRes = await fetch(`${BACKEND_URL}/db/${dbInfo.id}/seed`, { method: 'POST' })
      if (!seedRes.ok) throw new Error(`seed failed: HTTP ${seedRes.status}`)
      const seedResult = await seedRes.json()

      setDbId(dbInfo.id)
      setInfo({ ...dbInfo, seedMessage: seedResult.message })
      setStatus('ready')
    } catch (err) {
      setStatus('error')
      setInfo({ error: err.message })
    }
  }

  return (
    <div className="sandbox-card">
      <div className="sandbox-header">
        <h2>Sandbox Database</h2>
        <button className="run-button" onClick={handleCreate} disabled={status === 'creating'}>
          {status === 'creating' ? 'Creating…' : '+ New Sandbox DB'}
        </button>
      </div>

      {status === 'idle' && (
        <p className="hint">No sandbox database yet — click the button to spin one up and seed it.</p>
      )}

      {status === 'ready' && info && (
        <div className="sandbox-info">
          <p>✅ Ready — id: <code>{info.id}</code> on port <code>{info.port}</code></p>
          <p>{info.seedMessage}</p>
        </div>
      )}

      {status === 'error' && info && (
        <p className="sandbox-error">❌ {info.error}</p>
      )}
    </div>
  )
}