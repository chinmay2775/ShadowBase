import { useState, useRef } from 'react'
import Editor from '@monaco-editor/react'

const BACKEND_URL = 'http://localhost:8080'

export default function SqlEditor({ dbId }) {
  const [result, setResult] = useState(null)
  const [running, setRunning] = useState(false)
  const editorRef = useRef(null)

  function handleEditorMount(editor) {
    editorRef.current = editor
  }

  async function handleRun() {
    if (!dbId) {
      setResult({ success: false, error: 'No sandbox database yet — click "New Sandbox DB" above first.' })
      return
    }

    const sql = editorRef.current.getValue()
    setRunning(true)
    setResult(null)

    try {
      const res = await fetch(`${BACKEND_URL}/db/${dbId}/execute`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sql }),
      })
      const data = await res.json()
      setResult(data)
    } catch (err) {
      setResult({ success: false, error: err.message })
    } finally {
      setRunning(false)
    }
  }

  return (
    <div className="sql-editor-card">
      <div className="sql-editor-header">
        <h2>Migration Script</h2>
        <button className="run-button" onClick={handleRun}>▶ Run</button>
      </div>

      <Editor
        height="240px"
        defaultLanguage="sql"
        defaultValue={'-- Write your migration SQL here\nSELECT * FROM customers;'}
        theme="vs-dark"
        onMount={handleEditorMount}
        options={{
          minimap: { enabled: false },
          fontSize: 14,
        }}
      />
      
      {result && (
        <div className={`sql-output ${result.success ? 'success' : 'error'}`}>
          {result.success ? (
            <>
              <strong>✅ {result.message ?? 'Query succeeded'}</strong>
              {result.rows && <pre>{JSON.stringify(result.rows, null, 2)}</pre>}
            </>
          ) : (
            <strong>❌ {result.error}</strong>
          )}
        </div>
      )}
    </div>
  )
}