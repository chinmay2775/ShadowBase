import { useEffect, useState } from 'react'
import './ProductionConsole.css'

const BACKEND_URL = 'http://localhost:8080'

export default function ProductionConsole() {
  const [info, setInfo] = useState(null)
  const [infoError, setInfoError] = useState(null)
  const [sql, setSql] = useState('SELECT * FROM customers LIMIT 20;')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetch(`${BACKEND_URL}/production/info`)
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then(setInfo)
      .catch((err) => setInfoError(err.message))
  }, [])

  const runQuery = () => {
    setLoading(true)
    setError(null)
    setResult(null)

    fetch(`${BACKEND_URL}/production/query`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sql }),
    })
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then((data) => {
        if (data.success) {
          setResult(data)
        } else {
          setError(data.errorMessage)
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }

  return (
    <div className="production-console">
      <h2>Production Query Console</h2>
      <p className="production-console-desc">
        Read-only access to the mock production database — run SELECT queries instead of using psql.
      </p>

      <div className="production-info">
        {infoError && <p className="production-console-error">Could not load production info: {infoError}</p>}
        {info && (
          <>
            <span><strong>Host:</strong> {info.database.host}:{info.database.port}</span>
            <span><strong>Database:</strong> {info.database.database}</span>
            <span><strong>Kafka:</strong> {info.kafka.bootstrapServers}</span>
          </>
        )}
      </div>    

      <div className="production-console-controls">
        <textarea
          rows={4}
          value={sql}
          onChange={(e) => setSql(e.target.value)}
          placeholder="SELECT * FROM customers LIMIT 20;"
        />
        <button onClick={runQuery} disabled={loading || !sql.trim()}>
          {loading ? 'Running...' : 'Run Query'}
        </button>
      </div>

      {error && <p className="production-console-error">{error}</p>}

      {result && (
        <div className="production-console-results">
          <table>
            <thead>
              <tr>
                {result.columns.map((col) => (
                  <th key={col}>{col}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {result.rows.map((row, i) => (
                <tr key={i}>
                  {result.columns.map((col) => (
                    <td key={col}>{String(row[col])}</td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
          {result.rows.length === 0 && <p className="production-console-empty">Query succeeded, no rows returned.</p>}
        </div>
      )}
    </div>
  )
}