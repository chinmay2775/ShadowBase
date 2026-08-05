import './Sidebar.css'

const SECTIONS = [
  { id: 'sandbox', label: 'Sandbox' },
  { id: 'pipeline', label: 'Production Pipeline' },
  { id: 'migration', label: 'Migration Testing' },
]

export default function Sidebar({ activeSection, onSelect }) {
  return (
    <nav className="sidebar">
      {SECTIONS.map((s) => (
        <button
          key={s.id}
          className={`sidebar-button ${activeSection === s.id ? 'active' : ''}`}
          onClick={() => onSelect(s.id)}
        >
          {s.label}
        </button>
      ))}
    </nav>
  )
}