import { useState } from 'react'
import './ReplayResults.css'

const BACKEND_URL = 'http://localhost:8080'

// Day 3: Error Logging UI — lets you trigger a replay against a target
// sandbox and see which queries passed or failed, right in the browser
// instead of reading raw JSON in Postman.
export default function ReplayResults() {
  const [targetId, setTargetId] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [clearing, setClearing] = useState(false)
  const [error, setError] = useState(null)
  const [statusMessage, setStatusMessage] = useState(null)

  const runReplay = () => {
    if (!targetId.trim()) {
      setError('Enter a target sandbox database id first.')
      return
    }
    setLoading(true)
    setError(null)
    setStatusMessage(null)

    fetch(`${BACKEND_URL}/replay/${targetId.trim()}`, { method: 'POST' })
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then((data) => setResults(data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  const clearTrafficLog = () => {
    setClearing(true)
    setError(null)
    setStatusMessage(null)

    fetch(`${BACKEND_URL}/traffic-log`, { method: 'DELETE' })
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then(() => {
        setResults([]) // old results no longer reflect anything meaningful once the log is cleared
        setStatusMessage('Traffic log cleared.')
      })
      .catch((err) => setError(err.message))
      .finally(() => setClearing(false))
  }

  const failedCount = results.filter((r) => !r.success).length

  return (
    <div className="replay-results">
      <h2>Replay &amp; Error Log</h2>

      <div className="replay-controls">
        <input
          type="text"
          placeholder="Target sandbox database id"
          value={targetId}
          onChange={(e) => setTargetId(e.target.value)}
        />
        <button onClick={runReplay} disabled={loading}>
          {loading ? 'Replaying…' : 'Run Replay'}
        </button>
         <button className="secondary" onClick={clearTrafficLog} disabled={clearing}>
          {clearing ? 'Clearing…' : 'Clear Traffic Log'}
        </button>
      </div>

      {error && <p className="replay-error">{error}</p>}
      {statusMessage && <p className="replay-status">{statusMessage}</p>}

      {results.length > 0 && (
        <p className="replay-summary">
          {results.length} quer{results.length === 1 ? 'y' : 'ies'} replayed —{' '}
          {failedCount === 0 ? 'all passed' : `${failedCount} failed`}
        </p>
      )}

      <ul className="replay-list">
        {results.map((r, i) => (
          <li key={i} className={`replay-item ${r.success ? 'passed' : 'failed'}`}>
            <div className="replay-item-header">
              <span className="replay-badge">{r.success ? 'Passed' : 'Failed'}</span>
            </div>
            <pre className="replay-sql">{r.sql}</pre>
            {!r.success && <p className="replay-error-message">{r.errorMessage}</p>}
          </li>
        ))}
      </ul>
    </div>
  )
}