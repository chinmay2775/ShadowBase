import { useState } from 'react'
import './SchemaCheck.css'

const BACKEND_URL = 'http://localhost:8080'

export default function SchemaCheck() {
  const [targetId, setTargetId] = useState('')
  const [sql, setSql] = useState('')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const runCheck = () => {
    if (!targetId.trim() || !sql.trim()) {
      setError('Enter both a target sandbox database id and a SQL query.')
      return
    }
    setLoading(true)
    setError(null)
    setResult(null)

    fetch(`${BACKEND_URL}/schema-check/${targetId.trim()}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sql }),
    })
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then((data) => setResult(data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  return (
    <div className="schema-check">
      <h2>Pre-Migration Check</h2>

      <div className="schema-check-controls">
        <input
          type="text"
          placeholder="Target sandbox database id"
          value={targetId}
          onChange={(e) => setTargetId(e.target.value)}
        />
        <textarea
          placeholder="Paste a SQL query to check, e.g. SELECT name, email FROM customers"
          value={sql}
          onChange={(e) => setSql(e.target.value)}
          rows={3}
        />
        <button onClick={runCheck} disabled={loading}>
          {loading ? 'Checking…' : 'Check Query'}
        </button>
      </div>

      {error && <p className="schema-check-error">{error}</p>}

      {result && (
        <div className={`schema-check-result ${result.safe ? 'safe' : 'unsafe'}`}>
          <div className="schema-check-badge">
            {result.safe ? 'Safe — no issues found' : 'Unsafe — issues found'}
          </div>

          <p className="schema-check-detail">
            <strong>Tables:</strong> {result.tables.length > 0 ? result.tables.join(', ') : '(none detected)'}
          </p>
          <p className="schema-check-detail">
            <strong>Columns referenced:</strong>{' '}
            {result.referencedColumns.length > 0 ? result.referencedColumns.join(', ') : '(none — e.g. SELECT *)'}
          </p>

          {result.issues.length > 0 && (
            <ul className="schema-check-issues">
              {result.issues.map((issue, i) => (
                <li key={i}>{issue}</li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}