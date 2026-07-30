import { useEffect, useState } from 'react'
import './MetricsDashboard.css'

const BACKEND_URL = 'http://localhost:8080'

function errorRateStatus(rate) {
  if (rate === 0) return 'good'
  if (rate <= 5) return 'warning'
  return 'critical'
}

export default function MetricsDashboard() {
  const [metrics, setMetrics] = useState({
    eventsCaptured : 0,
    queriesReplayed :0,
    errorRate : 0,
  })

  useEffect(() => {
    const fetchMetrics = () => {
      fetch(`${BACKEND_URL}/metrics`)
        .then((res) => res.json())
        .then((data) => setMetrics(data))
        .catch(() => {}) 
    }

    fetchMetrics() 
    const interval = setInterval(fetchMetrics, 3000)
    return () => clearInterval(interval)
  }, [])

  return (
    <div className="metrics-dashboard">
      <div className="stat-tile">
        <div className="stat-value">{metrics.eventsCaptured}</div>
        <div className="stat-label">Events captured</div>
      </div>

      <div className="stat-tile">
        <div className="stat-value">{metrics.queriesReplayed}</div>
        <div className="stat-label">Queries replayed</div>
      </div>

      <div className={`stat-tile status-${errorRateStatus(metrics.errorRate)}`}>
        <div className="stat-value">{metrics.errorRate}%</div>
        <div className="stat-label">Error rate</div>
      </div>
    </div>
  )
}