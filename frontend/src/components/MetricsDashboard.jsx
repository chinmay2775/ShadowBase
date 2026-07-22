import './MetricsDashboard.css'

const STATS = [
  { key: 'captured', label: 'Events captured', value: 0 },
  { key: 'replayed', label: 'Queries replayed', value: 0 },
]

function errorRateStatus(rate) {
  if (rate === 0) return 'good'
  if (rate <= 5) return 'warning'
  return 'critical'
}

export default function MetricsDashboard() {
  const errorRate = 0

  return (
    <div className="metrics-dashboard">
      {STATS.map((stat) => (
        <div className="stat-tile" key={stat.key}>
          <div className="stat-value">{stat.value}</div>
          <div className="stat-label">{stat.label}</div>
        </div>
      ))}

      <div className={`stat-tile status-${errorRateStatus(errorRate)}`}>
        <div className="stat-value">{errorRate}%</div>
        <div className="stat-label">Error rate</div>
      </div>
    </div>
  )
}