import { useEffect, useState } from 'react'
import './MetricsDashboard.css'

const BACKEND_URL = 'http://localhost:8080'

function errorRateStatus(rate) {
  if (rate === 0) return 'good'
  if (rate <= 5) return 'warning'
  return 'critical'
}

export default function MetricsDashboard() {
  const [eventsCaptured, setEventsCaptured] = useState(0)
  const errorRate = 0 
  const queriesReplayed = 0

  useEffect(() => {
    const fetchMetrics = () => {
      fetch(`${BACKEND_URL}/metrics`)
        .then((res) => res.json())
        .then((data) => setEventsCaptured(data.eventsCaptured))
        .catch(() => {}) 
    }

    fetchMetrics() 
    const interval = setInterval(fetchMetrics, 3000)
    return () => clearInterval(interval)
  }, [])

  return (
    <div className="metrics-dashboard">
      <div className="stat-tile">
        <div className="stat-value">{eventsCaptured}</div>
        <div className="stat-label">Events captured</div>
      </div>

      <div className="stat-tile">
        <div className="stat-value">{queriesReplayed}</div>
        <div className="stat-label">Queries replayed</div>
      </div>

      <div className={`stat-tile status-${errorRateStatus(errorRate)}`}>
        <div className="stat-value">{errorRate}%</div>
        <div className="stat-label">Error rate</div>
      </div>
    </div>
  )
}